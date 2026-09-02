# UI 设计资料

## 设计源

- [Figma：一件 完整交互原型 V1](https://www.figma.com/design/XEHhVWEe14BACPoGeKYD76/%E4%B8%80%E4%BB%B6_%E5%AE%8C%E6%95%B4%E4%BA%A4%E4%BA%92%E5%8E%9F%E5%9E%8B_V1?node-id=0-1&p=f&t=2BpdK8rB5ew8IMc2-0)

Figma 文件是当前 UI 视觉和交互设计的源文件。仓库中的静态设计图用于研发查看、评审留档和版本追踪，不替代 Figma 源文件。

## UI 设计图导出状态

当前仓库暂未包含正式 UI 图片导出文件。待从 Figma 导出后，建议放入 `design/screens/`，并按页面与状态命名：

```text
design/
├── README.md
└── screens/
    ├── 01-create-default.png
    ├── 02-home-uncompleted.png
    ├── 03-home-completed.png
    ├── 04-completion-feedback.png
    ├── 05-record-calendar.png
    └── 06-settings.png
```

## 导出要求

- 格式：PNG
- 比例：优先 2x
- 范围：完整 Frame，不带 Figma 编辑器界面
- 命名：两位顺序号 + 页面或状态英文名
- 更新：Figma 原型有影响开发或验收的变更时，同步覆盖导出图并在提交说明中注明

