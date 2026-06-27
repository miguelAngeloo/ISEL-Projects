package pt.isel.ls.data

import pt.isel.ls.domain.Token

interface TokenData {
    fun getUserToken(uid: Int): Token?

    fun createToken(uid: Int): Token

    fun getUserByToken(token: String): Token?

    fun deleteToken(token: Token)
}
