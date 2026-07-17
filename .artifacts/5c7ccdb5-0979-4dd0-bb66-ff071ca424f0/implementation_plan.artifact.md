# Pre-release QA Fixes for RepoMuse Android (Revised)

This plan addresses several UX and stability issues identified during the pre-release QA pass, focusing on offline flow, GitHub URL handling, Gemini error handling, and UI polish.

## User Review Required

> [!IMPORTANT]
> **Gemini Model:** As per user instructions, the Gemini model `gemini-3.5-flash` will **NOT** be changed. It is the intended model for this project.

## Proposed Changes

### 1. GitHub URL Handling & Validation
*   **[MODIFY] [GitHubService.kt](file:///C:/Users/ckzz2/StudioProjects/RepoMuse/app/src/main/java/com/example/data/GitHubService.kt)**
    *   Refine `getRepoMetadata` to robustly parse URLs:
        *   Handle `https://github.com/owner/repo`, `https://github.com/owner/repo.git`, `github.com/owner/repo`, etc.
        *   Strip trailing slashes, `.git` suffixes, query parameters, and fragments.
        *   Improve error handling to return specific, friendly error strings instead of crashing or returning empty strings silently.
*   **[MODIFY] [AddEditProjectScreen.kt](file:///C:/Users/ckzz2/StudioProjects/RepoMuse/app/src/main/java/com/example/ui/screens/AddEditProjectScreen.kt)**
    *   Update the inline validation regex for GitHub URLs to match the improved parsing logic.
    *   Show friendly validation text when the URL is invalid.

### 2. Data Safety & UX during AI Generation
*   **[MODIFY] [AddEditProjectScreen.kt](file:///C:/Users/ckzz2/StudioProjects/RepoMuse/app/src/main/java/com/example/ui/screens/AddEditProjectScreen.kt)**
    *   **Disable Save:** The Save button in the TopAppBar will be disabled while `isGenerating` is true.
    *   **Prevent Double Tap:** The "Generate with AI" button is already disabled during generation, but I will ensure the logic is robust.
    *   **Friendly Errors:** Ensure Gemini errors are caught and displayed via Snackbar, never dumped into text fields.

### 3. UI Polish & Stability
*   **[MODIFY] [HomeScreen.kt](file:///C:/Users/ckzz2/StudioProjects/RepoMuse/app/src/main/java/com/example/ui/screens/HomeScreen.kt)**
    *   Improve empty state spacing and contrast for better readability.
    *   Refine Project Card spacing and accent line styling for a more premium feel.
*   **[MODIFY] [ProjectRepository.kt](file:///C:/Users/ckzz2/StudioProjects/RepoMuse/app/src/main/java/com/example/data/ProjectRepository.kt)**
    *   Ensure `syncFromCloud` handles offline states gracefully without blocking the UI or causing errors.

### 4. QA Documentation
*   **[NEW] [QA_CHECKLIST.md](file:///C:/Users/ckzz2/StudioProjects/RepoMuse/QA_CHECKLIST.md)**
    *   Create a manual QA checklist covering:
        *   Offline flow (Continue Offline).
        *   Project CRUD operations.
        *   GitHub URL parsing (various formats).
        *   AI generation (success and error states).
        *   PDF export functionality.
        *   App persistence after restart.

## Verification Plan

### Automated Tests
*   Run `./gradlew assembleDebug` to verify the build.

### Manual Verification
1.  **Offline Test:** Disable internet, open app, click "Continue Offline", create a project, restart app, verify project exists.
2.  **GitHub URL Test:** Input `github.com/google/guava.git`, click Generate, verify metadata is fetched.
3.  **Error Handling Test:** Temporary mangle API key, verify friendly Snackbar error.
4.  **PDF Test:** Export a project and verify the file is created in the Downloads/Documents folder.
