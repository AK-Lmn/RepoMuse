# Security Remediation: Removing Committed Google/Firebase Keys

GitHub secret scanning identified a Google/Firebase API key that had previously been committed in `app/google-services.json`. The key value has been redacted from this repository and is represented only as `[REDACTED_GOOGLE_API_KEY]` in this document.

## What is safe to commit

- `.env`, `google-services.json`, `app/google-services.json`, `debug.keystore`, and `local.properties` are ignored.
- Build folders are ignored.
- Developers must supply their own local `.env` and `app/google-services.json` files. Do not commit those files or real credentials.

## Required manual remediation

Removing a key from the current files does not revoke it. Treat the exposed key as compromised and, in the [Google Cloud Console Credentials page](https://console.cloud.google.com/apis/credentials), locate the affected key and delete, rotate, or regenerate it. Restrict replacement keys to the required APIs and to the Android application package and signing certificate where applicable.

After the key has been revoked or rotated, review the GitHub secret-scanning alert and dismiss it only with the appropriate resolution. Do not dismiss the alert while the original key remains usable.

This cleanup does not rewrite Git history, force-push, or rotate cloud credentials. If the alert remains after revocation, consider a separate, carefully planned historical purge later.
