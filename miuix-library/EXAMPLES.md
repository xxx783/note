# 使用示例

## 基础示例

### 1. 简单的弹簧动画

```kotlin
// Kotlin
import miuix.animation.Folme

val view = findViewById<View>(R.id.target_view)

// 创建透明度动画
Folme.useValueTarget(view)
    .addTarget("alpha", 0f, 1f)
    .setDuration(300)
    .start()

// 创建位移动画
Folme.useValueTarget(view)
    .addTarget("translationX", 0f, 100f)
    .setEasing(miux.animation.easing.SpringEasing())
    .start()
```

```java
// Java
import miuix.animation.Folme;

View view = findViewById(R.id.target_view);

// 创建透明度动画
Folme.useValueTarget(view)
    .addTarget("alpha", 0f, 1f)
    .setDuration(300)
    .start();
```

### 2. 状态动画

```kotlin
import miuix.animation.Folme

val button = findViewById<Button>(R.id.state_button)

// 创建状态动画
Folme.useStateTarget(button)
    .addState("normal", 1f)
    .addState("pressed", 0.8f)
    .addState("disabled", 0.5f)
    .setCurrentState("normal")

// 切换状态
button.setOnTouchListener { v, event ->
    when (event.action) {
        MotionEvent.ACTION_DOWN -> 
            Folme.getStateTarget(button).setCurrentState("pressed")
        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> 
            Folme.getStateTarget(button).setCurrentState("normal")
    }
    false
}
```

### 3. 使用自定义缓动函数

```kotlin
import miuix.animation.easing.CubicBezierEasing

// 使用三次贝塞尔缓动
val easing = CubicBezierEasing(0.68f, 0f, 0.265f, 1f)

Folme.useValueTarget(view)
    .addTarget("scaleX", 1f, 1.2f)
    .addTarget("scaleY", 1f, 1.2f)
    .setEasing(easing)
    .start()
```

## 高级示例

### 4. 天气动画渲染

```kotlin
class WeatherAnimationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val sunView: ImageView
    private val cloudView: ImageView
    
    init {
        // 初始化视图
        inflate(context, R.layout.weather_view, this)
        sunView = findViewById(R.id.sun)
        cloudView = findViewById(R.id.cloud)
        
        setupAnimations()
    }
    
    private fun setupAnimations() {
        // 太阳脉动动画
        Folme.useValueTarget(sunView)
            .addTarget("scale", 1f, 1.1f)
            .setDuration(2000)
            .setRepeatMode(ValueTarget.REVERSE)
            .setRepeatCount(ValueTarget.INFINITE)
            .start()
        
        // 云朵飘动动画
        Folme.useValueTarget(cloudView)
            .addTarget("translationX", 0f, 50f)
            .setDuration(3000)
            .setRepeatMode(ValueTarget.REVERSE)
            .setRepeatCount(ValueTarget.INFINITE)
            .start()
    }
    
    fun setWeatherType(type: WeatherType) {
        when (type) {
            WeatherType.SUNNY -> showSunny()
            WeatherType.CLOUDY -> showCloudy()
            WeatherType.RAINY -> showRainy()
        }
    }
    
    private fun showSunny() {
        Folme.getStateTarget(sunView).setCurrentState("visible")
        Folme.getStateTarget(cloudView).setCurrentState("hidden")
    }
    
    private fun showCloudy() {
        Folme.getStateTarget(sunView).setCurrentState("half_visible")
        Folme.getStateTarget(cloudView).setCurrentState("visible")
    }
}
```

### 5. 自定义 View 动画

```kotlin
class AnimatedButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatButton(context, attrs) {

    private var isAnimating = false

    override fun setPressed(pressed: Boolean) {
        super.setPressed(pressed)
        
        if (pressed && !isAnimating) {
            animatePress()
        }
    }
    
    private fun animatePress() {
        isAnimating = true
        
        // 按下效果
        Folme.useValueTarget(this)
            .addTarget("scale", 1f, 0.95f)
            .setDuration(100)
            .setEasing(miux.animation.easing.AccelerateEasing())
            .setEndListener {
                // 回弹效果
                Folme.useValueTarget(this@AnimatedButton)
                    .addTarget("scale", 0.95f, 1f)
                    .setDuration(200)
                    .setEasing(miux.animation.easing.SpringEasing())
                    .setEndListener {
                        isAnimating = false
                    }
                    .start()
            }
            .start()
    }
}
```

### 6. 列表项动画

```kotlin
class AnimatedAdapter(
    private val items: List<String>,
    private val context: Context
) : RecyclerView.Adapter<AnimatedAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
        
        // 入场动画
        if (position < lastAnimatedPosition) {
            lastAnimatedPosition = position
            animateEntry(holder.itemView, position)
        }
    }
    
    private fun animateEntry(itemView: View, position: Int) {
        itemView.apply {
            alpha = 0f
            translationY = 50f
            
            Folme.useValueTarget(this)
                .addTarget("alpha", 0f, 1f)
                .addTarget("translationY", 50f, 0f)
                .setDuration(300)
                .setStartDelay(position * 50)
                .setEasing(miux.animation.easing.DecelerateEasing())
                .start()
        }
    }
    
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(item: String) {
            itemView.findViewById<TextView>(R.id.text).text = item
        }
    }
}
```

## 性能优化

### 7. 批量更新

```kotlin
// 使用批量更新减少性能开销
Folme.useValueTarget(view1)
    .addTarget("alpha", 0f, 1f)
    .start()

Folme.useValueTarget(view2)
    .addTarget("alpha", 0f, 1f)
    .start()

// 批量提交
Folme.batchUpdate {
    // 所有动画会同时执行
}
```

### 8. 动画缓存

```kotlin
// 缓存动画配置
val springEasing = SpringEasing().apply {
    dampingRatio = 0.75f
    stiffness = 300f
}

// 重复使用缓存的缓动函数
fun animateView1(view: View) {
    Folme.useValueTarget(view)
        .addTarget("scale", 1f, 1.2f)
        .setEasing(springEasing)
        .start()
}

fun animateView2(view: View) {
    Folme.useValueTarget(view)
        .addTarget("rotation", 0f, 360f)
        .setEasing(springEasing)
        .start()
}
```

## 故障排除

### 9. 调试动画

```kotlin
// 启用动画调试
Folme.enableDebug()

// 添加动画监听器
Folme.useValueTarget(view)
    .addTarget("alpha", 0f, 1f)
    .addListener(object : AnimationListener {
        override fun onAnimationStart(animation: Animation) {
            Log.d("Animation", "Started: ${animation.target}")
        }
        
        override fun onAnimationEnd(animation: Animation) {
            Log.d("Animation", "Ended: ${animation.target}")
        }
        
        override fun onAnimationUpdate(animation: Animation, value: Float) {
            Log.d("Animation", "Update: $value")
        }
    })
    .start()
```

### 10. 处理生命周期

```kotlin
class MainActivity : AppCompatActivity() {

    private var animation: Animation? = null

    override fun onResume() {
        super.onResume()
        startAnimation()
    }
    
    override fun onPause() {
        super.onPause()
        // 暂停时取消动画，避免内存泄漏
        animation?.cancel()
    }
    
    private fun startAnimation() {
        animation = Folme.useValueTarget(view)
            .addTarget("alpha", 0f, 1f)
            .start()
    }
}
```

## 更多资源

- API 文档：https://yourusername.github.io/miuix/
- 示例项目：https://github.com/yourusername/miuix/tree/master/sample
- 问题反馈：https://github.com/yourusername/miuix/issues
