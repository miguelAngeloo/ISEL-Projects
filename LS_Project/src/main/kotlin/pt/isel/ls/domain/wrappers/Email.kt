package pt.isel.ls.domain.wrappers

@JvmInline
value class Email(
    val value: String,
) {
    init {
        require(value.isNotEmpty()) { "Email cannot be empty" }
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        require(emailRegex.matches(value)) { "Invalid email format" }
    }
}
