# 一件 APP 本地发布记录 V1.0.0

> 记录时间：2026-09-03 17:42:45 +08:00
> 最后更新：2026-09-04
> 发布渠道：本地签名 APK
> 当前状态：签名构建、静态门禁、真机核心闭环与 HyperOS 锁屏自然提醒通过；源码以 Release Candidate 标签归档，待签名密钥离线备份

## 1. 版本与源码状态

| 项目 | 值 |
| --- | --- |
| Application ID | `com.boxsmall.onething` |
| versionName | `1.0.0` |
| versionCode | `1` |
| minSdk / targetSdk | `26 / 36` |
| 归档父提交 | `75df9c147d54475b09d69cb49445da34711f3395` |
| 归档分支 | `codex/v1.0.0-rc1-archive` |
| 归档标签 | `v1.0.0-rc.1`；该标签指向包含本记录、完整 Android 源码和设计资源的归档提交 |

## 2. 固定签名

| 项目 | 值 |
| --- | --- |
| 密钥位置 | 当前 Windows 用户目录 `.onething-signing/onething-release.jks`，仓库外保存 |
| 配置位置 | 当前 Windows 用户目录 `.onething-signing/keystore.properties`，仓库外保存 |
| Alias | `onething-release` |
| 算法 | RSA 4096 / SHA256withRSA |
| 证书主题 | `CN=OneThing Local Release, OU=BoxSmall, O=BoxSmall, L=Local, ST=Local, C=CN` |
| 证书 SHA-256 | `87:75:39:D3:B7:B2:7E:E4:E1:C3:DF:11:C6:25:A2:AF:A2:D8:B8:A7:CC:B7:63:EF:31:DF:1D:D8:97:71:5F:C0` |
| 有效期至 | 2054-01-19 |
| APK 内签名 | v2、v3 验证通过；minSdk 26 不需要旧式 v1/JAR 签名 |
| 安装侧车 | 生成 `app-release.apk.idsig`；普通本地 APK 安装不依赖该文件 |

密钥文件和密码配置必须作为一个整体离线备份。丢失其中任意一个，都无法为同一 Application ID 生成可覆盖升级的后续版本。密码不得复制到本文、聊天、Git、Issue 或构建日志。

## 3. 构建产物

### 签名 Release APK

- 路径：`app/build/outputs/apk/release/app-release.apk`
- 大小：1,561,325 bytes
- SHA-256：`6EEE867A481C88FB83765AEEF8BC28966E8C292E044C53CEB6D29667BEEE4064`

### V4 安装签名侧车

- 路径：`app/build/outputs/apk/release/app-release.apk.idsig`
- 大小：19,020 bytes
- SHA-256：`B19648F05AFC30A3592DEE96953DCEEA0C369CD7A88D157FFDDC4A73BE7E66A2`

## 4. 已通过门禁

- Gradle 已从仓库外配置加载固定签名；四个字段只要出现部分缺失，配置阶段立即失败。
- 签名初始化脚本使用随机 256 位密码，并拒绝覆盖已有密钥。
- `apksigner verify --verbose --print-certs` 通过，证书和 APK SHA-256 已记录。
- Release 包经过 R8 和资源压缩，`debuggable` 未启用。
- Release Manifest 不包含 `INTERNET`、精确闹钟或无关敏感权限。
- Release 不包含 Debug 专用 `TestHostActivity`；`ReminderReceiver` 保持 `exported=false`。
- 当前自动化基线：JVM 35/35、Android 14 隔离 Debug 真机 32/32、API 36 模拟器 32/32、Lint 0 errors。
- 最低与最高目标系统矩阵已补齐：API 26、API 36 各完成 30/30 设备测试及签名 Release 创建、完成、冷启动持久化冒烟。
- 11 个核心页面/状态已在精确 `360 × 800dp` 视口与 `design/screens/` 生成同画布对比并逐页复核；结果 11/11 通过，证据位于 `build/ui-audit-360x800-2026-09-03/`。
- A1 图标已在 API 36 Pixel Launcher 应用抽屉以圆形 mask 实际渲染，品牌图形与安全区正常；OEM 方圆形 mask 和固定桌面的主题图标仍待实体设备签字。
- Android 12+ 设备迁移规则已补齐 `files/datastore/`，并由 2 项设备测试和打包资源反编译共同确认；云备份仍完整关闭。真实 Setup Wizard 双设备传输尚待人工验收。
- 当前哈希对应的新签名 Release 已在 API 36 模拟器完成全新安装和冷启动欢迎页冒烟；相较前一候选，除设备迁移 XML 修正外，还包含小米系“系统提醒保障”入口与说明弹窗。
- 当前哈希对应的新签名 Release 已在 Xiaomi Android 14 真机同签名覆盖安装；用户目标“吃饭”、9 月 3 日和 9 月 4 日完成记录、“今天已完成”和“连续第 2 天”保持不变，未执行清数据、结束目标或新建目标。
- Git 归档前清理了源码文件末尾空白并重新编译，因此 APK 哈希由早期 HyperOS 候选的 `10C084...` 更新为本记录所列 `6EEE867...`；最终哈希包已再次覆盖安装，正式目标、完成态、后台权限和次日 Alarm 均保留。
- 后续真机破坏性回归使用独立 `com.boxsmall.onething.debug`（“一件 Debug”）包，避免接触正式版数据；该变体配置不进入 Release。Debug 与 AndroidTest 已在 Xiaomi Android 14 上安装并完成 32/32 回归，正式版保持共存且未被测试改写。
- HyperOS 首轮真实到点测试发现 PowerKeeper 会把非精确 Alarm 推后数天，仅开启自启动仍不足。Debug 同时开启自启动、后台运行和无限制省电后，`10:00` 锁屏自然提醒在 `10:00:31` 发布，触发后正确续排到次日 `10:00` 且 `power_pending=--`；真实点击通知进入首页并自动清除。应用保留非精确 Alarm、无精确闹钟特殊权限的既定方案。
- 当前哈希对应候选已通过 JVM 35/35、Lint 0 errors、Debug/Test/Release 构建、Xiaomi Android 14 32/32 设备回归、锁屏自然提醒、通知点击，以及正式版覆盖安装与数据保留复核。
- 经设备持有人明确授权，Xiaomi 正式版已开启自启动并把省电策略设为无限制；系统界面确认两项为选中，`MIUIOP(10008)` 与 `RUN_ANY_IN_BACKGROUND` 均为 `allow`，待机桶为 `5`。重新调度的 `2026-09-05 20:00` 单次非精确 `RTC_WAKEUP` 为 `power_pending=--`，没有 HyperOS 延期标记。
- Git 归档以 `main@75df9c1` 为父提交，在独立分支 `codex/v1.0.0-rc1-archive` 保存，并以 `v1.0.0-rc.1` 标识；不改写 `main` 历史。归档范围包含 Android 工程、测试、设计源资源和全部同步文档，不包含构建缓存、APK、用户数据或签名材料。
- 仓库忽略 `*.jks`、`*.keystore` 和 `keystore.properties`；实际密钥与密码配置均位于仓库外。
- Xiaomi `2106118C`（Android 14）已卸载 Debug 签名版本并首次安装正式签名 Release；包版本为 `1.0.0 (1)`，应用未启用 `debuggable`。
- Release 首次冷启动、欢迎、创建、首页、完成、杀进程重启、记录、设置、结束确认、结束结果和历史归档均通过。
- 使用同一 APK 执行 `adb install -r` 覆盖安装成功；覆盖后 Room 目标和 DataStore 首次引导状态均保留，完成状态在后续冷启动中继续保留。
- 真机测试结束后已清除临时目标和历史；手机保留签名 Release，停在干净欢迎页，通知权限未授予，当前 Alarm 批次无 OneThing 待触发任务，AndroidTest 辅助包不存在。

## 5. 门禁状态

- [x] 设备持有人确认卸载当前 Debug 签名版本并安装正式签名 Release。
- [x] Release 首次冷启动、创建、完成、记录、设置、结束和历史闭环通过。
- [x] 使用同一正式签名覆盖安装 Release，确认 Room 与 DataStore 数据保留。
- [x] HyperOS Debug 隔离包完成“自启动 + 无限省电”锁屏真实到点、次日续排和通知点击验证。
- [x] 当前哈希 Release 覆盖安装后验证提醒保障入口、弹窗和系统应用信息跳转。
- [x] 设备持有人授权后为 Xiaomi 正式版开启自启动与无限制省电，并确认次日 20:00 Alarm 无延期标记。
- [x] 归档与产物完全对应的 Git commit，并使用 `v1.0.0-rc.1` 标签建立可追溯引用。
- [ ] 将 `onething-release.jks` 与 `keystore.properties` 一起复制到用户选定的离线安全介质，并验证备份可读取。

## 6. 标准命令

```powershell
.\scripts\Initialize-OneThingReleaseSigning.ps1
.\gradlew.bat :app:testDebugUnitTest :app:lintDebug :app:assembleRelease
.\scripts\Verify-OneThingRelease.ps1
```

`Initialize-OneThingReleaseSigning.ps1` 只在首次建立发布密钥时执行；发现目标文件已存在会停止，不得删除旧密钥后重新生成。
