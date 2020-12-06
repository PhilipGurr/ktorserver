package com.philipgurr.database

import com.philipgurr.User
import org.jetbrains.exposed.sql.transactions.transaction

class ExposedUserRepository : UserRepository {
    override fun getById(userId: Int) = transaction {
        UserEntity.findById(userId)?.toDTO()
    }

    override fun getByUsername(username: String) = transaction {
        UserEntity.find { Users.username eq username }.first().toDTO()
    }

    override fun add(user: User) = transaction {
        UserEntity.new {
            username = user.username
            password = user.password
        }.toDTO()
    }
}