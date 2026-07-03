# Migration Notes: Transition to String UUID Project IDs

This document outlines the architectural shift made in the RepoMuse database and synchronization strategy, moving from auto-incrementing integer IDs to globally unique client-side UUID strings.

---

## 1. Why we migrated
In the previous implementation, the local Room database auto-generated integer primary keys sequentially (`1`, `2`, `3`...).
While this works perfectly in a local-only database, it poses a **critical failure risk** once cloud synchronization is enabled:
- **Write Collisions:** If User A has created 2 local projects on Device 1 (assigned IDs `1` and `2`), and then logs in on Device 2 where the local database is empty, the first new project created on Device 2 will also be assigned ID `1`.
- **Data Overwriting:** When syncing to Firestore, the document paths are mapped as `/users/{userId}/projects/{projectId}`. Because both devices claim project ID `1`, Device 2 will instantly overwrite User A's project `1` from Device 1 on the cloud database.

### The Solution: Globally Unique UUIDs
By transitioning the `id` field of the `Project` model to a client-generated String representing a **UUID (Universally Unique Identifier)**, we guarantee that every case study has a globally unique key. Device 1 and Device 2 can now create projects in parallel without any risk of overlap, collision, or accidental deletion.

---

## 2. Changes Implemented

### Data Model & DAO
- **`Project.kt`**: Changed the `@PrimaryKey` column type from `Int` to `String`, initializing `id = ""`.
- **`ProjectDao.kt`**: Updated lookup (`getProjectById(id: String)`) and deletion (`deleteProjectById(id: String)`) signatures. Removed auto-generation flags.

### Data Repository & Synchronizer
- **`ProjectRepository.kt`**: Updated all references to use `String` ID values. Firestore document IDs now map directly from `project.id` as a clean string path, aligning perfectly with cloud identifiers.
- **`RepoMuseApplication.kt`**: Appended `.fallbackToDestructiveMigration()` to the `Room.databaseBuilder` chain. This allows the application to cleanly recreate the SQLite database files when upgrading from the older schema version, preventing start-up crashes.

### State & Navigation
- **`ProjectViewModel.kt`**: Changed internal query flows. When `saveProject` is called, the ViewModel detects if a project is new (indicated by a blank ID, or `"0"`, or `"new"`), and generates a random UUID (`UUID.randomUUID().toString()`) on the client before calling `repository.insert()`.
- **`MainActivity.kt`**: Updated NavHost routes. Replaced integer argument queries with standard string parameter passing. Navigating to `"addEdit/new"` indicates a new project is being created.
- **`HomeScreen.kt`, `AddEditProjectScreen.kt`, `ProjectDetailScreen.kt`**: Rewrote composable signatures, click actions, state flows, and navigation parameters to route with string identifiers.

---

## 3. Local Verification Actions
If you are developing this project locally, your previous emulator database will use the old schema. Because `.fallbackToDestructiveMigration()` is enabled, Room will automatically drop the old tables and create the new table structure. No manual SQLite adjustments or command-line clears are needed!
