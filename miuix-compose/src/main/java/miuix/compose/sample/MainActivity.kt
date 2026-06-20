package miuix.compose.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import miuix.compose.animation.*
import miuix.compose.components.*

/**
 * MIUIX Compose 示例应用
 * 展示所有可用的动画和组件
 */

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MiuixSampleApp()
                }
            }
        }
    }
}

@Composable
fun MiuixSampleApp() {
    var showExamples by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 标题
        Text(
            text = "MIUIX Compose 示例",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // 动画演示区域
        MiuixAnimationDemo()
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 组件演示区域
        MiuixComponentsDemo()
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 列表动画演示
        MiuixListAnimationDemo()
    }
}

@Composable
fun MiuixAnimationDemo() {
    var expanded by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }
    
    MiuixCard {
        Text(
            text = "动画演示",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // 弹簧动画按钮
        MiuixButton(
            onClick = { expanded = !expanded },
            text = if (expanded) "收起" else "展开",
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 动画方块
        Box(
            modifier = Modifier
                .size(80.dp)
                .miuixScaleSpring(
                    targetScale = if (expanded) 1.2f else 1f
                )
                .miuixRotateSpring(
                    targetRotation = if (expanded) 180f else 0f
                )
                .background(MaterialTheme.colorScheme.primary)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 脉冲动画
        Text(
            text = "脉冲动画:",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        MiuixPulseAnimation(
            scaleMin = 1f,
            scaleMax = 1.1f,
            durationMillis = 2000
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(MaterialTheme.colorScheme.secondary)
            )
        }
    }
}

@Composable
fun MiuixComponentsDemo() {
    var text by remember { mutableStateOf("") }
    var checked by remember { mutableStateOf(false) }
    var sliderValue by remember { mutableStateOf(50f) }
    var showDialog by remember { mutableStateOf(false) }
    
    MiuixCard {
        Text(
            text = "组件演示",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // 输入框
        MiuixTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = "请输入内容",
            showClearButton = true,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 开关
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "启用功能",
                modifier = Modifier.weight(1f)
            )
            MiuixSwitch(
                checked = checked,
                onCheckedChange = { checked = it }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 滑块
        Column {
            Text("滑块值：${sliderValue.toInt()}")
            Spacer(modifier = Modifier.height(8.dp))
            MiuixSlider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                valueRange = 0f..100f,
                steps = 10
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 进度条
        Column {
            Text("进度条")
            Spacer(modifier = Modifier.height(8.dp))
            MiuixProgressBar(
                progress = sliderValue / 100f
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 对话框按钮
        MiuixButton(
            onClick = { showDialog = true },
            text = "显示对话框",
            modifier = Modifier.fillMaxWidth()
        )
        
        // 对话框
        if (showDialog) {
            MiuixDialog(
                onDismissRequest = { showDialog = false },
                title = "MIUIX 对话框",
                confirmButtonText = "确定",
                dismissButtonText = "取消",
                onConfirm = { /* 处理确认 */ }
            ) {
                Text("这是一个带有弹簧动画的 MIUI 风格对话框")
            }
        }
    }
}

@Composable
fun MiuixListAnimationDemo() {
    val items = remember {
        (1..10).map { "列表项 $it" }.toList()
    }
    
    MiuixCard {
        Text(
            text = "列表动画演示",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        LazyColumn(
            modifier = Modifier.height(300.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            itemsIndexed(items) { index, item ->
                MiuixAnimatedListItem(index = index) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 徽章
                            MiuixBadge(
                                count = if (index % 3 == 0) index else 0,
                                modifier = Modifier.padding(end = 16.dp)
                            )
                            
                            Text(
                                text = item,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MiuixInteractiveCard() {
    var isHovered by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else if (isHovered) 1.02f else 1f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f)
    )
    
    val elevation by animateDpAsState(
        targetValue = if (isHovered) 8.dp else 4.dp,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f)
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "可交互卡片",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "悬停或点击我查看效果",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
