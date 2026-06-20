package miuix.compose.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import miuix.compose.animation.MiuixClickableBox
import miuix.compose.animation.MiuixEasings

/**
 * MIUIX Compose 组件库
 * 基于 Material Design 3 的 MIUI 风格组件
 */

// ==================== 按钮组件 ====================

/**
 * MIUI 风格按钮
 * 带有弹簧动画效果的点击反馈
 */
@Composable
fun MiuixButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp)
) {
    MiuixClickableBox(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .clip(shape)
                .background(containerColor)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = contentColor,
                fontSize = 16.sp,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

// ==================== 卡片组件 ====================

/**
 * MIUI 风格卡片
 * 带有阴影和圆角的卡片容器
 */
@Composable
fun MiuixCard(
    modifier: Modifier = Modifier,
    elevation: Dp = 4.dp,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .padding(16.dp)
    ) {
        content()
    }
}

// ==================== 输入框组件 ====================

/**
 * MIUI 风格输入框
 * 带有清除按钮和动画效果
 */
@Composable
fun MiuixTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
    showClearButton: Boolean = true
) {
    var isFocused by remember { mutableStateOf(false) }
    
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) MaterialTheme.colorScheme.primary else Color.Gray,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f),
        label = "borderColor"
    )
    
    Box(modifier = modifier) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = borderColor,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    if (value.isEmpty() && placeholder.isNotEmpty()) {
                        Text(
                            text = placeholder,
                            color = Color.Gray.copy(alpha = 0.5f),
                            fontSize = 16.sp
                        )
                    }
                    innerTextField()
                    
                    // 清除按钮
                    if (showClearButton && value.isNotEmpty() && isFocused) {
                        MiuixClickableBox(
                            onClick = { onValueChange("") },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(24.dp)
                        ) {
                            Text(
                                text = "✕",
                                color = Color.Gray,
                                fontSize = 14.sp,
                                modifier = Modifier.wrapContentSize()
                            )
                        }
                    }
                }
            },
            onFocusChanged = { isFocused = it.isFocused }
        )
    }
}

// ==================== 开关组件 ====================

/**
 * MIUI 风格开关
 * 带有弹簧动画的切换效果
 */
@Composable
fun MiuixSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val thumbColor by animateColorAsState(
        targetValue = if (checked) {
            if (enabled) MaterialTheme.colorScheme.primary else Color.Gray
        } else {
            Color.White
        },
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f),
        label = "thumbColor"
    )
    
    val trackColor by animateColorAsState(
        targetValue = if (checked) {
            if (enabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color.Gray.copy(alpha = 0.5f)
        } else {
            Color.Gray.copy(alpha = 0.3f)
        },
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f),
        label = "trackColor"
    )
    
    val thumbOffset by animateFloatAsState(
        targetValue = if (checked) 20f else 0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f),
        label = "thumbOffset"
    )
    
    Box(
        modifier = modifier
            .size(width = 52.dp, height = 32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(trackColor)
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(4.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(thumbColor)
                .offset(x = thumbOffset.dp)
        )
    }
}

// ==================== 进度条组件 ====================

/**
 * MIUI 风格进度条
 * 带有平滑动画的进度指示器
 */
@Composable
fun MiuixProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f),
        label = "progress"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(backgroundColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
    }
}

// ==================== 滑块组件 ====================

/**
 * MIUI 风格滑块
 * 带有弹簧动画的拖动条
 */
@Composable
fun MiuixSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0
) {
    val animatedValue by animateFloatAsState(
        targetValue = value,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f),
        label = "value"
    )
    
    val thumbColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .clickable {
                    // 点击跳转
                }
        ) {
            // 背景轨道
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(1.dp))
                    .background(trackColor)
            )
            
            // 进度轨道
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedValue)
                    .height(2.dp)
                    .align(Alignment.CenterStart)
                    .clip(RoundedCornerShape(1.dp))
                    .background(thumbColor)
            )
            
            // 滑块
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.CenterStart)
                    .offset(x = ((animatedValue * 100) - 10).dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(thumbColor)
                    .border(
                        width = 2.dp,
                        color = Color.White,
                        shape = RoundedCornerShape(10.dp)
                    )
            )
        }
    }
}

// ==================== 徽章组件 ====================

/**
 * MIUI 风格徽章
 * 带有脉冲动画的通知徽章
 */
@Composable
fun MiuixBadge(
    count: Int,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.error,
    contentColor: Color = Color.White
) {
    if (count > 0) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(10.dp))
                .background(containerColor)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = if (count > 99) "99+" else count.toString(),
                color = contentColor,
                fontSize = 12.sp,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

// ==================== 对话框组件 ====================

/**
 * MIUI 风格对话框
 * 带有入场动画的对话框
 */
@Composable
fun MiuixDialog(
    onDismissRequest: () -> Unit,
    title: String,
    confirmButtonText: String,
    dismissButtonText: String = "取消",
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    var showDialog by remember { mutableStateOf(true) }
    
    val scale by animateFloatAsState(
        targetValue = if (showDialog) 1f else 0.8f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f),
        label = "scale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (showDialog) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f),
        label = "alpha"
    )
    
    if (showDialog) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(onClick = onDismissRequest),
            contentAlignment = Alignment.Center
        ) {
            MiuixCard(
                modifier = Modifier
                    .scale(scale)
                    .alpha(alpha)
                    .widthIn(max = 320.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                content()
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    MiuixButton(
                        onClick = {
                            showDialog = false
                            onDismissRequest()
                        },
                        text = dismissButtonText,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    MiuixButton(
                        onClick = {
                            showDialog = false
                            onConfirm()
                        },
                        text = confirmButtonText,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
