package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ClientScreen
import com.example.ui.ServerScreen
import com.example.ui.theme.MyApplicationTheme

enum class ScreenState {
    LAUNCHER_HUB,
    SERVER_PANEL,
    CLIENT_MESSENGER
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                var currentScreen by remember { mutableStateOf(ScreenState.LAUNCHER_HUB) }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFFAF9FD)) // Bento Background
                ) {
                    AnimatedContent(
                        targetState = currentScreen,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "ScreenTransition"
                    ) { screen ->
                        when (screen) {
                            ScreenState.LAUNCHER_HUB -> LauncherHubView(
                                onNavigateToClient = { currentScreen = ScreenState.CLIENT_MESSENGER },
                                onNavigateToServer = { currentScreen = ScreenState.SERVER_PANEL }
                            )
                            ScreenState.SERVER_PANEL -> ServerScreen(
                                onBack = { currentScreen = ScreenState.LAUNCHER_HUB }
                            )
                            ScreenState.CLIENT_MESSENGER -> ClientScreen(
                                onBack = { currentScreen = ScreenState.LAUNCHER_HUB }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LauncherHubView(
    onNavigateToClient: () -> Unit,
    onNavigateToServer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "پیام‌رسان الف تاپ",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D1B20)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color(0xFF10B981), RoundedCornerShape(100))
                    )
                    Text(
                        text = "آماده برای اتصال به شبکه",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF059669)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFEADDFF), RoundedCornerShape(100))
                    .clickable { }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color(0xFF21005D),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Bento Grid Body
        // Row 1: Large primary Bento card (Client Messenger)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color(0xFFD0BCFF), RoundedCornerShape(28.dp))
                .clickable(onClick = onNavigateToClient)
                .testTag("launch_client_card")
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Message,
                        contentDescription = "Messenger",
                        tint = Color(0xFF21005D),
                        modifier = Modifier.size(32.dp)
                    )
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "آسان و سریع",
                            color = Color(0xFF21005D),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column {
                    Text(
                        "ورود به پیام‌رسان (کلاینت)",
                        color = Color(0xFF21005D),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "به دوستان خود متصل شوید، اسم خود را ثبت کنید و با هم گفتگو کنید.",
                        color = Color(0xFF21005D).copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Row 2: Split Bento cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left Bento block: Server Control Panel
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color(0xFFE8DEF8), RoundedCornerShape(28.dp))
                    .clickable(onClick = onNavigateToServer)
                    .testTag("launch_server_card")
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Dns,
                            contentDescription = "Server",
                            tint = Color(0xFF1D1B20),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            "پنل سرور",
                            color = Color(0xFF1D1B20),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "فعالسازی بستر لوکال شبکه",
                            color = Color(0xFF49454F),
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            // Right Bento block: Stats/Visual layout block
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color(0xFFFFD8E4), RoundedCornerShape(28.dp))
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.SignalCellularAlt,
                            contentDescription = "Status",
                            tint = Color(0xFF31111D),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            "سرعت انتقال",
                            color = Color(0xFF31111D),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "فوق‌العاده سریع و سبک",
                            color = Color(0xFF633B48),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // Row 3: Full-width bento row (Registry sync)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(24.dp))
                .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(24.dp))
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFFF97316), RoundedCornerShape(100))
                    )
                    Text(
                        "همگام‌سازی شبکه الف تاپ",
                        color = Color(0xFF1D1B20),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    "پورت فعال: 8080",
                    color = Color(0xFF49454F),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

