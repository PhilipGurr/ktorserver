package com.philipgurr.database

import com.philipgurr.Snippet
import com.philipgurr.User

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Users : IntIdTable() {
    val username = varchar("username", 50)
    val password = varchar("password", 50)
}

class UserEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserEntity>(Users)
    var username by Users.username
    var password by Users.password
    val snippets by SnippetEntity referrersOn Snippets.user
}

fun UserEntity.toDTO(): User {
    return User(
        id.value,
        username,
        password,
        snippets.map { it.toDTO() }
    )
}

object Snippets : IntIdTable() {
    val text = varchar("text", 150)
    val user = reference("user", Users)
}

class SnippetEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<SnippetEntity>(Snippets)
    var text by Snippets.text
    var user by UserEntity referencedOn Snippets.user
}

fun SnippetEntity.toDTO(): Snippet {
    return Snippet(
        id.value,
        text,
        user.id.value
    )
}