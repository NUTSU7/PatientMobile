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

### UI Organization — Clean Architecture, Feature-by-Role

The app follows **Clean Architecture + feature-by-role** in a single module:

- `core/` — shared infrastructure: `network/` (api, dto, interceptors, SafeApiCall),
  `datastore/` (token + preferences managers), `ui/` (theme, common, components, shared
  role-agnostic features), `util/`.
- `data/` — `repository/{common,patient}` implementations and `mapper/{common,patient}`
  DTO → domain mappers.
- `domain/` — pure Kotlin: `model/{common,patient,laborator}`, `repository/{common,patient,laborator}`
  contracts, and `usecase/{common,patient}` business logic (`operator fun invoke`).
- `feature/` — presentation by role: `auth/` (login + `AuthViewModel`), `patient/{feature}/`,
  `laborator/{feature}/`, each with its own `navigation/` shell.
- `navigation/` — root dispatcher: `AppDestination.kt` (routes) and `RootNavHost.kt` (role routing).
- **Icons**: All custom icons are Kotlin `ImageVector` files in `core/ui/theme/icons/`. No XML drawables for custom icons.
- **Theme tokens**: `Color.kt` (palette), `Dimens.kt` (padding/gaps/heights), `Shapes.kt` (corner radii), `Type.kt` (text styles).

Full guidelines in `RULES.md`.

---

## Roles

Role is a **typed enum** — `domain/model/common/UserRole.kt`:

```kotlin
enum class UserRole { PATIENT, LABORATOR, UNKNOWN }
```

- An account holds **exactly one role**. The role is returned by the backend on login/refresh
  and cached with the user in `EncryptedSharedPreferences`.
- A custom serializer maps raw backend strings to the enum and coerces unknown values to
  `UserRole.UNKNOWN` (forward-compatible).
- There is **no client-side role selector**: after login, `navigation/RootNavHost.kt` routes to
  `AppDestination.Patient` or `AppDestination.Laborator` based on the returned role.
- **Patient** is fully implemented. **Laborator** is currently **scaffolded** (navigation
  shell, screens + ViewModels, and a stub `LaboratorRepository`) pending backend endpoints.

| Role | Shell | Features |
|------|-------|----------|
| `PATIENT` | `feature/patient/navigation/` | dashboard, medicalHistory, uploadedAnalyses, profile, analysisExplanation, reportResults, registration |
| `LABORATOR` | `feature/laborator/navigation/` | dashboard, frequentAnalyses, statistics, profile, registration |

Shared, role-agnostic registration primitives live in `core/ui/components/auth/`
(`AuthInput`, `AuthLabel`).

---

## Project Structure

```
PatientMobile/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/semanticsoft/patientmobile/
│   │   │   │   ├── core/                     # Shared infrastructure
│   │   │   │   │   ├── network/
│   │   │   │   │   │   ├── api/              # AuthApiService, PatientApiService, SharedApiService,
│   │   │   │   │   │   │                     # ApiConstants, ResponseEntity
│   │   │   │   │   │   ├── dto/              # DTOs by area: auth/, patient/, shared/
│   │   │   │   │   │   ├── interceptors/     # Auth, Error, TokenRefresh
│   │   │   │   │   │   ├── NetworkStateProvider.kt
│   │   │   │   │   │   └── SafeApiCall.kt
│   │   │   │   │   ├── datastore/            # TokenManager, EncryptedTokenManager,
│   │   │   │   │   │                         # internal SecureTokenStore, UserPreferencesManager
│   │   │   │   │   ├── ui/                   # Shared design system
│   │   │   │   │   │   ├── common/           # spacing, system bars, shimmer
│   │   │   │   │   │   ├── components/       # role-agnostic widgets + auth/ primitives
│   │   │   │   │   │   ├── shared/           # upload/, documentViewer/
│   │   │   │   │   │   └── theme/            # Colors, Dimens, Shapes, Type, icons/
│   │   │   │   │   └── util/                 # ApiResult, exceptions, validators
│   │   │   │   ├── data/                     # Data Layer
│   │   │   │   │   ├── mapper/               # DTO → domain mappers: common/, patient/
│   │   │   │   │   └── repository/
│   │   │   │   │       ├── common/           # AuthRepositoryImpl, GlobalSyncManagerImpl
│   │   │   │   │       └── patient/          # Document/MedicalResult/MedicalHistory/Ocr/Dashboard/
│   │   │   │   │                             # ProcessingCoordinator/SharedLink impls
│   │   │   │   ├── domain/                   # Domain Layer (pure Kotlin)
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── common/           # User, UserRole, AuthResponse, RegistrationRequest
│   │   │   │   │   │   ├── patient/          # medical records, documents, analyses, dashboard models
│   │   │   │   │   │   └── laborator/        # Contract-only lab models (LabSubmission, LabResult)
│   │   │   │   │   ├── repository/
│   │   │   │   │   │   ├── common/           # AuthRepository, GlobalSyncManager
│   │   │   │   │   │   ├── patient/          # Document/MedicalResult/MedicalHistory/Ocr/Dashboard,
│   │   │   │   │   │   │                     # ProcessingCoordinator, SharedLinkRepository
│   │   │   │   │   │   └── laborator/        # LaboratorRepository (contract only)
│   │   │   │   │   └── usecase/
│   │   │   │   │       ├── common/           # Login, Logout, CheckSession, RegisterPatient,
│   │   │   │   │       │                     # ChangePassword, DeleteAccount
│   │   │   │   │       └── patient/          # BuildDashboard, LoadIndicatorHistory, GroupReportResults
│   │   │   │   ├── feature/                  # Presentation Layer (by role)
│   │   │   │   │   ├── auth/
│   │   │   │   │   │   ├── AuthViewModel.kt
│   │   │   │   │   │   └── login/            # LoginScreen, LoginViewModel
│   │   │   │   │   ├── patient/
│   │   │   │   │   │   ├── navigation/       # PatientNavScreen, PatientTab,
│   │   │   │   │   │   │                     # PatientPostLoginNavHost, components/
│   │   │   │   │   │   ├── dashboard/
│   │   │   │   │   │   ├── medicalHistory/
│   │   │   │   │   │   ├── uploadedAnalyses/
│   │   │   │   │   │   ├── analysisExplanation/
│   │   │   │   │   │   ├── reportResults/
│   │   │   │   │   │   ├── profile/
│   │   │   │   │   │   └── registration/
│   │   │   │   │   └── laborator/
│   │   │   │   │       ├── navigation/       # LaboratorNavScreen, LaboratorTab,
│   │   │   │   │       │                     # LaboratorPostLoginNavHost, components/
│   │   │   │   │       │                     # (FAB + temporary nav placeholders)
│   │   │   │   │       ├── common/           # role-scoped shared filter UI
│   │   │   │   │       ├── dashboard/
│   │   │   │   │       ├── frequentAnalyses/
│   │   │   │   │       ├── profile/
│   │   │   │   │       └── registration/
│   │   │   │   ├── navigation/               # Root dispatcher
│   │   │   │   │   ├── AppDestination.kt     # Root routes (login, role graphs)
│   │   │   │   │   └── RootNavHost.kt        # Role-based route resolver
│   │   │   │   ├── di/                       # NetworkModule, DataStoreModule,
│   │   │   │   │                             # DispatcherModule, DataModule
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── PatientMobileApp.kt       # App root composable
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
| **Root Navigation / Role Router** | `navigation/RootNavHost.kt` |
| **Root Routes** | `navigation/AppDestination.kt` |
| **Role Model** | `domain/model/common/UserRole.kt` |
| **Use Cases** | `domain/usecase/{common,patient}/` |
| **API Services** | `core/network/api/` (`AuthApiService`, `PatientApiService`, `SharedApiService`) |
| **DTOs & Mappers** | `core/network/dto/{auth,patient,shared}/` + `data/mapper/{common,patient}/` |
| **Token Store** | `core/datastore/` (`TokenManager`, `EncryptedTokenManager`, `SecureTokenStore`) |
| **Domain Models** | `domain/model/{common,patient,laborator}/` |
| **Repository Interfaces** | `domain/repository/{common,patient,laborator}/` |
| **Repository Implementations** | `data/repository/{common,patient}/` |
| **DI Modules** | `di/` (`NetworkModule`, `DataStoreModule`, `DispatcherModule`, `DataModule`) |
| **Role Screens** | `feature/{role}/{feature}/` |
| **Role Navigation Shells** | `feature/{role}/navigation/` |
| **Global Design System** | `core/ui/components/` |
| **Icons** | `core/ui/theme/icons/` |
| **Theme** | `core/ui/theme/` |

### Global Design System (`core/ui/components/`)

Shared, role-agnostic primitives and dialogs, e.g. `ErrorDialog`, `LoadingIndicator`,
`ScreenTopBar`, `AppBottomNavBar`, `FilePickerButton`, `DeleteConfirmationDialog`.

---

## How It Works

### App Launch

1. `MainActivity.kt` sets up the window and calls `setContent { PatientMobileTheme { PatientMobileApp() } }`.
2. `PatientMobileApp.kt` is a thin root composable delegating to `navigation/RootNavHost.kt`,
   which hosts the root `NavHost` with `AppDestination.Login` as the start destination.
3. `PatientApplication.kt` is annotated with `@HiltAndroidApp` to initialize the DI graph.

### Authentication & Role Routing

- **Login** (`feature/auth/login/`)
  - `LoginScreen.kt` — Compose UI with email/password fields (role-agnostic).
  - `LoginViewModel.kt` — calls `LoginUseCase`; on success emits
    `LoginEvent.LoginSuccess(role)` carrying the role returned by the backend.
  - `navigation/RootNavHost.kt` routes to `AppDestination.Patient` or `AppDestination.Laborator`
    based on that role and clears the back stack.
- **Patient Registration** (`feature/patient/registration/`) — calls `RegisterPatientUseCase`; on success → Patient graph.
- **Laborator Registration** (`feature/laborator/registration/`) — screen is built; backend
  wiring is a TODO (`RegistrationLabViewModel.onRegisterClick()`); on success → Laborator graph.
- **Shared Auth Components** (`core/ui/components/auth/`) — `AuthLabel`, `AuthInput`, `AuthTopHeader`.

Role is read from the cached `User` (`User.role: UserRole`) and exposed via
`AuthState.Authenticated(displayName, email, role)` in `AuthViewModel` (`CheckSessionUseCase`).

### Post-Login Shell (Bottom Navigation)

Each role has a shell with a bottom tab bar (there is **no navigation drawer**):

**Patient** — `feature/patient/navigation/`

| Tab | Screen |
|-----|--------|
| Acasă | `feature/patient/dashboard/DashboardScreen` |
| Istoric | `feature/patient/medicalHistory/MedicalHistoryScreen` |
| Analize | `feature/patient/uploadedAnalyses/UploadedAnalysesScreen` |
| Profil | `feature/patient/profile/ProfileScreen` |

A center FAB opens the shared upload flow (`core/ui/shared/upload/`).

**Laborator** — `feature/laborator/navigation/` (scaffold)

| Tab | Screen |
|-----|--------|
| Acasă | `feature/laborator/dashboard/LaboratorDashboardScreen` |
| Analize | `feature/laborator/frequentAnalyses/FrequentAnalysesScreen` |
| Implicare | `feature/laborator/navigation/components/LaboratorNavPlaceholderScreen` |
| Profil | `feature/laborator/profile/LaboratorProfileScreen` |

A center FAB uses the Figma upload-result icon and is UI-only for now (no action). The
`Implicare` route uses a temporary placeholder until its screen is built.

### Dashboard (`feature/patient/dashboard/`)

- Displays greeting/user info, basic health indicators with history sparklines, the marker
  summary (optim / la limită / atenție counts + health score), document count, and the AI
  summary with regenerate/loading states.
- Handles the empty and OCR-processing states via `EmptyUploadCard` / `ProcessingEmptyCard`.
- Always fetches fresh data from the backend; no demo/fake data fallbacks.
- `DashboardViewModel.kt` fetches documents and medical results and owns screen state; pure
  computation (marker summary, basic indicators, results signature) lives in
  `BuildDashboardUseCase`.

### File Upload (`core/ui/shared/upload/`)

- Dialog-style overlay for uploading medical documents (PDF, JPG, PNG).
- Shared across screens; `UploadFileViewModel.kt` handles selection and multipart upload.

### Theme System (`core/ui/theme/`)

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
  │                         ├── Istoric   → MedicalHistoryScreen
  │                         ├── Analize   → UploadedAnalysesScreen
  │                         └── Profil    → ProfileScreen
  │
  ├──role=LABORATOR────▶ Laborator graph
  │                         ├── Acasă      → LaboratorDashboardScreen
  │                         ├── Analize    → placeholder (nav shell)
  │                         ├── Implicare  → placeholder (nav shell)
  │                         └── Profil     → LaboratorProfileScreen
  │
  ├──▶ Patient Registration ──success──▶ Patient graph
  └──▶ Laborator Registration ─success──▶ Laborator graph
```

Root routes are declared in `navigation/AppDestination.kt`
(`Login`, `PatientRegistration`, `LaboratorRegistration`, `Patient`, `Laborator`, plus the
patient-only deep links `AnalysisExplanation` and `ReportResults`).

---

## Backend Integration

### API Services

The REST surface is split across three Retrofit interfaces, all created from the same
`Retrofit` instance in `NetworkModule`:

| Service | File | Scope |
|---------|------|-------|
| `AuthApiService` | `core/network/api/AuthApiService.kt` | `/auth/*` |
| `PatientApiService` | `core/network/api/PatientApiService.kt` | `/patient/*` |
| `SharedApiService` | `core/network/api/SharedApiService.kt` | `/shared/{token}` |

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
  (`core/datastore/EncryptedTokenManager.kt` → internal `SecureTokenStore.kt`) as
  `EncryptedSharedPreferences`. Interceptors depend on the injected `TokenManager`
  abstraction, never the store directly.
- `core/datastore/UserPreferencesManager.kt` stores non-sensitive preferences via
  DataStore, exposing `Flow` reads and `suspend` writes.

### Repository Implementations

| Repository | File | Responsibility |
|------------|------|----------------|
| `AuthRepository` | `common/AuthRepositoryImpl.kt` | Login, register, refresh, logout, user profile |
| `DocumentRepository` | `patient/DocumentRepositoryImpl.kt` | Upload/list/download, rename, delete, share, duplicate check |
| `MedicalResultRepository` | `patient/MedicalResultRepositoryImpl.kt` | Results by document/report/global; history |
| `OcrRepository` | `patient/OcrRepositoryImpl.kt` | Trigger OCR, poll status, cache results |
| `DashboardRepository` | `patient/DashboardRepositoryImpl.kt` | AI summary fetch/regenerate, report AI explanations |
| `MedicalHistoryRepository` | `patient/MedicalHistoryRepositoryImpl.kt` | CRUD medications and personal notes |
| `SharedLinkRepository` | `patient/SharedLinkRepositoryImpl.kt` | Consume shared document links |
| `GlobalSyncManager` | `common/GlobalSyncManagerImpl.kt` | Cross-screen sync broadcast |
| `ProcessingCoordinator` | `patient/ProcessingCoordinatorImpl.kt` | OCR processing orchestration |
| `LaboratorRepository` | — | **Contract only** (`domain/repository/laborator/`) — no implementation yet |

### Global Sync Engine

`GlobalSyncManager` is a singleton `SharedFlow`-based broadcast that keeps data-fetching screens
in sync. When a ViewModel mutates backend data (upload, medication add, note add), it calls
`globalSyncManager.triggerSync()`. Consumer ViewModels (`DashboardViewModel`,
`MedicalHistoryViewModel`, `ProfileViewModel`, `UploadedAnalysesViewModel`) collect
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
`core/util/Resource.kt` is **deprecated** — use `ApiResult<T>`.

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
| ViewModel Tests | `feature/{auth,patient}/` (e.g. `LoginViewModelTest`, `DashboardViewModelTest`, `RegistrationViewModelTest`) |
| Repository Tests | `data/repository/{common,patient}/` |
| Utility Tests | `core/util/`, `core/ui/shared/upload/` |
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

- **Global Design System** (`core/ui/components/`): universal primitives consumed by 2+ roles/screens.
- **Role features** (`feature/{role}/{feature}/components/`): feature-specific widgets (`internal`).
- **Shared features** (`core/ui/shared/{feature}/`): self-contained reusable flows (their own ViewModel).
- **Icons** (`core/ui/theme/icons/`): Kotlin `ImageVector` only.
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
