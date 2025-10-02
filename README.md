# Med Box AI Android App

Med Box AI is an Android application that helps caregivers and patients manage daily medication reminders. Built entirely with Kotlin and Jetpack Compose, the app stores schedules locally, delivers high-priority notifications, and automatically reschedules alarms so patients never miss a dose.

## Features

- 📅 **Daily scheduling** – Capture medication name, dosage, instructions, and reminder time.
- 🔔 **Reliable alerts** – Uses the Android `AlarmManager` API with exact alarms and foreground notifications.
- ✅ **One-tap adherence tracking** – Enable or pause reminders directly from the schedule list.
- ♻️ **Automatic recovery** – Reminders are restored when the device reboots.
- 🧱 **Offline-first storage** – Powered by Room for persistent, on-device data.

## Project structure

```
.
├── app
│   ├── build.gradle.kts        # Android module configuration
│   ├── src/main
│   │   ├── AndroidManifest.xml  # App manifest, permissions, and receivers
│   │   ├── java/com/medboxai/reminder
│   │   │   ├── data             # Room database entities, DAO, repository
│   │   │   ├── notifications    # Alarm + notification helpers
│   │   │   └── ui               # Main activity, Compose UI, and theme
│   │   └── res                  # Material theme, strings, icons, and layouts
├── build.gradle.kts            # Top-level Gradle configuration
├── gradle.properties           # Shared Gradle configuration
└── settings.gradle.kts         # Module listing
```

## Getting started

1. **Open in Android Studio**
   - From the welcome screen choose _Open_, then select the repository directory.
   - Android Studio will download the Android Gradle Plugin and project dependencies.

2. **Run on a device or emulator**
   - Use a device running Android 8.0 (API 26) or higher.
   - Grant the _Post Notifications_ permission when prompted on Android 13+.

3. **Create release build**
   - Use **Build → Generate Signed Bundle / APK…** to prepare a Play Store ready artifact.

## Testing

The project includes standard Android instrumentation and unit test dependencies. Tests can be run from Android Studio or the command line:

```bash
./gradlew test
./gradlew connectedAndroidTest
```

> Note: Running Gradle tasks requires the Android SDK and build tools, which are only available in a full Android development environment.

## Play Store readiness checklist

- Update the app icon and theme colors to match your brand.
- Add localized strings for your supported languages.
- Configure Play Store assets (screenshots, feature graphics, privacy policy).
- Sign release builds with your Play App Signing key.

Med Box AI provides a solid Kotlin foundation you can extend with adherence analytics, caregiver dashboards, or integrations with Bluetooth pill dispensers.
