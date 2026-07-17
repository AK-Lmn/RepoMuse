# RepoMuse Pre-Release QA Checklist

## 1. Offline & Authentication
- [ ] **Launch & Skip Login:** Clear app data, launch, tap "Continue Offline". Verify you reach the dashboard.
- [ ] **Local Persistence:** Add a project offline, close app, reopen. Verify project is still there.
- [ ] **GitHub Login:** Log in with GitHub. Verify previously added local projects (if synced) or new projects sync to cloud.

## 2. Project Management (CRUD)
- [ ] **Add Project:** Create a new project with title and manual notes.
- [ ] **Edit Project:** Modify an existing project's title and pitch.
- [ ] **Delete Project:** Delete a project and confirm it disappears from the list.
- [ ] **Search:** Add 3 projects, search for one by title. Verify filtering works.

## 3. GitHub URL Handling
- [ ] **Format Validation:** Test the following formats in the Add/Edit screen:
    - `https://github.com/google/guava` (Valid)
    - `https://github.com/google/guava.git` (Valid)
    - `github.com/google/guava/` (Valid)
    - `https://google.com` (Invalid - should show error)
- [ ] **Metadata Fetch:** Enter a valid GitHub URL and tap "Generate with AI". Verify description/tags are fetched if internet is available.

## 4. AI Generation (Gemini)
- [ ] **Success Path:** Tap "Generate with AI" on a project with a title. Verify fields (Pitch, Problem, Tech Stack, etc.) are populated.
- [ ] **Concurrency Safety:** Tap "Generate with AI" and immediately look at the TopAppBar Save icon. It should be disabled (grayed out) until generation finishes.
- [ ] **Error Path (No Internet):** Disable internet, tap "Generate with AI". Verify a friendly Snackbar appears: "Network error. Please check your internet connection."
- [ ] **Error Path (Invalid API Key):** (Developer only) Mangle the API key in local.defaults and verify "Authentication error (403)" or similar Snackbar appears.

## 5. PDF Export
- [ ] **Export:** Open a project detail, tap "Export Case Study as PDF".
- [ ] **File Picker:** Verify the Android document picker opens and suggests a filename like `RepoMuse_Project_Name.pdf`.
- [ ] **Verification:** Save the file and open it with a PDF viewer. Verify all sections (Pitch, Problem, etc.) are present and formatted nicely.

## 6. UI & Branding
- [ ] **App Icon:** Verify the RepoMuse logo appears on the home screen launcher.
- [ ] **App Name:** Verify the app label is "RepoMuse".
- [ ] **Empty State:** Clear all projects. Verify the empty state illustration and onboarding steps look polished and centered.
- [ ] **Dark Mode:** Verify the "Premium Dark" theme is consistent across all screens.

## 7. Release Readiness
- [ ] **Build:** Run `./gradlew assembleRelease` (if signing configured) or `assembleDebug`.
- [ ] **Install:** Install the resulting APK on a physical device.
- [ ] **Performance:** Scroll through a list of 10+ projects. Verify no significant jank.
