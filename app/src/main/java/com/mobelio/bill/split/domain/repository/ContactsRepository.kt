package com.mobelio.bill.split.domain.repository

import com.mobelio.bill.split.domain.model.PhoneContact
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for accessing phone contacts
 */
interface ContactsRepository {
    /**
     * Get all contacts from the phone
     */
    suspend fun getContacts(): List<PhoneContact>

    /**
     * Search contacts by name or contact value
     */
    suspend fun searchContacts(query: String): List<PhoneContact>

    /**
     * Check if contacts permission is granted
     */
    fun hasContactsPermission(): Boolean
}

