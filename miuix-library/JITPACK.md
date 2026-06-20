# JitPack 发布指南

## 如何发布到 JitPack

### 1. 准备工作

确保你的项目已经托管在 GitHub 上：
- 创建一个 GitHub 仓库
- 将代码推送到仓库
- 创建一个 Release Tag (例如：v1.0.0)

### 2. 配置 build.gradle

根目录的 `build.gradle` 已经配置好了 Maven 发布插件。

### 3. 创建 Release

在 GitHub 上创建一个新的 Release：
1. 进入你的 GitHub 仓库
2. 点击 "Releases" → "Create a new release"
3. Tag version: `v1.0.0`
4. 填写发布说明
5. 点击 "Publish release"

### 4. 验证发布

访问 JitPack 验证你的库：
```
https://jitpack.io/#yourusername/miuix
```

### 5. 使用方式

用户在项目中添加：

**项目级 build.gradle:**
```gradle
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

**模块级 build.gradle:**
```gradle
dependencies {
    implementation 'com.github.yourusername.miuix:core:1.0.0'
    implementation 'com.github.yourusername.miuix:animation:1.0.0'
    implementation 'com.github.yourusername.miuix:widget:1.0.0'
}
```

## 自动化发布 (可选)

### 使用 GitHub Actions 自动发布

创建 `.github/workflows/release.yml`:

```yaml
name: Release to JitPack

on:
  release:
    types: [created]

jobs:
  release:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      
      - name: Set up JDK 11
        uses: actions/setup-java@v2
        with:
          java-version: '11'
          distribution: 'adopt'
          
      - name: Build with Gradle
        run: ./gradlew build
```

## 版本管理

### 版本号规则

遵循语义化版本控制 (Semantic Versioning):
- MAJOR.MINOR.PATCH (例如：1.0.0)
- MAJOR: 不兼容的 API 更改
- MINOR: 向后兼容的功能新增
- PATCH: 向后兼容的问题修复

### 快照版本

开发中的版本可以使用 SNAPSHOT:
```gradle
version = '1.0.1-SNAPSHOT'
```

## 常见问题

### Q: JitPack 构建失败怎么办？
A: 访问 https://jitpack.io/#yourusername/miuix 查看构建日志

### Q: 如何更新已发布的版本？
A: 删除旧 tag，创建新 tag，推送新的 release

### Q: 支持私有仓库吗？
A: 支持，需要配置认证 token

## 相关资源

- JitPack 官方文档：https://jitpack.io/
- GitHub Releases: https://docs.github.com/en/repositories/releasing-projects-on-github
- 语义化版本：https://semver.org/
