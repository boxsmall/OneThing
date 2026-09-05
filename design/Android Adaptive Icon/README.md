# 一件 / OneThing — Android 正式图标资源包（A1 · 卡通目标图标版）

> 工程接入状态：2026-09-02 已完成 A1 Launcher Icon；2026-09-05 已在 V1.1 接入 8 个目标分类图标。
> V1.1 使用口径：完成动画固定使用品牌 `1.`；目标分类图标由用户明确选择并持久化，不做文字猜测。

## 说明
本资源包基于已确认的 A1 品牌 Logo 方案保留启动图标体系，
并将 App 内 **8 个正式目标图标** 更新为 **无人物卡通风格**。

## 已包含内容

### 1) 品牌与启动图标（沿用 A1）
- Adaptive Icon foreground
- Adaptive Icon background
- Android 13+ monochrome icon
- 各密度 launcher icon
- round launcher icon
- Play Store 图标
- A1 主 Logo 母版

生产用 Adaptive Icon 前景已围绕 `54,54` 缩放至 85%，保证主体落入 Android `66×66dp` 安全区。工程以 VectorDrawable 为权威源，不依赖旧版前景 PNG。

品牌颜色固定为：

- 品牌黑：`#141414`
- 品牌黄：`#FABF45`
- 图标背景：`#FDFBF7`

UI 的“待完成状态黄”仍为 `#F5B838`，它是状态色，不与品牌黄合并。

### 2) 卡通目标图标（新版）
分类如下：
- walk：走路
- read：阅读
- sleep：睡眠
- water：喝水
- stretch：拉伸
- study：学习
- medicine：吃药
- other：其他

## 目录结构

### Goal masters
- `goal-icons/master-png/`
  - 512×512 母版 PNG

### Goal densities
- `goal-icons/drawable-mdpi/` 48×48
- `goal-icons/drawable-hdpi/` 72×72
- `goal-icons/drawable-xhdpi/` 96×96
- `goal-icons/drawable-xxhdpi/` 144×144
- `goal-icons/drawable-xxxhdpi/` 192×192

### Android res ready
- `android/app/src/main/res/drawable-mdpi/`
- `android/app/src/main/res/drawable-hdpi/`
- `android/app/src/main/res/drawable-xhdpi/`
- `android/app/src/main/res/drawable-xxhdpi/`
- `android/app/src/main/res/drawable-xxxhdpi/`

以下目标分类 Vector Drawable 已复制到 `app/src/main/res/drawable` 并用于 V1.1：
- `@drawable/ic_goal_walk`
- `@drawable/ic_goal_read`
- `@drawable/ic_goal_sleep`
- `@drawable/ic_goal_water`
- `@drawable/ic_goal_stretch`
- `@drawable/ic_goal_study`
- `@drawable/ic_goal_medicine`
- `@drawable/ic_goal_other`

## 使用建议
- V1.1 创建页提供 8 项明确选择，默认 `other`；设置页可修改当前目标图标。
- 首页、记录页和历史卡片显示持久化图标；完成动画仍固定使用品牌 `1.`。
- 此版为 **带背景插画图标**，更适合内容模块图标，不建议当系统 launcher icon 使用。
- 如需透明背景版本，建议后续基于同一风格继续单独出一套。

## Play Store 备用素材

- `brand/play_store_icon_full_square.svg`
- `brand/play_store_icon_full_square_512.png`

以上为完整方形、无预制圆角和外部阴影的备用素材。旧文件 `play_store_icon_512.png` 保留作历史视觉参考，不作为未来 Play Console 上传文件。当前 V1 仍只做本地发布。

## 工程接入清单

当前 Android 工程接入以下品牌资源：

- `drawable/ic_launcher_foreground.xml`
- `drawable/ic_launcher_monochrome.xml`
- `mipmap-anydpi/ic_launcher.xml`
- `mipmap-anydpi/ic_launcher_round.xml`
- `mipmap-anydpi-v33/` monochrome 声明
- `drawable/ic_goal_walk.xml`
- `drawable/ic_goal_read.xml`
- `drawable/ic_goal_sleep.xml`
- `drawable/ic_goal_water.xml`
- `drawable/ic_goal_stretch.xml`
- `drawable/ic_goal_study.xml`
- `drawable/ic_goal_medicine.xml`
- `drawable/ic_goal_other.xml`

设计包中的多密度目标 PNG 不复制进 `app` 模块；运行时统一使用 Vector Drawable，避免 APK 重复资源。

## 预览
- `goal-icons/preview/goal_icons_preview_sheet.png`
- `goal-icons/preview/source_preview_board.png`
