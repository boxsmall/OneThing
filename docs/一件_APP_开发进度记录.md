# 「一件」APP 开发进度记录

> 首次开发日期：2026-09-02
> 当前阶段：M0 工程基线、M1 核心纵向切片完成，M2 适配与发布准备进行中
> 当前版本：`1.0.0 (1)`

## 1. 本轮已经落地

### 1.1 工程与构建

- 建立单模块 Android 工程和 Gradle Wrapper。
- Release 应用 ID与 namespace 为 `com.boxsmall.onething`；Debug 使用 `com.boxsmall.onething.debug` 与正式版隔离共存，代码 namespace 保持不变。
- 固定 `minSdk 26`、`compileSdk 36`、`targetSdk 36`、JVM/Kotlin Target 17。
- Debug 和 Release 构建类型已建立；Release 开启 R8 与资源压缩，正式签名仍按发布阶段从仓库外注入。
- Release Manifest 不声明 `INTERNET` 权限。
- 云备份关闭，Android 12+ 设备到设备迁移仅包含 Room 数据库和 DataStore 设置。
- 添加 Apache-2.0 `LICENSE`、Android/Gradle `.gitignore` 和 Room Schema 导出目录。

### 1.2 当前依赖锁定

| 组件 | 版本 | 说明 |
| --- | --- | --- |
| Android Gradle Plugin | 9.4.0 | 使用 AGP 9 内置 Kotlin |
| Gradle Wrapper | 9.6.0 | 与 AGP 9.4.0 基线匹配 |
| Kotlin / Compose Compiler Plugin | 2.3.21 | Compose 编译插件与 Kotlin 同版本 |
| Compose BOM | 2026.06.01 | Compose UI 1.11.4，仍兼容 compileSdk 36 |
| Room | 2.8.4 | KSP 2.3.11 生成代码并导出 Schema |
| DataStore | 1.2.1 | 保存欢迎状态和提醒设置 |
| Lifecycle | 2.10.0 | 2.11.0 要求 compileSdk 37，按已锁定 API 36 回退 |
| Core KTX | 1.18.0 | 1.19.0 要求 compileSdk 37，按已锁定 API 36 回退 |
| Navigation Compose | 2.9.8 | 已纳入版本目录，页面状态当前先使用轻量内部导航 |

### 1.3 数据与业务

- `GoalEntity` 使用可空唯一 `activeSlot` 索引，从数据库层保证最多一个进行中目标。
- `CompletionEntity` 使用 `(goalId, dateEpochDay)` 唯一索引，保证同一目标同一天最多完成一次。
- 已实现创建目标、完成今天、修改当前目标名称、结束目标、同日结束旧目标并创建/完成新目标的事务方法。
- 修改当前目标名称只更新 ACTIVE Goal 的名称，不改变 goalId、startDate、完成记录或统计。
- 目标名执行 trim、单行、非空、最多 20 个用户可见字符校验，组合 Emoji 按一个字符处理。
- 总完成天数、当前连续、最长连续均从 Completion 实时计算。
- 已实现 ACTIVE_UNDONE、ACTIVE_DONE、ACTIVE_INTERRUPTED 三态判定；gapDays 从昨天向前连续统计漏做日期，最早到 startDate。
- 七日窗口固定为过去 6 天加今天；目标开始日前显示中性状态。
- Room 保存目标和完成记录；DataStore 保存欢迎完成状态、提醒开关和时间。
- `AnalyticsTracker` 接口已保留，当前只注入 `NoOpAnalyticsTracker`。

### 1.4 提醒

- 默认开启并使用 20:00；设置页可开关提醒并通过 24 小时时间选择器修改时间。
- 修改提醒时间后 DataStore 更新会触发调度器先取消旧任务，再按新时间建立单次非精确提醒。
- Android 13+ 在创建首个目标且提醒开启后请求通知权限；拒绝后自动关闭提醒、保留用户选择的提醒时间，并提供应用系统设置入口。
- 使用 `AlarmManager.setAndAllowWhileIdle` 的单次非精确提醒，不申请精确闹钟权限。
- 当日时间已过时顺延到次日；每次触发后重新安排下一次提醒。
- 当天提前完成时取消当天提醒并直接安排次日提醒；关闭提醒或结束目标后取消全部未来计划。
- 接收器真正发出通知前重新读取提醒开关、当前目标和今日完成状态，避免竞态导致无效通知。
- 日期跨天、设备重启、应用更新、手动改时间或切换时区后重新计算计划。
- 通知不包含目标名，锁屏可见性为 `VISIBILITY_PRIVATE`。

### 1.5 UI 纵向切片

已接入以下可交互页面或状态：

- 欢迎页；
- 创建目标页；
- 首页未完成和已完成状态；
- 中断后首页；漏一天显示“昨天没有完成 / 今天继续”，多天显示“有几天没有继续了 / 今天回来就好”，不使用红色失败提示且不突出连续 0 天；
- 过去 6 天加今天的七日进度；
- 当前目标完整月历，周一为一周起点，支持向前切月和返回当前月前的月份；已完成、漏做、今天与非目标日期使用不同状态；
- 完成成功 Overlay，落库后显示，约 1.2 秒自动关闭且支持点击关闭；
- 设置页、当前目标改名、24 小时提醒时间选择和提醒开关；
- 结束目标确认；
- 结束结果页，展示目标名称、持续天数与完成天数；零完成时使用“这段记录已保存”；
- 无当前目标时开始下一件事，并清除结束结果流程；
- 历史目标列表、持续天数、完成天数和最长连续；从设置进入历史时返回设置，从结束结果进入时返回结束结果，从创建页进入时返回创建页。
- “关于一件”与离线隐私页面，展示动态版本号、本地数据口径、设备到设备迁移范围、卸载行为、Apache-2.0 和 GitHub 项目地址。
- 内部页面已接入统一系统返回分发：记录和设置返回首页、关于返回设置、历史返回来源页、结束结果返回无 ACTIVE 的创建状态；完成 Overlay 优先响应返回并关闭。
- 当前日期由 ViewModel 统一提供；应用回到前台、前台跨午夜以及系统日期/时间/时区改变时刷新首页、七日条、月历和统计。

当前主题已使用设计确认色值、固定浅色模式、系统安全区和至少 48dp 的主要点击区域。Launcher Icon 已替换为 A1 正式品牌 `1.`：黑色数字、品牌黄圆点和暖白背景；前景缩放至 Adaptive Icon 安全区，并为 Android 13+ 提供独立 monochrome 图层。由于 Lint 不识别基础 Adaptive Icon 与 `mipmap-anydpi-v33` 覆盖组合，只在两个基础 Launcher XML 上定向抑制已核实的 `MonochromeLauncherIcon` 误报，其他检查保持启用。品牌黄 `#FABF45` 与 UI 待完成状态黄 `#F5B838` 分开管理。8 类卡通目标图标未接入 V1。

### 1.6 2026-09-03 首批设计稿对齐

- 欢迎页按 `01 欢迎.png` 重构为 `1.`、`一件`、`一次，只坚持一件事。` 和单一 `开始使用` 操作，移除旧版扩展宣传文案。
- 创建页按 `02 创建目标.png` 调整标题、辅助文案、示例和 `开始坚持` 主操作；同时落实产品清单中高于静态图的提醒需求：默认开启、默认 `20:00`、可在创建前关闭或修改时间。
- 创建目标操作改为先持久化提醒开关与时间，再创建 Goal；因此关闭提醒时不会因 Active Goal 出现而申请 Android 13+ 通知权限，开启时仍沿用既有授权流程。
- 提醒区域使用整行单一 `toggleable` 语义和只读视觉 Switch，避免父容器与内部 Switch 同时响应导致一次点击切换两次；整行仍保留大于 48dp 的操作热区。
- 首页未完成、已完成和中断三态按 `03`、`05`、`07` 设计稿改为单一视觉焦点：顶部本地日期、今日任务、连续信息、7 日状态点、底部完成/记录入口；未完成使用品牌黄，已完成使用完成绿。
- 首页不再常驻三项统计看板；完整统计继续保留在坚持记录页。中断页继续保留 `gapDays=1` 与 `gapDays>1` 两套温和文案，不突出连续 0 天。
- 欢迎页、创建页和首页主体使用最大宽度 `520dp` 的居中容器；纵向内容允许滚动，防止窄屏、横屏或系统大字体下底部操作被裁切。系统安全区处理保持不变。
- 新增创建页 Compose 仪器测试，覆盖默认 `20:00`、关闭提醒后时间控件消失，以及提交结果携带 `reminderEnabled=false` 和原时间值。

### 1.7 2026-09-03 第二批设计稿与自适应实现

- `04 完成反馈` 改为深色半透明遮罩上的白色成功卡片，展示绿色对勾、`太棒了！`、`今天完成`、`连续第 N 天` 和 `明天继续。`；仍为首页内 Overlay，保留约 1.2 秒自动关闭、点击关闭和系统返回优先关闭。
- `06 坚持记录` 使用最大 `520dp` 的居中内容区；漏做日期改为中性空心状态，今天使用完成绿描边，不使用红色失败表达。
- `08 设置` 改为独立白色圆角卡片列表，覆盖当前目标、提醒时间、提醒开关、历史、结束目标、关于和返回；短高度下使用可滚动列表保证所有入口可访问。
- `09 结束确认` 由平台 `Dialog` 改为应用 Scaffold 内的全屏确认层，保持暖白背景与系统栏一致，展示目标、持续天数、累计完成、最长连续、继续坚持和危险操作；返回键先关闭确认层。
- `10 结束结果` 与 `11 我的历史` 完成居中宽度、滚动和大字体适配；历史卡片把持续/完成与最长连续分行展示，并根据入口显示 `返回设置`、`返回结束结果` 或 `返回创建目标`。
- 欢迎、创建、首页、完成 Overlay、结束确认和结束结果修正 Compose 宽度约束顺序：先应用 `widthIn`，再在受限宽度内 `fillMaxWidth`，使 520dp/420dp 最大宽度在横屏和宽屏上真实生效。
- 新增 `AdaptiveLayoutTest` 8 项，覆盖 320×480dp + 1.5 倍字体、360×320dp 短高度、记录、设置、历史、结束结果、结束确认和完成 Overlay；底部操作通过滚动仍可访问，历史长列表和大字体不裁切。

### 1.8 2026-09-03 P1 完成反馈动效与触觉

- 首页两个未完成分支的 `我完成了` 主按钮加入按压缩放反馈：按下目标比例 `0.97`，使用 140ms 补间恢复，不改变按钮布局尺寸或 48dp 以上热区。
- 轻触觉放在 Completion 成功落库并显示反馈层之后触发，使用 Compose `HapticFeedbackType.Confirm`；数据库写入失败或重复完成时不会产生虚假的成功触觉。
- 完成反馈采用 180ms 遮罩透明度过渡和 240ms 卡片透明度、`0.94 → 1.0` 缩放及 24px 上浮组合；自动展示 960ms 后先播放 240ms 退出，再从页面状态移除，总时长约 1.2 秒。
- 点击反馈层会复用同一退出流程，避免立即移除导致退出动画不可见；并使用状态门控保证自动关闭和点击关闭竞争时只调用一次 `onDismiss`。
- 完成反馈层新增 `completion-overlay` 测试标识及完整朗读描述，为后续 TalkBack 专项测试提供稳定语义节点。
- 新增 `CompletionFeedbackTest` 3 项：完成按钮单次回调、点击反馈层等待退出后单次关闭、自动展示结束后单次关闭。

### 1.9 2026-09-03 TalkBack 语义与 600dp/2.0 倍字体适配

- 创建目标输入框新增 `目标名称，最多 20 个字符` 语义；提醒时间按钮使用 `提醒时间，HH点mm分`，设置页 Switch 使用 `提醒通知`，避免只朗读输入示例、时间数字或无上下文的开关状态。
- 首页 7 日进度合并为一个稳定的朗读节点，按日期依次说明 `目标开始前`、`已完成`、`未完成`、`今天` 等状态，不再只依赖黄色/绿色圆点传达信息。
- 月历的每个有效日期增加 `M月d日 + 今天/已完成/未完成/未来日期/目标开始前/目标结束后` 状态描述；三项统计改为 `标签，数值` 的朗读顺序。
- 页面返回箭头统一标记为 `返回`，月份箭头标记为 `上个月` / `下个月`；视觉箭头从语义树中隐藏，避免 TalkBack 朗读为无意义符号。GitHub 卡片增加 `在浏览器中打开 GitHub 项目` 的按钮语义。
- 已完成首页的视觉对勾增加 `完成` 描述；设置卡片和 GitHub 卡片的装饰箭头不再成为独立朗读内容。
- `关于一件` 页面补齐与其他内部页一致的最大 `520dp` 居中容器，并保留长内容纵向滚动；欢迎、设置、关于页面增加仅用于布局回归的稳定测试标识。
- 新增 `AccessibilityLayoutTest` 6 项，覆盖 600dp 欢迎/关于居中宽度、320×480dp + 2.0 倍字体下设置入口可达，以及创建控件、7 日进度、返回/月历/统计、设置开关/GitHub 的无障碍语义。

### 1.10 2026-09-03 提醒系统设备级自动化

- `ReminderReceiver` 的核心执行流程拆为同模块可测试入口：每次触发都读取最新提醒设置、ACTIVE 目标、当前本地日期和通知权限，再决定是否发通知，并在末尾重新登记下一次提醒；正式接收器仍保持 `exported=false`，没有增加外部可调用的调试广播。
- `ReminderScheduler.applySettings` 返回本次真实系统登记结果；提醒关闭或没有 ACTIVE 目标时，在取消 Alarm 后同步取消对应 `PendingIntent`，设备测试可以同时核对业务决定和 `dumpsys alarm` 的实际系统状态。
- 通知内容入口增加唯一 Action，并使用 `CLEAR_TOP | SINGLE_TOP` 复用应用任务。`MainActivity.onNewIntent` 把通知入口转换为页面重置事件；即使用户原先停在设置、记录等内部页，点击提醒也会回到首页；没有 ACTIVE 目标时沿用首页根路由进入创建页。
- `BootReceiver` 继续只接受 `BOOT_COMPLETED`、`MY_PACKAGE_REPLACED`、`DATE_CHANGED`、`TIME_SET` 和 `TIMEZONE_CHANGED`，其他 Action 不触发重排；时区变化后的下一次提醒按新时区的本地时分重新计算。
- 新增 `ReminderSystemDeviceTest` 4 项，覆盖未完成目标发通知并登记 Alarm、当天完成后不发通知且跳到次日、关闭提醒取消 Alarm，以及五类系统广播的重排入口；通知标题、正文、ContentIntent、Alarm 注册和取消均读取 Android 14 真机系统状态断言。
- 新增通知入口路由设备测试 1 项，在 Debug 专用 `TestHostActivity` 中使用真实 `AppContainer`、Room、DataStore 和 `MainViewModel`，覆盖 `设置 → 通知入口 → 首页` 及 `无 ACTIVE → 通知入口 → 创建页`；Release 不包含测试宿主。

### 1.11 2026-09-03 本地 Release 签名准备

- Gradle 新增仓库外 Release 签名入口：优先读取 `ONETHING_*` 环境变量，否则读取当前 Windows 用户目录 `.onething-signing/keystore.properties`；签名字段部分缺失时在配置阶段立即失败，完整缺失时仍允许生成开发用未签名 Release。
- 新增 `scripts/Initialize-OneThingReleaseSigning.ps1`：生成 RSA 4096 / SHA256withRSA 固定密钥和随机 256 位密码，默认保存在用户目录 `.onething-signing`；脚本发现密钥或配置已存在时拒绝覆盖，密码不输出到终端。
- 新增 `scripts/Verify-OneThingRelease.ps1`：自动定位 Android SDK Build Tools，调用 `apksigner verify --verbose --print-certs`，并输出 APK 大小与 SHA-256。
- 当前电脑已生成首个固定 Release 密钥；证书 SHA-256 为 `87:75:39:D3:B7:B2:7E:E4:E1:C3:DF:11:C6:25:A2:AF:A2:D8:B8:A7:CC:B7:63:EF:31:DF:1D:D8:97:71:5F:C0`，有效期至 2054-01-19。密钥和密码配置均在仓库外，后续版本必须复用。
- 已生成签名 `app-release.apk` 并通过 v2/v3 签名验证；Release 无 `INTERNET`、无 Debug 测试宿主，`ReminderReceiver` 仍不可导出。详细产物与门禁记录见《一件_APP_本地发布记录_V1.0.0.md》。

## 2. 已执行验证

验证环境：Windows 11、Android Studio Quail 4、JBR 25.0.3、Android 16 / API 36 x86_64 AVD `OneThing_API_36`。

| 验证项 | 结果 |
| --- | --- |
| `:app:assembleDebug` | 通过，生成 Debug APK |
| `:app:assembleRelease` | 通过 R8、资源压缩和 Lint Vital，生成未签名 Release APK |
| `:app:testDebugUnitTest` | 通过，共 34 个 JVM 测试 |
| `:app:assembleDebugAndroidTest` | 通过 |
| `:app:connectedDebugAndroidTest` | 通过，共 7 个 API 36 设备测试 |
| `:app:lintDebug` | 通过；仅保留 compile/target 36 和兼容依赖版本相关提示 |
| A1 Launcher 资源检查 | 通过；Adaptive 前景已按 `0.85` 缩放进入安全区，Android 13+ monochrome 独立配置，设计资源可由 AAPT2 编译 |
| Play 商店备用图检查 | 通过；`512×512`、完整不透明方图、无预制圆角或外部阴影；当前本地发布不使用该文件 |
| Debug / Release APK 权限检查 | 通过；仅包含通知、开机恢复提醒及 AndroidX 自动生成的动态接收器权限，不包含 `INTERNET` |
| API 36 安装 | 通过 |
| 冷启动 Activity | `Status: ok`，未发现 OneThing `AndroidRuntime` 崩溃 |
| API 36 设置交互冒烟 | 通过；创建目标后打开设置，目标改名、提醒时间改为 21:00、进程重启持久化、设置进入历史再返回设置均符合预期 |
| API 36 完成反馈冒烟 | 通过；完成写入后约 2 秒检查时 Overlay 已关闭并显示“今天完成了”状态 |
| API 36 记录页冒烟 | 通过；主页“记录”进入当前目标月历，2026 年 9 月可切换到 8 月并返回当前月，三项统计与返回首页入口可访问 |
| API 36 结束生命周期冒烟 | 通过；零完成结束文案、结果进入历史再返回结果、开始下一件事、创建页历史弱入口均符合预期 |
| API 36 中断首页 Compose 测试 | 通过；分别验证 gapDays=1 和 gapDays>1 文案，并断言页面不展示“当前连续” |
| API 36 关于/隐私冒烟 | 通过；动态版本、完全离线说明、隐私说明、Apache-2.0、GitHub 与返回设置入口均可访问 |

设备测试覆盖：数据库唯一进行中目标、每日完成唯一性、同日结束旧目标并完成新目标、修改 ACTIVE 目标后保持 goalId/startDate/Completion、Android 运行时的 Emoji 可见字符分段，以及漏一天/多天两种中断首页 Compose 呈现。JVM 测试覆盖：空统计、今天/昨天连续、断档、最长连续、ACTIVE 三态与 gapDays、七日窗口、周一起始月历网格、目标名 trim/换行/Emoji/长度校验、提醒当日/次日调度。

无窗口 AVD 在首次冷启动时仍出现一次 System UI 自身 ANR；选择等待后 OneThing Activity 恢复为前台并完成上述交互冒烟，未发现应用 `AndroidRuntime` 崩溃。该现象仍记录为宿主模拟器图形/系统进程问题，不计为完整视觉验收；后续 UI 精调必须在有窗口 AVD 或真机重新截图比对。冒烟结束后已清除临时目标数据并关闭模拟器。

### 2.1 2026-09-03 Android 14 真机验证

验证设备：Xiaomi `2106118C`（设备代号 `odin`），Android 14 / API 34，安全补丁 `2024-08-01`，物理分辨率 `1080×2400`，密度 `440dpi`，`zh-CN`。安装包为 Debug `1.0.0 (1)`，`targetSdk 36`。

| 验证项 | 结果 |
| --- | --- |
| USB 安装、首次冷启动 | 通过；APK 安装成功，欢迎页完整显示，未发现 OneThing `AndroidRuntime` 崩溃或 ANR |
| 创建目标与通知授权 | 通过；空目标不可提交，输入后字符计数和提交状态正确，Android 14 通知权限弹窗正常 |
| 首页未完成/已完成状态 | 通过；7 日条、三项统计和底部操作完整显示；完成后 Overlay 正常出现并自动消失，统计更新为 1 天且当天不可重复完成 |
| 记录月历 | 通过；当前月、上月切换和返回当前月正常，完成后累计/当前连续/最长连续均为 1 天 |
| 设置与持久化 | 通过；目标改名、24 小时制提醒时间调整为 21:30、提醒开关关闭/恢复、进程强制停止后名称和时间保留 |
| AlarmManager 联动 | 通过；提醒开启时登记 `ReminderReceiver` 的 21:30 `RTC_WAKEUP`，关闭提醒或结束目标后取消，创建下一目标后恢复 |
| 通知拒绝恢复流程 | 通过；拒绝后自动关闭提醒且保留 21:30，应用说明弹窗和“一件”应用信息入口正确；恢复权限后可重新开启提醒 |
| 结束、结果和历史 | 通过；取消结束、确认结束、`1 天里，完成了 1 天`、历史归档、结果页进入历史再返回结果、开始下一件事均正常 |
| 关于与离线隐私 | 通过；版本、完全离线、迁移/卸载说明、Apache-2.0 和 GitHub 信息在真机上完整显示 |
| 设备仪器测试 | 最终复测 7 项全部通过：数据库与运行时约束 5 项、Compose 中断首页 2 项；AndroidJUnitRunner 输出 `OK (7 tests)`，耗时 3.516 秒 |
| 实际定时通知到达 | 未完成；已确认权限、通知设置和系统闹钟登记，未修改真机系统时间，也未绕过 `exported=false` 的接收器安全边界 |

真机发现的内部页面系统返回缺陷已经在代码中修复并完成同一 API 34 真机最终回归：轻量 `AppPage` 页面栈已接入统一 `BackHandler`，并为来源感知历史页建立纯逻辑回退测试。HyperOS 的 USB 安装提示会在 3 秒倒计时结束后自动选择“拒绝”，因此 ADB 会显示 `INSTALL_FAILED_USER_RESTRICTED`；开启“USB安装”和“USB调试（安全设置）”后，在提示页选择“继续安装”即可正常安装，不属于 APK 签名或 Android 用户策略问题。

### 2.2 2026-09-03 P0 可靠性推进

- 新增统一 `AppPage` 系统回退策略及 3 个 JVM 用例；HOME 交给系统退出，其他内部页按产品来源回退。
- 新增生命周期日期刷新：`RESUMED`、本地午夜、`DATE_CHANGED`、`TIME_SET`、`TIMEZONE_CHANGED` 都会刷新统一日期并重算提醒。
- `SystemDateProvider` 不再缓存应用启动时的默认时区，系统切换时区后使用新的本地日期。
- 新增 DST 23/25 小时日期边界测试，避免把“下一午夜”硬编码为 24 小时。
- 修复当天提前完成后次日提醒可能中断的问题；完成当天任务会跳过今天并安排下一天，而不是清空所有未来提醒。
- `ReminderReceiver` 改为通知前异步读取最新设置和目标状态；关闭、无 ACTIVE、已结束、今日已完成或无通知权限时跳过展示。
- Compose 中断状态测试改用显式 `TestHostActivity` 和 v2 `createAndroidComposeRule`；宿主位于 `src/debug` 主应用进程并仅在 Debug 清单中导出，Release 包不包含该测试入口，解决独立测试 APK 跨进程/跨 UID 启动失败。
- `:app:testDebugUnitTest` 通过：34 tests、0 failures；`:app:assembleDebug`、`:app:assembleDebugAndroidTest`、`:app:lintDebug`、`:app:assembleRelease` 均通过。
- Lint 为 0 Error/Fatal、9 Warning；Warning 仍全部是 SDK、AGP 和兼容依赖的更新提示。

### 2.3 2026-09-03 API 34 P0 最终回归与设备清理

- Debug 主 APK 与 AndroidTest APK 均在 Xiaomi `2106118C` / Android 14（API 34）安装成功；系统用户 0 无 `no_install_apps`、`no_install_unknown_sources` 等有效安装限制。
- HyperOS 安全中心会拦截测试运行器从后台拉起 Debug 宿主；确认 `MIUIOP(10021)` 为后台启动 Activity 操作后，仅在测试期间把 `com.boxsmall.onething` 临时设为 `allow`，完整测试结束立即恢复原值 `ignore`，未修改全局安全策略。
- AndroidJUnitRunner 真机测试最终结果为 7/7 通过：5 项 Room/字符策略测试和 2 项 Compose 中断首页测试均通过。
- 手工系统返回回归通过：记录 → 首页、关于 → 设置、设置来源历史 → 设置、设置 → 首页；完成反馈层出现后 100ms 发送系统返回，反馈层关闭、`MainActivity` 保持前台且完成记录保留。
- 首次流程按产品基线验证：欢迎页只出现一次；点击开始后创建页成为无目标根页，系统返回退出 App，重新打开直接回到创建页，不反复进入已完成欢迎页。
- 提醒调度真机验证：未完成时登记当天 20:00 的 `ReminderReceiver`；当天完成后取消当日任务并登记次日 20:00，符合“完成后跳过今天但保留后续提醒”的策略。
- 数据持久化验证通过：强制停止并冷启动后，目标名、当天完成状态和 1 天统计均恢复。
- 收尾已执行：主应用测试数据清除成功，AndroidTest 助手包卸载成功，临时后台启动权限恢复为 `ignore`；手机保留主应用 `com.boxsmall.onething`，当前为干净欢迎页。

### 2.4 2026-09-03 首批 UI 对齐验证

- Gradle Wrapper 首次重建缓存时，GitHub 分发下载速度过慢；改从腾讯云镜像获取同名 `gradle-9.6.0-bin.zip`，使用项目锁定 SHA-256 `bbaeb2fef8710818cf0e261201dab964c572f92b942812df0c3620d62a529a01` 校验完全一致后写入当前用户 Wrapper 缓存。项目的 `distributionUrl` 和版本锁定未修改。
- `:app:compileDebugKotlin`、`:app:assembleDebug`、`:app:assembleDebugAndroidTest`、`:app:testDebugUnitTest` 均通过；JVM 仍为 34 tests、0 failures。
- `:app:lintDebug` 通过，0 errors、9 warnings；Warning 仍为 SDK、AGP、Kotlin 与兼容依赖版本更新提示。
- 新版 Debug APK 已在 Xiaomi API 34 真机覆盖安装成功；冷启动进入新欢迎页，Activity 正常处于前台，近 200 行 `AndroidRuntime` 错误日志为空。
- 欢迎页和创建页已完成真机截图走查；360dp 基准结构、提醒开关、`20:00` 时间入口和底部按钮均完整显示，未被状态栏或手势区遮挡。
- 真机点击提醒 Switch 时发现并修复双重切换问题；修复后整行节点为单一 `checkable=true` 语义，一次点击由 `checked=true` 正确变为 `false`，`20:00` 隐藏并显示“不发送通知”。
- AndroidJUnitRunner 最终为 8/8 通过：原有数据库/字符策略 5 项、中断首页 2 项，以及新增创建页提醒关闭与提交参数 1 项；输出 `OK (8 tests)`，耗时 140.353 秒。测试结束已把临时 `MIUIOP(10021)` 恢复为 `ignore`。
- 提醒关闭分支真机通过：创建 `Walk20Minutes` 后直接进入首页，没有通知权限弹窗，当前 Alarm 批次中没有 OneThing 的 `ReminderReceiver`。
- 提醒开启分支真机通过：默认开关为开启、时间为 `20:00`；创建 `ReminderOn` 后系统显示“一件”通知授权弹窗，选择允许后权限为 `granted=true`，AlarmManager 登记当天 `20:00` 的单次 `RTC_WAKEUP`，窗口为 1 小时。
- 未完成首页、完成 Overlay 和已完成首页均完成 API 34 真机截图走查；完成操作后显示“今天已完成 / 连续第 1 天”，不再显示重复完成按钮。
- 最终 `:app:testDebugUnitTest`、`:app:lintDebug`、`:app:assembleDebug`、`:app:assembleRelease` 全部通过。Debug APK 为 13,191,529 bytes，SHA-256 `B5D8E5D025789E0BAD338141F5D11C79C633D59EBEE781E4B538A479EA0F8E8A`；未签名 Release APK 为 1,549,013 bytes，SHA-256 `BA590091A37AC5E88790E65ADD9E34FB52707838AA34F3EACEEBA7FA1669F775`。
- 收尾已再次清除主应用测试数据并卸载 AndroidTest 助手包；临时关闭的 `pointer_location` 和 `show_touches` 已恢复原值 `1`。手机仅保留主应用、无当前 OneThing Alarm，前台为干净欢迎页。

### 2.5 2026-09-03 第二批 UI、自适应与真横屏回归

- 完成反馈、坚持记录、设置、结束确认、结束结果和历史页均在 Xiaomi `2106118C` / Android 14（API 34）完成截图走查；记录页不使用红色惩罚表达，设置卡片、结束确认、结果与历史的内容和返回入口均完整显示。
- 首次结束确认采用平台 `Dialog` 时，HyperOS 系统栏会变为灰色；改为 Scaffold 内联全屏确认层后，状态栏、导航栏和页面均保持统一暖白，返回优先关闭确认层，未产生额外导航栈。
- 使用 `wm user-rotation lock 1` 在物理 `2400×1080`、应用可用区 `2296×1036` 的真横屏状态走查欢迎和创建页。走查发现 `fillMaxWidth().widthIn(...)` 使最大宽度失效，调整为 `widthIn(...).fillMaxWidth()` 后，内容以 520dp 居中，欢迎页底部按钮可滚动后完整访问，创建页输入和提醒卡片无横向拉伸或裁切。
- `AdaptiveLayoutTest` 8/8 通过；与数据库/字符策略 5 项、创建页 1 项、中断首页 2 项合并后，AndroidJUnitRunner 最终输出 `OK (16 tests)`。HyperOS 首次会确认测试 APK 拉起宿主，放行后完成回归；测试结束 `MIUIOP(10021)` 已恢复为 `ignore`。
- 最终 `:app:testDebugUnitTest` 通过，共 34 项；`:app:lintDebug` 为 0 errors、9 warnings；`:app:assembleDebug`、`:app:assembleRelease` 和 `:app:assembleDebugAndroidTest` 全部通过。
- Debug APK 为 13,191,529 bytes，SHA-256 `0D31C82EED022602075C112C766AED08E52F89CFEB95B27165FAA5C437CA36F8`；未签名 Release APK 为 1,549,013 bytes，SHA-256 `56C5D7808ECC818540E2EE6D570E77E1C77D95C5F7E6E18712971BBB8AE5874B`。
- 收尾已清除主应用测试数据、卸载 `com.boxsmall.onething.test`，并恢复 `rotation_mode=free`、`accelerometer_rotation=1`、`user_rotation=0`、`pointer_location=1`、`show_touches=1`；手机仅保留最新版主应用，前台为干净欢迎页。

### 2.6 2026-09-03 P1 完成反馈阶段验证

- `:app:compileDebugKotlin`、`:app:testDebugUnitTest`、`:app:lintDebug`、`:app:assembleDebug`、`:app:assembleRelease` 和 `:app:assembleDebugAndroidTest` 均通过；JVM 仍为 34 tests、0 failures，Lint 仍为 0 errors、9 个非阻塞版本更新提示。
- 新增 `CompletionFeedbackTest` 已在 Xiaomi Android 14 / API 34 真机单独执行，AndroidJUnitRunner 输出 `OK (3 tests)`；完成按钮回调和反馈层点击/自动关闭时序均通过。
- 手工主流程使用临时目标 `MotionTest` 验证：点击完成后 250ms 中间帧显示深色遮罩、白色上浮卡片和完整成功文案；约 1.2 秒后进入 `今天已完成 / 连续第 1 天` 状态，Completion 已真实落库。动效中间帧和最终状态截图分别保存在 `build/onething-overlay-live.png` 与 `build/onething-completed.png`。
- 完整 AndroidJUnitRunner 真机回归最终输出 `OK (19 tests)`，耗时 20.405 秒：数据库/字符策略 5 项、自适应布局 8 项、完成反馈 3 项、创建页 1 项和中断首页 2 项全部通过。
- 测试期间仅临时设置 `MIUIOP(10021)=allow`，测试结束输出确认恢复为 `ignore`。收尾已清除临时目标数据、卸载 `com.boxsmall.onething.test`、移除无效临时录屏和手机端测试文件，并恢复旋转及开发者触摸显示设置；手机只保留最新版主应用，当前为干净欢迎页。
- Debug APK 为 13,191,529 bytes，SHA-256 `4F56F82BC625BBFAB805FE8288B419DD7FD1441E1E6DEAE116AD7359E1E4DF36`；未签名 Release APK 为 1,549,013 bytes，SHA-256 `5452E633F281D7C47D7B788AEEE1EDE088486609AF447023022F42092783AAEB`。

### 2.7 2026-09-03 无障碍与 600dp 阶段验证

- `AccessibilityLayoutTest` 6/6 通过：600dp 最大宽度与左右居中、2.0 倍字体滚动可达、创建字段与提醒、7 日进度、月历日期状态、统计朗读、返回/月切换、设置 Switch 与 GitHub 入口语义均符合预期。
- 600dp 自动化使用当前窗口像素宽度动态计算测试密度，并采用 Compose 推荐的 `LocalWindowInfo.current.containerSize`，不依赖固定真机分辨率；宽度断言保留 1dp 像素换算容差。
- Xiaomi API 34 真机另以 `wm density 288` 将 1080px 屏幕临时映射为 600dp，欢迎页截图确认 520dp 内容容器居中、按钮不拉满屏幕；证据保存在 `build/onething-600dp.png`。截图后已执行 `wm density reset`，恢复物理 `440dpi`，系统字体保持原值 `1.0`。
- 最终 AndroidJUnitRunner 输出 `OK (25 tests)`，耗时 27.785 秒：数据库/字符策略 5 项、无障碍/600dp 6 项、自适应布局 8 项、完成反馈 3 项、创建页 1 项和中断首页 2 项全部通过。
- 验证脚本不再仅以 `adb shell am instrument` 进程码判断结果：HyperOS/AndroidJUnitRunner 在 JUnit 输出失败时仍可能返回 0，最终必须检查输出中是否出现 `OK (N tests)` 或 `FAILURES!!!`。
- `:app:testDebugUnitTest` 通过 34 项；`:app:lintDebug` 为 0 errors、9 个既有工具链/依赖版本提示；`:app:assembleDebug`、`:app:assembleRelease`、`:app:assembleDebugAndroidTest` 全部通过。
- Debug APK 为 13,191,529 bytes，SHA-256 `3C3A30CED61467BE31F7DBE42F3940CA83AA46C809D61D18DD73E76A0324C3E7`；未签名 Release APK 为 1,549,013 bytes，SHA-256 `F08AB8E353CC77049C754F8504994E88672B6478FF47DCFEAB5E831A5F26AAE4`。
- 收尾已清除主应用测试数据、卸载 `com.boxsmall.onething.test`、恢复 `MIUIOP(10021)=ignore`、物理 `440dpi`、字体 `1.0`、自动旋转与原开发者触摸显示设置；手机仅保留最新版主应用，前台为干净欢迎页。

### 2.8 2026-09-03 提醒系统自动化阶段验证

- JVM 提醒调度新增时区切换用例；`:app:testDebugUnitTest` 当前为 35 项全部通过。东京时区断言确认下一次触发保持配置的本地 `20:00`，不会沿用上海时区的旧偏移。
- Xiaomi `2106118C` / Android 14（API 34）提醒专项自动化 5/5 通过：真实通知发布、系统 Alarm 登记/取消、完成后跳过当天、五类系统广播重排入口及通知页面路由均通过。
- 通知设备测试临时显式传入权限已授予状态以稳定验证策略，同时真实读取 `NotificationManager.activeNotifications`、通知 ContentIntent 和 `dumpsys alarm`；测试没有修改系统时间，也没有把 `ReminderReceiver` 改为可导出。
- `dumpsys alarm` 断言最终限定在当前 `pending alarms` 区段，排除系统保留的已取消 Alarm 历史快照；加固后 4 个提醒系统用例再次输出 `OK (4 tests)`，耗时 1.838 秒。
- 通知入口回归发现并修复“应用已在设置页时点击通知仍停留设置页”的问题；修复后新建 Activity 和 `onNewIntent` 两条路径都产生首页重置事件，无 ACTIVE 时进入创建页。
- HyperOS 对测试 APK 拉起 Debug 测试宿主显示“启动应用”持久授权确认；该确认必须由设备持有人手动选择，自动化没有替用户开启持久系统授权。
- 最终 AndroidJUnitRunner 真机回归输出 `OK (30 tests)`：数据库/字符策略 5 项、提醒系统 4 项、通知入口路由 1 项、无障碍/600dp 6 项、自适应布局 8 项、完成反馈 3 项、创建页 1 项和中断首页 2 项全部通过。总耗时 341.934 秒，其中前半段包含等待设备持有人确认 HyperOS“启动应用”授权的时间。
- 最终质量门通过：`:app:testDebugUnitTest` 35/35、`:app:lintDebug` 0 errors / 9 个既有版本提示，`:app:assembleDebug`、`:app:assembleRelease`、`:app:assembleDebugAndroidTest` 全部成功。
- Debug APK 为 13,362,937 bytes，SHA-256 `E97D8C4ED233885DDD6CF2EA3CAF1EA44A6A7E5D7FFB8F50C73F55F4F065AE17`；未签名 Release APK 为 1,549,013 bytes，SHA-256 `394021908E07B26E9CA825F8B59EEBEB259D5F46E03CA5C2D30483BEF7D8193D`。
- 测试完成后执行设备清理：取消 OneThing 通知和 Alarm、清除主应用测试数据、卸载 AndroidTest 辅助包，并把临时 `MIUIOP(10021)` 恢复为原值 `ignore`；手机仅保留最新主应用并回到干净欢迎页。

### 2.9 2026-09-03 本地签名 Release 构建验证

- 仓库外固定密钥生成成功：`onething-release.jks` 与 `keystore.properties` 位于当前 Windows 用户目录 `.onething-signing`，项目 Git 忽略规则覆盖 `*.jks`、`*.keystore` 和 `keystore.properties`。
- `:app:assembleRelease` 使用 `release` SigningConfig 构建成功；`signingReport` 显示 Alias `onething-release`、RSA 4096 证书及固定 SHA-256 指纹。
- `apksigner` 验证通过：v2=true、v3=true；v1=false 符合 minSdk 26，v4 以独立 `.idsig` 侧车生成。
- Release APK 当前为 1,561,301 bytes，SHA-256 `D2EC1035D4F8BC6D1652BB60EFD15EF2A786D58C4C4EAB65E07DCDC8F5B681CA`；`.idsig` 为 19,020 bytes，SHA-256 `D70B6F11E897F8B16892B8152CB10FB93DF9840412374F230C025B2D19CCCF32`。
- AAPT 清单检查通过：Application ID `com.boxsmall.onething`、版本 `1.0.0 (1)`、minSdk 26、targetSdk 36；仅含通知、开机恢复提醒和 AndroidX 动态接收器保护权限，不含 `INTERNET`；Release 不含 `TestHostActivity`。
- 设备持有人明确授权后，Xiaomi `2106118C`（Android 14）已卸载 Debug 签名版本并首次安装正式签名 Release；`dumpsys package` 显示版本 `1.0.0 (1)`，未启用 `debuggable`。
- Release 真机核心闭环通过：首次冷启动进入欢迎页，创建 `ReleasePersist` 后进入未完成首页；完成后显示“今天已完成 / 连续第 1 天”，杀进程冷启动后目标、完成态和统计仍保留；记录页显示当天完成以及当前/累计/最长连续均为 1，设置页显示版本 1.0.0 和提醒已关闭。
- 同一签名 APK 使用 `adb install -r` 覆盖安装成功；覆盖后目标和首次引导状态保留，说明 Room 与 DataStore 数据未被清除。
- 另以干净数据完成结束确认、结束结果和历史归档验证；历史中可见已结束的 `ReleaseLifecycle` 及起止日期、持续天数和统计。
- 真机验证完成后已清除全部临时目标与历史；手机最终保留签名 Release 并停在干净欢迎页，通知权限为未授予、当前 Alarm 批次无 OneThing 任务、AndroidTest 辅助包不存在，`MIUIOP(10021)` 为 `ignore`。

### 2.10 2026-09-03 API 26 / API 36 发布矩阵回归

- 当前电脑新增 Android 26 平台、Google APIs x86_64 系统镜像和 `OneThing_API_26` AVD；既有 `OneThing_API_36` AVD 继续复用。两个模拟器均以 1080px 宽、440dpi 手机档执行。
- API 26 首轮 30 项设备测试中 26 项通过，4 项提醒测试仅因旧版 `dumpsys alarm` 使用 `Pending alarm batches` 分节格式而无法解析；测试解析器已兼容 API 26 旧格式和 Android 新版 `pending alarms` 格式，提醒专项复跑 4/4、完整复跑 30/30 通过。
- API 36 首轮发现全新模拟器未实际授予 `POST_NOTIFICATIONS`，与测试传入的“权限已授权”前置条件不一致；设备测试现于 API 33+ 在执行前通过 shell 授予测试包通知权限。提醒专项复跑 4/4、完整复跑 30/30 通过。
- API 26 和 API 36 均另行卸载 Debug/Test 包并安装固定密钥签名 Release；两端都完成欢迎、创建（提醒关闭）、未完成首页、完成一次、强制停止和冷启动恢复，目标、完成态和“连续第 1 天”均保留。
- API 36 无窗口 AVD 冷启动仍会偶发 System UI 自身 ANR；选择 `Wait` 后系统恢复，OneThing 欢迎页正常显示，`AndroidRuntime` 未发现应用崩溃。该问题继续归类为无窗口模拟器宿主问题。
- API 26 证据保存在 `build/api26/`，API 36 证据保存在 `build/api36/`；测试完成后已清除两个模拟器的临时应用数据并关闭模拟器，连接手机未被测试包覆盖。

### 2.11 2026-09-03 360 × 800 全状态视觉复核

- API 36 模拟器临时设置为 `1080 × 2400 / 480dpi`，得到精确 `360 × 800dp` 内容基准；按真实交互抓取欢迎、创建、首页未完成、完成反馈、首页已完成、坚持记录、设置、结束确认、结束结果和历史 10 个状态。
- 中断状态依赖跨日数据，Debug 专用 `TestHostActivity` 新增 `screen=interrupted` 审计入口，以固定历史快照渲染 `07 中断后首页`；该入口只存在于 `src/debug`，Release 清单和产物仍不包含测试 Activity，也不改变正式业务数据或路由。
- 11 张实现截图均缩放到 `360 × 800`，并与 `design/screens/` 对应稿在同一张 `720 × 800` 对比图中逐页检查；原始截图、UI 层级和 `compare-01.png` 至 `compare-11.png` 保存在 `build/ui-audit-360x800-2026-09-03/`。
- 复核未发现文字/控件裁切、横向溢出、手势区遮挡、错误颜色、圆角断裂或视觉层级混乱。静态稿与实现的可见差异均来自已确认口径：创建页增加默认开启的提醒控制和 V1 无文字记录说明；首页使用固定品牌 `1.`；日期、目标名、连续天数、月历与历史统计使用真实状态；系统状态栏和手势导航区由 Android 管理。
- 同一 API 36 Pixel Launcher 的应用抽屉已实机渲染固定品牌图标：圆形 mask 下暖白背景、黑色 `1` 和品牌黄圆点完整，前景未触碰裁切边界；证据为 `build/adaptive-icon-api36-app-drawer.png`。Android 13+ monochrome XML 已打包并可触发系统主题图标模式，但最终仍保留一项实体设备桌面固定图标签字，避免把 Launcher 预测栏的动态替换误判为应用资源问题。
- 视觉结论：11 个核心页面/状态在 `360 × 800dp` 基准下通过；没有新增业务 UI 修改。审计辅助 Activity 补齐 `enableEdgeToEdge()`，使 Debug 视觉入口与正式 Activity 的系统栏策略一致。
- 审计后完整质量门通过：JVM 单元测试 35/35；Lint 0 errors、9 个既有版本提示；Debug、固定签名 Release 和 AndroidTest 均构建成功。API 36 模拟器完整设备回归再次输出 `OK (30 tests)`，耗时 34.151 秒。
- 视觉审计完成时，固定签名 Release 未因 Debug 专用改动发生变化：APK 当时仍为 1,561,301 bytes，SHA-256 仍为 `D2EC1035D4F8BC6D1652BB60EFD15EF2A786D58C4C4EAB65E07DCDC8F5B681CA`；后续设备迁移规则修正产生的新候选包见 2.12。

### 2.12 2026-09-03 设备迁移策略与发布前仓库审计

- 数据提取规则复核发现：Preferences DataStore 的 `app_settings.preferences_pb` 实际位于应用 `files/datastore/`，原 Android 12+ `device-transfer` 规则只包含 `database` 和 `sharedpref`，无法满足“非敏感设置随设备迁移”的既定口径。
- `data_extraction_rules.xml` 已在 `device-transfer` 中加入 `file/datastore/`，同时继续只允许 `database`、DataStore 和 `sharedpref` 进入设备到设备迁移；`cloud-backup` 仍排除 root、file、database、sharedpref、external 全部域，API 30 及以下使用的 `backup_rules.xml` 也继续排除全部域。
- 新增 `BackupPolicyDeviceTest` 2 项，直接解析 APK 运行时资源，断言 Android 12+ 云备份排除项、设备迁移包含项和旧版全备份排除项。API 36 专项输出 `OK (2 tests)`。
- 新测试加入后，API 36 首轮完整 32 项中有 1 项在通知发布后立即读取 `activeNotifications` 时偶发为空；该单项原样复跑通过，确认不是业务调度失败。设备测试现最多轮询 2 秒等待系统通知服务完成异步发布，随后完整复跑输出 `OK (32 tests)`，耗时 58.423 秒。
- Android 12+ 打包资源已通过 `aapt2 dump xmltree` 复核，Release 内明确包含 `database/.`、`file/datastore/` 和 `sharedpref/.` 三个设备迁移入口；Manifest 仍只有通知、开机恢复和 AndroidX 动态接收器保护权限，不含 `INTERNET` 和 Debug `TestHostActivity`。
- 设备迁移规则修正后的固定签名 Release 当时已通过 v2/v3 签名验证，并在 API 36 模拟器完成全新安装、冷启动和欢迎页冒烟。该阶段 APK 为 1,561,325 bytes，SHA-256 `E1612DEF9ECE6CD40597B895B552C1D08DC9487A65734C19C2A3AAE02961854B`；`.idsig` 为 19,020 bytes，SHA-256 `3E60F9E81C13DB94D7FEAE3EC375B7F3F75B60FF0C1A9D85A8DC98999E34E585`。后续 HyperOS 提醒保障修正产生的当前候选见 2.15。
- 工作区审计未发现私钥、keystore、密码配置或硬编码凭据；命中的密码字段只存在于签名初始化脚本和 Gradle 的外部配置读取代码。`.gradle/`、`app/build/`、`build/`、`local.properties`、`*.jks`、`*.keystore` 和 `keystore.properties` 均已忽略，`git diff --check` 无空白错误。
- Git 当前仍位于 `main`，基线提交为 `75df9c1`（与 `origin/main` 一致）；开发成果尚未归档，共有 229 个已修改或未跟踪路径，主要是完整 Android 工程、正式图标资源和同步文档。未在没有用户明确授权时执行 `git add`、commit、tag 或 push。
- 真正的系统设备到设备传输仍需要两台 Android 12+ 设备或可执行 Setup Wizard 迁移的测试环境；当前已验证“规则正确打包 + 恢复后 Application 启动会按当前权限重排提醒”的确定性部分，不把模拟文件复制冒充系统迁移验收。
- 当前新候选包已使用同一正式签名在 Xiaomi `2106118C` 上执行 `adb install -r` 覆盖升级；安装成功，版本仍为 `1.0.0 (1)`，用户现有目标“吃饭”、当天已完成状态和“连续第 1 天”完整保留。全程未清除数据、未结束目标、未创建新目标，也未修改提醒设置。
- 覆盖升级触发提醒状态重排后，系统保留的当前有效任务为次日 `2026-09-04 20:00` 单次非精确 `RTC_WAKEUP`，通知权限仍为已允许；这符合“今天已完成则安排次日”的规则。实体手机的真实到点通知仍待自然时间或用户授权临时调整提醒后验证。

### 2.13 2026-09-03 正式数据隔离的 Debug 真机方案

- 为继续测试真实通知、下一目标和新旧目标隔离而不触碰手机中的正式目标“吃饭”，Debug 构建新增 `applicationIdSuffix = ".debug"` 和 `versionNameSuffix = "-debug"`；Debug 包为 `com.boxsmall.onething.debug` / `1.0.0-debug`，显示名称为“一件 Debug”。
- AndroidTest 自动跟随为 `com.boxsmall.onething.debug.test`，instrumentation target 已通过打包 Manifest 确认为 `com.boxsmall.onething.debug`。Release 仍为 `com.boxsmall.onething` / `1.0.0`，当前 Release APK 大小和 SHA-256 均未变化。
- 该方案使 Room、DataStore、通知权限、通知渠道和 Alarm 均按 Android 包隔离；后续可以清理 Debug 数据或卸载 Debug，而不清除、结束或覆盖 Release 的用户目标。
- Debug、AndroidTest 构建已通过；首次安装因 HyperOS 确认超时返回 `INSTALL_FAILED_USER_RESTRICTED`，设备持有人解锁并确认后，`com.boxsmall.onething.debug` 与 `com.boxsmall.onething.debug.test` 已成功安装并与正式版共存。
- 此次隔离方案只影响 Debug 变体，不进入 Release Manifest 或正式包；正式 Application ID `com.boxsmall.onething` 的发布和升级链不变。

### 2.14 2026-09-04 隔离 Debug 真机完整回归

- Xiaomi `2106118C`（Android 14 / API 34）已确认同时存在 Release、Debug 和 AndroidTest 三个独立包；Debug 为 `1.0.0-debug`。测试期间仅把 Debug 包的 HyperOS `MIUIOP(10021)` 从 `ignore` 临时设为 `allow`，结束后已恢复为 `ignore`。
- 首轮 32 项中“关闭提醒取消 Alarm”出现 1 个失败。手机 `AlarmManager` 的 Removal history 明确显示 Debug 闹钟已取消；根因是测试只搜索 `ReminderReceiver` 类名，误匹配了正式版“吃饭”的同名提醒，而非产品取消逻辑失败。
- 设备断言已改为同时匹配 Debug 包名和完整接收器组件名，单项复测输出 `OK (1 test)`，随后完整回归输出 `OK (32 tests)`，耗时 36.844 秒。数据库、字符策略、提醒/通知、系统广播重排、通知入口、无障碍、自适应布局、完成反馈、创建页和中断状态全部通过。
- 本轮未清除、结束、重命名或覆盖 Release 的目标和设置；正式包 `com.boxsmall.onething` 始终保留。Debug 测试数据由测试自身收尾，后续可单独卸载两个 `.debug` 包。

### 2.15 2026-09-04 HyperOS 提醒可靠性修正

- 隔离 Debug 真实到点测试在通知权限已允许、目标未完成、后台熄屏且设备处于充电/Doze ACTIVE 的条件下执行。`09:15` Alarm 到 `09:16:52` 未发出通知，系统将其标记为 `power_pending` 并推后约 3 天；正式版次日 20:00 任务也受到同类延迟。
- Debug 与 Release 的小米自启动操作 `MIUIOP(10008)` 均为 `ignore`。只对 Debug 临时改为 `allow` 并重新调度后，Debug Alarm 在登记时恢复 `power_pending=--`；第二次锁屏到点测试中，窗口结束后 PowerKeeper 又把任务推后约 3 天。这证明自启动是必要条件但单独不足，下一轮必须叠加“省电策略无限制”。未修改正式版的系统权限。
- 设置页已为小米、Redmi、POCO 增加条件显示的“系统提醒保障”卡片。提醒开启时可查看说明并打开当前应用系统信息页，文案明确要求开启自启动并把省电策略设为无限制；其他厂商不显示该附加项，V1 仍不申请精确闹钟特殊权限。
- `AccessibilityLayoutTest` 已覆盖保障卡在 2.0 倍字体下可访问，以及说明弹窗和系统设置按钮具备可点击语义。修正后本地门禁通过，Xiaomi Android 14 完整设备回归输出 `OK (32 tests)`，耗时 32.152 秒。
- Debug 同时开启自启动、后台运行和无限制省电后，`10:00` 锁屏自然到点测试成功：系统在 `10:00:31` 发布通知，约延迟 31 秒；通知渠道、私密锁屏可见性、标题和正文均符合基线。
- 触发后接收器已把下一次单次非精确 `RTC_WAKEUP` 续排到 `2026-09-05 10:00`，且 `power_pending=--`。真实点击通知后正确进入 Debug `MainActivity`，通知自动清除。
- 测试收尾已卸载 `com.boxsmall.onething.debug` 和 `com.boxsmall.onething.debug.test`，Debug 测试数据、通知、Alarm 与临时系统策略一并清除，正式版未清数据。
- HyperOS 修正后的初始候选 Release 为 1,561,325 bytes，SHA-256 `10C08450AB9A08DC061766923BC9AB24DDE8E85AB3DE78CE13B79C0AB8894AA2`。归档前清理源码文件末尾空白并重新编译后，最终归档候选哈希更新为 `6EEE867A481C88FB83765AEEF8BC28966E8C292E044C53CEB6D29667BEEE4064`；两者业务逻辑与签名证书相同，以 2.16 记录的最终哈希为准。
- 正式版设置页已实机确认“系统提醒保障”卡片、说明弹窗和“打开系统设置”跳转可用，系统应用信息页识别为“一件 1.0.0”。设备持有人明确授权后，已为正式包开启自启动并将省电策略设为无限制；系统界面分别确认复选框和单选框为选中，底层 `MIUIOP(10008)`、`RUN_ANY_IN_BACKGROUND` 均为 `allow`，待机桶为活跃级别 `5`。
- 正式版重新调度后的唯一有效任务为 `2026-09-05 20:00` 单次非精确 `RTC_WAKEUP`，`power_pending=--`，此前约 4 天的 HyperOS 延期已清除；目标、完成记录和提醒时间未改动。

### 2.16 2026-09-04 Git Release Candidate 归档

- 项目所有者已明确授权归档。源码以 `main@75df9c1` 为父提交，在独立分支 `codex/v1.0.0-rc1-archive` 保存，并使用 `v1.0.0-rc.1` 标签标识，不改写 `main` 历史。
- 归档范围包括完整 Android 源码、JVM/设备测试、Room Schema、Gradle Wrapper、签名初始化与验证脚本、A1 Adaptive Icon 和候选目标图标源资源，以及全部产品、开发、测试和发布文档。
- `.gitignore` 明确排除 `.gradle/`、所有 `build/`、`local.properties`、`*.jks`、`*.keystore` 与 `keystore.properties`；归档前扫描未发现私钥、keystore、硬编码密码、用户数据库或手机测试数据进入版本库。
- 归档前最终门禁再次通过：JVM 35/35、Lint 0 errors、Debug/AndroidTest/Release 构建成功；源码格式清理后重新执行测试与 Release 构建仍为 `BUILD SUCCESSFUL`。最终 Release APK 为 1,561,325 bytes，SHA-256 `6EEE867A481C88FB83765AEEF8BC28966E8C292E044C53CEB6D29667BEEE4064`，`.idsig` SHA-256 为 `B19648F05AFC30A3592DEE96953DCEEA0C369CD7A88D157FFDDC4A73BE7E66A2`，RSA 4096 证书及 v2/v3 签名验证通过。
- 最终归档候选已再次通过 `adb install -r` 覆盖 Xiaomi 正式版；目标“吃饭”和“今天已完成”状态保留，自启动、无限制省电、后台 `allow` 与待机桶 `5` 均未因升级丢失，`2026-09-05 20:00` Alarm 仍为 `power_pending=--`。
- Git 标签只归档源码与元数据，不把 APK 和 `.idsig` 提交到仓库；二进制产物继续位于本机构建目录，并由本文记录的 SHA-256 校验。

## 3. 下一阶段未完成项

以下内容仍属于 M2 后续开发，不应被误认为已经验收：

- 11 个页面的 `360 × 800dp` 同画布视觉复核已经通过；后续只在实体设备最终签字时复查系统字体渲染、触觉和页面切换体感，不再把静态稿中的动态示例数据当成实现目标；
- 在物理平板或折叠屏做一次 600dp+ 实机复核，并由人工使用 TalkBack 手势完整走通主流程；代码语义、焦点标签、600dp 密度模拟及 2.0 倍字体自动化已经完成；
- 使用两台 Android 12+ 设备或可运行 Setup Wizard 的迁移环境，完成一次真实设备到设备传输；当前 XML 打包规则和恢复后提醒重排逻辑已经自动验证；
- 近时锁屏真实到点、次日续排和通知点击已经通过；如需提高发布证据强度，可再做一次 24 小时以上长期留置，但不再阻塞 V1 本地候选包功能验收；
- 归档与当前 Release Candidate 产物完全对应的 Git commit，并把固定签名密钥与配置一起备份到用户选定的离线安全介质；
- A1 Adaptive Icon 已在 API 36 Pixel Launcher 应用抽屉通过圆形 mask 实际显示复核；仍需在实体设备对 OEM 方圆形 mask 和固定到桌面后的主题图标做最终签字，代码资源、打包检查和标准圆形显示已经完成。

## 4. 常用命令

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:lintDebug
.\gradlew.bat :app:connectedDebugAndroidTest
```

Debug APK：`app/build/outputs/apk/debug/app-debug.apk`。
