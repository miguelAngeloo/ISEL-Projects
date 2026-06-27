package pt.isel.ls.data.mem

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TokenDataMemTest {
    private lateinit var tokenDataMem: TokenDataMem

    @BeforeTest
    fun setup() {
        val memStorage = MemStorage()
        memStorage.clear()

        tokenDataMem = TokenDataMem()
    }

    @Test
    fun `createToken generates a valid UUID and stores it`() {
        val userId = 1

        val tokenObj = tokenDataMem.createToken(userId)

        assertNotNull(tokenObj)
        assertEquals(userId, tokenObj.uid)
        assertTrue(tokenObj.token.toString().isNotBlank())

        val retrieved = tokenDataMem.getUserByToken(tokenObj.token.toString())
        assertNotNull(retrieved)
        assertEquals(userId, retrieved.uid)
    }

    @Test
    fun `getToken returns null for non-existent token string`() {
        val result = tokenDataMem.getUserByToken("non-existent-uuid-string")
        assertNull(result)
    }

    @Test
    fun `getUserToken returns the correct token for a specific user`() {
        val userId = 42
        val createdToken = tokenDataMem.createToken(userId)

        val retrievedToken = tokenDataMem.getUserToken(userId)

        assertNotNull(retrievedToken)
        assertEquals(createdToken.token, retrievedToken.token)
    }

    @Test
    fun `deleteToken removes the token from storage`() {
        val userId = 10
        val tokenObj = tokenDataMem.createToken(userId)
        val tokenString = tokenObj.token.toString()

        assertNotNull(tokenDataMem.getUserByToken(tokenString))

        tokenDataMem.deleteToken(tokenObj)

        assertNull(tokenDataMem.getUserByToken(tokenString))
    }

    @Test
    fun `multiple tokens for different users are stored correctly`() {
        tokenDataMem.createToken(1)
        tokenDataMem.createToken(2)
        tokenDataMem.createToken(3)
        val storage = MemStorage()
        assertEquals(3, storage.tokensDB.size)
    }
}
