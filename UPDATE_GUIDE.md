# SecureTrack In-App Update Guide

This document explains the Over-The-Air (OTA) update mechanism implemented in SecureTrack. It is intended for developers and AI agents to understand how to maintain and release updates.

## Architecture

The app uses a simple, serverless update mechanism hosted entirely on GitHub.

- **Check Logic**: `com.securetrack.utils.UpdateChecker` fetches a raw JSON file from the GitHub repository.
- **Source of Truth**: `version.json` in the root of the `main` branch.
- **APK Hosting**: The APK is downloaded directly from the repository root (or Releases, if configured, but currently set to root `app-debug.apk`).

### Key Files
1.  **`version.json`**: Contains the latest version info (`versionCode`, `url`, `changes`).
2.  **`app/src/main/java/com/securetrack/utils/UpdateChecker.kt`**: Handles network check, version comparison, and download.
3.  **`app/src/main/res/xml/provider_paths.xml`**: Defines secure storage paths to share the downloaded APK with the system installer.

## How to Release a New Update

To push an update to users, follow these exact steps:

1.  **Update App Version**:
    - Open `app/build.gradle.kts` (or `build.gradle`).
    - Increment `versionCode` (e.g., `2` -> `3`) and `versionName` (e.g., `1.1.0` -> `1.2.0`).

2.  **Build the APK**:
    Run the Gradle build command:
    ```bash
    ./gradlew assembleDebug
    ```
    The APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`.

3.  **Update `version.json`**:
    Edit `version.json` in the project root. Update the fields to match your new build:
    ```json
    {
        "versionCode": 3,      // Must match the new versionCode in build.gradle
        "versionName": "1.2.0",
        "url": "https://github.com/Iamjunade/securetrack/raw/main/app-debug.apk",
        "changes": "- Description of what changed in this release"
    }
    ```
    *Note: The URL is configured to always pull `app-debug.apk` from the main branch root.*

4.  **Deploy**:
    Copy the built APK to the root and push everything to GitHub:
    ```bash
    cp app/build/outputs/apk/debug/app-debug.apk .
    git add version.json app-debug.apk app/build.gradle.kts
    git commit -m "chore: release version 1.2.0"
    git push
    ```

## Troubleshooting

-   **App says "You are up to date"**:
    -   Ensure `version.json` on GitHub has a higher `versionCode` than the installed app.
    -   Ensure you committed and pushed the changes to the `main` branch.

-   **Download Fails**:
    -   Check if the `url` in `version.json` is accessible. Open it in a browser; it should trigger a download log.

-   **Parse Error during Install**:
    -   This usually means the APK was corrupted during download or upload. Ensure Git LFS is not interfering (though not used here) and that the file size on GitHub matches the local build.
