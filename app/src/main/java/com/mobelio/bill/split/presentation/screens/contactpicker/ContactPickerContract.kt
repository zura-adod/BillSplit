package com.mobelio.bill.split.presentation.screens.contactpicker

import com.mobelio.bill.split.domain.model.PhoneContact

data class ContactPickerState(
    val contacts: List<PhoneContact> = emptyList(),
    val selectedContacts: Set<String> = emptySet(), // Contact IDs
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val hasPermission: Boolean = false,
    val showPermissionRequest: Boolean = false,
    val selectedContactDetails: Map<String, SelectedContactDetail> = emptyMap(), // Contact ID to detail
    val error: String? = null
)

data class SelectedContactDetail(
    val contactId: String,
    val selectedValue: String, // Phone or email
    val isPhone: Boolean
)

sealed interface ContactPickerEvent {
    data object LoadContacts : ContactPickerEvent
    data class SearchQueryChanged(val query: String) : ContactPickerEvent
    data class ToggleContactSelection(val contactId: String) : ContactPickerEvent
    data class SelectContactDetail(val contactId: String, val value: String, val isPhone: Boolean) : ContactPickerEvent
    data object ConfirmSelection : ContactPickerEvent
    data object RequestPermission : ContactPickerEvent
    data object PermissionGranted : ContactPickerEvent
    data object PermissionDenied : ContactPickerEvent
    data object ContinueWithoutContacts : ContactPickerEvent
    data object DismissError : ContactPickerEvent
}

