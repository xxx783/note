package com.yutie.note.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MarkdownToolbar(
    onFormatClick: (FormatType) -> Unit
) {
    var showHeadingMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            HeadingDropdownMenu(
                expanded = showHeadingMenu,
                onExpandChange = { showHeadingMenu = it },
                onSelect = { level ->
                    onFormatClick(FormatType.Heading(level))
                    showHeadingMenu = false
                }
            )

            Divider(modifier = Modifier.width(1.dp).height(32.dp))

            FormatIconButton(
                icon = Icons.Default.FormatBold,
                description = "加粗",
                onClick = { onFormatClick(FormatType.Bold) }
            )

            FormatIconButton(
                icon = Icons.Default.FormatItalic,
                description = "斜体",
                onClick = { onFormatClick(FormatType.Italic) }
            )

            FormatIconButton(
                icon = Icons.Default.StrikethroughS,
                description = "删除线",
                onClick = { onFormatClick(FormatType.Strikethrough) }
            )

            Divider(modifier = Modifier.width(1.dp).height(32.dp))

            FormatIconButton(
                icon = Icons.Default.List,
                description = "无序列表",
                onClick = { onFormatClick(FormatType.UnorderedList) }
            )

            FormatIconButton(
                icon = Icons.Default.FormatListNumbered,
                description = "有序列表",
                onClick = { onFormatClick(FormatType.OrderedList) }
            )

            FormatIconButton(
                icon = Icons.Default.FormatQuote,
                description = "引用",
                onClick = { onFormatClick(FormatType.Quote) }
            )

            Divider(modifier = Modifier.width(1.dp).height(32.dp))

            FormatIconButton(
                icon = Icons.Default.Code,
                description = "代码块",
                onClick = { onFormatClick(FormatType.CodeBlock) }
            )

            FormatIconButton(
                icon = Icons.Default.Link,
                description = "链接",
                onClick = { onFormatClick(FormatType.Link) }
            )

            FormatIconButton(
                icon = Icons.Default.Image,
                description = "图片",
                onClick = { onFormatClick(FormatType.Image) }
            )

            FormatIconButton(
                icon = Icons.Default.Minimize,
                description = "分割线",
                onClick = { onFormatClick(FormatType.HorizontalRule) }
            )
        }
    }
}

@Composable
fun HeadingDropdownMenu(
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    onSelect: (Int) -> Unit
) {
    val headingOptions = listOf(1 to "H1", 2 to "H2", 3 to "H3", 4 to "H4", 5 to "H5", 6 to "H6")

    Box {
        IconButton(onClick = { onExpandChange(true) }) {
            Icon(
                imageVector = Icons.Default.Title,
                contentDescription = "标题",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandChange(false) }
        ) {
            headingOptions.forEach { (level, label) ->
                DropdownMenuItem(
                    text = { Text(text = label) },
                    onClick = {
                        onSelect(level)
                    }
                )
            }
        }
    }
}

@Composable
fun FormatIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(40.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp)
        )
    }
}

sealed class FormatType {
    data class Heading(val level: Int) : FormatType()
    object Bold : FormatType()
    object Italic : FormatType()
    object Strikethrough : FormatType()
    object UnorderedList : FormatType()
    object OrderedList : FormatType()
    object Quote : FormatType()
    object CodeBlock : FormatType()
    object Link : FormatType()
    object Image : FormatType()
    object HorizontalRule : FormatType()
}

fun applyFormat(text: String, cursorPosition: Int, formatType: FormatType): Pair<String, Int> {
    return when (formatType) {
        is FormatType.Heading -> {
            val prefix = "#".repeat(formatType.level) + " "
            if (text.isNotEmpty() && text.lines().firstOrNull()?.startsWith("#") == true) {
                val lines = text.lines()
                val firstLine = lines[0].replace(Regex("^#+\\s*"), prefix)
                val newText = (listOf(firstLine) + lines.drop(1)).joinToString("\n")
                Pair(newText, cursorPosition)
            } else {
                Pair(prefix + text, cursorPosition + formatType.level + 1)
            }
        }
        FormatType.Bold -> applySurroundFormat(text, cursorPosition, "**")
        FormatType.Italic -> applySurroundFormat(text, cursorPosition, "*")
        FormatType.Strikethrough -> applySurroundFormat(text, cursorPosition, "~~")
        FormatType.UnorderedList -> applyLinePrefix(text, cursorPosition, "- ")
        FormatType.OrderedList -> applyLinePrefix(text, cursorPosition, "1. ")
        FormatType.Quote -> applyLinePrefix(text, cursorPosition, "> ")
        FormatType.CodeBlock -> {
            val codeBlock = "```\n$text\n```"
            Pair(codeBlock, cursorPosition + 4)
        }
        FormatType.Link -> {
            val link = "[文本]($text)"
            Pair(link, cursorPosition + 4)
        }
        FormatType.Image -> {
            val image = "![描述]($text)"
            Pair(image, cursorPosition + 7)
        }
        FormatType.HorizontalRule -> {
            Pair("\n---\n$text", 4)
        }
    }
}

private fun applySurroundFormat(text: String, cursorPosition: Int, wrapper: String): Pair<String, Int> {
    val before = text.substring(0, cursorPosition)
    val after = text.substring(cursorPosition)
    val newText = before + wrapper + after
    return Pair(newText, cursorPosition + wrapper.length)
}

private fun applyLinePrefix(text: String, cursorPosition: Int, prefix: String): Pair<String, Int> {
    val lines = text.substring(0, cursorPosition).split("\n")
    val currentLineStart = cursorPosition - lines.last().length
    val before = text.substring(0, currentLineStart)
    val after = text.substring(currentLineStart)
    val newText = before + prefix + after
    return Pair(newText, cursorPosition + prefix.length)
}
