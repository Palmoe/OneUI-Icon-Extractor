![One UI Icon Extractor Banner](./README_images/banner.webp)

# One UI Icon Extractor - One UI 图标提取器

[English](./README.md) | [한국어](./README.ko.md) | [日本語](./README.ja.md) | **简体中文** | [繁體中文](./README.zh-Hant.md)

从 One UI 8.5 开始，所有应用的图标都加入了精美的 3D 阴影效果，三星的默认应用及系统应用图标也是以此为基础进行设计的。<br>
然而，大多数应用图标提取工具在返回图标时并未包含此效果，遗憾的是，用户自行提取并发布，或在各大 wiki 等网站上注册的图标也未能体现出这一点。

为了实际返回带有该效果的图标，必须使用仅在三星设备上独占运行的 `semGetApplicationIconForIconTray(...)` 或 `semGetActivityIconForIconTray(...)`。<br>
这是通过对 One UI Home 进行逆向工程得出的，One UI Icon Extractor 正是使用了这一方法。（需要注意的是，在某些 Activity 中并未应用 3D 效果，这似乎是 One UI 的有意设计或是一个 Bug。）

该应用不仅能在 One UI 上运行，也支持第三方 Android 设备，但在这种情况下它将使用 Android 的原生 API。

## 安装
您可以在 GitHub Releases 页面下载。<br>
**[▶ 直接前往 GitHub Releases 页面](https://github.com/Palmoe/OneUI-Icon-Extractor/releases/latest)**

## 对比

| 原生 Android API | One UI Icon Extractor |
| :---: | :---: |
| <img src="./README_images/compare_original_1.webp" width="240"> | <img src="./README_images/compare_new_1.webp" width="240"> |
| <img src="./README_images/compare_original_2.webp" width="240"> | <img src="./README_images/compare_new_2.webp" width="240"> |

## 屏幕截图

| <img src="./README_images/screenshot_1.webp" width="280"> | <img src="./README_images/screenshot_2.webp" width="280"> |
| :---: | :---: |

## 进阶技巧
如果您想在自己的应用中应用 One UI Sans，请将 `sec` 字体族声明在 `sans-serif` 之前。

## 免责声明
***本项目是一个独立的开源项目，与 Samsung Electronics Co., Ltd. 没有任何附属、赞助或关联。本页面中用于说明目的的图标、徽标和商标的所有权利均归原作者三星所有。***

## 许可证
除非另有说明，本仓库中的源代码均基于 [Apache-2.0](./LICENSE) 许可发布。<br>
图标、徽标、截图、商标及其他第三方视觉素材不包含在该许可范围内。详情请参阅 [NOTICE](./NOTICE)。
