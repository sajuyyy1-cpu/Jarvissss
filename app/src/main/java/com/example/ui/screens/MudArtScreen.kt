package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.actions.AndroidActionManager
import com.example.data.model.MudArtCustomerEntity
import com.example.data.model.MudArtNoteEntity
import com.example.data.model.MudArtOrderEntity
import com.example.ui.theme.*

@Composable
fun MudArtScreen(
    orders: List<MudArtOrderEntity>,
    customers: List<MudArtCustomerEntity>,
    notes: List<MudArtNoteEntity>,
    onAddOrder: (customer: String, title: String, dim: String, amt: Double, status: String, date: String, notes: String) -> Unit,
    onUpdateOrderStatus: (id: Long, status: String) -> Unit,
    onDeleteOrder: (MudArtOrderEntity) -> Unit,
    onAddCustomer: (name: String, phone: String, email: String, city: String, notes: String) -> Unit,
    onDeleteCustomer: (MudArtCustomerEntity) -> Unit,
    onAddNote: (title: String, content: String, cat: String) -> Unit,
    onDeleteNote: (MudArtNoteEntity) -> Unit
) {
    val context = LocalContext.current
    var selectedSection by remember { mutableStateOf(0) } // 0: ORDERS, 1: CUSTOMERS, 2: NOTES & TECHNIQUES
    var showAddDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val sectionTitles = listOf("ORDERS", "CUSTOMERS", "TECHNIQUES")

    Scaffold(
        containerColor = JarvisBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = JarvisCyan,
                contentColor = JarvisBackground,
                shape = CircleShape,
                modifier = Modifier.testTag("mud_art_add_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Record")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("mud_art_screen")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "MAJIKHAN MUD ART",
                        color = JarvisCyan,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Traditional Lippan Art Business Archive // On-Device",
                        color = JarvisTextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = JarvisSurfaceElevated,
                    border = BorderStroke(0.8.dp, JarvisNeonAmber)
                ) {
                    Text(
                        text = "SECURE LOCAL",
                        color = JarvisNeonAmber,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Sub-tabs
            TabRow(
                selectedTabIndex = selectedSection,
                containerColor = JarvisSurface,
                contentColor = JarvisCyan,
                divider = { Divider(color = JarvisBorder, thickness = 0.8.dp) }
            ) {
                sectionTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedSection == index,
                        onClick = { selectedSection = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (selectedSection == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text("Search business archive...", color = JarvisTextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = JarvisCyan, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = JarvisTextPrimary,
                    unfocusedTextColor = JarvisTextPrimary,
                    focusedContainerColor = JarvisSurface,
                    unfocusedContainerColor = JarvisSurface,
                    focusedBorderColor = JarvisCyan,
                    unfocusedBorderColor = JarvisBorder
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Section Content
            when (selectedSection) {
                0 -> OrdersSection(
                    orders = orders.filter {
                        searchQuery.isBlank() || it.customerName.contains(searchQuery, ignoreCase = true) || it.artPieceTitle.contains(searchQuery, ignoreCase = true)
                    },
                    onUpdateStatus = onUpdateOrderStatus,
                    onDelete = onDeleteOrder
                )
                1 -> CustomersSection(
                    customers = customers.filter {
                        searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true) || it.city.contains(searchQuery, ignoreCase = true)
                    },
                    onDelete = onDeleteCustomer,
                    onCallCustomer = { phone -> AndroidActionManager.dialPhoneNumber(context, phone) }
                )
                2 -> NotesSection(
                    notes = notes.filter {
                        searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true) || it.content.contains(searchQuery, ignoreCase = true)
                    },
                    onDelete = onDeleteNote
                )
            }
        }
    }

    // Add Dialogs based on active section
    if (showAddDialog) {
        when (selectedSection) {
            0 -> AddOrderDialog(
                onDismiss = { showAddDialog = false },
                onSave = { cust, title, dim, amt, status, date, notesText ->
                    onAddOrder(cust, title, dim, amt, status, date, notesText)
                    showAddDialog = false
                }
            )
            1 -> AddCustomerDialog(
                onDismiss = { showAddDialog = false },
                onSave = { name, phone, email, city, notesText ->
                    onAddCustomer(name, phone, email, city, notesText)
                    showAddDialog = false
                }
            )
            2 -> AddNoteDialog(
                onDismiss = { showAddDialog = false },
                onSave = { title, content, cat ->
                    onAddNote(title, content, cat)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun OrdersSection(
    orders: List<MudArtOrderEntity>,
    onUpdateStatus: (Long, String) -> Unit,
    onDelete: (MudArtOrderEntity) -> Unit
) {
    if (orders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No orders in archive. Tap '+' to create order.", color = JarvisTextMuted, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
            items(orders, key = { it.id }) { order ->
                OrderCard(order = order, onUpdateStatus = { onUpdateStatus(order.id, it) }, onDelete = { onDelete(order) })
            }
        }
    }
}

@Composable
fun OrderCard(
    order: MudArtOrderEntity,
    onUpdateStatus: (String) -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (order.status) {
        "COMPLETED" -> JarvisNeonGreen
        "DISPATCHED" -> JarvisCyan
        "IN_PROGRESS" -> JarvisNeonAmber
        else -> JarvisLaserRed
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(BorderStroke(0.8.dp, JarvisBorder), RoundedCornerShape(10.dp)),
        color = JarvisSurface
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.artPieceTitle,
                    color = JarvisTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = statusColor.copy(alpha = 0.15f),
                    border = BorderStroke(0.8.dp, statusColor)
                ) {
                    Text(
                        text = order.status,
                        color = statusColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Client: ${order.customerName}", color = JarvisCyan, fontSize = 12.sp)
            if (order.dimensions.isNotBlank()) {
                Text(text = "Dimensions: ${order.dimensions}", color = JarvisTextSecondary, fontSize = 11.sp)
            }
            Text(text = "Amount: ₹${order.amount.toInt()}", color = JarvisNeonGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)

            if (order.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Notes: ${order.notes}", color = JarvisTextMuted, fontSize = 10.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Advance status buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("PENDING", "IN_PROGRESS", "COMPLETED", "DISPATCHED").forEach { st ->
                        if (st != order.status) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = JarvisSurfaceElevated,
                                border = BorderStroke(0.6.dp, JarvisBorder),
                                modifier = Modifier.clickable { onUpdateStatus(st) }
                            ) {
                                Text(
                                    text = "→ $st",
                                    color = JarvisTextSecondary,
                                    fontSize = 8.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = JarvisLaserRed, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun CustomersSection(
    customers: List<MudArtCustomerEntity>,
    onDelete: (MudArtCustomerEntity) -> Unit,
    onCallCustomer: (String) -> Unit
) {
    if (customers.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No customer records found.", color = JarvisTextMuted, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
            items(customers, key = { it.id }) { customer ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(BorderStroke(0.8.dp, JarvisBorder), RoundedCornerShape(10.dp)),
                    color = JarvisSurface
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = customer.name,
                                color = JarvisTextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Row {
                                if (customer.phone.isNotBlank()) {
                                    IconButton(onClick = { onCallCustomer(customer.phone) }, modifier = Modifier.size(28.dp)) {
                                        Icon(Icons.Default.Phone, contentDescription = "Dial", tint = JarvisCyan, modifier = Modifier.size(16.dp))
                                    }
                                }
                                IconButton(onClick = { onDelete(customer) }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = JarvisLaserRed, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        if (customer.phone.isNotBlank()) Text("Phone: ${customer.phone}", color = JarvisTextSecondary, fontSize = 11.sp)
                        if (customer.city.isNotBlank()) Text("Location: ${customer.city}", color = JarvisTextSecondary, fontSize = 11.sp)
                        if (customer.notes.isNotBlank()) Text("Preferences: ${customer.notes}", color = JarvisTextMuted, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun NotesSection(
    notes: List<MudArtNoteEntity>,
    onDelete: (MudArtNoteEntity) -> Unit
) {
    if (notes.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No art notes found.", color = JarvisTextMuted, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 80.dp)) {
            items(notes, key = { it.id }) { note ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(BorderStroke(0.8.dp, JarvisBorder), RoundedCornerShape(10.dp)),
                    color = JarvisSurface
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = note.title, color = JarvisCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            IconButton(onClick = { onDelete(note) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = JarvisLaserRed, modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = note.content, color = JarvisTextPrimary, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AddOrderDialog(
    onDismiss: () -> Unit,
    onSave: (customer: String, title: String, dim: String, amt: Double, status: String, date: String, notes: String) -> Unit
) {
    var customer by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var dim by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("PENDING") }
    var notesText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("LOG MUD ART ORDER", color = JarvisCyan, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = customer, onValueChange = { customer = it }, label = { Text("Customer Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Artwork Title (e.g. Mandala Lippan)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = dim, onValueChange = { dim = it }, label = { Text("Dimensions (e.g. 24x24 inch)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = amountText, onValueChange = { amountText = it }, label = { Text("Price (₹)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notesText, onValueChange = { notesText = it }, label = { Text("Design / Mirror Notes") }, maxLines = 2, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (customer.isNotBlank() && title.isNotBlank()) {
                        val amt = amountText.toDoubleOrNull() ?: 0.0
                        onSave(customer, title, dim, amt, status, "Today", notesText)
                    }
                },
                enabled = customer.isNotBlank() && title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan)
            ) {
                Text("SAVE ORDER", color = JarvisBackground, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCEL", color = JarvisTextSecondary) } },
        containerColor = JarvisSurfaceElevated
    )
}

@Composable
fun AddCustomerDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, email: String, city: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("NEW CLIENT PROFILE", color = JarvisCyan, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City / State") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notesText, onValueChange = { notesText = it }, label = { Text("Art Preferences") }, maxLines = 2, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onSave(name, phone, email, city, notesText) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan)
            ) {
                Text("RECORD CLIENT", color = JarvisBackground, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCEL", color = JarvisTextSecondary) } },
        containerColor = JarvisSurfaceElevated
    )
}

@Composable
fun AddNoteDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, content: String, cat: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var cat by remember { mutableStateOf("MATERIAL") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ADD ART TECHNIQUE NOTE", color = JarvisCyan, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("Formula / Craft Technique") }, maxLines = 4, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank() && content.isNotBlank()) onSave(title, content, cat) },
                enabled = title.isNotBlank() && content.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan)
            ) {
                Text("SAVE NOTE", color = JarvisBackground, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCEL", color = JarvisTextSecondary) } },
        containerColor = JarvisSurfaceElevated
    )
}
