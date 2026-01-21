package com.mobelio.bill.split.presentation.screens.contactpicker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobelio.bill.split.domain.model.ContactMethod
import com.mobelio.bill.split.domain.model.Participant
import com.mobelio.bill.split.domain.repository.ContactsRepository
import com.mobelio.bill.split.presentation.state.BillSplitStateHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactPickerViewModel @Inject constructor(
    private val contactsRepository: ContactsRepository,
    private val billSplitStateHolder: BillSplitStateHolder
) : ViewModel() {

    private val _state = MutableStateFlow(ContactPickerState())
    val state: StateFlow<ContactPickerState> = _state.asStateFlow()

    private var allContacts = listOf<com.mobelio.bill.split.domain.model.PhoneContact>()

    init {
        checkPermissionAndLoad()
    }

    fun onEvent(event: ContactPickerEvent) {
        when (event) {
            ContactPickerEvent.LoadContacts -> loadContacts()

            is ContactPickerEvent.SearchQueryChanged -> {
                _state.update { it.copy(searchQuery = event.query) }
                filterContacts(event.query)
            }

            is ContactPickerEvent.ToggleContactSelection -> {
                toggleSelection(event.contactId)
            }

            is ContactPickerEvent.SelectContactDetail -> {
                selectContactDetail(event.contactId, event.value, event.isPhone)
            }

            ContactPickerEvent.ConfirmSelection -> {
                confirmSelection()
            }

            ContactPickerEvent.RequestPermission -> {
                _state.update { it.copy(showPermissionRequest = true) }
            }

            ContactPickerEvent.PermissionGranted -> {
                _state.update { it.copy(hasPermission = true, showPermissionRequest = false) }
                loadContacts()
            }

            ContactPickerEvent.PermissionDenied -> {
                _state.update { it.copy(hasPermission = false, showPermissionRequest = false) }
            }

            ContactPickerEvent.ContinueWithoutContacts -> {
                _state.update { it.copy(showPermissionRequest = false) }
            }

            ContactPickerEvent.DismissError -> {
                _state.update { it.copy(error = null) }
            }
        }
    }

    private fun checkPermissionAndLoad() {
        val hasPermission = contactsRepository.hasContactsPermission()
        _state.update {
            it.copy(
                hasPermission = hasPermission,
                showPermissionRequest = !hasPermission
            )
        }
        if (hasPermission) {
            loadContacts()
        }
    }

    private fun loadContacts() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                allContacts = contactsRepository.getContacts()
                _state.update {
                    it.copy(
                        contacts = allContacts,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = "Failed to load contacts: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun filterContacts(query: String) {
        if (query.isBlank()) {
            _state.update { it.copy(contacts = allContacts) }
        } else {
            val lowerQuery = query.lowercase()
            val filtered = allContacts.filter { contact ->
                contact.name.lowercase().contains(lowerQuery) ||
                contact.phoneNumbers.any { it.contains(query) } ||
                contact.emails.any { it.lowercase().contains(lowerQuery) }
            }
            _state.update { it.copy(contacts = filtered) }
        }
    }

    private fun toggleSelection(contactId: String) {
        _state.update { state ->
            val currentSelected = state.selectedContacts.toMutableSet()
            val currentDetails = state.selectedContactDetails.toMutableMap()

            if (contactId in currentSelected) {
                currentSelected.remove(contactId)
                currentDetails.remove(contactId)
            } else {
                currentSelected.add(contactId)
                // Auto-select first phone or email
                val contact = allContacts.find { it.id == contactId }
                if (contact != null) {
                    val firstPhone = contact.phoneNumbers.firstOrNull()
                    val firstEmail = contact.emails.firstOrNull()
                    when {
                        firstPhone != null -> {
                            currentDetails[contactId] = SelectedContactDetail(
                                contactId = contactId,
                                selectedValue = firstPhone,
                                isPhone = true
                            )
                        }
                        firstEmail != null -> {
                            currentDetails[contactId] = SelectedContactDetail(
                                contactId = contactId,
                                selectedValue = firstEmail,
                                isPhone = false
                            )
                        }
                    }
                }
            }

            state.copy(
                selectedContacts = currentSelected,
                selectedContactDetails = currentDetails
            )
        }
    }

    private fun selectContactDetail(contactId: String, value: String, isPhone: Boolean) {
        _state.update { state ->
            val currentDetails = state.selectedContactDetails.toMutableMap()
            currentDetails[contactId] = SelectedContactDetail(
                contactId = contactId,
                selectedValue = value,
                isPhone = isPhone
            )
            state.copy(selectedContactDetails = currentDetails)
        }
    }

    private fun confirmSelection() {
        val state = _state.value
        val participants = state.selectedContactDetails.mapNotNull { (contactId, detail) ->
            val contact = allContacts.find { it.id == contactId } ?: return@mapNotNull null
            Participant(
                name = contact.name,
                contactValue = detail.selectedValue,
                contactMethod = if (detail.isPhone) ContactMethod.PHONE else ContactMethod.EMAIL,
                avatarUri = contact.avatarUri,
                isFromContacts = true
            )
        }

        billSplitStateHolder.addParticipants(participants)
    }
}

