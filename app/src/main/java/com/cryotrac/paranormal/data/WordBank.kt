package com.cryotrac.paranormal.data

val WORD_SEEDS = listOf(
    "shadow","void","haunt","tomb","whisper","echo","cold","spirit","vessel",
    "presence","wander","drift","hollow","mist","veil","mourn","linger","beyond",
    "threshold","passage","remnant","memory","trace","light","dark","signal",
    "frequency","static","pulse","flicker","resonance","channel","contact",
    "energy","force","matter","wave","field","zone","origin","center","edge",
    "convergence","manifest","reveal","conceal","hidden","open","close","pass",
    "follow","watch","hear","feel","sense","know","see","touch","speak","answer",
    "question","seek","find","lost","found","here","there","near","far","within",
    "without","above","below","between","before","after","now","then","always",
    "never","sometimes","once","again","still","silent","loud","quiet","warm",
    "cold","alive","gone","remain","return","leave","stay","come","go",
    "James","Mary","Robert","Patricia","John","Jennifer","Michael","Linda",
    "William","Barbara","David","Elizabeth","Richard","Susan","Joseph","Jessica",
    "Thomas","Sarah","Charles","Karen","Emma","Oliver","Sophia","Liam","Noah",
    "Ava","Isabella","Lucas","Mia","Mason","Charlotte","Ethan","Amelia",
    "fear","love","grief","anger","joy","sorrow","longing","peace","dread",
    "hope","despair","wonder","regret","guilt","pride","shame","trust",
    "forest","river","mountain","ocean","sky","earth","fire","wind","rain",
    "storm","fog","dust","ash","snow","ice","heat","stone","wood","metal","glass",
    "door","window","wall","floor","ceiling","room","hall","stair","bridge",
    "road","path","gate","lock","key","mirror","clock","candle","lamp","photo",
    "breathe","move","stand","fall","rise","push","pull","turn","open","close",
    "wait","listen","remember","forget","release","hold","carry","drop","throw",
    "catch","run","stop","begin","end","change","remain","appear","vanish"
)

fun buildWordBank(): List<String> {
    val bank = mutableListOf<String>()
    while (bank.size < 5000) bank.addAll(WORD_SEEDS.shuffled())
    return bank.take(5000)
}
