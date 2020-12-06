package com.philipgurr.database

import com.philipgurr.Snippet
import org.jetbrains.exposed.sql.transactions.transaction

class ExposedSnippetRepository : SnippetRepository {
    override fun getById(snippetId: Int) = transaction {
        SnippetEntity.findById(snippetId)?.toDTO()
    }

    override fun getAllForUser(userId: Int) = transaction {
        UserEntity.findById(userId)?.snippets?.map { it.toDTO() } ?: listOf()
    }

    override fun add(snippet: Snippet) = transaction {
        val associatedUser = UserEntity.findById(snippet.userId) ?: return@transaction null
        SnippetEntity.new {
            text = snippet.text
            user = associatedUser
        }.toDTO()
    }

}