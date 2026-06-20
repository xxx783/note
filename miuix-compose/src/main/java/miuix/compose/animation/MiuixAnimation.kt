package miuix.compose.animation

import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.platform.inspectable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.sin

/**
 * MIUIX Compose 动画引擎
 * 基于 Folme 物理引擎的 Compose 动画实现
 */

// ==================== 缓动函数 ====================

/**
 * 弹簧缓动 - 模拟真实弹簧物理效果
 */
class SpringEasing(
    val dampingRatio: Float = 0.75f,
    val stiffness: Float = 300f
) : Easing {
    override fun transform(fraction: Float): Float {
        return spring(dampingRatio = dampingRatio, stiffness = stiffness)
            .transform(fraction)
    }
}

/**
 * 弹跳缓动 - 模拟球体落地反弹效果
 */
class BounceEasing : Easing {
    override fun transform(fraction: Float): Float {
        return when {
            fraction < 1/2.75 -> 7.5625f * fraction * fraction
            fraction < 2/2.75 -> {
                val f = fraction - 1.5/2.75
                7.5625f * f * f + 0.75f
            }
            fraction < 2.5/2.75 -> {
                val f = fraction - 2.25/2.75
                7.5625f * f * f + 0.9375f
            }
            else -> {
                val f = fraction - 2.625/2.75
                7.5625f * f * f + 0.984375f
            }
        }
    }
}

/**
 * 三次贝塞尔缓动
 */
class CubicBezierEasing(
    private val x1: Float,
    private val y1: Float,
    private val x2: Float,
    private val y2: Float
) : Easing {
    override fun transform(fraction: Float): Float {
        return cubicBezier(x1, y1, x2, y2).transform(fraction)
    }
}

// ==================== 物理动画 ====================

/**
 * 弹簧动画状态
 */
class SpringAnimationState<T> internal constructor(
    private val initialValue: T,
    private val targetValue: T,
    private val dampingRatio: Float = 0.75f,
    private val stiffness: Float = 300f
) {
    val animatedValue: T by animateDpAsState(
        targetValue = targetValue as Dp,
        animationSpec = spring(dampingRatio = dampingRatio, stiffness = stiffness),
        finishedListener = { }
    )
}

// ==================== Compose 动画修饰符 ====================

/**
 * MIUIX 弹簧动画修饰符
 * 为 Compose 组件添加物理弹簧动画效果
 */
fun Modifier.miuixSpring(
    targetValue: Float,
    dampingRatio: Float = 0.75f,
    stiffness: Float = 300f,
    visibilityThreshold: Float = 0.001f
): Modifier = this.then(
    Modifier.animateFloatAsState(
        targetValue = targetValue,
        animationSpec = spring(dampingRatio = dampingRatio, stiffness = stiffness, visibilityThreshold = visibilityThreshold)
    ).let { anim ->
        Modifier.graphicsLayer {
            alpha = anim.value
        }
    }
)

/**
 * 缩放弹簧动画修饰符
 */
fun Modifier.miuixScaleSpring(
    targetScale: Float = 1f,
    dampingRatio: Float = 0.75f,
    stiffness: Float = 300f
): Modifier = this.then(
    Modifier.animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(dampingRatio = dampingRatio, stiffness = stiffness)
    ).let { anim ->
        Modifier.scale(anim.value)
    }
)

/**
 * 旋转弹簧动画修饰符
 */
fun Modifier.miuixRotateSpring(
    targetRotation: Float = 0f,
    dampingRatio: Float = 0.75f,
    stiffness: Float = 300f
): Modifier = this.then(
    Modifier.animateFloatAsState(
        targetValue = targetRotation,
        animationSpec = spring(dampingRatio = dampingRatio, stiffness = stiffness)
    ).let { anim ->
        Modifier.rotate(anim.value)
    }
)

/**
 * 平移弹簧动画修饰符
 */
fun Modifier.miuixTranslateSpring(
    translateX: Float = 0f,
    translateY: Float = 0f,
    dampingRatio: Float = 0.75f,
    stiffness: Float = 300f
): Modifier = this.then(
    Modifier.animateFloatAsState(
        targetValue = translateX,
        animationSpec = spring(dampingRatio = dampingRatio, stiffness = stiffness)
    ).let { animX ->
        Modifier.animateFloatAsState(
            targetValue = translateY,
            animationSpec = spring(dampingRatio = dampingRatio, stiffness = stiffness)
        ).let { animY ->
            Modifier.graphicsLayer {
                translationX = animX.value
                translationY = animY.value
            }
        }
    }
)

// ==================== 状态动画 ====================

/**
 * MIUIX 状态容器
 * 管理组件的不同状态和状态切换动画
 */
@Composable
fun <T> rememberMiuixState(
    initialState: T,
    vararg states: Pair<T, Float>
): MiuixState<T> where T : Any {
    val currentState = remember { mutableStateOf(initialState) }
    val stateMap = remember { states.toMap() }
    
    return remember {
        MiuixState(currentState, stateMap)
    }
}

class MiuixState<T>(
    private val currentState: MutableState<T>,
    private val stateMap: Map<T, Float>
) where T : Any {
    val value: T get() = currentState.value
    
    fun setState(newState: T) {
        currentState.value = newState
    }
    
    fun getAlphaForState(state: T): Float {
        return stateMap[state] ?: 1f
    }
}

// ==================== 交互动画 ====================

/**
 * 可点击的弹簧动画组件
 * 点击时自动播放缩放和透明度动画
 */
@Composable
fun MiuixClickableBox(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var isHovered by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else if (isHovered) 1.02f else 1f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f),
        label = "scale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 1f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f),
        label = "alpha"
    )
    
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .scale(scale)
            .alpha(alpha)
            .inspectable(
                debugInspectorInfo {
                    name = "MiuixClickableBox"
                    properties["isPressed"] = isPressed
                    properties["isHovered"] = isHovered
                }
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = { },
                onHover = { hovered -> isHovered = hovered },
                onPress = { pressed -> isPressed = pressed }
            )
    ) {
        content()
    }
}

// ==================== 入场动画 ====================

/**
 * 列表项入场动画
 * 为列表项添加阶梯式入场效果
 */
@Composable
fun MiuixAnimatedListItem(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(index) {
        kotlinx.coroutines.delay(index * 50)
        isVisible = true
    }
    
    val offsetY by animateDpAsState(
        targetValue = if (isVisible) 0.dp else 50.dp,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f),
        label = "offsetY"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 300f),
        label = "alpha"
    )
    
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .graphicsLayer {
                translationY = offsetY.value
            }
            .alpha(alpha)
    ) {
        content()
    }
}

// ==================== 脉冲动画 ====================

/**
 * 脉冲动画效果
 * 持续的缩放脉冲，适合通知、提示等场景
 */
@Composable
fun MiuixPulseAnimation(
    modifier: Modifier = Modifier,
    scaleMin: Float = 1f,
    scaleMax: Float = 1.1f,
    durationMillis: Int = 2000,
    repeatCount: Int = Infinite,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = scaleMin,
        targetValue = scaleMax,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Box(
        modifier = modifier.scale(scale)
    ) {
        content()
    }
}

// ==================== 缓动工具类 ====================

object MiuixEasings {
    val Spring = SpringEasing()
    val Bounce = BounceEasing()
    val Accelerate = AccelerateEasing()
    val Decelerate = DecelerateEasing()
    val Linear = LinearEasing
    
    fun cubic(x1: Float, y1: Float, x2: Float, y2: Float): Easing {
        return CubicBezierEasing(x1, y1, x2, y2)
    }
}

// 标准缓动实现
private object AccelerateEasing : Easing {
    override fun transform(fraction: Float): Float {
        return fraction * fraction
    }
}

private object DecelerateEasing : Easing {
    override fun transform(fraction: Float): Float {
        return 1f - (1f - fraction) * (1f - fraction)
    }
}

private object LinearEasing : Easing {
    override fun transform(fraction: Float): Float {
        return fraction
    }
}
