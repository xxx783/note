# MIUIX 项目结构

```
miuix-library/
├── README.md                      # 项目说明文档
├── LICENSE                        # MIT 许可证
├── .gitignore                     # Git 忽略文件配置
├── JITPACK.md                     # JitPack 发布指南
├── EXAMPLES.md                    # 使用示例文档
├── STRUCTURE.md                   # 项目结构说明（本文件）
├── build.gradle                   # 根项目构建配置
├── settings.gradle                # 项目设置
├── gradle.properties              # Gradle 属性配置
│
├── core/                          # 核心模块
│   ├── build.gradle               # 模块构建配置
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/              # Java 源代码
│       │   │   └── miuix/
│       │   │       └── core/
│       │   │           ├── util/
│       │   │           │   └── SystemProperties.java
│       │   │           └── widget/
│       │   │               ├── CheckedTextView.java
│       │   │               └── ClearableEditText.java
│       │   └── res/               # Android 资源
│       │       ├── values/
│       │       │   ├── strings.xml
│       │       │   └── colors.xml
│       │       └── drawable/
│       └── test/
│           └── java/              # 单元测试代码
│
├── animation/                     # 动画模块
│   ├── build.gradle
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/
│       │   │   └── miuix/
│       │   │       └── animation/
│       │   │           ├── Folme.java              # Folme 主类
│       │   │           ├── FolmeEase.java          # 缓动工具
│       │   │           ├── FolmeFactory.java       # 工厂类
│       │   │           ├── easing/                 # 缓动函数
│       │   │           │   ├── SpringEasing.java
│       │   │           │   ├── BounceEasing.java
│       │   │           │   └── CubicBezierEasing.java
│       │   │           ├── physics/                # 物理引擎
│       │   │           │   ├── SpringAnimation.java
│       │   │           │   ├── FlingAnimation.java
│       │   │           │   └── DampedHarmonicMotion.java
│       │   │           ├── controller/             # 动画控制器
│       │   │           │   ├── FolmeState.java
│       │   │           │   ├── FolmeTouch.java
│       │   │           │   └── StateManager.java
│       │   │           └── listener/               # 监听器
│       │   │               ├── AnimationListener.java
│       │   │               └── UpdateListener.java
│       │   └── res/
│       └── test/
│
├── widget/                        # 控件模块
│   ├── build.gradle
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/
│       │   │   └── miuix/
│       │   │       └── widget/
│       │   │           ├── ClearableEditText.java
│       │   │           ├── SeekingBar.java
│       │   │           ├── ProgressBar.java
│       │   │           └── PopupWindow.java
│       │   └── res/
│       │       ├── layout/
│       │       │   └── clearable_edit_text.xml
│       │       └── drawable/
│       │           └── seekbar_background.xml
│       └── test/
│
└── sample/                        # 示例应用（可选）
    ├── build.gradle
    └── src/
        └── main/
            ├── AndroidManifest.xml
            ├── java/
            │   └── miuix/
            │       └── sample/
            │           ├── MainActivity.java
            │           └── demo/
            │               ├── AnimationDemo.java
            │               └── WidgetDemo.java
            └── res/
                ├── layout/
                └── values/
```

## 模块说明

### core (核心模块)
**包名**: `miuix.core`

提供基础功能和工具类：
- 系统属性读取
- 基础视图增强
- 通用工具类

**主要类**:
- `SystemProperties` - 系统属性读取工具
- `CheckedTextView` - 复选文本视图
- `ClearableEditText` - 可清除文本框

### animation (动画模块)
**包名**: `miuix.animation`

Folme 动画引擎的核心实现：
- 物理动画系统
- 缓动函数库
- 状态动画
- 触摸反馈

**主要类**:
- `Folme` - 动画主入口
- `FolmeState` - 状态动画
- `FolmeTouch` - 触摸动画
- `SpringAnimation` - 弹簧动画
- `SpringEasing` - 弹簧缓动

### widget (控件模块)
**包名**: `miuix.widget`

MIUI 风格的自定义控件：
- 增强的基础控件
- 自定义对话框
- 弹出窗口

**主要类**:
- `ClearableEditText` - 带清除按钮的文本框
- `SeekingBar` - 自定义拖动条
- `PopupWindow` - 弹出窗口

## 文件说明

### 构建文件

- **build.gradle (根)** - 项目级构建配置，定义公共依赖和发布配置
- **settings.gradle** - 项目模块设置
- **gradle.properties** - Gradle 属性配置
- **build.gradle (模块)** - 各模块的构建配置

### 文档文件

- **README.md** - 项目介绍、安装指南、快速开始
- **LICENSE** - MIT 开源许可证
- **.gitignore** - Git 忽略规则
- **JITPACK.md** - JitPack 发布指南
- **EXAMPLES.md** - 详细使用示例
- **STRUCTURE.md** - 项目结构说明

### 源代码

- **core/src/main/java/** - 核心模块 Java 源代码
- **animation/src/main/java/** - 动画模块 Java 源代码
- **widget/src/main/java/** - 控件模块 Java 源代码

### 资源文件

- **res/values/** - 字符串、颜色、样式资源
- **res/layout/** - 布局文件
- **res/drawable/** - 可绘制资源

## 依赖关系

```
core (基础模块)
  └── 无内部依赖

animation (动画模块)
  └── 依赖 core 模块

widget (控件模块)
  └── 依赖 core 模块
```

## 发布流程

1. 更新版本号 (在根 build.gradle 中)
2. 提交代码到 Git
3. 创建 Git Tag (例如：v1.0.0)
4. 推送 Tag 到 GitHub
5. 创建 GitHub Release
6. JitPack 自动构建并发布

## 开发指南

### 添加新功能

1. 在对应模块的 `src/main/java/miuix/` 下创建新类
2. 编写单元测试
3. 更新 README.md 文档
4. 添加使用示例到 EXAMPLES.md

### 修复 Bug

1. 创建 Issue 描述问题
2. 创建分支修复问题
3. 编写测试用例
4. 提交 Pull Request

### 代码风格

- 遵循 Google Java Style Guide
- 使用 Kotlin 时遵循 Kotlin 代码规范
- 所有公共方法必须有 JavaDoc 注释
- 单元测试覆盖率至少 80%

## 测试

运行所有测试：
```bash
./gradlew test
```

运行特定模块测试：
```bash
./gradlew core:test
./gradlew animation:test
./gradlew widget:test
```

生成测试报告：
```bash
./gradlew jacocoTestReport
```

## 构建

调试构建：
```bash
./gradlew assembleDebug
```

发布构建：
```bash
./gradlew assembleRelease
```

## 相关资源

- [Gradle 用户指南](https://docs.gradle.org/)
- [Android 开发者文档](https://developer.android.com/)
- [Kotlin 文档](https://kotlinlang.org/)
- [JitPack 文档](https://jitpack.io/)
