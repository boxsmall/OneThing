# 一件 APP V1.1 品牌体验升级开发任务

> 规划日期：2026-09-05  
> 任务状态：开发中（真机与正式覆盖安装按用户要求暂缓）  
> 建议版本：`1.1.0 (2)`  
> 建议分支：`codex/v1.1-brand-experience`  
> 任务主题：目标图标、系统启动页、Logo 点亮完成动画统一升级

## 1. 任务目标

把现有设计资源转化为一次边界清晰、可测试、可覆盖升级的品牌体验迭代：

1. 接入此前制作的 8 个目标图标；
2. 增加与 A1 品牌一致的 Android 系统启动页；
3. 用 2026-09-05 确认的权威设计稿替换当前完成反馈视觉；
4. 保持现有业务、提醒、历史数据、无障碍和本地发布能力不退化。

该任务属于 V1.1，不移动或重写 `v1.0.0-rc.1`，也不直接在已封板的 Release Candidate 提交上修改代码。

## 2. 当前基线

| 范围 | 当前状态 | 本任务处理 |
| --- | --- | --- |
| Launcher Icon | Adaptive、Round、Monochrome 已进入 `app/src/main/res` | 不重做，只校验资源与 OEM mask |
| 8 个目标图标 | 设计包中已有 SVG、Android Vector 和多密度 PNG，尚未进入业务 UI | 接入 Android Vector，增加明确选择与持久化 |
| 启动页 | Manifest 直接使用 `Theme.OneThing`，没有专用 Splash Theme | 增加 AndroidX SplashScreen 兼容实现 |
| 完成反馈 | Compose 白色卡片 Overlay，约 1.2 秒 | 替换为权威稿的 7 阶段 Logo 点亮动画 |
| Goal 数据 | Room schema v1，`GoalEntity` 没有图标字段 | 升级 schema v2，增加非破坏迁移 |
| 正式设计 | `design/completion-animation/OneThing_Complete_Animation_Spec_Approved_V1.0.jpg` | 作为唯一视觉验收基准 |

## 3. 本轮固定决策（推荐方案）

### 3.1 版本与分支

- 从包含最新设计文档的当前状态创建 `codex/v1.1-brand-experience`；
- `versionName` 更新为 `1.1.0`，`versionCode` 更新为 `2`；
- Debug 继续使用 `com.boxsmall.onething.debug`，正式包继续使用 `com.boxsmall.onething`；
- 开发、破坏性测试和截图均先在 Debug 包进行；Release 只做最终覆盖升级验证。

### 3.2 目标图标

- 使用 8 个既有图标：`walk`、`read`、`sleep`、`water`、`stretch`、`study`、`medicine`、`other`；
- 采用用户明确选择，不根据目标文字自动猜测；
- 创建目标时默认选择 `other`，用户可以主动切换；
- 设置页允许修改当前目标图标；历史目标保留创建时或最后保存的图标；
- 首页、记录页和历史卡片显示目标图标；完成动画始终使用 A1 `1.` Logo，不随目标图标变化；
- Android 运行时优先使用 Vector Drawable，不复制多套 PNG 进入 APK。

这项决策会修订 V1“目标只使用固定品牌图形”的旧约束，但不会增加文字记录、账号、云同步或多目标能力。

### 3.3 启动页

- 使用 AndroidX `core-splashscreen` 和 `Theme.SplashScreen`；
- 不创建单独的 `SplashActivity`，避免 Android 12+ 出现双启动页；
- 背景使用 `#FDFBF7`，中心使用 A1 `1.` Logo，状态点为 `#FABF45`；
- 不放广告、按钮、进度条、长文案或网络内容；
- 不人为固定停留 1–2 秒，数据准备完成后立即进入应用；
- 退出动画只做约 `180–220ms` 的轻微淡出/缩放；
- 首次安装进入欢迎页，已有活动目标进入首页，通知点击仍进入正确页面。

### 3.4 完成动画

- 以 `OneThing_Complete_Animation_Spec_Approved_V1.0.jpg` 为唯一视觉基准；
- 使用 7 阶段、`1.5–1.8s` 流程：按钮按下、进入动画页、黄点点亮、半环绕行、变绿与文案、成功停留、返回首页；
- 动画图形层使用 Lottie Compose；动态文案、页面切换、语义和业务状态使用 Compose；
- 基础资源 `complete_success.json` 为 P0；`complete_streak_3.json`、`complete_streak_7.json`、`complete_streak_30.json` 为本任务 P1，缺失或加载失败时回退基础资源；
- Lottie 不包含中文文字，单个 JSON 建议不超过 200KB，不循环；
- 当前 Compose Overlay 保留为加载失败和“移除动画”环境的降级路径；
- 权威稿中的旧底部导航、插画卡片和页面壳层不进入代码。

## 4. 开发工作包

### WP0：基线固化与分支准备

- [ ] 将本次权威 JPG、动效规范和本任务计划纳入新分支首个提交；
- [ ] 确认 V1 RC 标签仍指向 `65314617803b4a8b95b846963a8389e6afffdd53`；
- [ ] 执行当前 JVM、Lint、Debug、AndroidTest 和 Release 构建，记录新任务开始前基线；
- [ ] 确认手机正式版数据不用于破坏性测试。

### WP1：目标图标资源与领域模型

- [ ] 从 `design/Android Adaptive Icon/goal-icons/android-vector/` 复制 8 个正式 Vector Drawable 到 App；
- [ ] 建立稳定的 `GoalIconKey` 枚举或值对象，数据库只保存稳定字符串 key，不保存资源 ID；
- [ ] 为未知 key 提供 `other` 回退；
- [ ] 建立 key、Drawable、中文名称与无障碍描述的单一映射；
- [ ] 校验所有图标在黄/绿容器、深浅背景、1.0×/2.0×字体和 600dp+ 布局中的清晰度。

### WP2：Room v1→v2 非破坏迁移

- [ ] `GoalEntity` 增加 `iconKey TEXT NOT NULL DEFAULT 'other'`；
- [ ] Room 数据库版本从 1 升到 2；
- [ ] 提供显式 `Migration(1, 2)`，禁止 destructive migration；
- [ ] 更新 `app/schemas/.../2.json`；
- [ ] 现有活动目标和历史目标迁移后统一为 `other`，不按名称猜测；
- [ ] 迁移后 Goal、Completion、提醒设置和当前完成状态必须完整保留。

### WP3：目标图标选择与展示

- [ ] 创建目标页增加 8 个图标的可横向换行选择器；
- [ ] 每项至少 48dp 热区，并同时提供图形、选中状态和可读标签；
- [ ] 默认 `other`，开始坚持时和目标名称一起原子保存；
- [ ] 设置页支持修改当前目标图标；
- [ ] 首页、记录页和历史卡片显示持久化后的图标；
- [ ] 不增加底部导航，不扩大为多目标管理或图标商店。

### WP4：Android 系统启动页

- [ ] 在版本目录中锁定 `androidx.core:core-splashscreen`；
- [ ] 新建 `Theme.OneThing.Starting`，配置暖白背景、A1 Logo 和 `postSplashScreenTheme`；
- [ ] Manifest 只给 `MainActivity` 使用 Starting Theme；
- [ ] 在 `MainActivity.onCreate()` 调用 `installSplashScreen()`，位置在 `super.onCreate()` 之前；
- [ ] 使用轻量退出动画，不人为延长冷启动；
- [ ] 验证 API 26、API 31+、API 36 的冷启动、热启动、深色系统主题和旋转；
- [ ] 验证通知点击、进程恢复和首次欢迎页路由不受影响；
- [ ] 输出 `design/screens/00 启动页.png` 和对应验收截图。

### WP5：Logo 点亮完成动画资源

- [ ] 将 A1 Logo 拆成 `Number_1` 与 `Status_Point`；
- [ ] 按权威稿制作 `Glow`、`Orbit_Path`、`Particle`、`Success_Check`；
- [ ] 基础动画严格按 `0–1800ms` 七阶段时间轴制作；
- [ ] 黄点缩放使用 `1 → 1.25 → 1.05`；
- [ ] 半环路径从右下向右、向上运动，不闭合、不做完整 360°；
- [ ] 粒子为 3–8 个小圆点或光点，不使用彩带；
- [ ] 黄点均匀过渡到 `#59AD5E`，完成后光晕缓慢消散；
- [ ] 校验 JSON 可解析、无外部图片依赖、无文字图层、单个文件建议不超过 200KB。

### WP6：Compose 接入与状态控制

- [ ] 保持“Completion 写入成功后才显示完成动画”的现有业务顺序；
- [ ] 点击后立即防重入，按钮视觉压至 96% 并轻回弹；
- [ ] 使用可控 progress 接入 7 阶段，动态显示今天完成、连续第 N 天和节点文案；
- [ ] 动画期间返回、切后台或进程重建时直接进入已完成首页，不重复播放；
- [ ] 动画加载失败时使用现有 Compose Overlay；
- [ ] 系统动画比例为 0 或启用“移除动画”时，跳过轨迹/粒子并快速显示完成结果；
- [ ] TalkBack 只朗读一次完整完成信息，装饰轨迹和粒子不进入语义树。

### WP7：测试、真机与文档

- [ ] 单元测试覆盖 icon key、未知值回退、连续天数和完成状态；
- [ ] Room migration test 从真实 v1 schema 升到 v2，并验证数据完整；
- [ ] Compose UI 测试覆盖图标选择、默认值、设置修改、重启持久化和历史展示；
- [ ] 完成动画测试覆盖单次触发、数据先写、正常结束、中断、加载失败和移除动画；
- [ ] 启动页测试覆盖首次、已有目标、通知打开、冷/热启动和无双闪屏；
- [ ] API 26、API 36 和 Xiaomi Android 14 Debug 包执行完整回归；
- [ ] Release 使用同一正式签名覆盖安装，确认 v1→v2 数据迁移、图标默认值和提醒状态保留；
- [ ] 更新 PRD、决策基线、开发进度、验收方案和本地发布记录；
- [ ] 记录最终 commit、APK SHA-256、签名验证和真机结果。

## 5. 推荐开发顺序

```text
权威设计与任务文档入库
→ 新建 V1.1 分支和版本号
→ 图标模型 + Room 迁移
→ 图标选择与页面展示
→ 系统启动页
→ 基础 Lottie 资源
→ Compose 接入与降级
→ 3/7/30 天轻增强资源
→ 自动化回归
→ Debug 真机验证
→ 签名 Release 覆盖升级
→ 文档与 Git 归档
```

先做数据迁移再做 UI，能最早暴露覆盖升级风险；启动页与完成动画在数据模型稳定后接入，避免多条改动同时干扰问题定位。

## 6. 发布级验收标准

### 6.1 图标

- [ ] 8 个图标均可选择，默认与未知值稳定回退到 `other`；
- [ ] 创建、设置、首页、记录和历史显示一致；
- [ ] 覆盖升级、杀进程和重启后选择不丢失；
- [ ] TalkBack 可读，选中状态不只依赖颜色。

### 6.2 启动页

- [ ] 无黑屏、白闪、双 Splash 或人为长时间停留；
- [ ] A1 Logo 安全区正确，背景和状态栏颜色连续；
- [ ] API 26、Android 12+ 和 API 36 行为一致；
- [ ] 首次、已有目标和通知点击均进入正确页面。

### 6.3 完成动画

- [ ] 视觉与权威 JPG 的 7 阶段顺序一致；
- [ ] 总时长在 1.5–1.8 秒，动画不循环；
- [ ] 数据保存成功后才播放，点击不会重复写入；
- [ ] 动画中断和进程恢复后直接显示已完成状态；
- [ ] 动态连续天数正确，Lottie 内无写死文字；
- [ ] 动画关闭或资源失败时有可用降级路径；
- [ ] 旧版底部导航和旧页面壳层未被带回。

### 6.4 工程与发布

- [ ] JVM、Room、Compose UI、设备测试和 Lint 全部通过；
- [ ] Debug、AndroidTest、Release 构建成功；
- [ ] Release 仍无 `INTERNET`、第三方统计和无关敏感权限；
- [ ] Lottie 单文件建议不超过 200KB，新增依赖后的 APK 体积变化有记录；
- [ ] 正式签名覆盖安装成功，原目标、完成记录和提醒设置不丢失；
- [ ] 文档、设计资产、代码、测试和发布记录与 Git commit 对应。

## 7. 明确不做

- 不恢复底部导航；
- 不照搬权威稿中的旧首页卡片、插画和日期布局；
- 不增加账号、云同步、社交分享、广告或 Analytics；
- 不增加多目标、文字记录、图标商店或自动图标猜测；
- 不把动态中文文案烘焙进 Lottie；
- 不创建独立 Splash Activity；
- 不移动 `v1.0.0-rc.1` 标签，不改写 `main` 历史。

## 8. 风险与控制

| 风险 | 控制方案 |
| --- | --- |
| Room 迁移导致正式数据丢失 | 显式 1→2 migration、schema 测试、Debug 复制数据验证、最后才覆盖正式包 |
| Lottie 与设计稿差异 | 使用权威 JPG 逐阶段核对，提供可控 progress 截图和真机慢速走查 |
| 动画资源失败 | 保留现有 Compose Overlay 降级，不阻塞完成数据闭环 |
| Splash 出现双页面或延迟 | 使用系统 SplashScreen，不创建 Activity，不固定延时 |
| 图标影响页面布局 | Vector Drawable、统一容器、长标题和 2.0× 字体测试 |
| 旧设计壳层误进入代码 | 验收明确禁止底部导航，只实现品牌动画核心 |

## 9. 预计工作量

| 阶段 | 预计时间 |
| --- | ---: |
| 分支、版本和基线 | 0.25 天 |
| 图标资源、模型、Room 迁移与 UI | 1–1.5 天 |
| 系统启动页 | 0.25–0.5 天 |
| Lottie 资源与 Compose 接入 | 1–1.5 天 |
| 自动化、真机、Release 与文档 | 0.75–1 天 |
| 合计 | 约 3–4.5 个工作日 |

如果先只交付基础完成动画，把 3/7/30 天增强资源顺延，首个可测试包预计可提前约 0.5 天。

## 10. 开发开始条件

下次开始开发前只需一次性确认以下推荐口径：

- 版本使用 `1.1.0 (2)`；
- 目标图标由用户明确选择，默认 `other`；
- 启动页只用系统 SplashScreen + A1 Logo，不增加第二屏；
- 完成动画按权威 JPG，实现动画核心但不照搬旧页面壳层；
- 基础动画为 P0，3/7/30 天增强为 P1；
- 允许 Room schema v1→v2 非破坏迁移。

全部按推荐方案确认后，即可按第 5 节顺序直接开发，不再需要额外产品选择。

## 11. 技术依据

- Android 官方建议使用 AndroidX SplashScreen 兼容库，并明确提示独立 Splash Activity 在 Android 12+ 可能造成双启动页：<https://developer.android.com/develop/ui/views/launch/splash-screen/migrate>
- AndroidX SplashScreen API 要求 Starting Theme、`postSplashScreenTheme`，并在 `super.onCreate()` 前调用 `installSplashScreen()`：<https://developer.android.com/reference/androidx/core/splashscreen/SplashScreen>
- Lottie Compose 官方接入与 progress 控制说明：<https://github.com/airbnb/lottie/blob/master/android-compose.md>
