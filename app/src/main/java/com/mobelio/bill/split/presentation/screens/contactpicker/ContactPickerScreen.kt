package com.mobelio.bill.split.presentation.screens.contactpicker

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.mobelio.bill.split.domain.model.PhoneContact

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ContactPickerScreen(
    viewModel: ContactPickerViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onContactsSelected: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    val contactsPermissionState = rememberPermissionState(
        permission = Manifest.permission.READ_CONTACTS
    ) { isGranted ->
        if (isGranted) {
            viewModel.onEvent(ContactPickerEvent.PermissionGranted)
        } else {
            viewModel.onEvent(ContactPickerEvent.PermissionDenied)
        }
    }

    // Check permission status
    LaunchedEffect(contactsPermissionState.status.isGranted) {
        if (contactsPermissionState.status.isGranted) {
            viewModel.onEvent(ContactPickerEvent.PermissionGranted)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Contacts") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.selectedContacts.isNotEmpty()) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(state.selectedContacts.size.toString())
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (state.hasPermission && state.selectedContacts.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp
                ) {
                    Button(
                        onClick = {
                            viewModel.onEvent(ContactPickerEvent.ConfirmSelection)
                            onContactsSelected()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Add ${state.selectedContacts.size} Participant${if (state.selectedContacts.size > 1) "s" else ""}",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.showPermissionRequest && !state.hasPermission -> {
                    PermissionRequestContent(
                        shouldShowRationale = contactsPermissionState.status.shouldShowRationale,
                        onRequestPermission = { contactsPermissionState.launchPermissionRequest() },
                        onContinueWithout = {
                            viewModel.onEvent(ContactPickerEvent.ContinueWithoutContacts)
                            onNavigateBack()
                        }
                    )
                }

                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                state.contacts.isEmpty() && state.hasPermission -> {
                    EmptyContactsContent()
                }

                else -> {
                    ContactsList(
                        contacts = state.contacts,
                        selectedContacts = state.selectedContacts,
                        selectedDetails = state.selectedContactDetails,
                        searchQuery = state.searchQuery,
                        onSearchQueryChanged = {
                            viewModel.onEvent(ContactPickerEvent.SearchQueryChanged(it))
                        },
                        onToggleContact = {
                            viewModel.onEvent(ContactPickerEvent.ToggleContactSelection(it))
                        },
                        onSelectDetail = { contactId, value, isPhone ->
                            viewModel.onEvent(ContactPickerEvent.SelectContactDetail(contactId, value, isPhone))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionRequestContent(
    shouldShowRationale: Boolean,
    onRequestPermission: () -> Unit,
    onContinueWithout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Contacts,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Access Contacts",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (shouldShowRationale) {
                "We need access to your contacts to help you quickly select participants for bill splitting. Your contacts are used locally and never uploaded."
            } else {
                "Allow access to your contacts to easily add participants from your phonebook."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRequestPermission,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Allow Contacts")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onContinueWithout,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Continue Without Contacts")
        }
    }
}

@Composable
private fun EmptyContactsContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.PersonOff,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No contacts found",
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            text = "Your contact list appears to be empty",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ContactsList(
    contacts: List<PhoneContact>,
    selectedContacts: Set<String>,
    selectedDetails: Map<String, SelectedContactDetail>,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
    onToggleContact: (String) -> Unit,
    onSelectDetail: (contactId: String, value: String, isPhone: Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Search contacts...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChanged("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(
                items = contacts,
                key = { it.id }
            ) { contact ->
                ContactItem(
                    contact = contact,
                    isSelected = contact.id in selectedContacts,
                    selectedDetail = selectedDetails[contact.id],
                    onToggle = { onToggleContact(contact.id) },
                    onSelectDetail = { value, isPhone ->
                        onSelectDetail(contact.id, value, isPhone)
                    }
                )
            }
        }
    }
}

@Composable
private fun ContactItem(
    contact: PhoneContact,
    isSelected: Boolean,
    selectedDetail: SelectedContactDetail?,
    onToggle: () -> Unit,
    onSelectDetail: (value: String, isPhone: Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.primaryContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            text = contact.name.firstOrNull()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = contact.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (!isSelected) {
                        // Show preview of contact info
                        val preview = contact.phoneNumbers.firstOrNull()
                            ?: contact.emails.firstOrNull()
                            ?: ""
                        if (preview.isNotEmpty()) {
                            Text(
                                text = preview,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggle() }
                )
            }

            // Show contact detail selection when selected
            if (isSelected && (contact.phoneNumbers.size > 1 || contact.emails.isNotEmpty())) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Select contact method:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Phone numbers
                contact.phoneNumbers.forEach { phone ->
                    ContactDetailOption(
                        icon = Icons.Default.Phone,
                        value = phone,
                        isSelected = selectedDetail?.selectedValue == phone && selectedDetail.isPhone,
                        onClick = { onSelectDetail(phone, true) }
                    )
                }

                // Emails
                contact.emails.forEach { email ->
                    ContactDetailOption(
                        icon = Icons.Default.Email,
                        value = email,
                        isSelected = selectedDetail?.selectedValue == email && !selectedDetail.isPhone,
                        onClick = { onSelectDetail(email, false) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ContactDetailOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

