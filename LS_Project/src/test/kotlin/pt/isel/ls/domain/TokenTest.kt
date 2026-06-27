package pt.isel.ls.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.uuid.Uuid

class TokenTest {
    @Test
    fun `create token with valid data`() {
        val uuid = Uuid.random()
        val uid = 42

        val token = Token(token = uuid, uid = uid)

        assertEquals(uuid, token.token)
        assertEquals(42, token.uid)
    }

    @Test
    fun `two tokens with same uuid and uid are equal`() {
        val uuid = Uuid.random()

        val token1 = Token(token = uuid, uid = 1)
        val token2 = Token(token = uuid, uid = 1)

        assertEquals(token1, token2)
    }

    @Test
    fun `two tokens with different uuids are not equal`() {
        val uuid1 = Uuid.random()
        val uuid2 = Uuid.random()

        val token1 = Token(token = uuid1, uid = 1)
        val token2 = Token(token = uuid2, uid = 1)

        assertNotEquals(token1, token2)
    }

    @Test
    fun `two tokens with different uids are not equal`() {
        val uuid = Uuid.random()

        val token1 = Token(token = uuid, uid = 1)
        val token2 = Token(token = uuid, uid = 2)

        assertNotEquals(token1, token2)
    }

    @Test
    fun `copy changes only the target field`() {
        val uuid = Uuid.random()
        val original = Token(token = uuid, uid = 10)

        val copied = original.copy(uid = 20)

        assertEquals(uuid, copied.token)
        assertEquals(20, copied.uid)
    }

    @Test
    fun `token toString contains uuid representation`() {
        val uuid = Uuid.random()
        val token = Token(token = uuid, uid = 5)

        val tokenString = token.token.toString()

        assertEquals(uuid.toString(), tokenString)
    }
}
