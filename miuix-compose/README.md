# MIUIX Compose - Jetpack Compose 版本

[![Platform](https://img.shields.io/badge/platform-Android-green.svg)](https://developer.android.com/)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)](https://android-arsenal.com/api?level=24)
[![Compose](https://img.shields.io/badge/compose-1.6.0-blue.svg)](https://developer.android.com/jetpack/compose)

**MIUIX for Jetpack Compose** - 为现代 Android 应用带来 MIUI 风格的流畅动画和精美组件

## 📖 简介

MIUIX Compose 是 MIUIX 框架的 Jetpack Compose 版本，提供了：
- 🎨 基于 Compose 的声明式动画 API
- 🎯 物理弹簧动画系统
- 🧩 精美的 MIUI 风格 Compose 组件
- 📱 完全兼容 Material Design 3
- 🌊 流畅的入场和交互动画

## ✨ 特性

### 动画系统
- **弹簧动画** - 基于真实物理规律的动画效果
- **缓动函数** - 30+ 种缓动曲线（Spring、Bounce、CubicBezier 等）
- **状态动画** - 声明式状态切换动画
- **交互动画** - 点击、悬停、按压反馈

### UI 组件
- **MiuixButton** - 带弹簧动画的按钮
- **MiuixCard** - MIUI 风格卡片
- **MiuixTextField** - 带清除动画的输入框
- **MiuixSwitch** - 弹簧动画开关
- **MiuixSlider** - 平滑动画滑块
- **MiuixDialog** - 带入场动画的对话框

## 📦 安装

### Gradle 依赖

在项目级别的 `settings.gradle` 中添加：
```gradle
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

include ':app'
include ':miuix-compose'
```

在模块级别的 `build.gradle` 中添加：
```gradle
android {
    // ... 其他配置
    
    buildFeatures {
        compose true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion '1.5.14'
    }
}

dependencies {
    // Compose BOM
    implementation platform('androidx.compose:compose-bom:2024.06.00')
    
    // MIUIX Compose
    implementation project(':miuix-compose')
    
    // 或者使用 AAR
    // implementation files('libs/miuix-compose-release.aar')
}
```

## 🚀 快速开始

### 1. 基础动画

```kotlin
@Composable
fun SimpleAnimationExample() {
    var expanded by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (expanded) 1.2f else 1f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f)
    )
    
    MiuixButton(
        onClick = { expanded = !expanded },
        text = "点我"
    )
    
    Box(
        modifier = Modifier
            .size(100.dp)
            .scale(scale)
            .background(Color.Blue)
    )
}
```

### 2. 使用修饰符动画

```kotlin
@Composable
fun ModifierAnimationExample() {
    var isPressed by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .size(100.dp)
            .miuixScaleSpring(
                targetScale = if (isPressed) 0.95f else 1f
            )
            .miuixTranslateSpring(
                translateY = if (isPressed) 5.dp else 0.dp
            )
            .clickable { isPressed = !isPressed }
            .background(Color.Blue)
    )
}
```

### 3. 使用组件

```kotlin
@Composable
fun ComponentsExample() {
    var text by remember { mutableStateOf("") }
    var checked by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        // MIUI 风格输入框
        MiuixTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = "请输入内容",
            showClearButton = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // MIUI 风格开关
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("开关")
            MiuixSwitch(
                checked = checked,
                onCheckedChange = { checked = it }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // MIUI 风格按钮
        MiuixButton(
            onClick = { showDialog = true },
            text = "显示对话框"
        )
        
        // MIUI 风格对话框
        if (showDialog) {
            MiuixDialog(
                onDismissRequest = { showDialog = false },
                title = "提示",
                confirmButtonText = "确定",
                onConfirm = { /* 处理确认 */ }
            ) {
                Text("这是一个 MIUI 风格的对话框")
            }
        }
    }
}
```

### 4. 列表项入场动画

```kotlin
@Composable
fun AnimatedListExample(items: List<String>) {
    LazyColumn {
        itemsIndexed(items) { index, item ->
            MiuixAnimatedListItem(index = index) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = item,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
```

### 5. 脉冲动画

```kotlin
@Composable
fun PulseAnimationExample() {
    MiuixPulseAnimation(
        scaleMin = 1f,
        scaleMax = 1.1f,
        durationMillis = 2000
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(Color.Red)
        )
    }
}
```

## 🎨 自定义缓动函数

```kotlin
// 使用预定义的缓动函数
val springEasing = MiuixEasings.Spring
val bounceEasing = MiuixEasings.Bounce

// 自定义三次贝塞尔缓动
val customEasing = MiuixEasings.cubic(0.68f, 0f, 0.265f, 1f)

// 在动画中使用
val animatedValue by animateFloatAsState(
    targetValue = targetValue,
    animationSpec = tween(
        durationMillis = 300,
        easing = MiuixEasings.Decelerate
    )
)
```

## 📊 组件展示

### 按钮
```kotlin
MiuixButton(
    onClick = { /* 处理点击 */ },
    text = "按钮",
    containerColor = MaterialTheme.colorScheme.primary,
    contentColor = MaterialTheme.colorScheme.onPrimary
)
```

### 卡片
```kotlin
MiuixCard(
    elevation = 4.dp,
    containerColor = MaterialTheme.colorScheme.surface
) {
    Text("卡片内容")
}
```

### 输入框
```kotlin
MiuixTextField(
    value = text,
    onValueChange = { text = it },
    placeholder = "请输入",
    showClearButton = true
)
```

### 开关
```kotlin
MiuixSwitch(
    checked = checked,
    onCheckedChange = { checked = it }
)
```

### 滑块
```kotlin
MiuixSlider(
    value = value,
    onValueChange = { value = it },
    valueRange = 0f..100f,
    steps = 10
)
```

## 🔧 高级用法

### 状态管理

```kotlin
@Composable
fun StateManagementExample() {
    val state = rememberMiuixState(
        initialState = "normal",
        "normal" to 1f,
        "pressed" to 0.95f,
        "disabled" to 0.5f
    )
    
    Box(
        modifier = Modifier
            .size(100.dp)
            .scale(state.getAlphaForState(state.value))
            .clickable {
                state.setState("pressed")
            }
            .background(Color.Blue)
    )
}
```

### 组合动画

```kotlin
@Composable
fun CombinedAnimationExample() {
    var expanded by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (expanded) 1.2f else 1f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f)
    )
    
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 360f else 0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f)
    )
    
    val offsetY by animateDpAsState(
        targetValue = if (expanded) (-50).dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f)
    )
    
    Box(
        modifier = Modifier
            .size(100.dp)
            .scale(scale)
            .rotate(rotation)
            .offset(y = offsetY)
            .clickable { expanded = !expanded }
            .background(Color.Blue)
    )
}
```

## ⚠️ 注意事项

1. **Compose 版本**: 需要 Compose 1.6.0 及以上版本
2. **Kotlin 版本**: 需要 Kotlin 1.9.0 及以上版本
3. **最小 SDK**: Android 7.0 (API 24)
4. **性能**: 大量动画同时运行会增加 GPU 负载

## 🎯 最佳实践

### 1. 使用 remember 保存动画状态
```kotlin
@Composable
fun GoodExample() {
    val animatedValue by remember {
        animateFloatAsState(...)
    }
}
```

### 2. 避免在动画中创建对象
```kotlin
// ❌ 不好的做法
@Composable
fun BadExample() {
    val animation = animateFloatAsState(...) // 每次重组都创建
}

// ✅ 好的做法
@Composable
fun GoodExample() {
    val animation by remember {
        mutableStateOf(animateFloatAsState(...))
    }
}
```

### 3. 使用 LaunchedEffect 控制动画时机
```kotlin
@Composable
fun TimedAnimationExample() {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(1000)
        visible = true
    }
    
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f
    )
}
```

## 📚 相关资源

- [Jetpack Compose 官方文档](https://developer.android.com/jetpack/compose)
- [Compose 动画指南](https://developer.android.com/jetpack/compose/animation)
- [Material Design 3](https://m3.material.io/)
- [MIUIX 原版文档](../README.md)

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📄 许可证

MIT License - 查看 [LICENSE](../LICENSE) 文件

---

**Made with ❤️ by MIUIX Team**
