package pt.isel.ls.data.mem

import pt.isel.ls.data.TokenData
import pt.isel.ls.domain.Token
import kotlin.uuid.Uuid

class TokenDataMem : MemStorage(), TokenData {
    override fun createToken(uid: Int): Token {
        tokensDB.removeIf { it.uid == uid }
        val token = Token(token = Uuid.random(), uid = uid)
        tokensDB.add(token)

        return token
    }

    override fun getUserToken(uid: Int): Token? {
        return tokensDB.find { it.uid == uid }
    }

    override fun getUserByToken(token: String): Token? {
        return tokensDB.find { it.token.toString() == token }
    }

    override fun deleteToken(token: Token) {
        tokensDB.removeIf { it.token == token.token }
    }
}
