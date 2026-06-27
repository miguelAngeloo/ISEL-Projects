package pt.isel.ls.domain.wrappers

@JvmInline
value class UserName(val value: String) {
    init {
        require(value.isNotEmpty()) { "Username cannot be empty" }
        require(value.length in 3..20) { "Invalid username length" }
    }
}
