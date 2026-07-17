# Pre-release QA Fixes for RepoMuse Android

This plan addresses several UX and stability issues identified during the pre-release QA pass, focusing on offline flow, GitHub URL handling, Gemini error handling, and UI polish.

## Proposed Changes

### 1. Offline Flow & Stability
*   **[MODIFY] [ProjectViewModel.kt](file:///C:/Users/ckzz2/StudioProjects/RepoMuse/app/src/main/java/com/example/ui/ProjectViewModel.kt)**
    *   Ensure `saveProject` handles blank/new IDs correctly to avoid navigation crashes. (Already mostly correct, will double check).
*   **[MODIFY] [ProjectRepository.kt](file:///C:/Users/ckzz2/StudioProjects/RepoMuse/app/src/main/java/com/example/data/ProjectRepository.kt)**
    *   Ensure `syncFromCloud` doesn't crash if the user is offline or Firebase is not initialized. (Already has try-catch, but will refine).

### 2. GitHub URL Handling
*   **[MODIFY] [GitHubService.kt](file:///C:/Users/ckzz2/StudioProjects/RepoMuse/app/src/main/java/com/example/data/GitHubService.kt)**
    *   Improve repository URL parsing to handle trailing slashes and `.git` suffixes more robustly.
*   **[MODIFY] [AddEditProjectScreen.kt](file:///C:/Users/ckzz2/StudioProjects/RepoMuse/app/src/main/java/com/example/ui/screens/AddEditProjectScreen.kt)**
    *   Refine the regex for inline validation to be more permissive for valid GitHub patterns.

### 3. Gemini API Stability
*   **[MODIFY] [GeminiService.kt](file:///C:/Users/ckzz2/StudioProjects/RepoMuse/app/src/main/java/com/example/data/GeminiService.kt)**
    *   Update model name to `gemini-1.5-flash` (or stable current version) if `gemini-3.5-flash` was a placeholder/typo.
    *   Ensure raw HTTP errors are caught and returned as clean error strings.

### 4. UI/UX Improvements
*   **[MODIFY] [AddEditProjectScreen.kt](file:///C:/Users/ckzz2/StudioProjects/RepoMuse/app/src/main/java/com/example/ui/screens/AddEditProjectScreen.kt)**
    *   Disable the **Save** button in the TopAppBar while AI generation is in progress to prevent data corruption.
    *   Add a loading overlay or clearer progress indicator during generation.
*   **[MODIFY] [HomeScreen.kt](file:///C:/Users/ckzz2/StudioProjects/RepoMuse/app/src/main/java/com/example/ui/screens/HomeScreen.kt)**
    *   Minor spacing adjustments to the project cards and empty state for better contrast and legibility.

### 5. PDF Export & Navigation
*   **[MODIFY] [ProjectDetailScreen.kt](file:///C:/Users/ckzz2/StudioProjects/RepoMuse/app/src/main/java/com/example/ui/screens/ProjectDetailScreen.kt)**
    *   Ensure the PDF export success toast appears clearly.
    *   Verify navigation routes for UUIDs are safely encoded if needed (though UUIDs are usually safe).

### 6. QA Checklist
*   **[NEW] [QA_CHECKLIST.md](file:///C:/Users/ckzz2/StudioProjects/RepoMuse/QA_CHECKLIST.md)**
    *   Create a comprehensive manual QA checklist for pre-release verification.

## Verification Plan

### Automated Tests
*   Run `./gradlew assembleDebug` to ensure everything builds correctly.
*   (Optional) Run existing unit tests.

### Manual Verification
*   Test "Continue Offline" flow.
*   Verify GitHub URL validation with various formats (`.git`, trailing slash).
*   Trigger Gemini errors (missing key, no internet) and check Snackbar messages.
*   Test PDF export and verify file naming.
*   Check adaptive icon on emulator.
