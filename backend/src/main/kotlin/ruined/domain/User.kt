package ruined.domain

/**
 * User domain entity.
 * @property id ID of the user.
 * @property email Email of the user.
 * @property password Base64 encoded hashed/salted password.
 * @property salt Salt of the user.
 */
data class User(
    val id: Int,
    val email: String,
    val password: String,
    val salt: String
)