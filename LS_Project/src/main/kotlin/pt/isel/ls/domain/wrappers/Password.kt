package pt.isel.ls.domain.wrappers

@JvmInline
value class Password(
    val value: String,
) {
    init {
        require(value.isNotEmpty()) { "Password cannot be empty" }
        require(value.length >= 6) { "Password must be at least 6 characters long" }
    }
}
