# NOVA — GitHub Actions APK Build Guide

This guide gets you a downloadable APK using **only your Android phone and a browser**.  
No PC, no Android Studio, no command line needed for the debug build.

---

## Part 1 — Get a Debug APK (Fastest — No signing setup)

### Step 1 · Create a GitHub account
Go to **github.com** in your phone browser → Sign Up (free).

### Step 2 · Create a new repository
1. Tap the **+** icon → **New repository**
2. Name it `nova-android` (or anything you like)
3. Set to **Public** (required for free GitHub Actions minutes) OR **Private** (you have 2 000 free minutes/month)
4. **Do NOT** tick "Add a README" — leave it empty
5. Tap **Create repository**

### Step 3 · Upload the project files
GitHub lets you upload files directly in the browser:

1. On the empty repo page tap **uploading an existing file**
2. Upload everything inside the `nova-android/` folder **and** the `.github/` folder from this project.

> **Tip:** If uploading dozens of files one by one is tedious, use the **Replit → Git panel** instead:
> 1. In Replit, open the **Git** tab (branch icon in the left sidebar)
> 2. Commit all files
> 3. Tap **Connect to GitHub** and push — all files land in your repo in one step

### Step 4 · Watch the build
1. In your GitHub repo tap the **Actions** tab
2. You will see **"Build Debug APK"** running automatically
3. Wait ~8 minutes for the green ✅

### Step 5 · Download the APK
Two ways to get it:

**Option A — GitHub Releases (easiest link to share)**
- In your repo tap **Releases** (right sidebar)
- Tap the release named **"NOVA – Latest Debug Build"**
- Tap **NOVA-debug-1.apk** → Download

**Option B — Actions Artifact**
- Actions tab → click the finished run → scroll to **Artifacts** → tap **NOVA-debug-1**

### Step 6 · Install on your phone
1. Open the downloaded `.apk` file from your Downloads
2. If prompted: **Settings → Install unknown apps → Allow for this browser**
3. Tap **Install** → **Open**
4. In NOVA: tap ⚙ → enter your OpenAI or Gemini API key → Save

---

## Part 2 — Get a Signed Release APK (Proper signing for distribution)

A release APK is signed with a unique key so Android can verify future updates come from you.

### Step 1 · Generate your signing keystore

You need a machine with the JDK installed (any PC, or a free cloud shell).  
**Free option:** Use [Replit Shell](https://replit.com) — the JDK is already installed there.

In the Replit Shell run:
```bash
chmod +x nova-android/scripts/generate-keystore.sh
bash nova-android/scripts/generate-keystore.sh
```

The script will:
- Ask you for a password (remember it — you cannot recover it)
- Generate `nova-release.keystore`
- Print four values to copy into GitHub Secrets

### Step 2 · Add secrets to GitHub
1. Go to your repo → **Settings** → **Secrets and variables** → **Actions**
2. Tap **New repository secret** and add each of the four secrets the script printed:

| Secret name | What to paste |
|---|---|
| `KEYSTORE_BASE64` | The long base64 string |
| `KEYSTORE_PASSWORD` | Your keystore password |
| `KEY_ALIAS` | `nova-key` |
| `KEY_PASSWORD` | Your key password |

### Step 3 · Trigger a release build
Option A — Push a version tag (from Replit Shell or GitHub UI):
```bash
git tag v1.0.0
git push origin v1.0.0
```

Option B — Manual trigger from the browser:
1. Actions tab → **Build Signed Release APK** → **Run workflow**
2. Enter `1.0.0` as the version name → **Run workflow**

### Step 4 · Download the signed APK
- Repo → **Releases** → **NOVA v1.0.0** → tap the `.apk` file

---

## Build times (approximate)

| Build type | Time |
|---|---|
| Debug APK | ~7–9 minutes |
| Release (signed) APK | ~8–10 minutes |

These run on GitHub's servers — your phone just needs to be online to download the result.

---

## Troubleshooting

### Build fails: "SDK location not found"
The `local.properties` file with `sdk.dir` is only needed for local builds.  
GitHub Actions uses the pre-installed Android SDK automatically — this error should not appear in CI. If it does, check that `local.properties` is **not** in `.gitignore` with a bad `sdk.dir` override.

The project's `.gitignore` already excludes `local.properties`, which is correct. GitHub Actions sets `ANDROID_HOME` automatically.

### Build fails: "License for package … not accepted"
Add this step to the workflow (already included in the provided YMLs — this note is for manual edits):
```yaml
- name: Accept SDK licenses
  run: yes | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --licenses
```

### Build fails: Gradle daemon OOM
The workflows already set `-Xmx4g`. GitHub Actions runners have 7 GB RAM — this should not occur. If it does, reduce to `-Xmx3g`.

### APK installs but crashes immediately
Enable crash logs: connect your phone via USB to any PC, run `adb logcat`, reproduce the crash, and share the output. Alternatively, check **Settings → Developer options → Bug report** on your phone.

---

## What each workflow file does

```
.github/workflows/
├── nova-debug-apk.yml      ← Runs on every push to main
│                              No secrets needed
│                              Uploads to Actions artifacts + "debug-latest" Release
│
└── nova-release-apk.yml    ← Runs when you push a v*.*.* tag or run manually
                               Requires 4 secrets (keystore)
                               Creates a proper versioned GitHub Release
```

---

## Keeping the app updated

Every time you push a change to the `main` branch, a new debug APK is built automatically and the `debug-latest` Release is updated. To install the update:
1. Download the new APK from Releases
2. Tap install — Android will update in place (same app, no data lost) for the debug build
3. For release builds, push a new tag (`v1.0.1`, `v1.1.0`, etc.)
