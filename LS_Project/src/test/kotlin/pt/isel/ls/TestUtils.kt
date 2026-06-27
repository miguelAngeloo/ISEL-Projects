package pt.isel.ls

import org.http4k.core.Method
import org.http4k.core.Request
import org.mindrot.jbcrypt.BCrypt
import pt.isel.ls.data.mem.MemManager
import pt.isel.ls.domain.User
import pt.isel.ls.domain.wrappers.Email
import pt.isel.ls.domain.wrappers.Password
import pt.isel.ls.domain.wrappers.UserName

fun MemManager.createTestUser(
    name: String = "Alice",
    email: String = "alice@isel.pt",
    pass: String = "secure123",
): Int =
    this.userData.createUser(
        User(0, UserName(name), Email(email), Password(BCrypt.hashpw(pass, BCrypt.gensalt()))),
    )

fun MemManager.createTestToken(uid: Int): String = this.tokenData.createToken(uid).token.toString()

fun bodyRequest(
    method: Method,
    uri: String,
    token: String? = null,
    body: String,
): Request {
    val req =
        Request(method, uri)
            .header("Content-Type", "application/json")
            .body(body)

    return if (token != null) req.header("Authorization", "Bearer $token") else req
}

fun simpleRequest(
    method: Method,
    uri: String,
    token: String? = null,
): Request {
    val req = Request(method, uri)
    return if (token != null) req.header("Authorization", "Bearer $token") else req
}
