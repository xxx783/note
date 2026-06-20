package com.yutie.note.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * 默认字体大小
 */
val LocalDefaultFontSize = compositionLocalOf { 16.sp }

/**
 * 当前字体家族类型（default, serif, monospace）
 */
val LocalFontFamilyType = compositionLocalOf { "default" }
