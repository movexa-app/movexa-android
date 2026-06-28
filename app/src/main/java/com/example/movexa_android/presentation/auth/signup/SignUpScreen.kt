package com.example.movexa_android.presentation.auth.signup

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.movexa.android.ui.theme.*

@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    var step by remember { mutableStateOf(1) }

    // Step 1 fields
    var name     by remember { mutableStateOf("") }
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Step 2 fields
    var age      by remember { mutableStateOf("") }
    var weight   by remember { mutableStateOf("") }
    var height   by remember { mutableStateOf("") }
    var goalIndex by remember { mutableStateOf(-1) }
    val goals = listOf("Lose weight", "Build muscle", "Improve endurance", "Stay active")

    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(MovexaBlue.copy(alpha = 0.10f), Color.Transparent),
                            radius = 700f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 48.dp)
            ) {
                // Step indicator
                StepIndicator(current = step, total = 2)

                Spacer(Modifier.height(32.dp))

                AnimatedContent(targetState = step, transitionSpec = {
                    slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                }, label = "step") { currentStep ->
                    when (currentStep) {
                        1 -> StepOne(
                            name = name, onNameChange = { name = it },
                            email = email, onEmailChange = { email = it },
                            password = password, onPasswordChange = { password = it },
                            passwordVisible = passwordVisible,
                            onTogglePassword = { passwordVisible = !passwordVisible },
                            onNext = { step = 2 }
                        )
                        2 -> StepTwo(
                            age = age, onAgeChange = { age = it },
                            weight = weight, onWeightChange = { weight = it },
                            height = height, onHeightChange = { height = it },
                            goals = goals,
                            selectedGoal = goalIndex,
                            onGoalSelect = { goalIndex = it },
                            isLoading = isLoading,
                            onBack = { step = 1 },
                            onFinish = {
                                viewModel.signUp(name, email, password, onSignUpSuccess)
                            }
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                if (step == 1) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Already have an account?", color = DarkTextSecondary, fontSize = 14.sp)
                        TextButton(onClick = onNavigateToLogin) {
                            Text("Sign In", color = MovexaBlue, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(current: Int, total: Int) {
    Column {
        Text(
            "MOVEXA",
            color = MovexaBlue,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 4.sp
        )
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(total) { i ->
                Box(
                    modifier = Modifier
                        .height(3.dp)
                        .weight(1f)
                        .background(
                            if (i < current) MovexaBlue else DarkBorder,
                            RoundedCornerShape(2.dp)
                        )
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text("Step $current of $total", color = DarkTextSecondary, fontSize = 12.sp)
    }
}

@Composable
private fun StepOne(
    name: String, onNameChange: (String) -> Unit,
    email: String, onEmailChange: (String) -> Unit,
    password: String, onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean, onTogglePassword: () -> Unit,
    onNext: () -> Unit
) {
    Column {
        Text("Create account", color = DarkTextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text("Set up your profile to get started", color = DarkTextSecondary, fontSize = 14.sp)
        Spacer(Modifier.height(32.dp))

        SignUpField(name, onNameChange, "Full name", Icons.Outlined.Person)
        Spacer(Modifier.height(14.dp))
        SignUpField(email, onEmailChange, "Email", Icons.Outlined.Email, KeyboardType.Email)
        Spacer(Modifier.height(14.dp))
        SignUpField(
            password, onPasswordChange, "Password", Icons.Outlined.Lock,
            isPassword = true, passwordVisible = passwordVisible,
            onToggle = onTogglePassword
        )

        Spacer(Modifier.height(28.dp))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MovexaBlue),
            enabled = name.isNotBlank() && email.isNotBlank() && password.length >= 6
        ) {
            Text("Continue", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        }
    }
}

@Composable
private fun StepTwo(
    age: String, onAgeChange: (String) -> Unit,
    weight: String, onWeightChange: (String) -> Unit,
    height: String, onHeightChange: (String) -> Unit,
    goals: List<String>,
    selectedGoal: Int,
    onGoalSelect: (Int) -> Unit,
    isLoading: Boolean,
    onBack: () -> Unit,
    onFinish: () -> Unit
) {
    Column {
        Text("Fitness profile", color = DarkTextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(6.dp))
        Text("Help us personalise your experience", color = DarkTextSecondary, fontSize = 14.sp)
        Spacer(Modifier.height(32.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.weight(1f)) {
                SignUpField(age, onAgeChange, "Age", Icons.Outlined.Cake, KeyboardType.Number)
            }
            Box(Modifier.weight(1f)) {
                SignUpField(weight, onWeightChange, "Weight (kg)", Icons.Outlined.FitnessCenter, KeyboardType.Number)
            }
        }
        Spacer(Modifier.height(14.dp))
        SignUpField(height, onHeightChange, "Height (cm)", Icons.Outlined.Height, KeyboardType.Number)

        Spacer(Modifier.height(24.dp))

        Text("Primary goal", color = DarkTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))

        goals.forEachIndexed { i, goal ->
            val selected = selectedGoal == i
            OutlinedButton(
                onClick = { onGoalSelect(i) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, if (selected) MovexaBlue else DarkBorder
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (selected) MovexaBlue.copy(alpha = 0.1f) else DarkSurface
                )
            ) {
                Text(
                    goal,
                    color = if (selected) MovexaBlue else DarkTextSecondary,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = DarkSurface),
                enabled = !isLoading
            ) {
                Text("Back", color = DarkTextPrimary, fontWeight = FontWeight.Medium)
            }
            Button(
                onClick = onFinish,
                modifier = Modifier.weight(2f).height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MovexaBlue),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Create Account", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
private fun SignUpField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onToggle: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = DarkTextSecondary, fontSize = 13.sp) },
        leadingIcon = { Icon(icon, null, tint = DarkTextSecondary, modifier = Modifier.size(19.dp)) },
        trailingIcon = if (isPassword) ({
            IconButton(onClick = { onToggle?.invoke() }) {
                Icon(
                    if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                    null, tint = DarkTextSecondary, modifier = Modifier.size(19.dp)
                )
            }
        }) else null,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MovexaBlue,
            unfocusedBorderColor = DarkBorder,
            focusedTextColor = DarkTextPrimary,
            unfocusedTextColor = DarkTextPrimary,
            cursorColor = MovexaBlue,
            focusedContainerColor = DarkSurface,
            unfocusedContainerColor = DarkSurface
        ),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}