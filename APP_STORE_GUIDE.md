# 앱 출시 안내 (Google Play · App Store)

기존 웹 사이트를 Capacitor로 감싼 앱입니다. 응시자용이며 관리자·감독 화면은 앱에서 숨겨집니다(관리자 기능은 서버에서도 앱 요청을 거부). 시험 문제와 채점은 기존처럼 Netlify 서버 기능에서 처리합니다.

## 앱이 하는 일 (웹과 다른 점)
| 항목 | Android | iOS |
|---|---|---|
| 스크린샷·화면 녹화 | **원천 차단** (검은 화면, FLAG_SECURE) | 사전 차단 불가 |
| 스크린샷 감지 | Android 14 이상 감지 → 즉시 종료 | 감지 즉시 종료 (찍힌 사진은 회수 불가) |
| 화면 녹화·미러링 감지 | Android 15 이상 | 녹화 시작·진행 중 감지 → 즉시 종료 |
| 관리자 알림 | 종료 기록이 관리자 화면에 10초 이내 팝업 | 동일 |

Android 13 이하는 차단(검은 화면)만 되고 감지 신호는 없습니다. iOS는 시스템 정책상 캡처를 미리 막는 방법이 없습니다.

## 반드시 필요한 것 (사용자 준비)
1. **개발자 계정**: Google Play(1회 25달러), Apple Developer Program(연 99달러). 업체 명의면 사업자 정보(D-U-N-S 번호)가 필요합니다.
2. **앱 식별자(번들 ID)**: 지금은 임시값 `kr.co.safetyexam.app`입니다. 한 번 출시하면 바꿀 수 없으니 확정 후 `capacitor.config.json`, `android/app/build.gradle`, `ios/App/App.xcodeproj`에서 함께 바꿉니다.
3. **개인정보처리방침 URL**: 이름·업체명·생년월일을 수집하므로 양쪽 스토어 모두 필수입니다. 스토어의 데이터 안전/개인정보 항목도 같은 내용으로 작성합니다.
4. **아이콘·스크린샷**: 1024×1024 아이콘, 폰 스크린샷 각 2장 이상.
5. **Mac(또는 클라우드 빌드)**: iOS 제출은 Xcode가 필요합니다. Mac이 없으면 GitHub Actions(macOS)나 Codemagic 같은 클라우드 빌드를 씁니다. 서명 인증서·프로비저닝은 계정 주인이 발급해야 합니다.

## 심사에서 걸릴 수 있는 점
- **심사용 계정/코드**: 시험은 6자리 코드가 있어야 들어갈 수 있고 코드는 2시간 후 만료됩니다. 심사원이 앱을 열어볼 수 없으면 반려됩니다. 심사 기간에 쓸 **데모 시험 모드**(결과를 저장하지 않는 고정 코드)를 추가하는 것을 권장합니다.
- **Apple 4.2(최소 기능)**: 웹사이트를 그대로 감싼 앱은 반려될 수 있습니다. 네이티브 캡처 방지·감지, 시험 전용 보안 기능을 심사 노트에 명확히 설명합니다.
- **Google Play 신규 개인 계정**: 비공개 테스트에서 테스터 12명이 14일 이상 참여해야 정식 출시할 수 있습니다. 조직 계정은 면제입니다.

## 빌드
- `npm install` → `npx cap sync` → Android: `npx cap open android`(Android Studio), iOS: `npx cap open ios`(Xcode)
- 푸시할 때마다 GitHub Actions(`Mobile build`)가 Android·iOS 컴파일을 확인합니다. 서명된 출시용 파일(AAB/IPA)은 계정과 키 설정 후 만듭니다.
- 서버 환경변수 `APP_ORIGINS`(기본 `https://localhost,capacitor://localhost`)가 앱의 요청을 허용합니다. 서버 주소를 바꾸면 `public/app.js`의 `SERVER_ORIGIN`도 바꿉니다.
- 서명 키(`*.keystore`, `*.jks`)는 저장소에 올리지 않습니다(.gitignore 처리).

## 아직 안 된 것
- 실제 기기에서의 동작 확인 (Android 14·15, iPhone)
- QR을 찍으면 앱이 바로 열리는 딥링크(Android App Links, iOS Universal Links). 지금은 앱에서 코드를 직접 입력합니다.
- 앱 아이콘·스플래시 화면 (현재 Capacitor 기본 이미지)
