# OneThing 完成动画设计资产

本目录保存从 ChatGPT 对话《一件 规划简单记录产品》中归档并重建的完成动画设计稿。

## 文件

- `OneThing_Complete_Animation_Spec_Approved_V1.0.jpg`：用户于 2026-09-05 明确确认的权威目标设计稿，所有视觉和动效验收以此为准；
- `OneThing_Complete_Animation_Reconstructed_Draft_V1.0.svg`：收到权威稿之前，根据对话文字重建的 8 帧可编辑草稿；
- `OneThing_Complete_Animation_Reconstructed_Draft_V1.0.png`：上述重建草稿的位图预览，不作为最终验收依据；
- 完整动效、Lottie、Android 接入和验收规范见 `docs/一件_OneThing_完成动画动效规范_V1.0.md`。

## 来源边界

任务读取接口最初只提供最终“Logo 点亮版”的文字方案，没有暴露图片附件、Lottie JSON 或 AE 工程，因此本地先产生了一版重建草稿。用户随后直接提供目标设计图，原图已原样保存为 `OneThing_Complete_Animation_Spec_Approved_V1.0.jpg`；该文件优先级高于重建 SVG/PNG。

## 当前状态

V1.1 已按权威 JPG 接入 Logo 点亮完成动画，只采用权威稿的动画核心，没有恢复旧版底部导航或改变现行首页信息架构。Android 运行时资源位于 `app/src/main/res/raw/complete_*.json`，生成脚本为 `tools/generate_completion_lottie.mjs`；Compose 负责动态文案、业务状态、无障碍和降级路径。视觉验收证据见根目录 `design-qa.md` 与 `design/qa/v1.1/`。
