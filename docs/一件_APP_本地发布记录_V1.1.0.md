# 一件 APP 本地发布记录 V1.1.0

> 记录日期：2026-09-05
> 发布渠道：本地签名 APK
> 当前结论：非真机开发与验收完成；实体手机安装和正式覆盖升级按用户要求暂缓

## 1. 版本与源码

| 项目 | 值 |
| --- | --- |
| Application ID | `com.boxsmall.onething` |
| Debug ID | `com.boxsmall.onething.debug` |
| 版本 | `1.1.0 (2)` |
| 分支 | `codex/v1.1-brand-experience` |
| 实现提交 | `31e9d32`（`feat: deliver V1.1 brand experience upgrade`） |
| V1 归档标签 | `v1.0.0-rc.1` 继续指向 `65314617803b4a8b95b846963a8389e6afffdd53`，未移动 |
| 构建时间 | 2026-09-05 12:20:43（Asia/Shanghai） |

实现提交包含目标图标、Room schema v2、系统 SplashScreen、4 个 Lottie 资源、Compose 接入、自动化测试、启动页图和视觉 QA 证据。随后仅补充同步文档，不改变 APK 输入代码与资源。

## 2. Release 产物

### APK

- 路径：`app/build/outputs/apk/release/app-release.apk`
- 大小：1,728,765 bytes
- SHA-256：`5B60C4DD1740F14047EA36C4DFF1A9AB125B1ACDA068BDE9D0F85E1768748C0E`

### V4 侧车

- 路径：`app/build/outputs/apk/release/app-release.apk.idsig`
- 大小：23,116 bytes
- SHA-256：`53D59B2D09E7CB501D59F2261FC1B92ED1456D926BB6E78A6A9027985AD90CF5`

### 体积变化

- V1.0.0 最终归档 APK：1,561,325 bytes。
- V1.1.0 当前 APK：1,728,765 bytes。
- 增加：167,440 bytes，约 10.72%。
- 主要来源：Lottie Compose 运行时和 4 个完成动画 JSON；8 个目标图标使用 Vector Drawable，没有复制多密度 PNG。

## 3. 签名验证

- 使用 V1.0.0 同一仓库外固定密钥构建，未生成或替换签名密钥。
- `apksigner verify --verbose --print-certs`：通过。
- APK Signature Scheme v2：通过。
- APK Signature Scheme v3：通过。
- 签名者：1。
- 密钥：RSA 4096。
- 证书 SHA-256：`87:75:39:D3:B7:B2:7E:E4:E1:C3:DF:11:C6:25:A2:AF:A2:D8:B8:A7:CC:B7:63:EF:31:DF:1D:D8:97:71:5F:C0`。

## 4. 权限与发布边界

打包 Manifest 只包含：

- `android.permission.POST_NOTIFICATIONS`；
- `android.permission.RECEIVE_BOOT_COMPLETED`；
- AndroidX 自动生成的 `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`。

确认不包含 `INTERNET`、精确闹钟、定位、存储、相机、麦克风或其他无关敏感权限；不包含第三方 Analytics、Crash、广告或账号 SDK。Release 不包含 Debug `TestHostActivity`，也不存在独立 `SplashActivity`。

## 5. 自动化与构建结果

| 门禁 | 结果 |
| --- | --- |
| JVM 单元测试 | 37/37 通过 |
| API 26 Android 设备测试 | 45/45 通过，0 failure / 0 error / 0 skipped |
| API 36 Android 设备测试 | 45/45 通过，0 failure / 0 error / 0 skipped |
| Lint Debug | 0 error；10 条仅为 SDK/依赖新版本提示 |
| Debug APK | 构建成功 |
| AndroidTest APK | 构建成功 |
| Release APK | R8、资源压缩、Lint Vital 与固定签名构建成功 |

设备测试覆盖 Room 1→2 真实迁移、图标持久化、创建与设置选择、历史展示、大字体和 600dp、动画资源解析、单次触发、数据先保存、返回中断、进程重建不重播、资源失败降级、移除动画降级、TalkBack 单次语义、提醒与通知路由。

## 6. 启动页与视觉验收

- API 26：冷启动成功（661ms）、热启动成功（2ms），深色系统主题与旋转后 Activity 保持有效。
- API 36：冷启动成功（模拟器本轮 4,867ms）、热启动成功（28ms），深色系统主题与旋转后 `MainActivity` 保持前台；冷启动耗时包含无窗口模拟器启动后的系统负载，不是人为 Splash 延时。
- Manifest 与运行时确认只使用 `MainActivity + Theme.OneThing.Starting`，无双 Splash Activity。
- 启动页设计输出：`design/screens/00 启动页.png`。
- 完成动画权威稿、API 36 当前实现和并排对照保存在 `design/qa/v1.1/`。
- 根目录 `design-qa.md` 结论：`final result: passed`，P0/P1/P2 均为 0。

## 7. 动效资源

| 文件 | 大小 | SHA-256 |
| --- | ---: | --- |
| `complete_success.json` | 9,399 bytes | `EF64FD818DAEE42ACEF52E0C4194F4FE9BF6F15BABC0EE564182170C0DFE9CB9` |
| `complete_streak_3.json` | 10,312 bytes | `8AE907155FEA29510DC1CF821A16EF2D2B37BBA6100ECAEFED00E9241B4BBF23` |
| `complete_streak_7.json` | 11,200 bytes | `46CC09AA384DC5C8F885B381D3EB8D2454619A0AD56A140F24D3FE3E1F08F9E2` |
| `complete_streak_30.json` | 12,985 bytes | `ABC3DE5AA2865098F5BAE6B5924CE85E8369C289CDFF476DBAAC0B1622904D6B` |

4 个文件均为 1080×1080、60fps、108 帧（1.8 秒）、不循环、无外部图片、无文字图层并通过 Lottie 实际解析测试，远低于单文件 200KB 建议上限。

## 8. 暂缓项目

根据用户明确要求，本轮不连接或操作实体手机。因此以下两项不是本地代码缺陷，也没有被描述为已完成：

- Xiaomi Android 14 Debug 实体机回归；
- 使用 V1.0.0 同一正式签名在实体手机执行 `1.0.0 → 1.1.0` 覆盖安装并人工确认原目标、Completion、提醒和厂商后台权限。

上述路径已有 Room 迁移、数据库重开、进程重建、提醒路由和双 API 模拟器自动化证据；需要发布到实体手机时，再执行一次不清数据的最终签字即可。

## 9. 标准复核命令

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:lintDebug :app:assembleDebug :app:assembleAndroidTest :app:assembleRelease
.\gradlew.bat :app:connectedDebugAndroidTest
.\scripts\Verify-OneThingRelease.ps1
```
