# SecureTrack ProGuard Rules

# Keep Room entities
-keep class com.securetrack.data.entities.** { *; }

# Keep Device Admin
-keep class com.securetrack.admin.** { *; }

# Keep Broadcast Receivers
-keep class com.securetrack.receivers.** { *; }
