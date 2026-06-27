package pt.isel.ls.domain

import kotlin.uuid.Uuid

data class Token(
    val token: Uuid,
    val uid: Int,
)
