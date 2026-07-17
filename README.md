# RepoMuse AI

[![Kotlin](https://img.shields.io/badge/Kotlin-Android-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://www.android.com/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/compose)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-FFCA28?logo=firebase&logoColor=black)](https://firebase.google.com/)
[![Gemini API](https://img.shields.io/badge/AI-Gemini%20API-4285F4?logo=google&logoColor=white)](https://ai.google.dev/)
[![MIT License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Latest release](https://img.shields.io/github/v/release/AK-Lmn/RepoMuse?display_name=tag&sort=semver)](https://github.com/AK-Lmn/RepoMuse/releases/latest)

RepoMuse AI is a native Android app that turns public GitHub repositories into professional, portfolio-ready case studies. It uses the GitHub REST API and Google Gemini API via REST/OkHttp to draft pitches, problem statements, core features, technical challenges, and resume bullet points.

## Download

Download the APK from [GitHub Releases](https://github.com/AK-Lmn/RepoMuse/releases/latest). Android may display a warning when installing an APK obtained outside the Play Store; review the source and install only if you trust it.

## Screen Showcase

<p align="center">
  <img src="assets/onboarding_mockup.png" width="300" alt="RepoMuse onboarding and login screen" />
  <img src="assets/dashboard_mockup.png" width="300" alt="RepoMuse project dashboard" />
</p>
<p align="center"><em>Left: onboarding and login. Right: the project dashboard.</em></p>

## Features

- **GitHub metadata extraction:** Paste a public repository URL to fetch its primary language, topics, and description.
- **AI case-study generation:** Uses the Gemini API (`gemini-3.5-flash`) to draft pitches, problem statements, challenges, and resume bullets.
- **Offline-first storage with sync:** Room stores projects locally; Cloud Firestore synchronizes them when signed in.
- **GitHub authentication:** Firebase Authentication handles GitHub sign-in.
- **PDF export:** Exports a finished case study to a structured PDF on the device.
- **Native UI:** Kotlin and Jetpack Compose with Material Design 3.

## Status

v1.0.0 is the first public release. The app is functional but still early-stage. To build locally, provide your own Gemini API key and Firebase configuration. Users who only want to try the app can download the APK through [Releases](https://github.com/AK-Lmn/RepoMuse/releases/latest).

## Tech Stack

- **Platform:** Native Android
- **Language:** Kotlin
- **UI:** Jetpack Compose (Material Design 3)
- **Architecture:** MVVM with Kotlin Coroutines and Flow
- **Local storage:** Room
- **Cloud:** Firebase Authentication and Cloud Firestore
- **Repository data:** GitHub REST API via OkHttp
- **AI:** Google Gemini API via REST/OkHttp (`gemini-3.5-flash`)
- **PDF export:** Android `PdfDocument`, Canvas, and Paint APIs

## Known Limitations

- Repository metadata fetches use public GitHub repository data.
- Unauthenticated GitHub API requests may be rate-limited.
- AI generation depends on the user's Gemini API key.
- Firebase login and sync require a local `app/google-services.json` setup.
- The APK is not Play Store signed or distributed yet.

## Build Locally

### Prerequisites

- Android Studio (Ladybug or newer recommended)
- JDK 17
- Gradle 9.6.1-compatible environment

### Setup

1. Clone the repository:

   ```bash
   git clone https://github.com/AK-Lmn/RepoMuse.git
   ```

2. Create a root `.env` file using `.env.example`, then add your own Gemini API key:

   ```env
   GEMINI_API_KEY=YOUR_API_KEY
   ```

3. Create a Firebase project, register Android package `com.aistudio.repomuse.mxyzp`, and download its `google-services.json` file.
4. Place that file at `app/google-services.json`. It is intentionally ignored by Git; `app/google-services.example.json` is a safe template.
5. In Firebase Authentication, enable GitHub sign-in and configure the GitHub OAuth application. Enable Cloud Firestore if you want sync.
6. Open the project in Android Studio, sync Gradle, and run the `app` configuration, or run:

   ```bash
   ./gradlew installDebug
   ```

## Security

`.env` and `google-services.json` are ignored and must never be committed. No real API keys, Firebase configuration, or other credentials are included in this repository. If a key is exposed, revoke or rotate it in the [Google Cloud Console](https://console.cloud.google.com/apis/credentials) before dismissing any secret-scanning alert.

## Support and Contact

For bugs or feature requests, open a [GitHub Issue](https://github.com/AK-Lmn/RepoMuse/issues). RepoMuse is an independent app and is not an official GitHub product.

## License

Released under the [MIT License](LICENSE).
