package com.guxiaopan.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.guxiaopan.MainActivity
import com.guxiaopan.common.Constants
import com.guxiaopan.ui.theme.GuXiaoPanTheme
import kotlinx.coroutines.flow.collectLatest

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GuXiaoPanTheme {
                LoginScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen() {
    val context = LocalContext.current
    val viewModel: MainViewModel = viewModel()
    val user by viewModel.user.collectAsStateWithLifecycle()
    val loginSuccess by viewModel.loginSuccess.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    // 自动登录检查
    LaunchedEffect(Unit) {
        viewModel.tryAutoLogin()
    }

    // 登录成功跳转
    LaunchedEffect(loginSuccess) {
        if (loginSuccess) {
            context.startActivity(Intent(context, MainActivity::class.java))
            (context as? ComponentActivity)?.finish()
        }
    }

    // Toast提示
    LaunchedEffect(Unit) {
        viewModel.toast.collectLatest { msg ->
            snackbar.showSnackbar(msg)
        }
    }

    var phone by remember { mutableStateOf("") }
    var authCode by remember { mutableStateOf("") }
    var showAuthInput by remember { mutableStateOf(false) }

    // 检查授权是否过期
    val isExpired = user?.let {
        it.limitDate > 0 && System.currentTimeMillis() > it.limitDate
    } ?: false

    val daysLeft = user?.let {
        if (it.limitDate > 0) {
            val remaining = ((it.limitDate - System.currentTimeMillis()) / (24 * 60 * 60 * 1000L)).toInt()
            maxOf(0, remaining)
        } else it.limitDays
    } ?: 0

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = { Text("股小判——散户炒股精灵") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                )
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "散户炒股精灵",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it.filter { ch -> ch.isDigit() || ch == '+' } },
                label = { Text("手机号") },
                placeholder = { Text("请输入手机号") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 显示剩余天数
            if (user != null && !isExpired) {
                Text(
                    text = "剩余体验天数：$daysLeft 天",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 授权到期提示
            if (isExpired) {
                Text(
                    text = "授权已到期！",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (!showAuthInput) {
                    TextButton(onClick = { showAuthInput = true }) {
                        Text("输入授权码")
                    }
                }
            }

            if (showAuthInput || isExpired) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = authCode,
                    onValueChange = { authCode = it },
                    label = { Text("授权码") },
                    placeholder = { Text("请输入授权码") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.submitAuthCode(authCode) },
                    enabled = authCode.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("激活授权")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { if (phone.isNotBlank()) viewModel.login(phone) },
                enabled = phone.length >= 11,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("登 录")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "联系微信/电话：${Constants.CONTACT_PHONE}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
