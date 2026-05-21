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
- [Agent Guidelines](#agent-guidelines)
- [Getting Started](#getting-started)

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose (Material 3) |
| **Architecture** | MVVM + Clean Architecture |
| **Dependency Injection** | Dagger Hilt |
| **Networking** | Retrofit 2 + OkHttp + Kotlinx Serialization |
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
- **Kotlinx Serialization** `1.6.3` — JSON serialization/deserialization
- **JakeWharton Retrofit Converter** `1.0.0` — Retrofit + Kotlinx Serialization bridge
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

### UI Organization — Feature-by-Package

The `ui/screens/` layer follows a strict **Feature-by-Package** structure:

- Every feature folder contains its `Screen`, `ViewModel`, and a nested `components/` folder for feature-specific widgets.
- **Global Design System** (`ui/components/`): Only shared, generic primitives (e.g. `LoadingIndicator`, `ScreenTopBar`) live here.
- **Shared cross-feature components** (like `ui/screens/auth/components/`) exist for composables shared by multiple features that are not universal enough for the global bucket.
- **Icons**: All custom icons are Kotlin `ImageVector` files in `ui/theme/icons/`. No XML drawables for custom icons.
- **Theme tokens**: `Color.kt` (palette), `Dimens.kt` (padding/gaps/heights), `Shapes.kt` (corner radii), `Type.kt` (text styles).

Full guidelines in `RULES.md`.

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
│   │   │   │   │   │   ├── dao/
│   │   │   │   │   │   ├── datastore/
│   │   │   │   │   │   └── db/
│   │   │   │   │   │       ├── entity/
│   │   │   │   │   │       └── extensions/
│   │   │   │   │   ├── model/         # Data-layer models (dashboard DTOs)
│   │   │   │   │   ├── remote/        # API service, interceptors, DTOs
│   │   │   │   │   │   ├── api/
│   │   │   │   │   │   │   └── dto/
│   │   │   │   │   │   └── interceptors/
│   │   │   │   │   └── repository/    # Repository implementations
│   │   │   │   ├── domain/            # Domain Layer
│   │   │   │   │   ├── model/         # Domain entities (User, MedicalResult, etc.)
│   │   │   │   │   └── repository/    # Repository interfaces
│   │   │   │   ├── di/                # Hilt Modules
│   │   │   │   ├── ui/                # Presentation Layer
│   │   │   │   │   ├── common/        # Shared UI utilities (spacing, system bars)
│   │   │   │   │   ├── components/    # Global Design System (7 shared widgets)
│   │   │   │   │   ├── navigation/    # AppDestination route definitions
│   │   │   │   │   ├── screens/       # Feature packages
│   │   │   │   │   │   ├── auth/
│   │   │   │   │   │   │   └── components/ # Shared auth primitives (AuthLabel, AuthInput)
│   │   │   │   │   │   ├── dashboard/
│   │   │   │   │   │   │   └── components/ # Dashboard-specific widgets
│   │   │   │   │   │   ├── login/
│   │   │   │   │   │   │   └── components/ # Login-specific widgets
│   │   │   │   │   │   ├── medicalHystory/
│   │   │   │   │   │   │   └── components/ # Medical-history-specific widgets
│   │   │   │   │   │   ├── navigation/
│   │   │   │   │   │   │   └── components/ # Drawer shell widgets (PostLoginDrawer, DrawerMenuItem)
│   │   │   │   │   │   ├── registration/
│   │   │   │   │   │   │   └── components/ # Registration-specific widgets
│   │   │   │   │   │   ├── shared/
│   │   │   │   │   │   │   └── upload/
│   │   │   │   │   │   │       └── components/ # Upload-specific widgets
│   │   │   │   │   └── theme/         # Colors, Typography, Theme, Icons
│   │   │   │   │       ├── icons/     # Kotlin ImageVector icons (all custom icons)
│   │   │   │   │       ├── Color.kt   # Color palette
│   │   │   │   │       ├── Dimens.kt  # Sizes, paddings, gaps, heights
│   │   │   │   │       ├── Shapes.kt  # Corner radius tokens
│   │   │   │   │       ├── Type.kt    # Text style tokens
│   │   │   │   │       └── Theme.kt   # MaterialTheme wiring
│   │   │   │   ├── util/              # Utilities & exceptions
│   │   │   │   ├── MainActivity.kt    # Entry activity
│   │   │   │   ├── PatientMobileApp.kt # Root navigation host
│   │   │   │   └── PatientApplication.kt # Application class (Hilt)
│   │   │   └── res/                   # Android resources (mipmap, values, xml)
│   │   │       └── drawable/          # Only launcher icons remain
│   │   ├── test/                      # Unit tests
│   │   └── androidTest/               # Instrumented tests
│   ├── build.gradle.kts               # App-level build config
│   └── proguard-rules.pro
├── docs/                              # Project documentation
├── gradle/
│   └── libs.versions.toml             # Version catalog
├── build.gradle.kts                   # Root build config
├── settings.gradle.kts
├── gradle.properties
└── RULES.md                           # Agent guidelines (local-only)
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
| **Global Design System** | `ui/components/` |
| **Feature Components** | `ui/screens/{feature}/components/` |
| **Icons** | `ui/theme/icons/` |
| **Theme** | `ui/theme/` |
| **Custom Exceptions** | `util/exceptions/` |

### Global Design System (`ui/components/`)

These 7 shared primitives are consumed by 2+ features:

| Component | Used By |
|-----------|---------|
| `ErrorDialog` | Dashboard |
| `FilePickerButton` | MedicalHistory, UploadFile |
| `LoadingIndicator` | Dashboard, Login, Registration, UploadFile |
| `PasswordField` | (unused — reserved) |
| `PasswordInputField` | (unused — reserved) |
| `ProgressIndicator` | (unused — reserved) |
| `ScreenTopBar` | Dashboard, MedicalHistory |

### Feature Components

| Feature | Components Folder | Files |
|---------|-------------------|-------|
| `auth` | `screens/auth/components/` | `AuthLabel`, `AuthInput` |
| `dashboard` | `screens/dashboard/components/` | `BasicIndicatorsCard`, `DashboardTopSection`, `EmptyUploadCard`, `GeneralMarkersCard`, `HealthScoreCard`, `MarkerOverviewSection`, `ResumeAICard`, `WarningCard` |
| `login` | `screens/login/components/` | (uses `auth/components/`) |
| `medicalHystory` | `screens/medicalHystory/components/` | `DocumentList`, `DocumentListItem`, `MedicalHistoryAnalysisSection`, `MedicalHistoryFiltersCard`, `MedicalHistoryTopBar`, `MedicinesSection`, `PersonalNotesSection` |
| `navigation` | `screens/navigation/components/` | `PostLoginDrawerContent`, `DrawerMenuItem` |
| `registration` | `screens/registration/components/` | (uses `auth/components/`) |
| `upload` | `shared/upload/components/` | `DragDropArea`, `DropboxButton`, `ExternalSourceButton`, `FileUploadComponent`, `UploadButton`, `UploadFileDialogContent` |

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

- **Shared Auth Components** (`ui/screens/auth/components/`)
  - `AuthLabel` and `AuthInput` are shared primitives used by both Login and Registration screens to avoid duplication.

### Post-Login Navigation

`NavScreen.kt` provides a **Modal Navigation Drawer** with tabs:

| Drawer Item | Screen | File |
|-------------|--------|------|
| Panou principal | Dashboard | `DashboardScreen.kt` |
| Istoric medical | Medical History | `MedicalHystoryScreen.kt` |

The drawer shows the user's profile, role, and a logout button. Drawer content is extracted into `navigation/components/PostLoginDrawerContent.kt`.

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
- Dashboard-specific widgets live in `ui/screens/dashboard/components/`.

### Medical History (`ui/screens/medicalHystory/`)

- Lists uploaded documents and their analysis history.
- `MedicalHystoryViewModel.kt` manages the state.
- Feature-specific widgets live in `ui/screens/medicalHystory/components/`.

### File Upload (`ui/shared/upload/`)

- Dialog-style overlay for uploading medical documents (PDF, JPG, PNG).
- Used by multiple screens (Dashboard, MedicalHistory) as a shared feature.
- `UploadFileViewModel.kt` handles file selection and multipart upload via `DocumentRepository`.
- Upload-specific widgets (dialog content, drag-drop area, source buttons) live in `ui/shared/upload/components/`.

### Theme System (`ui/theme/`)

| File | Purpose |
|------|---------|
| `Color.kt` | Color palette — **read-only**, add only |
| `Dimens.kt` | `AppDimens` object — padding, gaps, button/input heights, corner radii |
| `Shapes.kt` | `AppShapes` — wired into `MaterialTheme` |
| `Type.kt` | `Typography` — text styles for headlines, titles, body, labels |
| `Theme.kt` | `PatientMobileTheme` — wires `LightColors`, `Typography`, `AppShapes` |
| `icons/` | 9 Kotlin `ImageVector` files (all custom icons) + `PathHelper.kt` SVG parser |

---

## Navigation Flow

```
Login ──success──▶ Dashboard (drawer)
  │                    │
  │                    ├── Panou principal → DashboardScreen
  │                    ├── Istoric medical → MedicalHystoryScreen
  │                    └── Logout → Login
  │
  └──▶ Registration ──success──▶ Dashboard
```

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
| POST | `/patient/documents/duplicate-check` | Yes | Check SHA-256 duplicates |
| GET | `/patient/documents` | Yes | List documents (paginated) |
| GET | `/patient/documents/{id}` | Yes | Document metadata |
| GET | `/patient/documents/{id}/file` | Yes | Download file |
| PATCH | `/patient/documents/{id}` | Yes | Rename document |
| DELETE | `/patient/documents/{id}` | Yes | Delete document |
| POST | `/patient/documents/{id}/share` | Yes | Create share link |
| POST | `/patient/documents/{id}/ocr` | Yes | Trigger OCR extraction |
| GET | `/patient/ocr/{id}` | Yes | Get OCR extraction status |
| GET | `/patient/documents/{id}/ocr` | Yes | List OCR extractions |
| GET | `/patient/documents/{id}/results` | Yes | Medical results for document |
| GET | `/patient/results` | Yes | All results (paginated) |
| GET | `/patient/results/{id}` | Yes | Single result |
| GET | `/patient/results/{id}/history` | Yes | Result history |
| GET | `/patient/documents/{id}/reports` | Yes | Reports for document |
| GET | `/patient/dashboard` | Yes | Dashboard summary |
| POST | `/patient/dashboard/summary` | Yes | Request AI explanation |
| POST | `/patient/history/medications` | Yes | Create medication |
| GET | `/patient/history/medications` | Yes | List medications |
| PUT | `/patient/history/medications/{id}` | Yes | Update medication |
| DELETE | `/patient/history/medications/{id}` | Yes | Delete medication |
| POST | `/patient/history/notes` | Yes | Create personal note |
| GET | `/patient/history/notes` | Yes | List notes |
| PUT | `/patient/history/notes/{id}` | Yes | Update note |
| DELETE | `/patient/history/notes/{id}` | Yes | Delete note |
| GET | `/shared/{token}` | No | Consume shared link |

### Network Stack

- **Base URL**: Set per build type in `build.gradle.kts` (see [Build Variants](#build-variants)).
- **AuthInterceptor**: Automatically attaches `Authorization: Bearer <token>` to non-public requests. Skips login/register/refresh endpoints.
- **TokenRefreshAuthenticator**: OkHttp `Authenticator` that intercepts 401 responses, refreshes the token (using a dedicated `@Named("refresh")` OkHttpClient), and retries the request. Uses `synchronized` + double-check to prevent concurrent refresh races.
- **ErrorInterceptor**: Passively inspects error responses for logging; does not throw exceptions.
- **Logging**: OkHttp logging is enabled only in `DEBUG` builds.
- **Kotlinx Serialization**: JSON via JakeWharton's Retrofit converter (bridged through `KotlinxConverterBridge.java`). Configured with `ignoreUnknownKeys`, `isLenient`, and `coerceInputValues`.
- **SafeApiCall**: Wraps all API calls in `try-catch`, mapping `HttpException` → `ApiResult.HttpError`, `401` → `ApiResult.AuthError`, and `IOException` → `ApiResult.NetworkError`.

### Token Security

- Tokens are encrypted using AndroidX Security Crypto (`EncryptedTokenManager.kt`).
- `SecureTokenStore.kt` provides the underlying encrypted storage.
- `UserPreferencesManager.kt` handles non-sensitive preferences via DataStore.

### Local Database (Room)

`PatientDatabase.kt` contains **9 entities** at version 2:

| Entity | DAO | Purpose |
|--------|-----|---------|
| `UserEntity` | `UserDao` | Cached user profile |
| `DocumentEntity` | `DocumentDao` | Cached document list |
| `MedicalResultEntity` | `MedicalResultDao` | Cached analysis results |
| `MedicalReportEntity` | `MedicalReportDao` | Cached reports |
| `AuditLogEntity` | `AuditLogDao` | Security/audit logs |
| `OcrExtractionEntity` | `OcrExtractionDao` | Cached OCR extractions |
| `MedicationEntity` | `MedicationDao` | Cached medications |
| `PersonalNoteEntity` | `PersonalNoteDao` | Cached personal notes |
| `SharedLinkEntity` | `SharedLinkDao` | Cached shared links |

- **Migrations**: `Migration(1, 2)` handles the v1→v2 upgrade (recreates `documents`, `medical_results`, `medical_reports` tables; adds `audit_logs`, `ocr_extractions`, `medications`, `personal_notes`, `shared_links`).
- **TypeConverters**: `RoomConverters` handles `List<String>`, `SyncStatus`, enums, `LocalDate`/`Instant` via JSON serialization.
- Development note: If migration fails, reset the database with `adb shell pm clear com.semanticsoft.patientmobile`.

### Repository Implementations

| Repository | File | Responsibility |
|------------|------|----------------|
| `AuthRepository` | `AuthRepositoryImpl.kt` | Login, register, refresh, logout, user profile |
| `DocumentRepository` | `DocumentRepositoryImpl.kt` | Upload, list, download, rename, delete documents; share links; duplicate check |
| `MedicalResultRepository` | `MedicalResultRepositoryImpl.kt` | Fetch results by document, report, or globally; result history |
| `AuditRepository` | `AuditRepositoryImpl.kt` | Local audit logging |
| `OcrRepository` | `OcrRepositoryImpl.kt` | Trigger OCR, poll extraction status, cache results |
| `DashboardRepository` | `DashboardRepositoryImpl.kt` | Fetch dashboard summary, request AI explanations |
| `MedicalHistoryRepository` | `MedicalHistoryRepositoryImpl.kt` | CRUD medications and personal notes |
| `SharedLinkRepository` | `SharedLinkRepositoryImpl.kt` | Consume shared document links |

### Result Type: `ApiResult<T>`

All repository methods return `ApiResult<T>`, a sealed class:

| Variant | Meaning |
|---------|---------|
| `Success(data)` | Operation completed successfully |
| `HttpError(code, message)` | Server returned an error (4xx/5xx) |
| `NetworkError` | No internet connectivity or DNS failure |
| `AuthError` | 401 Unauthorized — session expired |

Utilities:
- `safeApiCall { ... }` — Catches Retrofit/network exceptions and maps to `ApiResult` variants.
- `ApiResult.map { }` — Transforms `Success` data while preserving error states (exception-safe).
- `ApiResult.toUserMessage()` — Maps result to a human-readable error string.

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
│   │   ├── dao/          # Room DAOs (UserDao, DocumentDao, MedicalResultDao, etc.)
│   │   ├── datastore/    # EncryptedTokenManager, TokenManager, UserPreferencesManager
│   │   └── db/           # PatientDatabase, entities, EntityDomainMappers
│   ├── model/            # Data-layer UI models (DashboardModels)
│   ├── remote
│   │   ├── api/          # PatientApiService, DTOs (Auth, Document, Ocr, Dashboard, etc.)
│   │   │   └── dto/      # AuthDtos, DocumentDtos, OcrDtos, DashboardDtos, MedicalHistoryDtos, SharedLinkDtos, DtoMappers
│   │   ├── interceptors/ # AuthInterceptor, ErrorInterceptor, TokenRefreshAuthenticator
│   │   ├── KotlinxConverterBridge.java  # Java bridge for Retrofit converter
│   │   └── SafeApiCall.kt  # safeApiCall wrapper
│   └── repository/       # Repository impls (Auth, Document, MedicalResult, Ocr, Dashboard, MedicalHistory, SharedLink, Audit, NetworkState)
├── domain
│   ├── model/            # Pure domain entities (User, MedicalResult, PatientDocument, OcrExtraction, DashboardSummary, Medication, PersonalNote, SharedLink, etc.)
│   └── repository/       # Repository contracts (Auth, Document, MedicalResult, Ocr, Dashboard, MedicalHistory, SharedLink, Audit)
├── di
│   ├── DatabaseModule.kt
│   ├── DataStoreModule.kt
│   ├── NetworkModule.kt
│   └── RepositoryModule.kt
├── ui
│   ├── common/           # DashboardSpacing, SetStatusBar
│   ├── components/       # Global Design System (7 shared widgets)
│   ├── navigation/       # AppDestination sealed class
│   ├── screens/
│   │   ├── auth/
│   │   │   └── components/    # AuthInput, AuthLabel (shared by login + registration)
│   │   ├── dashboard/
│   │   │   └── components/    # BasicIndicatorsCard, DashboardTopSection, EmptyUploadCard, etc.
│   │   ├── login/             # LoginScreen, LoginViewModel
│   │   │   └── components/    # (uses auth/components/)
│   │   ├── medicalHystory/     # MedicalHystoryScreen, MedicalHystoryViewModel
│   │   │   └── components/    # DocumentList, MedicalHistoryAnalysisSection, etc.
│   │   ├── navigation/        # NavScreen, PostLoginTab
│   │   │   └── components/    # PostLoginDrawerContent, DrawerMenuItem
│   │   ├── registration/      # RegistrationScreen, RegistrationViewModel
│   │   │   └── components/    # (uses auth/components/)
│   │   ├── shared/
│   │   │   └── upload/         # UploadFileScreen, UploadFileViewModel, UploadFileModels
│   │   │       └── components/ # DragDropArea, UploadFileDialogContent, etc.
│   └── theme/
│       ├── icons/         # Kotlin ImageVector files (CloseIcon, WarningIcon, etc.)
│       ├── Color.kt       # Color palette
│       ├── Dimens.kt      # AppDimens (paddings, gaps, heights, radii)
│       ├── Shapes.kt      # AppShapes (corner radii tokens)
│       ├── Type.kt        # Typography (text style tokens)
│       └── Theme.kt       # PatientMobileTheme
├── util
│   ├── ApiResult.kt       # ApiResult<T> sealed class (Success, HttpError, NetworkError, AuthError)
│   ├── ApiResultExtensions.kt # map, onSuccess, onError, toUserMessage extensions
│   ├── PasswordValidator.kt
│   └── Resource.kt        # @Deprecated — replaced by ApiResult<T>
├── MainActivity.kt
├── PatientMobileApp.kt   # Root NavHost with Login, Registration, Dashboard routes
└── PatientApplication.kt # @HiltAndroidApp
```

---
