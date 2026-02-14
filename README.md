# SecureTrack: Military-Grade Android Defense

![Version](https://img.shields.io/badge/version-2.1.0-neon_cyan?style=for-the-badge&logo=android)
![Status](https://img.shields.io/badge/status-active-success?style=for-the-badge)
![License](https://img.shields.io/badge/license-MIT-blueviolet?style=for-the-badge)
![Platform](https://img.shields.io/badge/platform-Android_10%2B-orange?style=for-the-badge&logo=android)

> **"The ultimate anti-theft defense system that deceives thieves and empowers owners."**

**SecureTrack** is an advanced Android security application designed to prevent device loss and unauthorized access through deception protocols and remote command execution. Built with a "Cyberguard" aesthetic, it combines military-grade functionality with a premium 2026 futuristic UI.

---

## 🚀 Key Features

| Feature | Description | Protection Level |
| :--- | :--- | :--- |
| **Fake Shutdown** | Simulates a convincing "Power Off" sequence while the device remains active in the background. Thieves believe the device is dead, but it's tracking them. | ⭐⭐⭐⭐⭐ (Deception) |
| **Intruder Selfie** | Automatically captures high-resolution photos of anyone attempting to unlock the device or force a shutdown. Logs are saved locally. | ⭐⭐⭐⭐⭐ (Identification) |
| **Shutdown Lock** | Prevents unauthorized power-off attempts by requiring a secure PIN to access the power menu. | ⭐⭐⭐⭐ (Prevention) |
| **Remote Command** | Control your device via SMS commands (Siren, Location, Wipe) even without internet access. | ⭐⭐⭐⭐ (Recovery) |
| **Cyberguard UI** | A fully immersive, glassmorphic interface with CRT scanlines and neon typography. | N/A (UX) |

---

## 🛠️ Architecture

SecureTrack operates using a robust service-based architecture to ensure persistence and reliability:

-   **CoreProtectionService**: The brain of the application. Manages the "Fake Shutdown" state, monitors lock screen interactions, and coordinates intruder capture.
-   **SmsReceiver**: High-priority broadcast receiver that intercepts specific SMS commands to trigger remote actions.
-   **Admin Policy**: Utilizes `DeviceAdminReceiver` to prevent uninstallation and monitor failed unlock attempts.

---

## 📥 Installation

### Prerequisites
-   Android 10.0 (API Level 29) or higher.
-   Device with Camera and GPS.

### Download
Get the latest stable release directly from our website or GitHub Releases.

[**Download APK v2.1.0**](https://securetrack-landing.vercel.app)

---

## 🤝 Contributing

We welcome contributions from the community! Please read our [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

**Current Focus Areas:**
-   [ ] Optimizing Camera API for faster capture.
-   [ ] Enhancing the "Fake Shutdown" animation realism.
-   [ ] expanding SMS command vocabulary.

---

## 🔒 Security

Security is our top priority. If you discover a vulnerability, please see our [SECURITY.md](SECURITY.md) policy for responsible disclosure.

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

<p align="center">
  <small>Designed & Developed by <strong>Meta Minds</strong></small>
</p>
