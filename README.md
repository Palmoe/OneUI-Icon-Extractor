![One UI Icon Extractor Banner](./docs/images/banner.webp)

# One UI Icon Extractor

**English** | [한국어](./README.ko.md) | [日本語](./README.ja.md) | [简体中文](./README.zh-Hans.md) | [繁體中文](./README.zh-Hant.md)

Since One UI 8.5, a beautiful 3D shadow effect has been added to all app icons, and Samsung's default and system app icons are specifically designed to incorporate this detail.<br>
However, most app icon extraction tools fail to capture this shadow effect. Unfortunately, this means that icons extracted and distributed by users, or those found on various wikis, often lack this visual element.

To properly extract icons with this effect intact, you must use the `semGetApplicationIconForIconTray(...)` or `semGetActivityIconForIconTray(...)` methods, which operate exclusively on Samsung devices.<br>
This was discovered by reverse engineering One UI Home, and One UI Icon Extractor utilizes these exact methods. (Note: The 3D effect is not applied to certain activities, which appears to be either an intended behavior of One UI or a bug.)

While this app also works on non-Samsung Android devices, it will fall back to using standard Android APIs in those environments.

## Installation
You can download the app from the GitHub Releases page.<br>
**[▶ Go to GitHub Releases](https://github.com/Palmoe/OneUI-Icon-Extractor/releases/latest)**

## Comparison

| Standard Android API | One UI Icon Extractor |
| :---: | :---: |
| <img src="./docs/images/compare_original_1.webp" width="240"> | <img src="./docs/images/compare_new_1.webp" width="240"> |
| <img src="./docs/images/compare_original_2.webp" width="240"> | <img src="./docs/images/compare_new_2.webp" width="240"> |

## Screenshots

| <img src="./docs/images/screenshot_1.webp" width="280"> | <img src="./docs/images/screenshot_2.webp" width="280"> |
| :---: | :---: |

## Pro Tip (For developers)
If you want to apply the One UI Sans font to your own app, declare the `sec` font family before `sans-serif`.

## Disclaimer
***This is an independent open-source project and is not affiliated with, endorsed by, or associated with Samsung Electronics Co., Ltd. in any way. All rights to the icons, logos, and trademarks used on this page for illustrative purposes belong to their original creator, Samsung.***

## License
Unless otherwise noted, the source code in this repository is licensed under [Apache-2.0](./LICENSE).<br>
Icons, logos, screenshots, trademarks, and other third-party visual assets are not covered by that license. See [NOTICE](./NOTICE).
