# PatientMobile

PatientMobile is an Android application for patients to manage their medical documents, view analysis results, and track health indicators, and for laboratories to receive submissions and enter results. It connects to a backend API for document storage, OCR processing, and medical result analysis.

The app now supports **two account roles**: `PATIENT` and `LABORATOR`.

## Table of Contents

- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Roles](#roles)
- [Project Structure](#project-structure)
- [How It Works](#how-it-works)
- [Navigation Flow](#navigation-flow)
- [Backend Integration](#backend-integration)
- [Build Variants](#build-variants)
- [Testing](#testing)
- [Design System](#design-system)
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
| **Local Storage** | EncryptedSharedPreferences (tokens/user) + DataStore Preferences |
| **Async** | Kotlin Coroutines + Flow |
| **Build System** | Gradle with Kotlin DSL + Version Catalog (`libs.versions.toml`) |

### Key Dependencies

- **AndroidX Compose BOM** `2024.09.03` — UI toolkit
- **Navigation Compose** `2.8.2` — In-app navigation
- **Hilt** `2.52` — Dependency injection
- **Retrofit** `2.10.0` — REST API client
- **OkHttp** `4.12.0` — HTTP client with logging
- **Kotlinx Serialization** `1.6.3` — JSON serialization/deserialization
- **JakeWharton Retrofit Converter** `1.0.0` — Retrofit + Kotlinx Serialization bridge
- **Security Crypto** `1.1.0-alpha06` — Encrypted token/user storage
- **DataStore** `1.1.1` — Typed preferences

> There is **no Room database**. Persistence is `EncryptedSharedPreferences` (tokens and the
> cached `User`) plus DataStore (app preferences).

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
│  - Remote API, EncryptedStore, DataStore│
└─────────────────────────────────────────┘
```

### Design Patterns

- **MVVM**: Each screen has a `ViewModel` exposing `UiState` and `Events`.
- **Repository Pattern**: Domain defines interfaces; the data layer provides implementations.
- **Dependency Injection**: All modules wired via Hilt (`di/`).
- **Unidirectional Data Flow**: UI emits actions → ViewModel processes → State updates → UI recomposes.

### UI Organization — Feature-by-Package, scoped by Role

The `ui/` layer is organized **feature-by-package inside a role namespace**:

- `ui/screens/{role}/{feature}/` contains the feature's `Screen`, `ViewModel`, and a nested
  `components/` folder for feature-specific widgets.
- Each role has its own post-login shell under `ui/navigation/{role}/`.
- **Role-agnostic** code lives in `ui/components/` (design system), `ui/common/`, `ui/shared/`
  (reusable features), `ui/screens/auth/`, `ui/screens/login/`, and `ui/theme/`.
- **Icons**: All custom icons are Kotlin `ImageVector` files in `ui/theme/icons/`. No XML drawables for custom icons.
- **Theme tokens**: `Color.kt` (palette), `Dimens.kt` (padding/gaps/heights), `Shapes.kt` (corner radii), `Type.kt` (text styles).

Full guidelines in `RULES.md`.

---

## Roles

Role is a **typed enum** — `domain/model/UserRole.kt`:

```kotlin
enum class UserRole { PATIENT, LABORATOR, UNKNOWN }
```

- An account holds **exactly one role**. The role is returned by the backend on login/refresh
  and cached with the user in `EncryptedSharedPreferences`.
- A custom serializer maps raw backend strings to the enum and coerces unknown values to
  `UserRole.UNKNOWN` (forward-compatible).
- There is **no client-side role selector**: after login, `PatientMobileApp.kt` routes to
  `AppDestination.Patient` or `AppDestination.Laborator` based on the returned role.
- **Patient** is fully implemented. **Laborator** is currently **scaffolded** (navigation
  shell, screens + ViewModels, and a stub `LaboratorRepository`) pending backend endpoints.

| Role | Shell | Features |
|------|-------|----------|
| `PATIENT` | `ui/navigation/patient/` | dashboard, medicalHystory, uploadedAnalyses, medicalResults, profile, analysisExplanation, reportResults, registration |
| `LABORATOR` | `ui/navigation/laborator/` | dashboard, submissions, results, profile, registration |

Shared, role-agnostic registration primitives live in `ui/screens/auth/components/`
(`AuthInput`, `AuthLabel`).

---

## Project Structure

```
PatientMobile/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/semanticsoft/patientmobile/
│   │   │   │   ├── data/                     # Data Layer
│   │   │   │   │   ├── local/
│   │   │   │   │   │   ├── preferences/      # UserPreferencesManager (DataStore)
│   │   │   │   │   │   └── session/          # TokenManager, EncryptedTokenManager,
│   │   │   │   │   │                         # internal SecureTokenStore
│   │   │   │   │   ├── model/                # Data-layer UI models (DashboardModels)
│   │   │   │   │   ├── remote/
│   │   │   │   │   │   ├── api/              # AuthApiService, PatientApiService, SharedApiService,
│   │   │   │   │   │   │   │                 # ApiConstants, ResponseEntity
│   │   │   │   │   │   │   └── dto/          # DTOs by area: auth/, patient/, shared/
│   │   │   │   │   │   │       └── mappers/  # DTO → domain mappers by area
│   │   │   │   │   │   ├── interceptors/     # Auth, Error, TokenRefresh
│   │   │   │   │   │   └── SafeApiCall.kt
│   │   │   │   │   └── repository/           # AuthRepositoryImpl, NetworkStateProvider
│   │   │   │   │       ├── patient/          # Document/MedicalResult/MedicalHistory/Ocr/Dashboard impls
│   │   │   │   │       └── shared/           # SharedLink, GlobalSyncManager, ProcessingCoordinator impls
│   │   │   │   ├── domain/                   # Domain Layer
│   │   │   │   │   ├── model/                # Pure domain entities
│   │   │   │   │   │   └── laborator/        # Contract-only lab models (LabSubmission, LabResult)
│   │   │   │   │   └── repository/           # Contracts: Auth/GlobalSync/SharedLink/ProcessingCoordinator
│   │   │   │   │       ├── patient/          # Document/MedicalResult/MedicalHistory/Ocr/Dashboard
│   │   │   │   │       └── laborator/        # LaboratorRepository (contract only)
│   │   │   │   ├── di/                       # NetworkModule, DataStoreModule,
│   │   │   │   │                             # RepositoryModule, PatientModule
│   │   │   │   ├── ui/                       # Presentation Layer
│   │   │   │   │   ├── common/               # spacing, system bars, shimmer
│   │   │   │   │   ├── components/           # Global Design System (role-agnostic)
│   │   │   │   │   ├── navigation/
│   │   │   │   │   │   ├── AppDestination.kt # Root routes (login, role graphs)
│   │   │   │   │   │   ├── patient/          # PatientNavScreen, PatientTab,
│   │   │   │   │   │   │                     # PatientPostLoginNavHost, components/
│   │   │   │   │   │   └── laborator/        # LaboratorNavScreen, LaboratorTab,
│   │   │   │   │   │                         # LaboratorPostLoginNavHost
│   │   │   │   │   ├── screens/
│   │   │   │   │   │   ├── auth/             # role-agnostic auth primitives + AuthViewModel
│   │   │   │   │   │   ├── login/            # role-agnostic login
│   │   │   │   │   │   ├── patient/          # patient-only features
│   │   │   │   │   │   │   ├── dashboard/
│   │   │   │   │   │   │   ├── documents/
│   │   │   │   │   │   │   ├── medicalHystory/
│   │   │   │   │   │   │   ├── medicalResults/
│   │   │   │   │   │   │   ├── uploadedAnalyses/
│   │   │   │   │   │   │   ├── analysisExplanation/
│   │   │   │   │   │   │   ├── reportResults/
│   │   │   │   │   │   │   ├── profile/
│   │   │   │   │   │   │   └── registration/
│   │   │   │   │   │   └── laborator/        # laborator-only features
│   │   │   │   │   │       ├── dashboard/
│   │   │   │   │   │       ├── submissions/
│   │   │   │   │   │       ├── results/
│   │   │   │   │   │       ├── profile/
│   │   │   │   │   │       └── registration/
│   │   │   │   │   ├── shared/               # role-agnostic reusable features
│   │   │   │   │   │   ├── upload/
│   │   │   │   │   │   └── documentViewer/
│   │   │   │   │   └── theme/                # Colors, Dimens, Shapes, Type, icons/
│   │   │   │   ├── util/                     # ApiResult, exceptions, validators
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── PatientMobileApp.kt       # Root NavHost + role router
│   │   │   │   └── PatientApplication.kt     # @HiltAndroidApp
│   │   │   └── res/                          # mipmap, values, xml
│   │   ├── test/                             # Unit tests
│   │   └── androidTest/                      # Instrumented tests
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── docs/
├── gradle/libs.versions.toml
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── RULES.md                                  # Agent guidelines (local-only)
```

### Where to Find Key Components

| Component | Location |
|-----------|----------|
| **Application Entry** | `MainActivity.kt` |
| **Root Navigation / Role Router** | `PatientMobileApp.kt` |
| **Root Routes** | `ui/navigation/AppDestination.kt` |
| **Role Model** | `domain/model/UserRole.kt` |
| **API Services** | `data/remote/api/` (`AuthApiService`, `PatientApiService`, `SharedApiService`) |
| **DTOs & Mappers** | `data/remote/api/dto/{auth,patient,shared}/` + `dto/mappers/` |
| **Token Store** | `data/local/session/` (`TokenManager`, `EncryptedTokenManager`, `SecureTokenStore`) |
| **Domain Models** | `domain/model/` (+ `domain/model/laborator/`) |
| **Repository Interfaces** | `domain/repository/` (+ `patient/`, `laborator/`) |
| **Repository Implementations** | `data/repository/` (+ `patient/`, `shared/`) |
| **DI Modules** | `di/` (`NetworkModule`, `DataStoreModule`, `RepositoryModule`, `PatientModule`) |
| **Role Screens** | `ui/screens/{role}/{feature}/` |
| **Role Navigation Shells** | `ui/navigation/{role}/` |
| **Global Design System** | `ui/components/` |
| **Icons** | `ui/theme/icons/` |
| **Theme** | `ui/theme/` |

### Global Design System (`ui/components/`)

Shared, role-agnostic primitives and dialogs, e.g. `ErrorDialog`, `LoadingIndicator`,
`ScreenTopBar`, `FilePickerButton`, `DeleteConfirmationDialog`.

---

## How It Works

### App Launch

1. `MainActivity.kt` sets up the window and calls `setContent { PatientMobileTheme { PatientMobileApp() } }`.
2. `PatientMobileApp.kt` hosts the root `NavHost` with `AppDestination.Login` as the start destination.
3. `PatientApplication.kt` is annotated with `@HiltAndroidApp` to initialize the DI graph.

### Authentication & Role Routing

- **Login** (`ui/screens/login/`)
  - `LoginScreen.kt` — Compose UI with email/password fields (role-agnostic).
  - `LoginViewModel.kt` — calls `AuthRepository.login()`; on success emits
    `LoginEvent.LoginSuccess(role)` carrying the role returned by the backend.
  - `PatientMobileApp.kt` routes to `AppDestination.Patient` or `AppDestination.Laborator`
    based on that role and clears the back stack.
- **Patient Registration** (`ui/screens/patient/registration/`) — calls `AuthRepository.register()`; on success → Patient graph.
- **Laborator Registration** (`ui/screens/laborator/registration/`) — screen is built; backend
  wiring is a TODO (`RegistrationLabViewModel.onRegisterClick()`); on success → Laborator graph.
- **Shared Auth Components** (`ui/screens/auth/components/`) — `AuthLabel`, `AuthInput`, `AuthTopHeader`.

Role is read from the cached `User` (`User.role: UserRole`) and exposed via
`AuthState.Authenticated(displayName, email, role)` in `AuthViewModel`.

### Post-Login Shell (Bottom Navigation)

Each role has a shell with a bottom tab bar (there is **no navigation drawer**):

**Patient** — `ui/navigation/patient/`

| Tab | Screen |
|-----|--------|
| Acasă | `patient/dashboard/DashboardScreen` |
| Istoric | `patient/medicalHystory/MedicalHystoryScreen` |
| Analize | `patient/uploadedAnalyses/UploadedAnalysesScreen` |
| Profil | `patient/profile/ProfileScreen` |

A center FAB opens the shared upload flow (`ui/shared/upload/`).

**Laborator** — `ui/navigation/laborator/` (scaffold)

| Tab | Screen |
|-----|--------|
| Panou | `laborator/dashboard/LaboratorDashboardScreen` |
| Lucrari | `laborator/submissions/LaboratorSubmissionsScreen` |
| Rezultate | `laborator/results/LaboratorResultsScreen` |
| Profil | `laborator/profile/LaboratorProfileScreen` |

### Dashboard (`ui/screens/patient/dashboard/`)

- Displays greeting/user info, attention items, basic health indicators, marker categories,
  general marker cards, AI summary, warning cards, and clinical pillar cards.
- Always fetches fresh data from the backend; no demo/fake data fallbacks.
- `DashboardViewModel.kt` fetches documents and medical results, computes summaries, and builds
  the AI summary string.

### File Upload (`ui/shared/upload/`)

- Dialog-style overlay for uploading medical documents (PDF, JPG, PNG).
- Shared across screens; `UploadFileViewModel.kt` handles selection and multipart upload.

### Theme System (`ui/theme/`)

| File | Purpose |
|------|---------|
| `Color.kt` | Color palette — **read-only**, add only |
| `Dimens.kt` | `AppDimens` object — padding, gaps, heights, corner radii |
| `Shapes.kt` | `AppShapes` — wired into `MaterialTheme` |
| `Type.kt` | `Typography` — text styles |
| `Theme.kt` | `PatientMobileTheme` |
| `icons/` | Kotlin `ImageVector` icons + `PathHelper.kt` |

---

## Navigation Flow

```
Login ──role=PATIENT───▶ Patient graph
  │                         ├── Acasă     → DashboardScreen
  │                         ├── Istoric   → MedicalHystoryScreen
  │                         ├── Analize   → UploadedAnalysesScreen
  │                         └── Profil    → ProfileScreen
  │
  ├──role=LABORATOR────▶ Laborator graph
  │                         ├── Panou     → LaboratorDashboardScreen
  │                         ├── Lucrari   → LaboratorSubmissionsScreen
  │                         ├── Rezultate → LaboratorResultsScreen
  │                         └── Profil    → LaboratorProfileScreen
  │
  ├──▶ Patient Registration ──success──▶ Patient graph
  └──▶ Laborator Registration ─success──▶ Laborator graph
```

Root routes are declared in `ui/navigation/AppDestination.kt`
(`Login`, `PatientRegistration`, `LaboratorRegistration`, `Patient`, `Laborator`, plus the
patient-only deep links `AnalysisExplanation` and `ReportResults`).

---

## Backend Integration

### API Services

The REST surface is split across three Retrofit interfaces, all created from the same
`Retrofit` instance in `NetworkModule`:

| Service | File | Scope |
|---------|------|-------|
| `AuthApiService` | `data/remote/api/AuthApiService.kt` | `/auth/*` |
| `PatientApiService` | `data/remote/api/PatientApiService.kt` | `/patient/*` |
| `SharedApiService` | `data/remote/api/SharedApiService.kt` | `/shared/{token}` |

**Auth**

| Method | Endpoint | Auth | Purpose |
|--------|----------|------|---------|
| POST | `/auth/register` | No | Create account |
| POST | `/auth/login` | No | Login & receive JWT |
| POST | `/auth/refresh` | No | Refresh tokens |
| POST | `/auth/logout` | Yes | Revoke refresh token |
| GET | `/auth/me` | Yes | Get current user |
| POST | `/auth/change-password` | Yes | Change password |
| DELETE | `/auth/account` | Yes | Delete account |

**Patient**

| Method | Endpoint | Purpose |
|--------|----------|---------|
| POST | `/patient/documents` | Upload document (multipart) |
| POST | `/patient/documents/duplicate-check` | Check SHA-256 duplicates |
| POST | `/patient/documents/bulk-delete` | Bulk delete documents |
| GET | `/patient/documents` | List documents (paginated) |
| GET | `/patient/documents/{id}` | Document metadata |
| GET | `/patient/documents/{id}/file` | Download file |
| PATCH | `/patient/documents/{id}` | Rename document |
| DELETE | `/patient/documents/{id}` | Delete document |
| POST | `/patient/documents/{id}/share` | Create share link |
| POST | `/patient/documents/{id}/extractions` | Start OCR extraction |
| GET | `/patient/documents/{id}/extractions/{runId}` | Extraction status |
| GET | `/patient/documents/{id}/extractions` | List extractions |
| GET | `/patient/documents/{id}/results` | Medical results for document |
| GET | `/patient/medical/results` | All results (paginated) |
| GET | `/patient/medical/results/latest` | Latest results |
| GET | `/patient/medical/results/history` | Results history by test |
| GET | `/patient/results/{id}` | Single result |
| GET | `/patient/results/{id}/history` | Result history |
| GET | `/patient/documents/{id}/reports` | Reports for document |
| GET | `/patient/medical/reports/{reportId}/results` | Report with results |
| PATCH | `/patient/medical/reports/{reportId}` | Update report review |
| POST | `/patient/medical/reports/{reportId}/ai-explanation` | AI explanation for report |
| GET | `/patient/dashboard` | Dashboard summary |
| GET | `/patient/dashboard/ai-summary` | AI summary |
| POST | `/patient/dashboard/ai-summary/regenerate` | Regenerate AI summary |
| POST | `/patient/dashboard/summary` | Request AI explanation |
| POST | `/patient/medical-history/medications` | Create medication |
| GET | `/patient/medical-history/medications` | List medications |
| PUT | `/patient/medical-history/medications/{id}` | Update medication |
| DELETE | `/patient/medical-history/medications/{id}` | Delete medication |
| POST | `/patient/medical-history/personal-notes` | Create personal note |
| GET | `/patient/medical-history/personal-notes` | List notes |
| PUT | `/patient/medical-history/personal-notes/{id}` | Update note |
| DELETE | `/patient/medical-history/personal-notes/{id}` | Delete note |
| POST | `/patient/medical-history/medications/ocr-drafts` | Medication OCR draft |
| POST | `/patient/medical-history/personal-notes/ocr-drafts` | Note OCR draft |

**Shared**

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/shared/{token}` | Consume shared link |

> **Laborator endpoints** are not yet defined. The lab data layer is **contract only**:
> `domain/repository/laborator/LaboratorRepository.kt` and `domain/model/laborator/`
> exist, but there is no implementation, DTO, or DI binding. Add them when the backend
> is available.

### Network Stack

- **Base URL**: Set per build type in `build.gradle.kts` (see [Build Variants](#build-variants)).
- **AuthInterceptor**: Attaches `Authorization: Bearer <token>` to non-public requests. Skips login/register/refresh. Reads the token through the injected `TokenManager`.
- **TokenRefreshAuthenticator**: OkHttp `Authenticator` that refreshes on 401 and retries; uses a dedicated `@Named("refresh")` client with synchronized double-check. Reads/writes tokens through the injected `TokenManager`.
- **ErrorInterceptor**: Passively logs error responses; does not throw.
- **Logging**: OkHttp logging enabled only in `DEBUG`.
- **Kotlinx Serialization**: via JakeWharton's Retrofit converter (bridged through `KotlinxConverterBridge.java`). Configured with `ignoreUnknownKeys`, `isLenient`, `coerceInputValues`.
- **SafeApiCall**: wraps API calls, mapping `HttpException` → `ApiResult.HttpError`, `401` → `ApiResult.AuthError`, `IOException` → `ApiResult.NetworkError`, `SerializationException` → `ApiResult.HttpError`. It rethrows `CancellationException` and lets unexpected (programmer) exceptions propagate rather than reporting them as `HttpError(-1)`.

### Token & User Security

- Tokens and the cached `User` (including `role`) are stored with AndroidX Security Crypto
  (`data/local/session/EncryptedTokenManager.kt` → internal `SecureTokenStore.kt`) as
  `EncryptedSharedPreferences`. Interceptors depend on the injected `TokenManager`
  abstraction, never the store directly.
- `data/local/preferences/UserPreferencesManager.kt` stores non-sensitive preferences via
  DataStore, exposing `Flow` reads and `suspend` writes.

### Repository Implementations

| Repository | File | Responsibility |
|------------|------|----------------|
| `AuthRepository` | `AuthRepositoryImpl.kt` | Login, register, refresh, logout, user profile |
| `DocumentRepository` | `patient/DocumentRepositoryImpl.kt` | Upload/list/download, rename, delete, share, duplicate check |
| `MedicalResultRepository` | `patient/MedicalResultRepositoryImpl.kt` | Results by document/report/global; history |
| `OcrRepository` | `patient/OcrRepositoryImpl.kt` | Trigger OCR, poll status, cache results |
| `DashboardRepository` | `patient/DashboardRepositoryImpl.kt` | Dashboard summary, AI explanations |
| `MedicalHistoryRepository` | `patient/MedicalHistoryRepositoryImpl.kt` | CRUD medications and personal notes |
| `SharedLinkRepository` | `shared/SharedLinkRepositoryImpl.kt` | Consume shared document links |
| `GlobalSyncManager` | `shared/GlobalSyncManagerImpl.kt` | Cross-screen sync broadcast |
| `ProcessingCoordinator` | `shared/ProcessingCoordinatorImpl.kt` | OCR processing orchestration |
| `LaboratorRepository` | — | **Contract only** (`domain/repository/laborator/`) — no implementation yet |

### Global Sync Engine

`GlobalSyncManager` is a singleton `SharedFlow`-based broadcast that keeps data-fetching screens
in sync. When a ViewModel mutates backend data (upload, medication add, note add), it calls
`globalSyncManager.triggerSync()`. Consumer ViewModels (`DashboardViewModel`,
`MedicalHystoryViewModel`, `ProfileViewModel`, `UploadedAnalysesViewModel`) collect
`syncEvents` in `init` and re-fetch. Screens also use `LifecycleResumeEffect` to refresh on return.

### Result Type: `ApiResult<T>`

All repository methods return `ApiResult<T>`:

| Variant | Meaning |
|---------|---------|
| `Success(data)` | Operation completed successfully |
| `HttpError(code, message)` | Server error (4xx/5xx) |
| `NetworkError` | No connectivity / DNS failure |
| `AuthError` | 401 Unauthorized — session expired |

Utilities: `safeApiCall { }`, `ApiResult.map { }`, `ApiResult.toUserMessage()`.
`util/Resource.kt` is **deprecated** — use `ApiResult<T>`.

---

## Build Variants

Configured in `app/build.gradle.kts`:

| Build Type | `BASE_URL` | Use Case |
|------------|-----------|----------|
| `debug` | `http://10.0.2.2:8080/api/v1/` | Local emulator testing |
| `dev` | `https://dev-api.example.com/api/v1/` | Shared dev backend |
| `release` | `https://api.example.com/api/v1/` | Production |

**Compile SDK**: `34` · **Min SDK**: `29` · **Target SDK**: `34` · **Java/Kotlin Target**: `11`

---

## Testing

### Unit Tests (`src/test/`)

| Test Category | Location |
|---------------|----------|
| ViewModel Tests | `ui/screens/{role}/{feature}/` |
| Repository Tests | `data/repository/` |
| Utility Tests | `util/` |
| Coroutine Rule | `testutil/MainDispatcherRule.kt` |

**Libraries**: JUnit 4, Mockito, Mockito-Kotlin, kotlinx-coroutines-test, Arch Core Testing

### Instrumented Tests (`src/androidTest/`)

- Compose UI tests (JUnit 4 + Compose Test)

### Running Tests

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest
```

---

## Design System

- **Global Design System** (`ui/components/`): universal primitives consumed by 2+ roles/screens.
- **Role features** (`ui/screens/{role}/{feature}/components/`): feature-specific widgets (`internal`).
- **Shared features** (`ui/shared/{feature}/`): self-contained reusable flows (their own ViewModel).
- **Icons** (`ui/theme/icons/`): Kotlin `ImageVector` only.
- **Overlays**: bottom sheets for sorting/filtering, custom Surface dialogs for focused forms and
  destructive confirms, `AlertDialog` for system errors. See `RULES.md` for the exact patterns.

---

## Agent Guidelines

`RULES.md` is the authoritative guide for package structure (including the **Roles & Package
Structure** section), naming, dialogs/sheets, data loading strategy, and code style. Read it
before adding features or new roles.

---

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
