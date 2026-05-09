package com.cryotrac.paranormal.viewmodel

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.SoundPool
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cryotrac.paranormal.data.QUESTIONS
import com.cryotrac.paranormal.data.buildWordBank
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale
import kotlin.math.abs
import kotlin.math.sqrt

class CryotracViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    // ── Session timer ─────────────────────────────────────────────────────────
    private val _sessionTime = MutableStateFlow("00:00")
    val sessionTime: StateFlow<String> = _sessionTime
    private var sessionStartMs = System.currentTimeMillis()

    // ── CH-01 Touch Sensor ────────────────────────────────────────────────────
    private val _ch01On = MutableStateFlow(false)
    val ch01On: StateFlow<Boolean> = _ch01On
    private val _ch01Signal = MutableStateFlow(0f)
    val ch01Signal: StateFlow<Float> = _ch01Signal
    private val _touchCount = MutableStateFlow(0)
    val touchCount: StateFlow<Int> = _touchCount

    // ── CH-02 EMF ─────────────────────────────────────────────────────────────
    private val _emfOn = MutableStateFlow(false)
    val emfOn: StateFlow<Boolean> = _emfOn
    private val _emfMag = MutableStateFlow(0f)
    val emfMag: StateFlow<Float> = _emfMag
    private val _emfX = MutableStateFlow(0f); val emfX: StateFlow<Float> = _emfX
    private val _emfY = MutableStateFlow(0f); val emfY: StateFlow<Float> = _emfY
    private val _emfZ = MutableStateFlow(0f); val emfZ: StateFlow<Float> = _emfZ
    private val _emfBaseline = MutableStateFlow<Float?>(null)
    val emfBaseline: StateFlow<Float?> = _emfBaseline
    private val _emfStatus = MutableStateFlow("OFFLINE")
    val emfStatus: StateFlow<String> = _emfStatus
    private val _emfAnomalyCount = MutableStateFlow(0)
    val emfAnomalyCount: StateFlow<Int> = _emfAnomalyCount
    private val _emfLive = MutableStateFlow(false)
    val emfLive: StateFlow<Boolean> = _emfLive
    private var emfBaselineReads = mutableListOf<Float>()
    private var emfAlertCooldown = false
    private var emfSimMag = 48f
    private var emfSimJob: Job? = null

    // ── GHOSTEC ───────────────────────────────────────────────────────────────
    private val _ghostecRunning = MutableStateFlow(false)
    val ghostecRunning: StateFlow<Boolean> = _ghostecRunning
    private val _ghostecWord = MutableStateFlow("-- READY --")
    val ghostecWord: StateFlow<String> = _ghostecWord
    private val _ghostecProgress = MutableStateFlow(0f)
    val ghostecProgress: StateFlow<Float> = _ghostecProgress
    private val _ghostecCounter = MutableStateFlow("0000/5000")
    val ghostecCounter: StateFlow<String> = _ghostecCounter
    private val _recentWords = MutableStateFlow<List<String>>(emptyList())
    val recentWords: StateFlow<List<String>> = _recentWords
    private var ghostecJob: Job? = null

    // ── Questions ─────────────────────────────────────────────────────────────
    private val _currentQuestion = MutableStateFlow("")
    val currentQuestion: StateFlow<String> = _currentQuestion
    private var questionPool = mutableListOf<String>()
    private var lastQuestion = ""

    // ── TTS ───────────────────────────────────────────────────────────────────
    private var tts: TextToSpeech? = null
    private var ttsReady = false

    // ── SoundPool (sonar touch effect) ────────────────────────────────────────
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(1)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        ).build()
    private var sonarSoundId  = 0
    private var sonarStreamId = 0
    private var sonarLoaded   = false

    // ── Sensor ────────────────────────────────────────────────────────────────
    private val sensorManager =
        application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    init {
        startTimer()
        initTts(application)
        initSensor()
        showNextQuestion()
        sonarSoundId = soundPool.load(application, com.cryotrac.paranormal.R.raw.sonar, 1)
        soundPool.setOnLoadCompleteListener { _, _, _ -> sonarLoaded = true }
    }

    // ── Timer ─────────────────────────────────────────────────────────────────
    private fun startTimer() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                val elapsed = (System.currentTimeMillis() - sessionStartMs) / 1000
                val m = (elapsed / 60).toString().padStart(2, '0')
                val s = (elapsed % 60).toString().padStart(2, '0')
                _sessionTime.value = "$m:$s"
            }
        }
    }

    fun resetSession() {
        sessionStartMs = System.currentTimeMillis()
        _touchCount.value = 0
        _emfAnomalyCount.value = 0
        emfAlertCooldown = false
        _emfBaseline.value = null
        emfBaselineReads.clear()
    }

    // ── CH-01 ─────────────────────────────────────────────────────────────────
    fun toggleCh01() { _ch01On.value = !_ch01On.value; if (!_ch01On.value) _ch01Signal.value = 0f }
    fun updateCh01Signal(signal: Float) { if (_ch01On.value) _ch01Signal.value = signal }
    fun incrementTouch() {
        _touchCount.value++
        playSonar()
    }

    private fun playSonar() {
        if (!sonarLoaded) return
        if (sonarStreamId != 0) soundPool.stop(sonarStreamId)
        sonarStreamId = soundPool.play(sonarSoundId, 1f, 1f, 1, 0, 1f)
    }

    // ── EMF Sensor ────────────────────────────────────────────────────────────
    fun toggleEmf() {
        if (_emfOn.value) {
            _emfOn.value = false
            _emfStatus.value = "OFFLINE"
            _emfMag.value = 0f
            _emfX.value = 0f; _emfY.value = 0f; _emfZ.value = 0f
            emfSimJob?.cancel()
            sensorManager.unregisterListener(this)
        } else {
            _emfOn.value = true
            initSensor()
        }
    }

    private fun initSensor() {
        if (magnetometer != null) {
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL)
            _emfLive.value = true
            _emfStatus.value = "CALIBRATING"
        } else {
            _emfLive.value = false
            _emfStatus.value = "CALIBRATING"
            startEmfSimulation()
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (!_emfOn.value) return
        if (event.sensor.type != Sensor.TYPE_MAGNETIC_FIELD) return
        val x = event.values[0]; val y = event.values[1]; val z = event.values[2]
        val mag = sqrt(x * x + y * y + z * z)
        _emfX.value = x; _emfY.value = y; _emfZ.value = z; _emfMag.value = mag
        autoCalibrate(mag)
        updateEmfStatus(mag)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    private fun autoCalibrate(mag: Float) {
        if (_emfBaseline.value != null) return
        emfBaselineReads.add(mag)
        if (emfBaselineReads.size >= 8) {
            _emfBaseline.value = emfBaselineReads.average().toFloat()
        }
    }

    fun calibrateEmf() {
        val m = _emfMag.value
        val baseline = if (m > 0f) m else emfSimMag
        _emfBaseline.value = baseline
        emfBaselineReads.clear()
        updateEmfStatus(if (m > 0f) m else emfSimMag)
    }

    private fun updateEmfStatus(mag: Float) {
        val baseline = _emfBaseline.value ?: run { _emfStatus.value = "CALIBRATING"; return }
        val delta = abs(mag - baseline)
        val status = when {
            delta >= 70 || mag > 250 -> "ANOMALY"
            delta >= 35              -> "HIGH"
            delta >= 15              -> "ELEVATED"
            else                     -> "AMBIENT"
        }
        _emfStatus.value = status
        if (status == "ANOMALY") triggerEmfAlert()
    }

    private fun triggerEmfAlert() {
        if (emfAlertCooldown) return
        emfAlertCooldown = true
        _emfAnomalyCount.value++
        speakText("E M F anomaly detected", rate = 0.95f, pitch = 0.85f)
        viewModelScope.launch { delay(12_000); emfAlertCooldown = false }
    }

    private fun startEmfSimulation() {
        // Seed initial values immediately so calibrate works on first tap
        val sx = emfSimMag * 0.55f; val sy = emfSimMag * 0.60f; val sz = emfSimMag * 0.45f
        val sm = sqrt(sx * sx + sy * sy + sz * sz)
        _emfX.value = sx; _emfY.value = sy; _emfZ.value = sz; _emfMag.value = sm
        emfSimJob = viewModelScope.launch {
            while (true) {
                delay(500)
                val drift = (Math.random() - 0.5).toFloat() * 1.5f
                emfSimMag = (emfSimMag + drift + (48f - emfSimMag) * 0.05f).coerceIn(15f, 200f)
                val sx = emfSimMag * 0.55f + (Math.random() - 0.5).toFloat() * 3f
                val sy = emfSimMag * 0.60f + (Math.random() - 0.5).toFloat() * 3f
                val sz = emfSimMag * 0.45f + (Math.random() - 0.5).toFloat() * 3f
                val sm = sqrt(sx * sx + sy * sy + sz * sz)
                _emfX.value = sx; _emfY.value = sy; _emfZ.value = sz; _emfMag.value = sm
                autoCalibrate(sm); updateEmfStatus(sm)
            }
        }
    }

    // ── GHOSTEC ───────────────────────────────────────────────────────────────
    fun toggleGhostec() { if (_ghostecRunning.value) stopGhostec() else startGhostec() }

    private fun startGhostec() {
        val bank = buildWordBank()
        _ghostecRunning.value = true
        _recentWords.value = emptyList()
        ghostecJob = viewModelScope.launch {
            bank.forEachIndexed { i, word ->
                if (!isActive) return@forEachIndexed
                val upper = word.uppercase()
                _ghostecWord.value    = upper
                _ghostecCounter.value = "${(i + 1).toString().padStart(4, '0')}/5000"
                _ghostecProgress.value = (i + 1) / 5000f
                _recentWords.value = (_recentWords.value + upper).takeLast(5)
                speakText(word, rate = 1.1f)
                delay(150)
            }
            _ghostecWord.value = "-- SEQUENCE COMPLETE --"
            _ghostecRunning.value = false
        }
    }

    fun stopGhostec() {
        ghostecJob?.cancel(); _ghostecRunning.value = false; tts?.stop()
    }

    // ── Questions ─────────────────────────────────────────────────────────────
    fun showNextQuestion() {
        if (questionPool.isEmpty()) {
            questionPool = QUESTIONS.shuffled().toMutableList()
            if (questionPool.firstOrNull() == lastQuestion && questionPool.size > 1)
                questionPool.add(questionPool.removeFirst())
        }
        lastQuestion = questionPool.removeFirst()
        _currentQuestion.value = lastQuestion
    }

    fun speakCurrentQuestion() = speakText(_currentQuestion.value, rate = 1.0f, pitch = 0.9f)

    // ── TTS ───────────────────────────────────────────────────────────────────
    private fun initTts(context: Context) {
        tts = TextToSpeech(context) { status ->
            ttsReady = status == TextToSpeech.SUCCESS
            if (ttsReady) tts?.language = Locale.US
        }
    }

    fun speakText(text: String, rate: Float = 1.0f, pitch: Float = 1.0f) {
        if (!ttsReady || tts == null) return
        tts?.setSpeechRate(rate); tts?.setPitch(pitch)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "ct_${System.currentTimeMillis()}")
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(this)
        emfSimJob?.cancel()
        ghostecJob?.cancel()
        tts?.shutdown()
        soundPool.release()
    }
}
