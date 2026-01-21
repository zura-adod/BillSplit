package com.mobelio.bill.split.di

import com.mobelio.bill.split.data.repository.ContactsRepositoryImpl
import com.mobelio.bill.split.domain.repository.ContactsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindContactsRepository(
        impl: ContactsRepositoryImpl
    ): ContactsRepository
}

