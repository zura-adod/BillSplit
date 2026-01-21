package com.mobelio.bill.split.data.repository

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import com.mobelio.bill.split.domain.model.PhoneContact
import com.mobelio.bill.split.domain.repository.ContactsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ContactsRepository {

    override suspend fun getContacts(): List<PhoneContact> {
        if (!hasContactsPermission()) {
            return emptyList()
        }

        val contactsMap = mutableMapOf<String, MutablePhoneContact>()

        // Get contact names
        val contentResolver: ContentResolver = context.contentResolver
        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                ContactsContract.Contacts.PHOTO_THUMBNAIL_URI
            ),
            null,
            null,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " ASC"
        )

        cursor?.use {
            val idIndex = it.getColumnIndex(ContactsContract.Contacts._ID)
            val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
            val photoIndex = it.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI)

            while (it.moveToNext()) {
                val id = it.getString(idIndex) ?: continue
                val name = it.getString(nameIndex) ?: ""
                val photoUri = it.getString(photoIndex)

                contactsMap[id] = MutablePhoneContact(
                    id = id,
                    name = name,
                    avatarUri = photoUri
                )
            }
        }

        // Get phone numbers
        val phoneCursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            null,
            null,
            null
        )

        phoneCursor?.use {
            val contactIdIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (it.moveToNext()) {
                val contactId = it.getString(contactIdIndex) ?: continue
                val number = it.getString(numberIndex) ?: continue

                contactsMap[contactId]?.phoneNumbers?.add(number)
            }
        }

        // Get emails
        val emailCursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Email.CONTACT_ID,
                ContactsContract.CommonDataKinds.Email.ADDRESS
            ),
            null,
            null,
            null
        )

        emailCursor?.use {
            val contactIdIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Email.CONTACT_ID)
            val emailIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)

            while (it.moveToNext()) {
                val contactId = it.getString(contactIdIndex) ?: continue
                val email = it.getString(emailIndex) ?: continue

                contactsMap[contactId]?.emails?.add(email)
            }
        }

        // Filter contacts with at least one phone or email
        return contactsMap.values
            .filter { it.phoneNumbers.isNotEmpty() || it.emails.isNotEmpty() }
            .map { it.toPhoneContact() }
    }

    override suspend fun searchContacts(query: String): List<PhoneContact> {
        val allContacts = getContacts()
        if (query.isBlank()) return allContacts

        val lowerQuery = query.lowercase()
        return allContacts.filter { contact ->
            contact.name.lowercase().contains(lowerQuery) ||
            contact.phoneNumbers.any { it.contains(query) } ||
            contact.emails.any { it.lowercase().contains(lowerQuery) }
        }
    }

    override fun hasContactsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private data class MutablePhoneContact(
        val id: String,
        val name: String,
        val avatarUri: String?,
        val phoneNumbers: MutableList<String> = mutableListOf(),
        val emails: MutableList<String> = mutableListOf()
    ) {
        fun toPhoneContact() = PhoneContact(
            id = id,
            name = name,
            phoneNumbers = phoneNumbers.distinct(),
            emails = emails.distinct(),
            avatarUri = avatarUri
        )
    }
}

