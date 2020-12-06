package com.philipgurr.di

import com.philipgurr.database.ExposedSnippetRepository
import com.philipgurr.database.ExposedUserRepository
import com.philipgurr.database.SnippetRepository
import com.philipgurr.database.UserRepository
import org.koin.dsl.module
import org.koin.experimental.builder.singleBy

val applicationModule = module(createdAtStart = true) {
    singleBy<UserRepository, ExposedUserRepository>()
    singleBy<SnippetRepository, ExposedSnippetRepository>()
}