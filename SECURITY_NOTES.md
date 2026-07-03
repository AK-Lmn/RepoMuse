# Security Remediation: Removing Committed Google/Firebase Keys

This document explains the security measures taken to resolve a GitHub secret scanning alert for **RepoMuse AI**, explains how to properly configure your local Firebase settings, and details the steps required to clean up Git history.

---

## 🚨 Why GitHub Flagged `google-services.json`

GitHub’s automated secret scanning scanned the repository and detected a valid Google API Key inside `app/google-services.json` (under the `api_key` array with the value `AIzaSyBVyXpkvwY9mNdrCGMPx-wproK-CKfCcIQ`).

While `google-services.json` is a configuration file meant for your Android app build process, committing the real file to a public repository:
1. **Exposes active Google API keys / Firebase configurations** to the public.
2. **Allows unauthorized third-party usage** of your project's Firebase resources.
3. **Triggers automated alerts** in security monitoring tools.

---

## 🛠️ Security Actions Completed

1. **Ignored configuration files:** Added `google-services.json` and `app/google-services.json` to both the root `.gitignore` and `/app/.gitignore` files so that they can never accidentally be committed again.
2. **Created clean placeholders:** Added `/app/google-services.example.json` containing safe, dummy configuration fields. Developers can copy this template locally to construct their own active Firebase setup.
3. **Verified key cleanliness:** Audited the entire source code (`Kotlin` classes, `XML` layouts, and build configs) to confirm that no other API Keys, Client Secrets, Personal Access Tokens, or passwords are hardcoded.

---

## 🔒 Crucial Security Warnings (Rotate Your Keys!)

> [!WARNING]
> **Removing a file from Git's latest commit DOES NOT delete it from previous commits or Git history.** 
> If your repository was public when the key was exposed, the key is considered compromised.

To ensure your Firebase project and billing remain secure, please complete these steps:

1. **Rotate/Delete Exposed Key:** Go to the [Google Cloud Console Credentials Page](https://console.cloud.google.com/apis/credentials), find the key matching `AIzaSyBVyXpkvwY9mNdrCGMPx-wproK-CKfCcIQ`, and **delete or regenerate** it.
2. **Restrict API Keys:** When creating/using Google Cloud API keys, always add **API restrictions** (e.g., restrict usage to only Firebase Services) and **application restrictions** (e.g., restrict usage to your specific Android application package name and SHA-1 fingerprint).

---

## 🧹 Manual GitHub History Cleanup Steps

Since the file `app/google-services.json` was previously tracked and committed, you must run the following commands to clear it from your git index and optionally purge it from historical git history.

### Step 1: Remove `google-services.json` from git tracking
Run this from your repository root directory to untrack the file while keeping your local physical copy intact:
```bash
git rm --cached app/google-services.json
git add .gitignore app/.gitignore app/google-services.example.json SECURITY_NOTES.md
git commit -m "Remove Firebase config from repository"
git push
```

### Step 2: Clear historical commits (Highly Recommended)
If the secret scanning alert remains open, it means the key is still accessible in older commits or pull request histories. To completely purge the file from all commits, tags, and branch history, use `git filter-repo` or BFG Repo-Cleaner.

#### Option A: Purge history using `git filter-repo` (Recommended)
1. Install `git-filter-repo` (via pip: `pip install git-filter-repo` or brew: `brew install git-filter-repo`).
2. Run the following command in a fresh clone of your repository:
   ```bash
   git filter-repo --path app/google-services.json --invert-paths
   ```
3. Push the purged history back to your repository:
   ```bash
   git push origin --force --all
   git push origin --force --tags
   ```

#### Option B: Request GitHub to scan and dismiss
Once you have rotated the exposed key in the Google Cloud Console, you can safely go to the **Security** -> **Secret scanning** tab on GitHub and dismiss the alert as **"Revoked"** or **"Used in testing/dummy data"**.

---

## ℹ️ Local Development Configuration

During local development, your app builds perfectly because Gradle will locate your local `/app/google-services.json` (which is safely ignored by git). Simply follow the project guide to obtain your own configuration file.
