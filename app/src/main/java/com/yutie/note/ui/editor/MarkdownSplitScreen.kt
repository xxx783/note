package com.yutie.note.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ViewMode {
    EDIT_ONLY,
    SPLIT,
    PREVIEW_ONLY
}

@Composable
fun MarkdownSplitScreen(
    viewMode: ViewMode,
    title: String,
    content: String,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onCursorPositionChange: (Int) -> Unit,
    isProUser: Boolean = true
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue(content)) }

    LaunchedEffect(content) {
        if (textFieldValue.text != content) {
            textFieldValue = TextFieldValue(content, textFieldValue.selection)
        }
    }

    val handleContentChange = { newTextFieldValue: TextFieldValue ->
        textFieldValue = newTextFieldValue
        onContentChange(newTextFieldValue.text)
        onCursorPositionChange(newTextFieldValue.selection.start)
    }

    when (viewMode) {
        ViewMode.EDIT_ONLY -> {
            EditOnlyMode(
                title = title,
                textFieldValue = textFieldValue,
                onTitleChange = onTitleChange,
                onContentChange = handleContentChange
            )
        }
        ViewMode.SPLIT -> {
            SplitMode(
                title = title,
                textFieldValue = textFieldValue,
                onTitleChange = onTitleChange,
                onContentChange = handleContentChange,
                isProUser = isProUser
            )
        }
        ViewMode.PREVIEW_ONLY -> {
            PreviewOnlyMode(content = content)
        }
    }
}

@Composable
fun EditOnlyMode(
    title: String,
    textFieldValue: TextFieldValue,
    onTitleChange: (String) -> Unit,
    onContentChange: (TextFieldValue) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        BasicTextField(
            value = title,
            onValueChange = onTitleChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            textStyle = TextStyle(
                fontSize = 24.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (title.isEmpty()) {
                        Text(
                            text = "标题",
                            style = TextStyle(
                                fontSize = 24.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        )
                    }
                    innerTextField()
                }
            },
            maxLines = 3
        )

        BasicTextField(
            value = textFieldValue,
            onValueChange = onContentChange,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            textStyle = TextStyle(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Box(modifier = Modifier.fillMaxSize()) {
                    if (textFieldValue.text.isEmpty()) {
                        Text(
                            text = "输入内容...",
                            style = TextStyle(
                                fontSize = 16.sp,
                                lineHeight = 24.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                            )
                        )
                    }
                    innerTextField()
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
            keyboardActions = KeyboardActions(onAny = { /* Handle action */ }),
            maxLines = Int.MAX_VALUE
        )
    }
}

@Composable
fun SplitMode(
    title: String,
    textFieldValue: TextFieldValue,
    onTitleChange: (String) -> Unit,
    onContentChange: (TextFieldValue) -> Unit,
    isProUser: Boolean
) {
    Row(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.background)
        ) {
            BasicTextField(
                value = title,
                onValueChange = onTitleChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textStyle = TextStyle(
                    fontSize = 22.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (title.isEmpty()) {
                            Text(
                                text = "标题",
                                style = TextStyle(
                                    fontSize = 22.sp,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            )
                        }
                        innerTextField()
                    }
                },
                maxLines = 2
            )

            Divider(modifier = Modifier.fillMaxWidth())

            BasicTextField(
                value = textFieldValue,
                onValueChange = onContentChange,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                textStyle = TextStyle(
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (textFieldValue.text.isEmpty()) {
                            Text(
                                text = "输入 Markdown...",
                                style = TextStyle(
                                    fontSize = 15.sp,
                                    lineHeight = 22.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            )
                        }
                        innerTextField()
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                keyboardActions = KeyboardActions(onAny = { /* Handle action */ }),
                maxLines = Int.MAX_VALUE
            )
        }

        Divider(modifier = Modifier.width(1.dp).fillMaxHeight())

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Text(
                text = "预览",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                ),
                modifier = Modifier.padding(16.dp)
            )

            Divider(modifier = Modifier.fillMaxWidth())

            Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                MarkdownPreviewPanel(content = textFieldValue.text)
            }
        }
    }
}

@Composable
fun PreviewOnlyMode(
    content: String
) {
    Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        MarkdownPreviewPanel(content = content)
    }
}
