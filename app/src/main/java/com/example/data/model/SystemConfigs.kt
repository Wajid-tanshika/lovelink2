package com.example.data.model

data class FirebaseAppConfig(
    val googleServicesJson: String = "",
    val projectId: String = "lovelink-app-prod",
    val storageBucket: String = "lovelink-app-prod.appspot.com",
    val apiKey: String = "AIzaSyD-LovelinkProdApiKeySample2026",
    val appId: String = "1:1234567890:android:abc123def456",
    val isAuthEnabled: Boolean = true,
    val isFirestoreEnabled: Boolean = true,
    val isStorageEnabled: Boolean = true,
    val isFcmEnabled: Boolean = true,
    val isAnalyticsEnabled: Boolean = true,
    val isCrashlyticsEnabled: Boolean = true,
    val isAppCheckEnabled: Boolean = true,
    val lastValidatedTimestamp: Long = System.currentTimeMillis()
)

enum class CallingProvider {
    AGORA,
    WEBRTC,
    JITSI
}

data class CallingProviderConfig(
    val selectedProvider: CallingProvider = CallingProvider.AGORA,
    val agoraAppId: String = "agora_app_id_sample_883921",
    val agoraAppCertificate: String = "",
    val agoraTempToken: String = "",
    val agoraChannelName: String = "lovelink_channel",
    val agoraTokenServerUrl: String = "https://agora-token-service.lovelink.app/token",
    val webrtcSignalingUrl: String = "wss://webrtc-signaling.lovelink.app",
    val jitsiServerUrl: String = "https://meet.jit.si/lovelink",
    val isAudioEnabled: Boolean = true,
    val isVideoEnabled: Boolean = true
)

enum class StorageProvider {
    FIREBASE_STORAGE,
    CUSTOM_S3,
    CLOUDINARY
}

data class StorageProviderConfig(
    val selectedProvider: StorageProvider = StorageProvider.FIREBASE_STORAGE,
    val firebaseBucket: String = "lovelink-app-prod.appspot.com",
    val customEndpointUrl: String = "https://s3.us-east-1.amazonaws.com/lovelink-media",
    val cloudinaryCloudName: String = "",
    val cloudinaryUploadPreset: String = "",
    val compressImages: Boolean = true,
    val imageQualityRatio: Int = 80, // 80%
    val maxImageDimensionPx: Int = 1920,
    val compressVideos: Boolean = true,
    val videoMaxBitrateKbps: Int = 2000
)
