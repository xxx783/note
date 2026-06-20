package com.yutie.note.ui

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yutie.note.R
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.navigation.NavController
import com.yutie.note.bean.NoteBean
import com.yutie.note.ui.custom.CustomConfirmDialog
import com.yutie.note.ui.custom.CustomEmptyView
import com.yutie.note.ui.custom.CustomSearchBar
import com.yutie.note.ui.custom.CustomTitleBar
import com.yutie.note.ui.viewmodel.HomeViewModel
import com.yutie.note.utils.DateUtils
import com.yutie.note.utils.SupabaseClient
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume

/**
 * Home Page - Note List
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val allNotes by viewModel.allNotes.collectAsState()
    @Suppress("UNUSED_VARIABLE") val scope = rememberCoroutineScope()
    
    var showAnnouncementDialog by remember { mutableStateOf(false) }
    var announcementTitle by remember { mutableStateOf("") }
    var announcementContent by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        checkAndShowAnnouncement(
            context = context,
            onShowAnnouncement = { title, content ->
                announcementTitle = title
                announcementContent = content
                showAnnouncementDialog = true
            }
        )
    }
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<NoteBean?>(null) }
    var showCloudDeleteDialog by remember { mutableStateOf(false) }
    var deleteFromCloud by remember { mutableStateOf(false) }
    
    var isInBatchMode by remember { mutableStateOf(false) }
    val selectedNotes = remember { mutableStateListOf<Long>() }
    
    var showMoreDropdown by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    var isSearching by remember { mutableStateOf(false) }
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            CustomTitleBar(
                title = stringResource(R.string.str_home_title),
                showBackButton = false,
                rightIcon = if (isInBatchMode) Icons.Default.Close else 
                           if (isSearching) Icons.Default.Close else Icons.Default.MoreVert,
                onBackClick = { },
                onRightClick = {
                    if (isInBatchMode) {
                        isInBatchMode = false
                        selectedNotes.clear()
                    } else if (isSearching) {
                        isSearching = false
                        viewModel.setSearchQuery("")
                    } else {
                        showMoreDropdown = true
                    }
                }
            )
            
            if (isInBatchMode) {
                BatchActionBar(
                    selectedCount = selectedNotes.size,
                    totalCount = allNotes.size,
                    onSelectAll = {
                        if (selectedNotes.size < allNotes.size) {
                            selectedNotes.clear()
                            selectedNotes.addAll(allNotes.map { it.id })
                        } else {
                            selectedNotes.clear()
                        }
                    },
                    onDelete = {
                        showDeleteDialog = true
                    },
                    onExit = {
                        isInBatchMode = false
                        selectedNotes.clear()
                    }
                )
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.TopEnd)
            ) {
                DropdownMenu(
                    expanded = showMoreDropdown,
                    onDismissRequest = { showMoreDropdown = false },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.str_search)) },
                        onClick = {
                            showMoreDropdown = false
                            isSearching = true
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.str_batch_manage)) },
                        onClick = {
                            showMoreDropdown = false
                            isInBatchMode = true
                        },
                        leadingIcon = {
                            Icon(Icons.Default.SelectAll, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.str_settings)) },
                        onClick = {
                            showMoreDropdown = false
                            navController.navigate("settings")
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Settings, contentDescription = null)
                        }
                    )
                }
            }
            
            if (isSearching) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    CustomSearchBar(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = stringResource(R.string.str_search_hint),
                        onClear = { viewModel.setSearchQuery("") }
                    )
                }
            }
            
            if (allNotes.isEmpty()) {
                CustomEmptyView(message = stringResource(R.string.str_empty))
            } else {
                var overScrollAmount by remember { mutableStateOf(0f) }
                val scale by animateFloatAsState(
                    targetValue = 1f + (overScrollAmount / 2000f).coerceIn(0f, 0.05f),
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "overscroll_scale"
                )
                
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .graphicsLayer {
                            scaleY = scale
                        },
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = allNotes,
                        key = { it.id }
                    ) { note ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            ) + slideInVertically(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                initialOffsetY = { it / 2 }
                            )
                        ) {
                            NoteItem(
                                note = note,
                                isSelected = selectedNotes.contains(note.id),
                                isInBatchMode = isInBatchMode,
                                onClick = {
                                    if (isInBatchMode) {
                                        if (selectedNotes.contains(note.id)) {
                                            selectedNotes.remove(note.id)
                                        } else {
                                            selectedNotes.add(note.id)
                                        }
                                    } else {
                                        navController.navigate("noteEdit/${note.id}")
                                    }
                                },
                                onLongClick = {
                                    if (!isInBatchMode) {
                                        isInBatchMode = true
                                        selectedNotes.add(note.id)
                                    }
                                },
                                onSelectClick = {
                                    if (selectedNotes.contains(note.id)) {
                                        selectedNotes.remove(note.id)
                                    } else {
                                        selectedNotes.add(note.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
            
            if (showAnnouncementDialog) {
                Dialog(
                    onDismissRequest = { showAnnouncementDialog = false }
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .heightIn(max = 600.dp)
                            .padding(16.dp),
                        shape = RoundedCornerShape(28.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Campaign,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = announcementTitle,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = announcementContent,
                                    style = MaterialTheme.typography.bodyLarge,
                                    lineHeight = 24.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Button(
                                onClick = { showAnnouncementDialog = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(stringResource(R.string.str_known))
                            }
                        }
                    }
                }
            }
            
            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showDeleteDialog = false
                        noteToDelete = null
                    },
                    title = { Text(stringResource(R.string.str_delete_note)) },
                    text = {
                        Column {
                            Text(
                                text = if (selectedNotes.size > 1) {
                                    stringResource(R.string.str_delete_multiple_confirm, selectedNotes.size)
                                } else {
                                    stringResource(R.string.str_delete_single_confirm)
                                }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { 
                                    deleteFromCloud = !deleteFromCloud 
                                }
                            ) {
                                Checkbox(
                                    checked = deleteFromCloud,
                                    onCheckedChange = { deleteFromCloud = it }
                                )
                                Text(stringResource(R.string.str_delete_cloud_confirm))
                            }
                            if (deleteFromCloud) {
                                Text(
                                    text = stringResource(R.string.str_cloud_delete_message),
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showDeleteDialog = false
                                showCloudDeleteDialog = true
                            }
                        ) {
                            Text(stringResource(R.string.str_delete))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showDeleteDialog = false
                                noteToDelete = null
                            }
                        ) {
                            Text(stringResource(R.string.str_cancel))
                        }
                    }
                )
            }
            
            if (showCloudDeleteDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showCloudDeleteDialog = false
                        deleteFromCloud = false
                    },
                    title = { 
                        Text(stringResource(R.string.str_cloud_delete_title)) 
                    },
                    text = {
                        Text(stringResource(R.string.str_cloud_delete_message))
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (isInBatchMode && selectedNotes.isNotEmpty()) {
                                    if (deleteFromCloud) {
                                        viewModel.deleteNotesFromCloud(selectedNotes.toList())
                                    } else {
                                        viewModel.deleteNotes(selectedNotes.toList())
                                    }
                                    selectedNotes.clear()
                                    isInBatchMode = false
                                } else if (noteToDelete != null) {
                                    if (deleteFromCloud) {
                                        viewModel.deleteNoteFromCloud(noteToDelete!!)
                                    } else {
                                        viewModel.deleteNote(noteToDelete!!)
                                    }
                                }
                                showCloudDeleteDialog = false
                                deleteFromCloud = false
                                noteToDelete = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(stringResource(R.string.str_confirm_delete))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showCloudDeleteDialog = false
                                deleteFromCloud = false
                            }
                        ) {
                            Text(stringResource(R.string.str_cancel))
                        }
                    }
                )
            }
        }
        
        var isFabPressed by remember { mutableStateOf(false) }
        val fabScale by animateFloatAsState(
            targetValue = if (isFabPressed) 0.9f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "fab_scale"
        )
        
        FloatingActionButton(
            onClick = {
                isFabPressed = true
                navController.navigate("noteEdit/0")
                isFabPressed = false
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(56.dp)
                .scale(fabScale),
            shape = CircleShape,
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.str_add_note),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun BatchActionBar(
    selectedCount: Int,
    totalCount: Int,
    onSelectAll: () -> Unit,
    onDelete: () -> Unit,
    onExit: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(onClick = onExit) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null
                    )
                }
                
                Text(
                    text = "$selectedCount / $totalCount",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onSelectAll) {
                    Icon(
                        imageVector = if (selectedCount == totalCount) Icons.Default.ClearAll else Icons.Default.SelectAll,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (selectedCount == totalCount) stringResource(R.string.str_cancel) else stringResource(R.string.str_confirm))
                }
                
                FilledTonalButton(
                    onClick = onDelete,
                    enabled = selectedCount > 0,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.str_delete))
                }
            }
        }
    }
}

private suspend fun checkAndShowAnnouncement(
    context: android.content.Context,
    onShowAnnouncement: (String, String) -> Unit
) {
    val url = "${SupabaseClient.SUPABASE_URL}/rest/v1/gonggao"
    
    val request = okhttp3.Request.Builder()
        .url(url)
        .get()
        .addHeader("apikey", SupabaseClient.SUPABASE_ANON_KEY)
        .addHeader("Content-Type", "application/json")
        .build()
    
    val response = suspendCancellableCoroutine<okhttp3.Response?> { continuation ->
        SupabaseClient.client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                if (continuation.isActive) continuation.resume(null)
            }
            
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (continuation.isActive) continuation.resume(response)
            }
        })
    }
    
    response?.use {
        if (it.isSuccessful) {
            val body = it.body?.string()
            if (!body.isNullOrBlank()) {
                val jsonArray = JSONArray(body)
                
                var maxId = -1L
                for (i in 0 until jsonArray.length()) {
                    val json = jsonArray.getJSONObject(i)
                    val id = json.optLong("id", -1)
                    if (id > maxId) {
                        maxId = id
                    }
                }
                
                val latestAnnouncementId = maxId + 1
                
                var latestAnnouncement: JSONObject? = null
                for (i in 0 until jsonArray.length()) {
                    val json = jsonArray.getJSONObject(i)
                    val id = json.optLong("id", -1)
                    if (id == latestAnnouncementId) {
                        latestAnnouncement = json
                        break
                    }
                }
                
                if (latestAnnouncement == null && maxId != -1L) {
                    for (i in 0 until jsonArray.length()) {
                        val json = jsonArray.getJSONObject(i)
                        val id = json.optLong("id", -1)
                        if (id == maxId) {
                            latestAnnouncement = json
                            break
                        }
                    }
                }
                
                if (latestAnnouncement != null) {
                    val id = latestAnnouncement.optLong("id", -1)
                    val title = latestAnnouncement.optString("title", context.getString(R.string.str_system_announcement))
                    val content = latestAnnouncement.optString("content", "")
                    val isImportant = latestAnnouncement.optBoolean("is_important", false)
                    
                    val prefs = context.getSharedPreferences("announcement_prefs", Context.MODE_PRIVATE)
                    val lastViewedAnnouncementId = prefs.getLong("last_viewed_announcement_id", -1)
                    
                    if (isImportant && id != lastViewedAnnouncementId) {
                        onShowAnnouncement(title, content)
                        prefs.edit().putLong("last_viewed_announcement_id", id).apply()
                    }
                }
            }
        }
    }
}

fun stringResource(context: Context, resId: Int): String {
    return context.getString(resId)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteItem(
    note: NoteBean,
    isSelected: Boolean = false,
    isInBatchMode: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onSelectClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "press_scale"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .combinedClickable(
                onClick = {
                    isPressed = true
                    onClick()
                    isPressed = false
                },
                onLongClick = onLongClick
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 6.dp else 2.dp
        ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isInBatchMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onSelectClick() },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(60.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (note.isTop == 1) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = note.title.ifEmpty { stringResource(R.string.str_empty) },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (note.isTop == 1) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        
                        if (note.isEncrypt == 1) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = DateUtils.getRelativeTimeDesc(note.modifyTime),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
                
                Text(
                    text = if (note.isEncrypt == 1) {
                        "🔒 " + stringResource(R.string.str_note_content_hint)
                    } else {
                        note.content.ifEmpty { stringResource(R.string.str_empty) }
                            .take(80) + if (note.content.length > 80) "..." else ""
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
