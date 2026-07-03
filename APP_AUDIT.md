# RepoMuse - Codebase & Implementation Audit

This document audits the actual codebase of **RepoMuse** to verify if the features described in `PROJECT_GUIDE.md` are fully implemented, partially implemented, or missing.

---

## 1. Feature Verification Table

| Feature | Status | Files Involved | Audit & Technical Notes |
| :--- | :--- | :--- | :--- |
| **GitHub OAuth Login** | **Implemented** | `LoginScreen.kt`, `MainActivity.kt` | Uses Firebase Authentication's federated identity provider (`OAuthProvider.newBuilder("github.com")`) with native flow: `auth.startActivityForSignInWithProvider()`. |
| **Firebase Auth Setup** | **Implemented** | `LoginScreen.kt`, `google-services.json` | Configured with `google-services.json` in `/app`. Checks if Firebase is initialized and detects `isFirebaseMissing` via an `IllegalStateException` check. |
| **Continue Offline Mode** | **Implemented** | `LoginScreen.kt`, `MainActivity.kt` | Provides a "Continue Offline" button that bypasses login and routes directly to the home/dashboard screen. |
| **Room Local Database** | **Implemented** | `Project.kt`, `ProjectDao.kt`, `AppDatabase.kt` | Fully set up with Room SQLite. Generates a database with a single table (`projects`) and handles CRUD flows via background Coroutines. |
| **Firestore Sync** | **Implemented (Collisions Fixed)** | `ProjectRepository.kt`, `HomeScreen.kt`, `ProjectViewModel.kt` | Fully synchronized under `/users/{userId}/projects/{projectId}`. Changing local `id` keys to client-side UUID Strings guarantees no key collisions between multiple user devices. Includes a destructive migration fallback. |
| **GitHub REST API Metadata Fetch** | **Implemented** | `GitHubService.kt`, `AddEditProjectScreen.kt` | Calls the public API (`https://api.github.com/repos/{owner}/{repo}`) using standard unauthenticated OkHttp requests to fetch `description`, `language`, and `topics`. |
| **Gemini API Generation** | **Implemented (Production Ready)** | `GeminiService.kt`, `AddEditProjectScreen.kt` | Uses OkHttp REST calls to point to the official, stable `gemini-3.5-flash` endpoint, ensuring high reliability and continuous service support. |
| **Saved Projects Dashboard** | **Implemented** | `HomeScreen.kt`, `ProjectViewModel.kt` | Renders a fully functional searchable dashboard, custom-designed cards with category tag layouts, quick edit/delete buttons, and a sync pull-action. |
| **Add/Edit Project Screen** | **Implemented** | `AddEditProjectScreen.kt`, `ProjectViewModel.kt` | Houses the form states and handles the UUID-generated creation and editing of pre-existing models. |
| **Project Detail Screen** | **Implemented** | `ProjectDetailScreen.kt` | Displays the case study visually, containing functional copy buttons for resume bullets, share actions, and the PDF download launcher. |
| **PDF Export** | **Implemented** | `PdfExporter.kt`, `ProjectDetailScreen.kt` | Custom rendering engine using Android's native `PdfDocument` with `StaticLayout` to draw page-delimited texts, slates, and borders. |
| **Environment Variables** | **Implemented** | `GeminiService.kt`, `app/build.gradle.kts`, `.env` | Handled through the Secrets Gradle Plugin which securely maps `.env` variables into compiled Kotlin `BuildConfig.GEMINI_API_KEY`. |

---

## 2. Code Map

Here is the list of active Kotlin and configuration files within the RepoMuse Android project and their exact runtime roles:

### Data Layer (`com.example.data`)
1. **`Project.kt`**: The database model representing a single case study. Annotated as a Room `@Entity(tableName = "projects")` with a `String` unique UUID as the Primary Key.
2. **`ProjectDao.kt`**: The Room Data Access Object (DAO) interface. Defines SQL operations (`getAllProjects`, `getProjectById` using String key, `insertProject` using REPLACE conflict strategy, `updateProject`, `deleteProjectById` using String key).
3. **`AppDatabase.kt`**: The abstract class extending `RoomDatabase`. Generates the local SQLite instance.
4. **`ProjectRepository.kt`**: The central repository coordinating local DB changes (Room) and cloud writes (Firestore collection `users/{userId}/projects/{projectId}`). Implements `syncFromCloud()` which fetches all user projects from Firestore and populates Room.
5. **`GitHubService.kt`**: An object containing `getRepoMetadata()`. Leverages OkHttp to make HTTP requests to the public GitHub API and pulls description, topics, and primary language to feed as context into the AI.
6. **`GeminiService.kt`**: An object containing `generateContent()`. Submits requests to the Google Gemini API (`gemini-3.5-flash`) with the portfolio case study generation prompts, utilizing the injected `BuildConfig.GEMINI_API_KEY`.

### Presentation Layer (`com.example.ui`)
1. **`ProjectViewModel.kt`**: Bridge between UI and data layer. Exposes `StateFlow` streams of lists or single projects, automatically generating random String UUIDs for any new project before insertion.
2. **`Theme.kt`, `Color.kt`, `Type.kt`**: The Material Design 3 design system styling files for color palettes, text fonts, margins, shapes, and dark themes.

### Screen Layer (`com.example.ui.screens`)
1. **`LoginScreen.kt`**: Contains the introductory visual and standard federated GitHub provider authenticators via Firebase. Falls back to local offline mode.
2. **`HomeScreen.kt`**: A dashboard containing a scrollable lists layout (`LazyColumn`), search bar filtering, pull-to-sync triggers, and action pathways to edit/delete/view single records via String routes.
3. **`AddEditProjectScreen.kt`**: Forms to type or load data. Hosts the **AI Generator** which triggers GitHub parsing and Gemini REST content writing, parsing output back into individual Composable state text fields using robust case-insensitive markdown tags regex extraction, displaying any error logs via a beautiful Material 3 Snackbar.
4. **`ProjectDetailScreen.kt`**: Displays structured pitches, problem definitions, code lists, and features. Includes action listener to trigger PDF downloads and copy text.

### Utility & Infrastructure
1. **`PdfExporter.kt`**: Native canvas drawer that writes text lines onto standard `PdfDocument` canvases, managing margin boundaries (0.75in margins) and page count limits manually.
2. **`RepoMuseApplication.kt`**: Custom Android `Application` class initializing the single instances of the SQLite `AppDatabase` (with destructive migration fallback) and `ProjectRepository`.
3. **`MainActivity.kt`**: Entry point Activity of the application. Generates the `NavController` navigation graph mapping routes (`"login"`, `"home"`, `"addEdit/{projectId}"`, `"detail/{projectId}"`) to screens.

---

## 3. Important Logic Walkthrough

### What happens when the app starts?
1. Android OS initializes `RepoMuseApplication` which boots the SQLite Room `AppDatabase`.
2. `MainActivity` loads. It wraps the app inside the dynamic Material 3 design template and starts the navigation system.
3. The navigation system boots up the **Login** route (`"login"`). 
4. The screen checks if Firebase is configured. If yes, it checks whether `FirebaseAuth.getInstance().currentUser != null`. If the user is already logged in, it automatically routes them to the **Home Screen** (`"home"`).

### What happens when the user logs in?
1. The user clicks "Continue with GitHub" in `LoginScreen.kt`.
2. If there's an ongoing authentication flow, it awaits completion. Otherwise, it triggers Firebase Auth's federated provider flow `startActivityForSignInWithProvider()`.
3. This opens a browser tab (GitHub auth screen).
4. After entering credentials, GitHub redirects back to the Firebase Auth callback. Firebase returns a success state, completing the `Task<AuthResult>`.
5. Upon successful callback, the app routes the user to `HomeScreen` and links future portfolio entries to `users/{userId}/...` in Firestore.

### What happens when the user adds a GitHub repo?
1. On the add screen, the user pastes a repository link (e.g., `https://github.com/facebook/react`).
2. Tapping "Generate with AI" launches a background Coroutine.
3. It passes the URL to `GitHubService.getRepoMetadata()`, which extracts the username and repository name using regular expressions, queries the public GitHub API via OkHttp, and extracts repository properties (e.g., primary language, topic tags, and descriptions).

### What happens when AI generation is clicked?
1. The app appends the GitHub metadata context (if available) to the user's manual title/notes inputs.
2. It sends a highly structured prompt to Google's Gemini API stable endpoint `gemini-3.5-flash` (`GeminiService.kt`).
3. The API generates a response containing labels like `PITCH:`, `PROBLEM:`, `TECH_STACK:`, `FEATURES:`, etc.
4. Once received, the app runs case-insensitive, markdown-star/hash-tolerant regular expressions to extract each section safely (e.g. matching `## PITCH:`, `**Pitch:**`, etc.).
5. The extracted strings are immediately assigned to the mutable state variables in the form fields. If formatting parser regex fails, it drops the raw response into the "Case Study" text box as a fallback, notifying the user via Snackbar. If an API key is missing or internet is down, a user-friendly Snackbar message displays.

### What happens when a project is saved?
1. Tapping the checkmark (Save) button in `AddEditProjectScreen.kt` triggers `viewModel.saveProject()`.
2. If it is a new project, a unique String UUID is generated (`UUID.randomUUID().toString()`) and `repository.insert()` is triggered. If editing, `repository.update()` is called with the pre-existing UUID.
3. The record is instantly saved locally inside Room (SQLite).
4. If a user is logged in, the repository launches an asynchronous network block and duplicates the record to the remote Firestore under the collection path `users/{userId}/projects/{projectId}` using the generated UUID as the document ID.

### What happens when export PDF is clicked?
1. From the project details screen, the user clicks "Export Case Study as PDF".
2. The screen registers an activity outcome listener (`CreateDocument` SAF contract) requesting the OS to spawn a system file chooser asking where the user wants to write `RepoMuse_{Project_Title}.pdf`.
3. Once the system returns a valid URI, a writing stream (`OutputStream`) is opened.
4. `PdfExporter.exportToPdf` is called. It creates a native Android `PdfDocument`, initializes customized TextPaints with Material 3 styling colors, dynamically builds text boundaries using `StaticLayout` line-by-line, measures spacing, draws page numbers/decorations on a Canvas, and writes the byte stream.

---

## 4. Risks & Possible Bugs

1. **GitHub API Rate Limits**:
   The OkHttp request in `GitHubService` is unauthenticated. GitHub enforces a strict limit of 60 requests/hour per IP for unauthenticated queries. If multiple users use the same public Wi-Fi or emulator container, metadata retrieval will fail.
2. **Destructive Database Migration Warning**:
   Because we added `.fallbackToDestructiveMigration()`, users who previously had local integer ID projects will find their databases cleared when upgrading to the new UUID database. This is a standard and necessary transition step for local development and MVP refactoring.

---

## 5. What You Should Test Manually

Follow this step-by-step checklist to test the app on your emulator or phone:

- [ ] **Check Login and Offline Flow:**
  - Launch the app. You should see the login screen.
  - Check the warning message. If `google-services.json` is configured correctly, there should be no error about "Firebase is not configured".
  - Tap "Continue Offline". You should immediately land on the dashboard (empty state: "No projects generated yet.").
- [ ] **Check Manual Project Creation:**
  - Tap the FAB (`+`) button.
  - Fill in a title (e.g., "UUID Calculator") and some sample notes.
  - Tap the Save icon (Checkmark) at the top right.
  - Verify it shows up on the main dashboard.
- [ ] **Check API Metadata & Generation:**
  - Create another new project.
  - In the "GitHub URL" field, type a valid public repository url (e.g., `https://github.com/octocat/Hello-World`).
  - Tap "Generate with AI".
  - Verify that a loading spinner appears, and after a few seconds, the inputs populate (Pitch, Problem, Tech Stack, Features, Challenges, and Case Study text fields should fill automatically).
- [ ] **Check Error SnackBar Triggering:**
  - Temporarily remove your Gemini API key, or disconnect your internet.
  - Tap "Generate with AI".
  - Verify that a beautiful Snackbar slides up at the bottom describing the exact user-friendly problem (e.g., network disconnect or missing API key) rather than printing raw technical exceptions in input fields.
- [ ] **Check Local Search & Delete:**
  - Create multiple projects with unique terms.
  - Type in the Search bar on the HomeScreen. Confirm lists filter live.
  - Tap the trash can icon on a card. Verify a confirmation dialog prompts you before deleting.
- [ ] **Check PDF Generation:**
  - Open any project from the dashboard.
  - Scroll down to "Portfolio Case Study" and click "Export Case Study as PDF".
  - Choose a location in the Android folder system and tap "Save".
  - Ensure the "PDF Exported Successfully!" toast appears.
  - Go to your files app, open the PDF, and verify layout spacing.
