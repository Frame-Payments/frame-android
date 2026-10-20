package com.framepayments.frameonboarding.views

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.framepayments.frameonboarding.classes.BusinessRole
import com.framepayments.frameonboarding.networking.beneficialowners.BeneficialOwner
import com.framepayments.frameonboarding.viewmodels.FrameOnboardingViewModel
import com.framepayments.framesdk_ui.reusable.ContinueButton
import com.framepayments.framesdk_ui.reusable.ValidatedTextField
import com.framepayments.framesdk_ui.theme.LocalFrameTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OwnersAndControllersScreen(viewModel: FrameOnboardingViewModel) {
    val theme = LocalFrameTheme.current
    val owners by viewModel.beneficialOwners.collectAsState()
    val isLoading by viewModel.isPerformingAction.collectAsState()
    var showingInvite by remember { mutableStateOf(false) }
    var expandedOwnerId by remember { mutableStateOf<String?>(null) }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(BusinessRole.OWNER) }
    var formError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { viewModel.loadBeneficialOwners() }

    Scaffold(
        containerColor = theme.colors.surface,
        topBar = {
            TopAppBar(
                title = { Text("Owners and controllers", style = theme.fonts.heading) },
                navigationIcon = {
                    TextButton(onClick = { viewModel.moveBack() }) { Text("Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = theme.colors.surface)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Add anyone who owns 25% or more of the business, and anyone who controls it.",
                    style = theme.fonts.bodySmall,
                    color = theme.colors.textSecondary
                )
                Spacer(Modifier.height(theme.spacing.formBlock))
                owners.forEach { owner ->
                    OwnerRow(
                        owner = owner,
                        expanded = expandedOwnerId == owner.id && owner.status == "invite_sent",
                        onToggle = {
                            if (owner.status == "invite_sent") {
                                expandedOwnerId = if (expandedOwnerId == owner.id) null else owner.id
                            }
                        },
                        onResend = { viewModel.resendBeneficialOwnerInvite(owner.id) },
                        onRemove = { viewModel.removeBeneficialOwner(owner.id) }
                    )
                    Spacer(Modifier.height(8.dp))
                }
                if (showingInvite) {
                    ValidatedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        prompt = "First name",
                        error = null,
                        inlineError = true
                    )
                    Spacer(Modifier.height(8.dp))
                    ValidatedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        prompt = "Last name",
                        error = null,
                        inlineError = true
                    )
                    Spacer(Modifier.height(8.dp))
                    ValidatedTextField(
                        value = email,
                        onValueChange = { email = it },
                        prompt = "Email",
                        error = null,
                        keyboardType = KeyboardType.Email,
                        inlineError = true
                    )
                    Row {
                        BusinessRole.entries.forEach { item ->
                            TextButton(onClick = { role = item }) {
                                Text(
                                    text = if (item == BusinessRole.OWNER) "Owner" else "Controller",
                                    color = if (role == item) theme.colors.primaryButton else theme.colors.textSecondary
                                )
                            }
                        }
                    }
                    formError?.let { message ->
                        Text(text = message, style = theme.fonts.caption, color = theme.colors.error)
                    }
                    Row {
                        TextButton(onClick = {
                            showingInvite = false
                            formError = null
                        }) { Text("Cancel") }
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = {
                            if (firstName.isBlank() || lastName.isBlank() || email.isBlank()) {
                                formError = "Enter a name and email."
                                viewModel.reportUserError("Enter a name and email.")
                            } else {
                                formError = null
                                viewModel.inviteBeneficialOwner(
                                    firstName.trim(),
                                    lastName.trim(),
                                    email.trim(),
                                    role
                                ) { sent ->
                                    if (sent) {
                                        firstName = ""
                                        lastName = ""
                                        email = ""
                                        showingInvite = false
                                    }
                                }
                            }
                        }) { Text("Send invite") }
                    }
                } else {
                    TextButton(onClick = { showingInvite = true }) {
                        Text("Add owner or controller", color = theme.colors.primaryButton)
                    }
                }
            }
            ContinueButton(isLoading = isLoading, onClick = { viewModel.confirmBeneficialOwners() })
        }
    }
}

@Composable
private fun OwnerRow(
    owner: BeneficialOwner,
    expanded: Boolean,
    onToggle: () -> Unit,
    onResend: () -> Unit,
    onRemove: () -> Unit
) {
    val theme = LocalFrameTheme.current
    val name = listOfNotNull(owner.firstName, owner.lastName).joinToString(" ").ifBlank { owner.email ?: "Owner" }
    val initials = listOfNotNull(owner.firstName?.firstOrNull(), owner.lastName?.firstOrNull())
        .joinToString("")
        .ifBlank { "?" }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, theme.colors.surfaceStroke, RoundedCornerShape(theme.radii.medium))
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = initials,
                style = theme.fonts.label,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .border(1.dp, theme.colors.surfaceStroke, CircleShape)
                    .padding(top = 8.dp)
            )
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(text = name, style = theme.fonts.body, color = theme.colors.textPrimary)
                Text(
                    text = listOf(roleLabel(owner), statusLabel(owner.status)).filter { it.isNotEmpty() }.joinToString(" · "),
                    style = theme.fonts.caption,
                    color = theme.colors.textSecondary
                )
            }
            if (owner.status == "invite_sent") {
                TextButton(onClick = onToggle) { Text(if (expanded) "Hide" else "More") }
            }
        }
        if (expanded) {
            Row {
                TextButton(onClick = onResend) { Text("Resend invite") }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onRemove) { Text("Remove") }
            }
        }
    }
}

private fun roleLabel(owner: BeneficialOwner): String {
    val roles = owner.roles.orEmpty()
    return when {
        roles.contains("owner") -> "OWNER"
        roles.contains("controller") -> "CONTROLLER"
        else -> roles.firstOrNull()?.uppercase().orEmpty()
    }
}

private fun statusLabel(status: String?): String = when (status) {
    "completed" -> "COMPLETED"
    "invite_sent" -> "INVITE SENT"
    else -> "PENDING"
}
