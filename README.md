# Dolphin - Focus Guardian App

Dolphin is a phone usage guardian app for students that monitors distraction apps, detects harmful continuous usage, and shows psychologically intelligent notifications based on the student's own goals and future-self description.

### Live Link: https://drive.google.com/file/d/12dcKG1vzqrJq2YlzyfWrL22OQPwFTJM5/view

## Building the App

### Prerequisites
- Android Studio (latest stable version)
- JDK 8 or higher
- Android SDK with API level 26 (minimum) and 34 (target)

### Build Instructions

1. Open the project in Android Studio
2. Wait for Gradle sync to complete
3. Build the APK:
   - **Debug APK**: `Build > Build Bundle(s) / APK(s) > Build APK(s)`
   - **Or use Gradle**: Run `./gradlew assembleDebug` (Linux/Mac) or `gradlew.bat assembleDebug` (Windows)

The APK will be generated at: `app/build/outputs/apk/debug/app-debug.apk`

### Installation

1. Enable "Install from Unknown Sources" on your Android device
2. Transfer the APK to your device
3. Install and open the app
4. Complete the onboarding flow:
   - Set your goal
   - Describe your future self
   - Select distraction apps
   - Choose strictness mode
   - Set sleep schedule
   - Grant required permissions (Usage Access and Notifications)

## Features

- **Continuous Usage Monitoring**: Tracks app usage in real-time
- **Smart Notifications**: Context-aware nudges based on strictness mode
- **Sleep Schedule Awareness**: Stricter limits during sleep hours
- **Three Strictness Modes**:
  - **Gentle**: 20 min limit (10 min late-night), dismissible reminders
  - **Firm**: 12 min limit (6 min late-night), persistent reminders
  - **Hardcore**: 8 min limit (4 min late-night), identity-based nudges
- **Offline Operation**: All functionality works without internet

## Technical Details

- **Language**: Java
- **Minimum SDK**: 26 (Android 8.0)
- **Target SDK**: 34
- **UI System**: XML layouts with Material Design 3
- **Package**: com.dolphin.focus

## Permissions Required

- **Usage Access**: To monitor which apps are being used
- **Notifications**: To send focus reminders
- **Foreground Service**: To continuously monitor app usage

## Project Structure

```
app/src/main/
├── java/com/dolphin/focus/
│   ├── data/          # ConfigRepository for SharedPreferences
│   ├── model/          # Data models (UserConfig, SleepSchedule, StrictnessMode)
│   ├── notifications/ # NudgeEngine for notifications
│   ├── service/        # UsageMonitorService (foreground service)
│   ├── ui/
│   │   ├── main/       # MainActivity (dashboard)
│   │   ├── onboarding/ # OnboardingActivity
│   │   └── settings/   # SettingsActivity
│   └── util/           # TimeUtils, AppUtils
└── res/                # Resources (layouts, strings, colors, themes)
```






