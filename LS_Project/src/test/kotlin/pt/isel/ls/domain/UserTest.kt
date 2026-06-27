package pt.isel.ls.domain

import pt.isel.ls.domain.wrappers.Email
import pt.isel.ls.domain.wrappers.Password
import pt.isel.ls.domain.wrappers.UserName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class UserTest {
    @Test
    fun `New user`() {
        val user =
            User(
                uid = 67,
                name = UserName("Miguel"),
                email = Email("miguel@isel.pt"),
                password = Password("123456"),
            )

        assertEquals(67, user.uid)
        assertEquals(UserName("Miguel"), user.name)
        assertEquals(Email("miguel@isel.pt"), user.email)

        assertNotEquals(12, user.uid)
        assertNotEquals(Email("rodrigo@isel.pt"), user.email)
    }
}
