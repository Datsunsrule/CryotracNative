package com.cryotrac.paranormal.audio

import android.content.ContentValues
import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Records the session through the microphone (room audio plus whatever the
 * phone itself is playing) to an .m4a file.
 *
 * Audio is captured to a private cache file first; [publish] then copies it
 * into the shared Recordings/Ghostrac folder (Music/Ghostrac before Android 12)
 * so it shows up in the phone's file and audio apps.
 */
class SessionRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var file: File? = null

    /** Returns false if the microphone could not be opened. */
    fun start(): Boolean {
        if (recorder != null) return true
        val name = "ghostrac_" + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date()) + ".m4a"
        val out = File(context.cacheDir, name)
        val r = if (Build.VERSION.SDK_INT >= 31) MediaRecorder(context)
                else @Suppress("DEPRECATION") MediaRecorder()
        return try {
            r.setAudioSource(MediaRecorder.AudioSource.MIC)
            r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            r.setAudioSamplingRate(44_100)
            r.setAudioEncodingBitRate(128_000)
            r.setOutputFile(out.absolutePath)
            r.prepare()
            r.start()
            recorder = r
            file = out
            true
        } catch (e: Exception) {
            r.release()
            out.delete()
            false
        }
    }

    /** Stops recording. Returns the captured file, or null if nothing usable was recorded. */
    fun stop(): File? {
        val r = recorder ?: return null
        val f = file
        recorder = null
        file = null
        // stop() throws if it is called before any audio was captured
        val ok = try { r.stop(); true } catch (e: RuntimeException) { false }
        r.release()
        if (!ok || f == null) { f?.delete(); return null }
        return f
    }

    /** Moves a finished recording into shared storage. Returns a human-readable location. */
    fun publish(f: File): String {
        if (Build.VERSION.SDK_INT >= 29) {
            try {
                val dir = if (Build.VERSION.SDK_INT >= 31) Environment.DIRECTORY_RECORDINGS
                          else Environment.DIRECTORY_MUSIC
                val relPath = "$dir/Ghostrac"
                val resolver = context.contentResolver
                val values = ContentValues().apply {
                    put(MediaStore.Audio.Media.DISPLAY_NAME, f.name)
                    put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp4")
                    put(MediaStore.Audio.Media.RELATIVE_PATH, relPath)
                    put(MediaStore.Audio.Media.IS_PENDING, 1)
                }
                val uri = resolver.insert(
                    MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), values
                ) ?: error("MediaStore insert failed")
                resolver.openOutputStream(uri)!!.use { out -> f.inputStream().use { it.copyTo(out) } }
                values.clear()
                values.put(MediaStore.Audio.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
                f.delete()
                return "$relPath/${f.name}"
            } catch (e: Exception) {
                // fall through to app storage
            }
        }
        val dest = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), f.name)
        f.copyTo(dest, overwrite = true)
        f.delete()
        return dest.absolutePath
    }
}
