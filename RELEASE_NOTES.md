# RepoMuse v1.0.0

RepoMuse is a native Android app that turns public GitHub repositories into portfolio-ready case studies.

This is the first public release. The app is functional but still early-stage; APK downloads are available through [GitHub Releases](https://github.com/AK-Lmn/RepoMuse/releases/latest).

## Main features

- Public GitHub repository metadata import and GitHub authentication
- AI-generated pitches, problem statements, features, challenges, and resume bullets
- Local persistence and optional Firestore sync
- PDF export for generated case studies

## Setup and security

Provide your own local `.env` and `app/google-services.json` files before building. These files contain project-specific configuration and are ignored by Git. AI generation requires your own Gemini API key, and Firebase login/sync requires local Firebase configuration. No real API keys, Firebase configuration, or other credentials are included in this repository.
