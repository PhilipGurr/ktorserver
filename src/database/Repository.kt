package com.philipgurr.database

import com.philipgurr.Snippet
import com.philipgurr.User

interface SnippetRepository {
    fun getById(snippetId: Int): Snippet?
    fun getAllForUser(userId: Int): List<Snippet>
    fun add(snippet: Snippet): Snippet?
}

interface UserRepository {
    fun getById(userId: Int): User?
    fun getByUsername(username: String): User?
    fun add(user: User): User
}