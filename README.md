# PatientMobile

PatientMobile is an Android application for patients to manage their medical documents, view analysis results, and track health indicators. It connects to a backend API for document storage, OCR processing, and medical result analysis.

## Table of Contents

- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [How It Works](#how-it-works)
- [Navigation Flow](#navigation-flow)
- [Backend Integration](#backend-integration)
- [Build Variants](#build-variants)
- [Testing](#testing)
- [Documentation](#documentation)
- [Getting Started](#getting-started)

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose (Material 3) |
| **Architecture** | MVVM + Clean Architecture |
| **Dependency Injection** | Dagger Hilt |
| **Networking** | Retrofit 2 + OkHttp + Gson |
| **Local Database** | Room (SQLite) |
| **Local Storage** | DataStore Preferences + EncryptedSharedPreferences |
| **Async** | Kotlin Coroutines + Flow |
| **Build System** | Gradle with Kotlin DSL + Version Catalog (`libs.versions.toml`) |

### Key Dependencies

- **AndroidX Compose BOM** `2024.09.03` — UI toolkit
- **Navigation Compose** `2.8.2` — In-app navigation
- **Hilt** `2.52` — Dependency injection
- **Room** `2.6.1` — Local persistence
- **Retrofit** `2.10.0` — REST API client
- **OkHttp** `4.12.0` — HTTP client with logging
- **Security Crypto** `1.1.0-alpha06` — Encrypted token storage
- **DataStore** `1.1.1` — Typed preferences

---

## Architecture

The app follows **Clean Architecture** with three main layers:

```
┌─────────────────────────────────────────┐
│  Presentation Layer  (UI / ViewModels)  │
│  - Screens, Components, Theme           │
├─────────────────────────────────────────┤
│  Domain Layer        (Business Logic)   │
│  - Models, Repository Interfaces        │
├─────────────────────────────────────────┤
│  Data Layer          (Sources / Cache)  │
│  - Remote API, Local DB, DataStore      │
└─────────────────────────────────────────┘
```

### Design Patterns

- **MVVM**: Each screen has a `ViewModel` exposing `UiState` and `Events`.
- **Repository Pattern**: Domain defines interfaces; data layer provides implementations.
- **Dependency Injection**: All modules wired via Hilt (`di/`).
- **Unidirectional Data Flow**: UI emits actions → ViewModel processes → State updates → UI recomposes.

---

## Project Structure

```
PatientMobile/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/semanticsoft/patientmobile/
│   │   │   │   ├── data/              # Data Layer
│   │   │   │   │   ├── local/         # Room DB, DataStore, DAOs
│   │   │   │   │   ├── model/         # Data-layer models (dashboard DTOs)
│   │   │   │   │   ├── remote/        # API service, interceptors, DTOs
│   │   │   │   │   └── repository/    # Repository implementations
│   │   │   │   ├── domain/            # Domain Layer
│   │   │   │   │   ├── model/         # Domain entities (User, MedicalResult, etc.)
│   │   │   │   │   └── repository/    # Repository interfaces
│   │   │   │   ├── di/                # Hilt Modules
│   │   │   │   ├── ui/                # Presentation Layer
│   │   │   │   │   ├── common/        # Shared UI utilities (spacing, system bars)
│   │   │   │   │   ├── components/    # Reusable Compose components
│   │   │   │   │   ├── navigation/    # Navigation destinations
│   │   │   │   │   ├── screens/       # Feature screens + ViewModels
│   │   │   │   │   └── theme/         # Colors, Typography, Theme
│   │   │   │   ├── util/              # Utilities & exceptions
│   │   │   │   ├── MainActivity.kt    # Entry activity
│   │   │   │   ├── PatientMobileApp.kt # Root navigation host
│   │   │   │   └── PatientApplication.kt # Application class (Hilt)
│   │   │   └── res/                   # Android resources (drawables, mipmap, values, xml)
│   │   ├── test/                      # Unit tests
│   │   └── androidTest/               # Instrumented tests
│   ├── build.gradle.kts               # App-level build config
│   └── proguard-rules.pro
├── docs/                              # Project documentation
├── gradle/
│   └── libs.versions.toml             # Version catalog
├── build.gradle.kts                   # Root build config
├── settings.gradle.kts
└── gradle.properties
```

### Where to Find Key Components

| Component | Location |
|-----------|----------|
| **Application Entry** | `MainActivity.kt` |
| **Root Navigation** | `PatientMobileApp.kt` |
| **Navigation Routes** | `ui/navigation/AppDestination.kt` |
| **API Interface** | `data/remote/api/PatientApiService.kt` |
| **Database** | `data/local/db/PatientDatabase.kt` |
| **DAOs** | `data/local/dao/` |
| **Entities** | `data/local/db/entity/` |
| **Domain Models** | `domain/model/` |
| **Repository Interfaces** | `domain/repository/` |
| **Repository Implementations** | `data/repository/` |
| **DI Modules** | `di/` |
| **UI Screens** | `ui/screens/{feature}/` |
| **Shared Components** | `ui/components/` |
| **Theme** | `ui/theme/` |
| **Custom Exceptions** | `util/exceptions/` |

---

## How It Works

### App Launch

1. `MainActivity.kt` sets up the window (light status bar, white background) and calls `setContent { PatientMobileTheme { PatientMobileApp() } }`.
2. `PatientMobileApp.kt` hosts a `NavHost` with `AppDestination.Login` as the start destination.
3. `PatientApplication.kt` is annotated with `@HiltAndroidApp` to initialize the DI graph.

### Authentication Flow

- **Login** (`ui/screens/login/`)
  - `LoginScreen.kt` — Compose UI with email/password fields.
  - `LoginViewModel.kt` — Handles login action, error mapping (invalid credentials, rate limit, API errors), and emits `LoginEvent.LoginSuccess` to trigger navigation.
  - On success, the user is navigated to the Dashboard with the back stack cleared.

- **Registration** (`ui/screens/registration/`)
  - `RegistrationScreen.kt` — Collects role, name, email, password, confirm password.
  - `RegistrationViewModel.kt` — Validates input and calls `AuthRepository.register()`.
  - On success, navigates to Dashboard.

### Post-Login Navigation

`NavScreen.kt` provides a **Modal Navigation Drawer** with tabs:

| Drawer Item | Screen | File |
|-------------|--------|------|
| Panou principal | Dashboard | `DashboardScreen.kt` |
| Istoric medical | Medical History | `MedicalHystoryScreen.kt` |

The drawer shows the user's profile, role, and a logout button.

### Dashboard (`ui/screens/dashboard/`)

- Displays:
  - Greeting and user info
  - Attention items (markers needing attention)
  - Basic health indicators with trends
  - Marker categories
  - General marker cards
  - AI summary
  - Warning cards
  - Clinical pillar cards
- **Demo Mode**: If no documents are uploaded or the API fails, the dashboard falls back to **demo data** so the UI is always usable.
- `DashboardViewModel.kt` fetches documents and medical results, computes summaries, and builds an AI summary string.

### Medical History (`ui/screens/medicalHystory/`)

- Lists uploaded documents and their analysis history.
- `MedicalHystoryViewModel.kt` manages the state.

### File Upload (`ui/screens/uploadFile/`)

- Dialog-style overlay for uploading medical documents (PDF, JPG, PNG).
- `UploadFileViewModel.kt` handles file selection and multipart upload via `DocumentRepository`.

### Analysis History (`ui/screens/navigation/`)

- `AnalysisHistoryScreen.kt` — Dedicated screen for viewing past analyses.

---

## Backend Integration

### API Service

`PatientApiService.kt` defines all REST endpoints:

| Method | Endpoint | Auth | Purpose |
|--------|----------|------|---------|
| POST | `/auth/register` | No | Create account |
| POST | `/auth/login` | No | Login & receive JWT |
| POST | `/auth/refresh` | No | Refresh tokens |
| POST | `/auth/logout` | Yes | Revoke refresh token |
| GET | `/auth/me` | Yes | Get current user |
| POST | `/patient/documents` | Yes | Upload document (multipart) |
| GET | `/patient/documents` | Yes | List documents (paginated) |
| GET | `/patient/documents/{id}` | Yes | Document metadata |
| GET | `/patient/documents/{id}/file` | Yes | Download file |
| GET | `/patient/documents/{id}/results` | Yes | Medical results for document |

### Network Stack

- **Base URL**: Set per build type in `build.gradle.kts` (see [Build Variants](#build-variants)).
- **AuthInterceptor**: Automatically attaches `Authorization: Bearer <token>` to requests.
- **ErrorInterceptor**: Maps HTTP errors to typed exceptions (`InvalidCredentialsException`, `RateLimitException`, `ApiException`, etc.).
- **Logging**: OkHttp logging is enabled only in `DEBUG` builds.
- **Gson**: Custom serializers for `LocalDate` and `Instant`.

### Token Security

- Tokens are encrypted using AndroidX Security Crypto (`EncryptedTokenManager.kt`).
- `SecureTokenStore.kt` provides the underlying encrypted storage.
- `UserPreferencesManager.kt` handles non-sensitive preferences via DataStore.

### Local Database (Room)

`PatientDatabase.kt` contains five entities:

| Entity | DAO | Purpose |
|--------|-----|---------|
| `UserEntity` | `UserDao` | Cached user profile |
| `DocumentEntity` | `DocumentDao` | Cached document list |
| `MedicalResultEntity` | `MedicalResultDao` | Cached analysis results |
| `MedicalReportEntity` | `MedicalReportDao` | Cached reports |
| `AuditLogEntity` | `AuditLogDao` | Security/audit logs |

- Migrations: Currently uses destructive fallback (`fallbackToDestructiveMigration`) until explicit migrations are added.

### Repository Implementations

| Repository | File | Responsibility |
|------------|------|----------------|
| `AuthRepository` | `AuthRepositoryImpl.kt` | Login, register, refresh, logout, user profile |
| `DocumentRepository` | `DocumentRepositoryImpl.kt` | Upload, list, download documents; syncs with Room |
| `MedicalResultRepository` | `MedicalResultRepositoryImpl.kt` | Fetch results by document; syncs with Room |
| `AuditRepository` | `AuditRepositoryImpl.kt` | Local audit logging |

All repositories emit `Flow<Resource<T>>` for UI consumption:
- `Resource.Loading` — ongoing operation
- `Resource.Success(data)` — completed successfully
- `Resource.Error(message)` — failed with error message

---

## Build Variants

Configured in `app/build.gradle.kts`:

| Build Type | `BASE_URL` | Use Case |
|------------|-----------|----------|
| `debug` | `http://10.0.2.2:8080/api/v1/` | Local emulator testing |
| `dev` | `https://dev-api.example.com/api/v1/` | Shared dev backend |
| `release` | `https://api.example.com/api/v1/` | Production |

**Compile SDK**: `34`  
**Min SDK**: `29` (Android 10+)  
**Target SDK**: `34`  
**Java/Kotlin Target**: `11`

---

## Testing

### Unit Tests (`src/test/`)

| Test Category | Location |
|---------------|----------|
| ViewModel Tests | `ui/screens/{feature}/` |
| Repository Tests | `data/repository/` |
| Utility Tests | `util/` |
| Coroutine Rule | `testutil/MainDispatcherRule.kt` |

**Libraries**: JUnit 4, Mockito, Mockito-Kotlin, kotlinx-coroutines-test, Arch Core Testing

### Instrumented Tests (`src/androidTest/`)

- Room database tests
- Compose UI tests (JUnit 4 + Compose Test)

### Running Tests

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest
```


## Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- JDK 17 (configured in `gradle.properties` via `org.gradle.java.home`)
- Android SDK 34

### Build & Run

```bash
# Debug build (local emulator)
./gradlew assembleDebug

# Dev build
./gradlew assembleDev

# Release build
./gradlew assembleRelease
```

Install the debug APK:
```bash
./gradlew installDebug
```

---

## Package Reference

```
com.semanticsoft.patientmobile
├── data
│   ├── local
│   │   ├── dao/          # Room DAOs
│   │   ├── datastore/    # Encrypted tokens & preferences
│   │   └── db/           # Database, entities, mappers
│   ├── model/            # Data-layer UI models (DashboardModels)
│   ├── remote
│   │   ├── api/          # Retrofit service, DTOs, response wrapper
│   │   └── interceptors/ # Auth & error interceptors
│   └── repository/       # Repository implementations
├── domain
│   ├── model/            # Pure domain entities
│   └── repository/       # Repository contracts
├── di
│   ├── DatabaseModule.kt
│   ├── DataStoreModule.kt
│   ├── NetworkModule.kt
│   └── RepositoryModule.kt
├── ui
│   ├── common/           # Shared UI helpers
│   ├── components/       # Reusable Compose widgets
│   ├── navigation/       # AppDestination sealed class
│   ├── screens/          # Feature screens + ViewModels
│   │   ├── dashboard/
│   │   ├── login/
│   │   ├── medicalHystory/
│   │   ├── navigation/   # NavScreen, AnalysisHistoryScreen
│   │   ├── registration/
│   │   └── uploadFile/
│   └── theme/            # Material 3 theme, colors, typography
├── util
│   ├── exceptions/       # Typed exceptions
│   ├── ApiExceptionMessageMapper.kt
│   ├── PasswordValidator.kt
│   └── Resource.kt
├── MainActivity.kt
├── PatientMobileApp.kt
└── PatientApplication.kt
```

---

*Generated for PatientMobile — com.semanticsoft.patientmobile*
