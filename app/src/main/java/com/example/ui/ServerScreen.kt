package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.LogEntry
import com.example.models.LogType
import com.example.models.Message
import com.example.models.User
import com.example.server.SimpleServer
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val isRunning by SimpleServer.isRunning.collectAsState()
    val users by SimpleServer.registeredUsers.collectAsState()
    val messages by SimpleServer.messages.collectAsState()
    val logs by SimpleServer.logs.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Logs, 1: Users, 2: Messages

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "مدیریت سرور الف تاپ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1D1B20)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("server_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF1D1B20))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFAF9FD)
                )
            )
        },
        containerColor = Color(0xFFFAF9FD) // Light bento theme background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Row 1: Large Bento Card (Server control + IP)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFD0BCFF), RoundedCornerShape(28.dp))
                    .padding(20.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(
                                        if (isRunning) Color(0xFF10B981) else Color(0xFFEF4444),
                                        CircleShape
                                    )
                            )
                            Text(
                                if (isRunning) "روشن • فعال" else "خاموش • غیرفعال",
                                color = Color(0xFF21005D),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        // Copy IP Button
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Server IP", SimpleServer.getIpAddress())
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "آی‌پی سرور کپی شد!", Toast.LENGTH_SHORT).show()
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color.White.copy(alpha = 0.3f),
                                contentColor = Color(0xFF21005D)
                            ),
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("copy_ip_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Copy IP", modifier = Modifier.size(16.dp))
                        }
                    }

                    Column {
                        Text(
                            "آدرس اتصال سرور (مخصوص کلاینت‌ها)",
                            color = Color(0xFF21005D).copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "${SimpleServer.getIpAddress()}:8080",
                            color = Color(0xFF21005D),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Power Control Button (Stretched bento-style)
                    Button(
                        onClick = {
                            if (isRunning) {
                                SimpleServer.stopServer()
                            } else {
                                SimpleServer.startServer()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1C1B1F),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(100.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("server_toggle_button")
                    ) {
                        Icon(
                            if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = if (isRunning) "Stop" else "Start",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (isRunning) "توقف فعالیت سرور" else "راه‌اندازی سرور شبکه",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Row 2: Split Bento cards (Users count & Messages count)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Users found bento card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(Color(0xFFE8DEF8), RoundedCornerShape(28.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Group, contentDescription = "Users", tint = Color(0xFF1D1B20), modifier = Modifier.size(16.dp))
                        }
                        Column {
                            Text(users.size.toString(), color = Color(0xFF1D1B20), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Text("کاربر ثبت‌شده", color = Color(0xFF49454F), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                // Messages processed bento card
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(Color(0xFFFFD8E4), RoundedCornerShape(28.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Messages", tint = Color(0xFF31111D), modifier = Modifier.size(14.dp))
                        }
                        Column {
                            Text(messages.size.toString(), color = Color(0xFF31111D), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Text("پیام جابجا شده", color = Color(0xFF633B48), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            // Tabs navigation (Modern elegant light tabs)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF3EDF7), RoundedCornerShape(100.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TabItem(title = "گزارشات شبکه", isActive = activeTab == 0, modifier = Modifier.weight(1f)) { activeTab = 0 }
                TabItem(title = "کاربران (${users.size})", isActive = activeTab == 1, modifier = Modifier.weight(1f)) { activeTab = 1 }
                TabItem(title = "پیام‌ها", isActive = activeTab == 2, modifier = Modifier.weight(1f)) { activeTab = 2 }
            }

            // Tab Content Frame
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(24.dp))
                    .border(1.dp, Color(0xFFE7E0EC), RoundedCornerShape(24.dp))
                    .padding(14.dp)
            ) {
                when (activeTab) {
                    0 -> LogsTab(logs = logs)
                    1 -> UsersTab(users = users)
                    2 -> MessagesTab(messages = messages)
                }
            }

            // Clear Database button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = {
                        SimpleServer.clearData()
                        Toast.makeText(context, "اطلاعات با موفقیت پاک شد", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFB3261E)),
                    modifier = Modifier.testTag("server_clear_data_button")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Clear Server", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("حذف کلیه اطلاعات سرور", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TabItem(title: String, isActive: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(if (isActive) Color(0xFFE8DEF8) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = if (isActive) Color(0xFF21005D) else Color(0xFF49454F),
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            fontSize = 12.sp
        )
    }
}

@Composable
fun LogsTab(logs: List<LogEntry>) {
    if (logs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("گزارشی موجود نیست", color = Color(0xFF49454F), fontSize = 13.sp)
        }
    } else {
        val listState = androidx.compose.foundation.lazy.rememberLazyListState()
        LaunchedEffect(logs.size) {
            if (logs.isNotEmpty()) {
                listState.animateScrollToItem(logs.size - 1)
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            items(logs, key = { it.id }) { log ->
                LogItemRow(log)
            }
        }
    }
}

@Composable
fun LogItemRow(log: LogEntry) {
    val typeColor = when (log.type) {
        LogType.INFO -> Color(0xFF0284C7)
        LogType.SUCCESS -> Color(0xFF059669)
        LogType.WARNING -> Color(0xFFD97706)
        LogType.ERROR -> Color(0xFFDC2626)
        LogType.MESSAGE -> Color(0xFF7C3AED)
    }

    val typeLabel = when (log.type) {
        LogType.INFO -> "اطلاعات"
        LogType.SUCCESS -> "موفق"
        LogType.WARNING -> "هشدار"
        LogType.ERROR -> "خطا"
        LogType.MESSAGE -> "پیام"
    }

    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val timeString = timeFormat.format(Date(log.timestamp))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFAF9FD), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFE7E0EC), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(typeColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(typeLabel, color = typeColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
            Text(timeString, color = Color(0xFF79747E), fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = log.message,
            color = Color(0xFF1D1B20),
            fontSize = 12.sp,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun UsersTab(users: List<User>) {
    if (users.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Person, contentDescription = "Empty", tint = Color(0xFF79747E), modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("هیچ کاربری در سرور ثبت‌نام نکرده است.", color = Color(0xFF49454F), fontSize = 12.sp)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(users) { user ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFAF9FD), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFE7E0EC), RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFEADDFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.name.take(1).uppercase(),
                            color = Color(0xFF21005D),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(user.name, color = Color(0xFF1D1B20), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "فعال در بستر شبکه محلی",
                            color = Color(0xFF49454F),
                            fontSize = 10.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color(0xFF10B981), CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
fun MessagesTab(messages: List<Message>) {
    if (messages.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Send, contentDescription = "Empty", tint = Color(0xFF79747E), modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("هنوز پیامی در سرور رد و بدل نشده است.", color = Color(0xFF49454F), fontSize = 12.sp)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(messages) { msg ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFAF9FD), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFE7E0EC), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    msg.sender,
                                    color = Color(0xFF0284C7),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                               )
                                Text("➔", color = Color(0xFF79747E), fontSize = 9.sp)
                                Text(
                                    msg.receiver,
                                    color = Color(0xFF7C3AED),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                            
                            val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
                            Text(
                                timeFormat.format(Date(msg.timestamp)),
                                color = Color(0xFF79747E),
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            msg.text,
                            color = Color(0xFF1D1B20),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
