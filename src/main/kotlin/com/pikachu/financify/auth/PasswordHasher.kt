package com.pikachu.financify.auth

import org.mindrot.jbcrypt.BCrypt

object PasswordHasher {

    private const val LOG_ROUNDS = 12

    fun hash(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt(LOG_ROUNDS))
    }

    fun verify(password: String, hashedPassword: String): Boolean {
        return BCrypt.checkpw(password, hashedPassword)
    }
}
