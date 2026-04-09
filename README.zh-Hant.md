![One UI Icon Extractor Banner](./docs/images/banner.webp)

# One UI Icon Extractor - One UI 圖示擷取器

[English](./README.md) | [한국어](./README.ko.md) | [日本語](./README.ja.md) | [简体中文](./README.zh-Hans.md) | **繁體中文**

從 One UI 8.5 開始，所有應用程式的圖示都加入了精美的 3D 陰影效果，三星的預設應用程式及系統應用程式圖示也是以此為基礎進行設計的。<br>
然而，大多數應用程式圖示擷取工具在傳回圖示時並未包含此效果，遺憾的是，使用者自行擷取並發佈，或在各大 wiki 等網站上註冊的圖示也未能體現出這一點。

為了實際傳回帶有該效果的圖示，必須使用僅在三星裝置上獨佔運行的 `semGetApplicationIconForIconTray(...)` 或 `semGetActivityIconForIconTray(...)`。<br>
這是透過對 One UI Home 進行逆向工程得出的，One UI Icon Extractor 正是使用了這一方法。（需要注意的是，在某些 Activity 中並未套用 3D 效果，這似乎是 One UI 的有意設計或是一個 Bug。）

該應用程式不僅能在 One UI 上運行，也支援第三方 Android 裝置，但在這種情況下它將使用 Android 的原生 API。

## 安裝
您可以在 GitHub Releases 頁面下載。<br>
**[▶ 直接前往 GitHub Releases 頁面](https://github.com/Palmoe/OneUI-Icon-Extractor/releases/latest)**

## 比較

| 原生 Android API | One UI Icon Extractor |
| :---: | :---: |
| <img src="./docs/images/compare_original_1.webp" width="240"> | <img src="./docs/images/compare_new_1.webp" width="240"> |
| <img src="./docs/images/compare_original_2.webp" width="240"> | <img src="./docs/images/compare_new_2.webp" width="240"> |

## 螢幕截圖

| <img src="./docs/images/screenshot_1.webp" width="280"> | <img src="./docs/images/screenshot_2.webp" width="280"> |
| :---: | :---: |

## 進階技巧
如果您想在自己的應用程式中套用 One UI Sans，請將 `sec` 字體族宣告在 `sans-serif` 之前。

## 免責聲明
***本專案是一個獨立的開源專案，與 Samsung Electronics Co., Ltd. 沒有任何附屬、贊助或關聯。本頁面中用於說明目的的圖示、標誌和商標的所有權利均歸原作者三星所有。***

## 授權條款
除非另有註明，本儲存庫中的原始碼均以 [Apache-2.0](./LICENSE) 授權釋出。<br>
圖示、標誌、截圖、商標及其他第三方視覺素材不包含在該授權範圍內。詳情請參閱 [NOTICE](./NOTICE)。
