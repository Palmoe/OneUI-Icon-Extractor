![One UI Icon Extractor Banner](./docs/images/banner.webp)

# One UI Icon Extractor - One UI 아이콘 추출기

[English](./README.md) | **한국어** | [日本語](./README.ja.md) | [简体中文](./README.zh-Hans.md) | [繁體中文](./README.zh-Hant.md)

One UI 8.5부터 모든 앱의 아이콘에는 멋진 3D 그림자 효과가 추가되었고, 삼성의 기본 앱 및 시스템 앱 아이콘은 이를 고려하여 디자인되었습니다.<br>
하지만 대부분의 앱 아이콘 추출 도구는 이를 포함하여 아이콘을 반환하지 않으며, 사용자가 추출하여 배포하거나 각종 wiki 등에 등록된 아이콘에도 아쉽게도 이는 반영되어있지 않습니다.

이를 실제로 반환하기 위해서는 삼성 기기에서만 독점으로 작동하는 `semGetApplicationIconForIconTray(...)` 혹은 `semGetActivityIconForIconTray(...)` 을 사용해야 합니다.<br>
이는 One UI Home의 역공학을 통해 알아냈으며, One UI Icon Extractor는 이를 사용합니다. (단, 일부 액티비티에서는 3D 효과가 적용되지 않으며, 이는 One UI의 의도된 동작 혹은 버그인 것으로 보입니다.)

이 앱은 One UI뿐만이 아니라 타사 Android에서도 작동하지만, 이 때는 Android의 기본 API를 사용합니다.

## 설치
GitHub Releases 페이지에서 다운로드할 수 있습니다.<br>
**[▶ GitHub Releases 페이지로 바로가기](https://github.com/Palmoe/OneUI-Icon-Extractor/releases/latest)**

## 비교

| 기본 Android API | One UI Icon Extractor |
| :---: | :---: |
| <img src="./docs/images/compare_original_1.webp" width="240"> | <img src="./docs/images/compare_new_1.webp" width="240"> |
| <img src="./docs/images/compare_original_2.webp" width="240"> | <img src="./docs/images/compare_new_2.webp" width="240"> |

## 스크린샷

| <img src="./docs/images/screenshot_1.webp" width="280"> | <img src="./docs/images/screenshot_2.webp" width="280"> |
| :---: | :---: |

## 프로 팁 (개발자 전용)
자신의 앱에 One UI Sans를 반영하고 싶다면, `sec` 폰트 패밀리를 `sans-serif` 앞에 선언하세요.

## 면책 조항
***이 프로젝트는 독립적인 오픈소스 프로젝트이며, Samsung Electronics Co., Ltd.와 어떠한 제휴, 보증 또는 관련도 없습니다. 본 페이지에 설명 목적으로 사용된 아이콘, 로고 및 상표의 모든 권리는 원저작자인 삼성에 있습니다.***

## 라이선스
별도 표기가 없는 한, 이 저장소의 소스 코드는 [Apache-2.0](./LICENSE) 라이선스를 따릅니다.<br>
아이콘, 로고, 스크린샷, 상표 및 기타 서드파티 시각 자산은 해당 라이선스에 포함되지 않습니다. 자세한 내용은 [NOTICE](./NOTICE)를 참고하세요.
