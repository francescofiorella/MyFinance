# Releasing

A release is one command. Once CI has passed on the commit, the [Release workflow](../.github/workflows/release.yml) generates the baseline profile on an emulator, commits it with the new version as "Release X.Y.Z" on `main`, tags that commit `vX.Y.Z`, builds the signed APK, publishes the GitHub Release and posts it to Telegram.

```
git push                                        # GitHub's main must have everything you want to ship
gh workflow run release.yml -f version=5.2.0
gh run watch                                    # pick the run; shows each step live
git pull                                        # brings back the "Release 5.2.0" commit and the tag
```

Don't edit the version by hand: the workflow writes it to `app/version.properties`, and `app/build.gradle.kts` derives the version code from it.

## What the workflow does

| Job | What it does | Typical time |
| --- | --- | --- |
| Check the version | Fails unless the version is `MAJOR.MINOR.PATCH`, newer than the last release tag, not tagged yet, and the run is on `main` | under a minute |
| Wait for CI | Fails unless the CI run of the same commit (lint, JVM and screenshot tests, device tests) passed; waits for it while it's still running, so a release can be started right after the push | seconds, or until CI finishes |
| Generate the baseline profile | Runs `BaselineProfileGenerator` on an API 36 emulator, signed in with the test account, and checks that both profile files came out with `startup-prof.txt` smaller than `baseline-prof.txt` | 20–40 minutes |
| Build and publish | Commits the version and the profile, tags the commit, builds and verifies the signed APK, pushes commit and tag together, then creates the GitHub Release | about 10 minutes |
| Post to Telegram | Sends the release notes as a message, then the APK as a file | under a minute |

Nothing is pushed or published until the APK is built and signed, and the commit and tag are pushed in one atomic step: if `main` moved since the run started, neither lands.

**Release notes** are the subjects of the commits since the previous release tag, one per line, without the "Release X.Y.Z" commits. Write commit subjects with that in mind.

**Version rules:** always `MAJOR.MINOR.PATCH`, always newer than the last release, MINOR and PATCH below 1000. The version code is MAJOR × 1,000,000 + MINOR × 1,000 + PATCH (5.2.0 → 5002000).

## Trying it without publishing

```
gh workflow run release.yml -f version=5.2.0 -f dry_run=true
```

A dry run does everything except touch `main` and Telegram: the commit goes to the branch `release-dry/5.2.0`, the tag is `v5.2.0-dry.<run number>` and the release is a pre-release. Dry-run tags never count as the last release. Delete it afterwards:

```
gh release delete v5.2.0-dry.<run number> --cleanup-tag --yes
git push origin --delete release-dry/5.2.0
```

## When a run fails

| Where it failed | What's already done | What to do |
| --- | --- | --- |
| Wait for CI | Nothing | Fix what CI reported, push, and run the same command again; for a flaky test, `gh run rerun <CI run id> --failed`, then start the release again |
| Check the version, Generate the baseline profile, or Build and publish before the push | Nothing | Read the error, fix it, run the same command again |
| Build and publish after the push, or Post to Telegram | Commit and tag are on GitHub | `gh run rerun <run id> --failed` re-runs only what failed |
| You want to see why | — | `gh run view <run id> --log-failed`; the generator's reports are in the run's `baseline-profile-reports` artifact |
| A release went out wrong | Release, tag, commit | `gh release delete vX.Y.Z --cleanup-tag --yes`, then release a new patch version; never reuse a version |

The profile generation is described step by step, with its failure messages, in [baselineProfile/README.md](../baselineProfile/README.md).

## Secrets

The workflow reads eight repository secrets, set with `gh secret set <NAME>`:

| Secret | What it is |
| --- | --- |
| `KEYSTORE_BASE64` | The release keystore (`.jks`), base64-encoded |
| `KEYSTORE_PASSWORD` | The keystore's password |
| `KEY_ALIAS` | The alias of the signing key in the keystore |
| `KEY_PASSWORD` | The signing key's password |
| `BP_EMAIL` | Email of the test account the profile generator signs in with |
| `BP_PASSWORD` | That account's password |
| `TELEGRAM_BOT_TOKEN` | The token @BotFather gave the bot, which is an admin of the channel |
| `TELEGRAM_CHAT_ID` | The channel id (`-100…`, or `@channel` for a public channel) |

The release keystore must be the key the published app is signed with, or users can't update over their installed version. Keep a copy of it and its passwords off this PC.

## Building a signed release locally

`app/build.gradle.kts` signs release builds only when the key comes from the environment; without it, `assembleRelease` produces an unsigned APK as before. In PowerShell:

```
$env:RELEASE_KEYSTORE_FILE = "C:\path\to\release.jks"
$env:RELEASE_KEYSTORE_PASSWORD = "…"
$env:RELEASE_KEY_ALIAS = "…"
$env:RELEASE_KEY_PASSWORD = "…"
.\gradlew :app:assembleRelease
```
