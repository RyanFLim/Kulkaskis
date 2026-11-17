package org.umn.kulkaskis.ui.auth

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import org.umn.kulkaskis.ui.home.kFieldColors
import org.umn.kulkaskis.ui.theme.KulkaskisTheme

@Composable
fun AuthScreen(
    isRegister: Boolean,
    loading: Boolean,
    message: String?,
    onSubmit: (String, String) -> Unit,
    onSwitch: () -> Unit,
    onGuest: (() -> Unit)? = null,
    onNameChange: (String) -> Unit = {}
) {
    var fullName by remember { mutableStateOf("") }
    var nameTouched by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    // A field shows its error after the user has typed in it, or after pressing the button.
    var emailTouched by remember { mutableStateOf(false) }
    var passwordTouched by remember { mutableStateOf(false) }
    var confirmTouched by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }

    val nameError: String? = if (!isRegister) null else when {
        fullName.isBlank() -> "Full name is required"
        fullName.trim().length < 2 -> "Name is too short"
        else -> null
    }
    val emailError: String? = when {
        email.isBlank() -> "Email is required"
        !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> "Enter a valid email address"
        else -> null
    }
    val passwordError: String? = when {
        password.isBlank() -> "Password is required"
        password.length < 6 -> "Password must be at least 6 characters"
        else -> null
    }
    val confirmError: String? = if (!isRegister) null else when {
        confirm.isBlank() -> "Please confirm your password"
        confirm != password -> "Passwords do not match"
        else -> null
    }

    val showNameError = (nameTouched || submitted) && nameError != null
    val showEmailError = (emailTouched || submitted) && emailError != null
    val showPasswordError = (passwordTouched || submitted) && passwordError != null
    val showConfirmError = (confirmTouched || submitted) && confirmError != null

    val fieldShape = RoundedCornerShape(10.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(56.dp))
        // Logo: green rounded square with "K"
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(22.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("K", color = MaterialTheme.colorScheme.onPrimary, fontSize = 34.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "Kulkaskis",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            if (isRegister) "Start tracking what is in your fridge"
            else "Track your fridge, waste less",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(28.dp))

        if (isRegister) {
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it; nameTouched = true; onNameChange(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Full name") },
                singleLine = true,
                isError = showNameError,
                supportingText = { if (showNameError) Text(nameError.orEmpty()) },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                shape = fieldShape,
                colors = kFieldColors()
            )
            Spacer(Modifier.height(4.dp))
        }
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; emailTouched = true },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") },
            singleLine = true,
            isError = showEmailError,
            supportingText = { if (showEmailError) Text(emailError.orEmpty()) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            shape = fieldShape,
            colors = kFieldColors()
        )
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; passwordTouched = true },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
            singleLine = true,
            isError = showPasswordError,
            supportingText = { if (showPasswordError) Text(passwordError.orEmpty()) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = if (isRegister) ImeAction.Next else ImeAction.Done
            ),
            shape = fieldShape,
            colors = kFieldColors()
        )
        if (isRegister) {
            Spacer(Modifier.height(4.dp))
            OutlinedTextField(
                value = confirm,
                onValueChange = { confirm = it; confirmTouched = true },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Confirm password") },
                singleLine = true,
                isError = showConfirmError,
                supportingText = { if (showConfirmError) Text(confirmError.orEmpty()) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                shape = fieldShape,
                colors = kFieldColors()
            )
        }

        // Message from the server (wrong password, account created, ...)
        if (message != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = if (message.startsWith("Account created"))
                    MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                submitted = true
                // Submission is blocked until every field is valid
                val valid = nameError == null && emailError == null && passwordError == null && confirmError == null
                if (valid) onSubmit(email.trim(), password)
            },
            enabled = !loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    if (isRegister) "Register" else "Log in",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (!isRegister && onGuest != null) {
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onGuest,
                enabled = !loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Continue as guest (demo)", fontWeight = FontWeight.SemiBold)
            }
        }

        TextButton(onClick = onSwitch) {
            Text(
                if (isRegister) "Already have an account? Log in" else "No account yet? Register",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun AuthScreenPreview() {
    KulkaskisTheme {
        AuthScreen(
            isRegister = false,
            loading = false,
            message = null,
            onSubmit = { _, _ -> },
            onSwitch = {},
            onGuest = {}
        )
    }
}