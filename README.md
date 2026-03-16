# FoodShare - Android Application

**Share your meals with the world!**

An Android application for sharing food photos with nutrition information.

## Authors
- **Kobi Shabaton** (212358477)
- **Itay Benbenisti** (212374797)

## Tech Stack
- Kotlin
- MVVM Architecture
- Firebase Authentication
- Room Database (local cache)
- Retrofit (API calls)
- Navigation Component + SafeArgs
- Material Design

## Features
- User authentication via Firebase
- Post meals with images and descriptions
- Nutrition information from external API
- Like and comment on posts
- User profiles with stats
- Local caching with Room database

## Architecture
```
app/
├── data/
│   ├── local/         # Room database
│   ├── remote/        # Retrofit API
│   └── repository/    # Data repository
│
├── ui/
│   ├── auth/          # Login/Register
│   ├── feed/          # Main feed
│   ├── post/          # Post details, create/edit
│   ├── profile/       # User profile
│   └── common/        # Shared components
│
├── viewmodel/         # ViewModels
│
└── utils/             # Utilities
```

## Requirements
- Android Studio Arctic Fox or later
- Minimum SDK: 24 (Android 7.0)
- Target SDK: 34 (Android 14)

## Setup
1. Clone the repository
2. Open in Android Studio
3. Add `google-services.json` from Firebase Console
4. Sync Gradle
5. Run on emulator or device

## API
Connects to FoodShare Backend API:
- Base URL configured in `app/build.gradle`
- Uses Firebase token for authentication

## License
ISC
