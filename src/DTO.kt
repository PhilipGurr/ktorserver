package com.philipgurr

data class Snippet(
    val snippetId: Int = 0,
    val text: String,
    val userId: Int
)

data class User(
    val userId: Int = 0,
    val username: String,
    val password: String,
    val snippets: List<Snippet> = listOf()
)

data class UsernamePassword(
    val username: String,
    val password: String
)