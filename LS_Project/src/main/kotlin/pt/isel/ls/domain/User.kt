package pt.isel.ls.domain

import pt.isel.ls.domain.wrappers.Email
import pt.isel.ls.domain.wrappers.Password
import pt.isel.ls.domain.wrappers.UserName

data class User(
    val uid: Int,
    val name: UserName,
    val email: Email,
    val password: Password,
)
