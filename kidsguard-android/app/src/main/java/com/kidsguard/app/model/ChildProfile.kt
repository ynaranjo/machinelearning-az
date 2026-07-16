package com.kidsguard.app.model

/** Perfil de hijo: cada uno tiene sus propias apps permitidas, límites y horarios. */
data class ChildProfile(
    val id: Int,
    val name: String,
    val emoji: String
)
