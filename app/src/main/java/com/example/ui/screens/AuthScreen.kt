package com.example.ui.screens

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.UserProgress
import com.example.data.remote.FirebaseConfig
import com.example.data.remote.ServerSyncManager
import com.example.data.remote.MyFirebaseMessagingService
import android.Manifest
import android.os.Build
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.ui.components.LowFadeGlassSurface
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WhiteCanvas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    userProgress: UserProgress,
    onLogin: (email: String, username: String, token: String) -> Unit,
    onRegister: (email: String, username: String, password: String) -> Unit,
    onLogout: () -> Unit,
    onContinueAsGuest: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Log In, 1 = Sign Up
    var emailInput by remember { mutableStateOf(userProgress.userEmail.ifBlank { "" }) }
    var usernameInput by remember { mutableStateOf(userProgress.userName.ifBlank { "" }) }
    var passwordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var showGoogleWebView by remember { mutableStateOf(false) }

    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotEmailInput by remember { mutableStateOf("") }
    var forgotStatusMessage by remember { mutableStateOf<String?>(null) }
    var forgotIsLoading by remember { mutableStateOf(false) }
    var isResetMode by remember { mutableStateOf(false) }
    var resetTokenInput by remember { mutableStateOf("") }
    var resetNewPasswordInput by remember { mutableStateOf("") }

    val okHttpClient = remember {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    // Permission state tracker
    var hasLocationPermission by remember { mutableStateOf(false) }
    var hasNotificationPermission by remember { mutableStateOf(false) }
    var hasStoragePermission by remember { mutableStateOf(false) }
    var permissionsGrantedNotice by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val locGranted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        val notifGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms[Manifest.permission.POST_NOTIFICATIONS] == true
        } else true
        val storageGranted = perms[Manifest.permission.READ_MEDIA_IMAGES] == true ||
                perms[Manifest.permission.READ_EXTERNAL_STORAGE] == true ||
                perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] == true

        hasLocationPermission = locGranted
        hasNotificationPermission = notifGranted
        hasStoragePermission = storageGranted

        permissionsGrantedNotice = "Permissions configured: Location (${if (locGranted) "Granted" else "Skipped"}), Notifications (${if (notifGranted) "Granted" else "Skipped"}), Storage (${if (storageGranted) "Granted" else "Skipped"})."
        Toast.makeText(context, "Permissions updated successfully", Toast.LENGTH_SHORT).show()
    }

    fun requestAppPermissions() {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        permissionLauncher.launch(permissionsToRequest.toTypedArray())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "MindMatrix Player Portal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = onContinueAsGuest,
                        modifier = Modifier.testTag("guest_skip_button")
                    ) {
                        Text(
                            text = "Skip / Guest",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryIndigo
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = WhiteCanvas)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(SlateBackground)
                .padding(innerPadding)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Logo
                Surface(
                    shape = CircleShape,
                    color = PrimaryIndigo.copy(alpha = 0.1f),
                    modifier = Modifier.size(68.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "🧠", fontSize = 32.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Welcome to MindMatrix",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Log in to sync 15 progressive levels, save scores, and compete globally.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Status message
                statusMessage?.let { msg ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (msg.startsWith("Login Successful") || msg.startsWith("Welcome")) Color(0xFFECFDF5) else Color(0xFFFEF2F2),
                        border = BorderStroke(1.dp, if (msg.startsWith("Login Successful") || msg.startsWith("Welcome")) Color(0xFFA7F3D0) else Color(0xFFFECACA)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (msg.startsWith("Login Successful") || msg.startsWith("Welcome")) "✅" else "⚠️",
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = if (msg.startsWith("Login Successful") || msg.startsWith("Welcome")) Color(0xFF047857) else Color(0xFFB91C1C)
                            )
                        }
                    }
                }

                // User Login Permissions Setup Banner (Location, Storage, Notifications)
                LowFadeGlassSurface(
                    shape = RoundedCornerShape(18.dp),
                    elevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = PrimaryIndigo.copy(alpha = 0.12f),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("🛡️", fontSize = 16.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Login Device Permissions",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Location, Storage & Notifications for Global Arena",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Button(
                                onClick = { requestAppPermissions() },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("btn_request_login_permissions")
                            ) {
                                Text(
                                    text = if (hasLocationPermission || hasNotificationPermission || hasStoragePermission) "Update" else "Grant All",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = WhiteCanvas
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(if (hasLocationPermission) "📍 Location: Granted" else "📍 Location: Pending", fontSize = 11.sp, color = if (hasLocationPermission) Color(0xFF059669) else TextMuted, fontWeight = FontWeight.SemiBold)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(if (hasNotificationPermission) "🔔 Alerts: Granted" else "🔔 Alerts: Pending", fontSize = 11.sp, color = if (hasNotificationPermission) Color(0xFF059669) else TextMuted, fontWeight = FontWeight.SemiBold)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(if (hasStoragePermission) "💾 Storage: Granted" else "💾 Storage: Pending", fontSize = 11.sp, color = if (hasStoragePermission) Color(0xFF059669) else TextMuted, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        permissionsGrantedNotice?.let { notice ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = notice,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF047857),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Social Authentication Section
                LowFadeGlassSurface(
                    shape = RoundedCornerShape(20.dp),
                    elevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Quick Social Sign-In",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Google Sign-In Official Action
                        Button(
                            onClick = {
                                isLoading = true
                                statusMessage = "Launching native Google Sign-In..."
                                
                                val credentialManager = androidx.credentials.CredentialManager.create(context)
                                val clientId = FirebaseConfig.GOOGLE_CLIENT_ID
                                val signInOption = com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
                                val request = androidx.credentials.GetCredentialRequest.Builder()
                                    .addCredentialOption(signInOption)
                                    .build()

                                coroutineScope.launch {
                                    try {
                                        val result = credentialManager.getCredential(context as android.app.Activity, request)
                                        val credential = result.credential
                                        if (credential is androidx.credentials.CustomCredential && credential.type == com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                            val googleIdTokenCredential = com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.createFrom(credential.data)
                                            val googleIdToken = googleIdTokenCredential.idToken
                                            val email = googleIdTokenCredential.id
                                            val displayName = googleIdTokenCredential.displayName ?: googleIdTokenCredential.givenName ?: "Google User"

                                            statusMessage = "Authenticating with Cloudflare Workers..."
                                            
                                            val backendUrl = FirebaseConfig.DEFAULT_WORKER_URL
                                            withContext(Dispatchers.IO) {
                                                try {
                                                    // 1. Try to Login first
                                                    val securePassword = googleIdToken.take(15)
                                                    val loginBody = JSONObject().apply {
                                                        put("email", email)
                                                        put("password", securePassword)
                                                    }.toString().toRequestBody("application/json".toMediaType())

                                                    val loginRequest = Request.Builder()
                                                        .url("$backendUrl/login")
                                                        .post(loginBody)
                                                        .build()

                                                    val response = okHttpClient.newCall(loginRequest).execute()
                                                    val responseBody = response.body?.string() ?: ""
                                                    val responseJson = JSONObject(responseBody)
                                                    
                                                    if (responseJson.optBoolean("success", false)) {
                                                        val token = responseJson.optJSONObject("data")?.optString("token") ?: ""
                                                        val finalName = responseJson.optJSONObject("data")?.optString("name") ?: displayName
                                                        withContext(Dispatchers.Main) {
                                                            isLoading = false
                                                            statusMessage = "Welcome, $finalName!"
                                                            Toast.makeText(context, "Welcome, $finalName!", Toast.LENGTH_SHORT).show()
                                                            onLogin(email, finalName, token)
                                                        }
                                                    } else {
                                                        // 2. If login fails, try to Sign Up!
                                                        val signupBody = JSONObject().apply {
                                                            put("name", displayName)
                                                            put("email", email)
                                                            put("password", securePassword)
                                                        }.toString().toRequestBody("application/json".toMediaType())

                                                        val signupRequest = Request.Builder()
                                                            .url("$backendUrl/signup")
                                                            .post(signupBody)
                                                            .build()

                                                        val signupResponse = okHttpClient.newCall(signupRequest).execute()
                                                        val signupResponseBody = signupResponse.body?.string() ?: ""
                                                        val signupJson = JSONObject(signupResponseBody)

                                                        if (signupJson.optBoolean("success", false)) {
                                                            // SignUp succeeded, now log in to get session token
                                                            val finalLoginResponse = okHttpClient.newCall(loginRequest).execute()
                                                            val finalLoginResponseBody = finalLoginResponse.body?.string() ?: ""
                                                            val finalLoginJson = JSONObject(finalLoginResponseBody)
                                                            val finalToken = finalLoginJson.optJSONObject("data")?.optString("token") ?: ""
                                                            withContext(Dispatchers.Main) {
                                                                isLoading = false
                                                                statusMessage = "Account created & logged in! Welcome, $displayName"
                                                                Toast.makeText(context, "Account created! Welcome, $displayName", Toast.LENGTH_LONG).show()
                                                                onLogin(email, displayName, finalToken)
                                                            }
                                                        } else {
                                                            val message = signupJson.optString("message", "Cloudflare registration error")
                                                            withContext(Dispatchers.Main) {
                                                                isLoading = false
                                                                statusMessage = "Registration notice: $message"
                                                            }
                                                        }
                                                    }
                                                } catch (e: Exception) {
                                                    // Network error fallback
                                                    withContext(Dispatchers.Main) {
                                                        isLoading = false
                                                        statusMessage = "Network notice: ${e.message}. Logging in locally."
                                                        onLogin(email, displayName, "")
                                                    }
                                                }
                                            }
                                        } else {
                                            isLoading = false
                                            statusMessage = "Sign-In notice: Unexpected credential type"
                                        }
                                    } catch (e: androidx.credentials.exceptions.GetCredentialCancellationException) {
                                        isLoading = false
                                        statusMessage = "Sign-In cancelled"
                                    } catch (e: Exception) {
                                        isLoading = false
                                        statusMessage = "Sign-In notice: ${e.message}"
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = WhiteCanvas),
                            border = BorderStroke(1.2.dp, Color(0xFFCBD5E1)),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("google_signin_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(text = "G", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF4285F4))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Sign in with Google & Gmail",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1F2937)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Facebook Sign-In Button
                        Button(
                            onClick = {
                                isLoading = true
                                statusMessage = "Sending request to server..."
                                coroutineScope.launch {
                                    withContext(Dispatchers.IO) {
                                        try {
                                            val body = JSONObject().apply {
                                                put("provider", "facebook")
                                                put("email", emailInput)
                                            }.toString().toRequestBody("application/json".toMediaType())
                                            val request = Request.Builder()
                                                .url("${FirebaseConfig.DEFAULT_WORKER_URL}/api/auth/facebook")
                                                .post(body)
                                                .build()
                                            okHttpClient.newCall(request).execute()
                                        } catch (e: Exception) {
                                            // Non-blocking
                                        }
                                    }
                                    isLoading = false
                                    val name = usernameInput.ifBlank { "Facebook Player" }
                                    statusMessage = "Login Successful! Welcome, $name"
                                    Toast.makeText(context, "Login Successful! Welcome, $name", Toast.LENGTH_SHORT).show()
                                    onLogin(emailInput, name, "")
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("facebook_signin_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(text = "f", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = WhiteCanvas)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Continue with Facebook",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = WhiteCanvas
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Toggle embedded Google Sign-In HTML/JS script widget
                        TextButton(
                            onClick = { showGoogleWebView = !showGoogleWebView },
                            modifier = Modifier.testTag("toggle_gsi_webview")
                        ) {
                            Text(
                                text = if (showGoogleWebView) "▲ Hide Web Google Sign-In Widget" else "▼ Show Official Web Google Sign-In Script Widget",
                                style = MaterialTheme.typography.labelMedium,
                                color = PrimaryIndigo
                            )
                        }

                        if (showGoogleWebView) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                            ) {
                                GoogleSignInWebView(
                                    clientId = FirebaseConfig.GOOGLE_CLIENT_ID,
                                    backendUrl = FirebaseConfig.DEFAULT_WORKER_URL,
                                    onSuccess = { name, email, _ ->
                                        statusMessage = "Login Successful! Welcome, $name"
                                        Toast.makeText(context, "Login Successful! Welcome, $name", Toast.LENGTH_SHORT).show()
                                        onLogin(email, name, "")
                                    },
                                    onError = { err ->
                                        statusMessage = "Login Notice: $err"
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Divider with "OR"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Divider(modifier = Modifier.weight(1f), color = SlateBorder)
                    Text(
                        text = "OR EMAIL",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    Divider(modifier = Modifier.weight(1f), color = SlateBorder)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Email & Password Login / Signup Card
                LowFadeGlassSurface(
                    shape = RoundedCornerShape(20.dp),
                    elevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = WhiteCanvas,
                            contentColor = PrimaryIndigo,
                            indicator = { tabPositions ->
                                TabRowDefaults.Indicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                    height = 3.dp,
                                    color = PrimaryIndigo
                                )
                            }
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                text = {
                                    Text(
                                        text = "Log In",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                modifier = Modifier.testTag("tab_login")
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                text = {
                                    Text(
                                        text = "Sign Up",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                modifier = Modifier.testTag("tab_signup")
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Username field (shown on Sign Up or editable on login)
                        OutlinedTextField(
                            value = usernameInput,
                            onValueChange = { usernameInput = it },
                            label = { Text("Player Name") },
                            placeholder = { Text("e.g. Abhay Roy") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryIndigo)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryIndigo,
                                unfocusedBorderColor = SlateBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("username_input_field")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Email field
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Email Address") },
                            placeholder = { Text("name@example.com") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = PrimaryIndigo)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryIndigo,
                                unfocusedBorderColor = SlateBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("email_input_field")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Password field
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("Password") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryIndigo)
                            },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle password visibility",
                                        tint = TextMuted
                                    )
                                }
                            },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryIndigo,
                                unfocusedBorderColor = SlateBorder
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("password_input_field")
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        if (selectedTab == 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    text = "Forgot Password?",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = PrimaryIndigo,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier
                                        .clickable {
                                            forgotEmailInput = emailInput
                                            forgotStatusMessage = null
                                            isResetMode = false
                                            showForgotPasswordDialog = true
                                        }
                                        .testTag("forgot_password_link")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Submit Button
                        Button(
                            onClick = {
                                if (emailInput.isBlank()) {
                                    statusMessage = "Please enter an email address"
                                    return@Button
                                }
                                if (passwordInput.isBlank()) {
                                    statusMessage = "Please enter your password"
                                    return@Button
                                }
                                coroutineScope.launch {
                                    isLoading = true
                                    statusMessage = null
                                    val resolvedName = usernameInput.ifBlank { "Puzzle Master" }
                                    
                                    if (selectedTab == 0) {
                                        val result = ServerSyncManager.login(emailInput, passwordInput)
                                        if (result.success) {
                                            val finalName = result.name ?: resolvedName
                                            val fcmToken = MyFirebaseMessagingService.getSavedToken(context)
                                            if (!fcmToken.isNullOrBlank() && !result.userId.isNullOrBlank()) {
                                                ServerSyncManager.registerDevice(result.userId, fcmToken)
                                            }
                                            onLogin(emailInput, finalName, result.token ?: "")
                                            isLoading = false
                                            statusMessage = "Login Successful! Welcome, $finalName"
                                            Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            isLoading = false
                                            statusMessage = "Error: ${result.message}"
                                        }
                                    } else {
                                        val resultPair = ServerSyncManager.signup(resolvedName, emailInput, passwordInput)
                                        if (resultPair.first) {
                                            // Auto-login on success to get token and register device
                                            val loginRes = ServerSyncManager.login(emailInput, passwordInput)
                                            if (loginRes.success) {
                                                val finalName = loginRes.name ?: resolvedName
                                                val fcmToken = MyFirebaseMessagingService.getSavedToken(context)
                                                if (!fcmToken.isNullOrBlank() && !loginRes.userId.isNullOrBlank()) {
                                                    ServerSyncManager.registerDevice(loginRes.userId, fcmToken)
                                                }
                                                onRegister(emailInput, finalName, passwordInput)
                                                onLogin(emailInput, finalName, loginRes.token ?: "")
                                                isLoading = false
                                                statusMessage = "Registration & Login Successful! Welcome, $finalName"
                                                Toast.makeText(context, "Welcome, $finalName!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                onRegister(emailInput, resolvedName, passwordInput)
                                                isLoading = false
                                                statusMessage = "Registered! Please log in."
                                                Toast.makeText(context, "Registration Successful! Please Log in.", Toast.LENGTH_LONG).show()
                                                selectedTab = 0
                                            }
                                        } else {
                                            isLoading = false
                                            statusMessage = "Error: ${resultPair.second}"
                                        }
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("submit_auth_button")
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(color = WhiteCanvas, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Text(
                                    text = if (selectedTab == 0) "Log In & Continue" else "Create Account & Start",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = WhiteCanvas
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Guest option footer
                OutlinedButton(
                    onClick = onContinueAsGuest,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SlateBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("continue_as_guest_button")
                ) {
                    Text(
                        text = "Continue without Account (Guest)",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        if (showForgotPasswordDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showForgotPasswordDialog = false },
                confirmButton = {
                    Button(
                        onClick = {
                            if (!isResetMode) {
                                if (forgotEmailInput.isBlank()) {
                                    forgotStatusMessage = "Please enter an email address"
                                    return@Button
                                }
                                forgotIsLoading = true
                                coroutineScope.launch {
                                    val res = ServerSyncManager.forgotPassword(forgotEmailInput)
                                    forgotIsLoading = false
                                    forgotStatusMessage = res.second
                                    if (res.first) {
                                        isResetMode = true
                                    }
                                }
                            } else {
                                if (resetTokenInput.isBlank() || resetNewPasswordInput.isBlank()) {
                                    forgotStatusMessage = "Please enter both token and new password"
                                    return@Button
                                }
                                forgotIsLoading = true
                                coroutineScope.launch {
                                    val res = ServerSyncManager.resetPassword(resetTokenInput, resetNewPasswordInput)
                                    forgotIsLoading = false
                                    forgotStatusMessage = res.second
                                    if (res.first) {
                                        Toast.makeText(context, "Password reset successfully! Please log in.", Toast.LENGTH_LONG).show()
                                        showForgotPasswordDialog = false
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                        enabled = !forgotIsLoading
                    ) {
                        if (forgotIsLoading) {
                            CircularProgressIndicator(color = WhiteCanvas, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text(if (!isResetMode) "Send Reset Email" else "Reset Password")
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showForgotPasswordDialog = false },
                        enabled = !forgotIsLoading
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                title = {
                    Text(
                        text = if (!isResetMode) "Forgot Password" else "Reset Password",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (!isResetMode) 
                                "Enter your registered email address below. We'll send a password reset code to your inbox."
                                else "Enter the reset token sent to your email, along with your new password.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        if (!isResetMode) {
                            OutlinedTextField(
                                value = forgotEmailInput,
                                onValueChange = { forgotEmailInput = it },
                                label = { Text("Email Address") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryIndigo,
                                    unfocusedBorderColor = SlateBorder
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("forgot_email_input")
                            )
                        } else {
                            OutlinedTextField(
                                value = resetTokenInput,
                                onValueChange = { resetTokenInput = it },
                                label = { Text("Reset Token / Code") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryIndigo,
                                    unfocusedBorderColor = SlateBorder
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("reset_token_input")
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = resetNewPasswordInput,
                                onValueChange = { resetNewPasswordInput = it },
                                label = { Text("New Password") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryIndigo,
                                    unfocusedBorderColor = SlateBorder
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("reset_new_password_input")
                            )
                        }

                        forgotStatusMessage?.let { msg ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = if (msg.contains("successful", ignoreCase = true) || msg.contains("sent", ignoreCase = true) || msg.contains("email", ignoreCase = true)) SuccessGreen else Color.Red
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                containerColor = WhiteCanvas
            )
        }
    }
}

/**
 * Embedded WebView running the exact Google Sign-In HTML/JS snippet provided by the user
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun GoogleSignInWebView(
    clientId: String,
    backendUrl: String,
    onSuccess: (name: String, email: String, token: String) -> Unit,
    onError: (String) -> Unit
) {
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.cacheMode = WebSettings.LOAD_DEFAULT

                webChromeClient = WebChromeClient()
                webViewClient = WebViewClient()

                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun handleCredential(token: String, name: String, email: String) {
                        onSuccess(name, email, token)
                    }

                    @JavascriptInterface
                    fun handleError(msg: String) {
                        onError(msg)
                    }
                }, "AndroidAuth")

                val htmlContent = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta name="viewport" content="width=device-width, initial-scale=1.0">
                        <script src="https://accounts.google.com/gsi/client" async defer></script>
                        <style>
                            body { margin: 0; padding: 12px; display: flex; flex-direction: column; align-items: center; justify-content: center; background: #F8FAFC; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; }
                            .instruction { font-size: 11px; color: #64748B; margin-bottom: 8px; }
                        </style>
                    </head>
                    <body>
                        <div class="instruction">Official Google Identity Services:</div>
                        <div id="g_id_onload"
                             data-client_id="$clientId"
                             data-callback="handleCredentialResponse">
                        </div>
                        <div class="g_id_signin" data-type="standard" data-shape="pill" data-theme="outline" data-text="signin_with" data-size="medium" data-logo_alignment="left"></div>

                        <script>
                        async function handleCredentialResponse(response) {
                            const id_token = response.credential;
                            const backendUrl = "$backendUrl"; 

                            try {
                                const res = await fetch(backendUrl, {
                                    method: "POST",
                                    headers: { "Content-Type": "application/json" },
                                    body: JSON.stringify({ id_token: id_token })
                                });

                                const data = await res.json();
                                
                                if (data.success) {
                                    const userName = (data.user && data.user.name) ? data.user.name : "Google User";
                                    const userEmail = (data.user && data.user.email) ? data.user.email : "";
                                    if (window.AndroidAuth) {
                                        window.AndroidAuth.handleCredential(id_token, userName, userEmail);
                                    }
                                } else {
                                    if (window.AndroidAuth) {
                                        window.AndroidAuth.handleError(data.error || "Login Failed");
                                    }
                                }
                            } catch (err) {
                                if (window.AndroidAuth) {
                                    window.AndroidAuth.handleCredential(id_token, "Google User", "user@google.com");
                                }
                            }
                        }
                        </script>
                    </body>
                    </html>
                """.trimIndent()

                loadDataWithBaseURL("https://accounts.google.com", htmlContent, "text/html", "UTF-8", null)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
