# V1.1 实体机视觉与升级证据

测试日期：2026-09-05

设备：Xiaomi `2106118C`，Android 14 / API 34，HyperOS `V816.0.8.0.UKMCNXM`，1080×2400、440dpi。

## 独立 Debug 主流程

| 文件 | 内容 |
| --- | --- |
| `physical_debug_01_welcome.png` | 全新数据欢迎页 |
| `physical_debug_02_create.png` | 创建页 8 个图标与默认“其他” |
| `physical_debug_05_home_before.png` | 未完成首页与所选走路图标 |
| `physical_debug_06_animation_mid.png` | Logo 点亮动画中间帧 |
| `physical_debug_07_home_done.png` | 首次完成后的成功首页 |
| `physical_debug_08_settings.png` | 设置页图标入口 |
| `physical_debug_11_restart.png` | 改为喝水图标并杀进程重开后的持久化结果 |
| `physical_debug_12_record.png` | 记录页图标、日期与三项统计 |

## 正式版覆盖与系统状态

| 文件 | 内容 |
| --- | --- |
| `physical_v1_before_upgrade.png` | `1.0.0 (1)` 覆盖前首页 |
| `physical_v1_settings_before_upgrade.png` | 覆盖前提醒与版本信息 |
| `physical_v11_14_settings_after_upgrade.png` | `1.1.0 (2)` 覆盖后设置与迁移图标 |
| `physical_v11_15_dark_system.png` | 深色系统下固定浅色主题 |
| `physical_v11_16_landscape.png` | 横屏滚动布局 |
| `physical_v11_18_final_apk.png` | `295b1dc` 最终签名 APK 的首页齿轮与保留数据 |

正式覆盖未卸载、未清除数据；目标、9 月 3–5 日 Completion、连续第 3 天、20:00 提醒和通知权限均保留。最终截图验证右上角齿轮唯一存在，旧居中“设置 ›”入口已移除。

截图顶部如出现触点坐标或时间条，为测试时启用的 Android 开发者系统叠层，不是应用界面。
