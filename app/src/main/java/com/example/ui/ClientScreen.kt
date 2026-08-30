package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.client.SimpleClient
import com.example.models.Message
import com.example.models.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Client connection states
    var serverIp by remember { mutableStateOf("127.0.0.1") }
    var userName by remember { mutableStateOf("") }
    var isRegistered by remember { mutableStateOf(false) }
    var isConnecting by remember { mutableStateOf(false) }

    // Live state updated by polling
    var registeredUsers by remember { mutableStateOf<List<User>>(emptyList()) }
    var allMessages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var isServerOnline by remember { mutableStateOf(false) }

    // Navigation sub-state within client
    var activeChatPartnerName by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // Network polling loop for live messages and online status
    LaunchedEffect(isRegistered, serverIp, userName) {
        if (isRegistered) {
            while (true) {
                try {
                    // Check if server is reachable
                    val pingOk = SimpleClient.ping(serverIp)
                    isServerOnline = pingOk

                    if (pingOk) {
                        // Poll latest registered users
                        val latestUsers = SimpleClient.getUsers(serverIp)
                        registeredUsers = latestUsers

                        // Poll latest messages for current user
                        val latestMessages = SimpleClient.getMessages(serverIp, userName)
                        allMessages = latestMessages
                    }
                } catch (e: Exception) {
                    isServerOnline = false
                }
                delay(1500) // Poll every 1.5s for near-instant messaging
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isRegistered) "پیام‌رسان الف تاپ" else "اتصال به شبکه الف تاپ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1D1B20)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (activeChatPartnerName != null) {
                                activeChatPartnerName = null // Return to main list
                            } else {
                                onBack()
                            }
                        },
                        modifier = Modifier.testTag("client_back_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF1D1B20))
                    }
                },
                actions = {
                    if (isRegistered) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(if (isServerOnline) Color(0xFF10B981) else Color(0xFFEF4444), CircleShape)
                            )
                            Text(
                                if (isServerOnline) "برخط" else "قطع اتصال",
                                fontSize = 11.sp,
                                color = if (isServerOnline) Color(0xFF059669) else Color(0xFFDC2626),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFAF9FD)
                )
            )
        },
        containerColor = Color(0xFFFAF9FD) // Light bento theme background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (!isRegistered) {
                // Connection/Registration screen styled with Bento Blocks
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Bento Hero Card (Lavender)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFD0BCFF), RoundedCornerShape(28.dp))
                            .padding(24.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color.White.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Send,
                                    contentDescription = "Logo",
                                    tint = Color(0xFF21005D),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    "به الف تاپ خوش آمدید",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF21005D)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "سامانه پیام‌رسانی غیرمتمرکز محلی روی بستر وای‌فای و سرور پستچی.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF21005D).copy(alpha = 0.8f),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    // Bento Form Card (White with gray border)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(24.dp))
                            .border(1.dp, Color(0xFFCAC4D0), RoundedCornerShape(24.dp))
                            .padding(20.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                "اطلاعات ورود خود را مشخص کنید",
                                color = Color(0xFF1D1B20),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // Name Input
                            OutlinedTextField(
                                value = userName,
                                onValueChange = { userName = it },
                                label = { Text("نام کاربری شما") },
                                placeholder = { Text("مثلا: علیرضا، سارا") },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("client_username_input"),
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "User", tint = Color(0xFF49454F)) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF6750A4),
                                    unfocusedBorderColor = Color(0xFFCAC4D0)
                                )
                            )

                            // Server IP Input
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = serverIp,
                                    onValueChange = { serverIp = SimpleClient.cleanHost(it) },
                                    label = { Text("آدرس آی‌پی سرور (پستچی)") },
                                    placeholder = { Text("مثلا: 127.0.0.1 یا 192.168.1.100") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("client_server_ip_input"),
                                    leadingIcon = { Icon(Icons.Default.Computer, contentDescription = "IP", tint = Color(0xFF49454F)) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF6750A4),
                                        unfocusedBorderColor = Color(0xFFCAC4D0)
                                    )
                                )

                                // Preset quick-fill chips
                                val deviceIp = remember { com.example.server.SimpleServer.getIpAddress() }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    SuggestionChip(
                                        onClick = { serverIp = "127.0.0.1" },
                                        label = { Text("127.0.0.1 (محلی)", fontSize = 11.sp) },
                                        shape = RoundedCornerShape(100.dp)
                                    )
                                    if (deviceIp != "127.0.0.1" && deviceIp.isNotBlank()) {
                                        SuggestionChip(
                                            onClick = { serverIp = deviceIp },
                                            label = { Text("وای‌فای ($deviceIp)", fontSize = 11.sp) },
                                            shape = RoundedCornerShape(100.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Connect Action Button (Bento style active dark block)
                    Button(
                        onClick = {
                            val cleanName = userName.trim()
                            val cleanIp = SimpleClient.cleanHost(serverIp)

                            if (cleanName.isEmpty()) {
                                Toast.makeText(context, "لطفا ابتدا نام کاربری خود را وارد کنید", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (cleanIp.isEmpty()) {
                                Toast.makeText(context, "لطفا آی‌پی سرور را وارد کنید", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            serverIp = cleanIp
                            isConnecting = true
                            coroutineScope.launch {
                                val pingOk = SimpleClient.ping(cleanIp)
                                if (pingOk) {
                                    val regOk = SimpleClient.registerUser(cleanIp, cleanName)
                                    if (regOk) {
                                        isRegistered = true
                                        Toast.makeText(context, "ثبت‌نام و اتصال با موفقیت انجام شد!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "ثبت نام ناموفق بود.", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        "عدم اتصال به سرور! لطفا ابتدا سرور را از بخش «مدیریت سرور» روشن کنید یا آی‌پی صحیح را وارد نمایید.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                isConnecting = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("client_connect_button"),
                        shape = RoundedCornerShape(100.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1C1B1F),
                            contentColor = Color.White
                        ),
                        enabled = !isConnecting
                    ) {
                        if (isConnecting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("ثبت‌نام و ورود به شبکه", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Registered App Portal
                if (activeChatPartnerName != null) {
                    // Chat detail window
                    ChatDetailView(
                        serverIp = serverIp,
                        currentUser = userName,
                        chatPartner = activeChatPartnerName!!,
                        messages = allMessages.filter {
                            (it.sender.lowercase() == userName.lowercase() && it.receiver.lowercase() == activeChatPartnerName!!.lowercase()) ||
                            (it.sender.lowercase() == activeChatPartnerName!!.lowercase() && it.receiver.lowercase() == userName.lowercase())
                        }
                    )
                } else {
                    // Start portal screen (Contacts/Search/Recent)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Profile Banner Bento Card (Light Purple)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE8DEF8), RoundedCornerShape(24.dp))
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(Color(0xFFD0BCFF), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        userName.take(1).uppercase(),
                                        color = Color(0xFF21005D),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                                Column {
                                    Text(
                                        "کاربر: $userName",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF1D1B20)
                                    )
                                    Text(
                                        "آدرس پستچی: $serverIp",
                                        fontSize = 11.sp,
                                        color = Color(0xFF49454F),
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        // Search Bar (White with light border)
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("جستجوی نام مخاطب...") },
                            singleLine = true,
                            shape = RoundedCornerShape(100.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("client_search_input"),
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF49454F)) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color(0xFF49454F))
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = Color(0xFFD0BCFF),
                                unfocusedBorderColor = Color(0xFFCAC4D0)
                            )
                        )

                        // Users List heading
                        Text(
                            if (searchQuery.isEmpty()) "گفتگوهای فعال" else "نتایج جستجو در شبکه",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF49454F),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )

                        // Filter list depending on Search Query
                        val filteredUsers = if (searchQuery.isEmpty()) {
                            val recentUserNames = allMessages.map {
                                if (it.sender.lowercase() == userName.lowercase()) it.receiver else it.sender
                            }.distinct().filter { it.lowercase() != userName.lowercase() }
                            
                            recentUserNames.map { name ->
                                registeredUsers.find { it.name.lowercase() == name.lowercase() } ?: User(name)
                            }
                        } else {
                            registeredUsers.filter {
                                it.name.lowercase().contains(searchQuery.lowercase()) &&
                                it.name.lowercase() != userName.lowercase()
                            }
                        }

                        // Users List Display (Bento Grid Style Lists)
                        if (filteredUsers.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .background(Color.White, RoundedCornerShape(24.dp))
                                    .border(1.dp, Color(0xFFE7E0EC), RoundedCornerShape(24.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        if (searchQuery.isEmpty()) Icons.Default.Message else Icons.Default.Search,
                                        contentDescription = "Empty",
                                        tint = Color(0xFFCAC4D0),
                                        modifier = Modifier.size(44.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        if (searchQuery.isEmpty()) "هنوز گفتگویی ندارید.\nاز کادر بالا نام مخاطب را جستجو کنید." 
                                        else "مخاطبی با این نام پیدا نشد.",
                                        color = Color(0xFF49454F),
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 24.dp)
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(filteredUsers, key = { it.name }) { user ->
                                    UserListItemRow(
                                        user = user,
                                        lastMessage = allMessages.lastOrNull {
                                            (it.sender.lowercase() == userName.lowercase() && it.receiver.lowercase() == user.name.lowercase()) ||
                                            (it.sender.lowercase() == user.name.lowercase() && it.receiver.lowercase() == userName.lowercase())
                                        },
                                        onClick = {
                                            activeChatPartnerName = user.name
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserListItemRow(user: User, lastMessage: Message?, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(18.dp))
            .border(1.dp, Color(0xFFE7E0EC), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("user_item_${user.name}")
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Color(0xFFFFD8E4), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    user.name.take(1).uppercase(),
                    color = Color(0xFF31111D),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    user.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF1D1B20)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    lastMessage?.text ?: "شروع گفتگو با کاربر...",
                    maxLines = 1,
                    fontSize = 11.sp,
                    color = if (lastMessage != null) Color(0xFF49454F) else Color(0xFF6750A4),
                    fontWeight = if (lastMessage != null) FontWeight.Normal else FontWeight.Medium
                )
            }

            if (lastMessage != null) {
                val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
                Text(
                    timeFormat.format(Date(lastMessage.timestamp)),
                    fontSize = 9.sp,
                    color = Color(0xFF79747E),
                    fontFamily = FontFamily.Monospace
                )
            } else {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Chat",
                    tint = Color(0xFF79747E),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun ChatDetailView(serverIp: String, currentUser: String, chatPartner: String, messages: List<Message>) {
    val coroutineScope = rememberCoroutineScope()
    var typedMessage by remember { mutableStateOf(TextFieldValue("")) }
    val listState = rememberLazyListState()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Chat Header Banner Bento style (White border bottom)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .border(1.dp, Color(0xFFE7E0EC))
                .padding(vertical = 10.dp, horizontal = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(Color(0xFFE8DEF8), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        chatPartner.take(1).uppercase(),
                        color = Color(0xFF21005D),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Column {
                    Text(
                        chatPartner,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF1D1B20)
                    )
                    Text("در حال گفتگو در شبکه...", fontSize = 10.sp, color = Color(0xFF49454F))
                }
            }
        }

        // Messages Box
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val isMe = msg.sender.lowercase() == currentUser.lowercase()
                ChatBubble(text = msg.text, isMe = isMe, timestamp = msg.timestamp)
            }
        }

        // Message Input Row with Bento Style rounded input bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .border(1.dp, Color(0xFFE7E0EC))
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Send button
                IconButton(
                    onClick = {
                        val txt = typedMessage.text.trim()
                        if (txt.isEmpty()) return@IconButton
                        
                        coroutineScope.launch {
                            val success = SimpleClient.sendMessage(serverIp, currentUser, chatPartner, txt)
                            if (success) {
                                typedMessage = TextFieldValue("")
                            }
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color(0xFF1C1B1F),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .size(46.dp)
                        .testTag("client_send_button")
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Send",
                        modifier = Modifier.size(18.dp)
                    )
                }

                OutlinedTextField(
                    value = typedMessage,
                    onValueChange = { typedMessage = it },
                    placeholder = { Text("پیام خود را بنویسید...") },
                    singleLine = false,
                    maxLines = 4,
                    shape = RoundedCornerShape(100.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("client_message_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFFAF9FD),
                        unfocusedContainerColor = Color(0xFFFAF9FD),
                        focusedBorderColor = Color(0xFFD0BCFF),
                        unfocusedBorderColor = Color(0xFFE7E0EC)
                    )
                )
            }
        }
    }
}

@Composable
fun ChatBubble(text: String, isMe: Boolean, timestamp: Long) {
    val bubbleShape = if (isMe) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
    }

    val bubbleBg = if (isMe) Color(0xFFD0BCFF) else Color(0xFFF3EDF7)
    val textColor = if (isMe) Color(0xFF21005D) else Color(0xFF1D1B20)
    val align = if (isMe) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = align
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 260.dp)
                .background(bubbleBg, bubbleShape)
                .border(1.dp, if (isMe) Color(0xFFD0BCFF) else Color(0xFFE7E0EC), bubbleShape)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = text,
                    color = textColor,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
                
                val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
                Text(
                    timeFormat.format(Date(timestamp)),
                    fontSize = 8.sp,
                    color = if (isMe) Color(0xFF21005D).copy(alpha = 0.6f) else Color(0xFF79747E),
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
