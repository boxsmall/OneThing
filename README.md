# 一件（OneThing）

「一件」是一款极简的单目标坚持工具：用户只设置一件每天要做的事，完成后轻点一次，产品负责记录、提醒，并展示坚持进度。

## 交互原型与 UI 设计

- [Figma：一件 完整交互原型 V1](https://www.figma.com/design/XEHhVWEe14BACPoGeKYD76/%E4%B8%80%E4%BB%B6_%E5%AE%8C%E6%95%B4%E4%BA%A4%E4%BA%92%E5%8E%9F%E5%9E%8B_V1?node-id=0-1&p=f&t=2BpdK8rB5ew8IMc2-0)
- UI 设计图导出与命名说明：[design/README.md](design/README.md)

> Figma 是当前 UI 与交互的设计源。开发实现以已确认的产品文档、交互清单和 Figma 原型共同为准；发生冲突时，应先完成产品确认并同步更新文档。

## 项目文档

| 文档 | 用途 |
| --- | --- |
| [V1 开发决策基线](docs/一件_APP_V1_开发决策基线.md) | 已锁定的工程、产品、设计、隐私与本地发布口径 |
| [产品需求文档 PRD V1.0](docs/一件_APP_产品需求文档_PRD_V1.0.md) | 产品范围、业务规则、页面与状态定义 |
| [开发任务拆解与验收用例 V1.0](docs/一件_APP_开发任务拆解与验收用例_V1.0.md) | 研发任务分解和功能级验收条件 |
| [前端调整与交互实现清单 V1.0](docs/一件_APP_前端调整与交互实现清单_V1.0.md) | 前端页面、组件、状态和交互实现依据 |
| [开发执行路径 V1.0](docs/一件_APP_开发执行路径_V1.0.md) | 从准备、开发到发布的执行顺序与交付物 |
| [验收测试方案 V1.0](docs/一件_APP_验收测试方案_V1.0.md) | 测试范围、环境、用例执行及准出标准 |
| [开发进度记录](docs/一件_APP_开发进度记录.md) | 当前已实现范围、构建版本、验证结果和后续工作 |
| [V1.1 品牌体验升级任务](docs/一件_APP_V1.1_品牌体验升级开发任务.md) | 目标图标、系统启动页和完成动效的开发与验收记录 |
| [V1.1.0 本地发布记录](docs/一件_APP_本地发布记录_V1.1.0.md) | V1.1 候选 APK、签名、哈希、体积与测试结果 |

当前开发分支为 `codex/v1.1-brand-experience`，应用版本为 `1.1.0 (2)`。V1.1 已完成 8 个目标图标、Room v1→v2 非破坏迁移、Android 系统启动页和 Logo 点亮完成动画，并已在 Xiaomi Android 14 实体机完成独立 Debug 主流程与正式签名 `1.0.0 → 1.1.0` 无清数据覆盖验收。

## V1 产品边界

- 同时只进行一个目标
- 每天完成一次，记录完成或未完成
- 每天最多一次提醒
- 完全不支持文字记录、备注、日记、图片或附件
- 暂不包含补签、登录注册、云同步、社区、排行榜、积分、会员和商城

## V1 开发基线

- 应用 ID：`com.boxsmall.onething`
- Android：`minSdk 26`、`compileSdk 36`、`targetSdk 36`
- 技术栈：Kotlin、Jetpack Compose、Room、DataStore
- 发布方式：本地签名 APK，不接入应用商店
- 数据策略：核心数据保存在本地，关闭云备份，仅允许设备到设备迁移
- 网络策略：不接入第三方 Analytics、广告、账号或 Crash SDK；Release 不声明 `INTERNET` 权限
- 开源协议：[Apache License 2.0](LICENSE)

## 本地开发

当前仓库已经是可构建的 Android 工程。使用 Android Studio 打开根目录，或在已配置 JDK 17+ 与 Android SDK 36 的终端执行：

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:lintDebug
```

Debug APK 输出到 `app/build/outputs/apk/debug/app-debug.apk`。本地开发进度和设备测试结果见[开发进度记录](docs/一件_APP_开发进度记录.md)。

本地 Release 使用仓库外固定密钥签名。首次在发布电脑上执行：

```powershell
.\scripts\Initialize-OneThingReleaseSigning.ps1
.\gradlew.bat :app:assembleRelease
.\scripts\Verify-OneThingRelease.ps1
```

密钥默认保存在当前 Windows 用户目录的 `.onething-signing` 中，不会写入仓库；必须把 `onething-release.jks` 与 `keystore.properties` 一起做离线备份。正式签名产物输出到 `app/build/outputs/apk/release/app-release.apk`。

## 建议阅读顺序

1. V1 开发决策基线
2. 产品需求文档
3. `design/screens/` 静态 UI 图与 Figma 完整交互原型
4. 前端调整与交互实现清单
5. 开发任务拆解与验收用例
6. 开发执行路径
7. 验收测试方案
