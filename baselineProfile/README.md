# Baseline Profile generation

What to expect on screen during a `:app:generateBaselineProfile` run, and how to tell it worked.

## Before you start

- One **physical device** on ADB, and only one — the plugin uses `useConnectedDevices = true`, so a second device or a running emulator makes the run ambiguous.
- **API 33+** (or rooted API 28+). Profile collection needs it; `minSdk 29` is not sufficient on its own.
- Screen **unlocked and set to stay awake**. The run drives the UI for many minutes; a lock screen kills it.
- **Network reachable** — the first iteration signs in against Firebase.
- A second terminal on the logs:

```
adb logcat -s MyFinanceBP
```

## Run it

```
.\gradlew.bat :app:generateBaselineProfile "-Pandroid.testInstrumentationRunnerArguments.bpEmail=you@example.com" "-Pandroid.testInstrumentationRunnerArguments.bpPassword=your-password"
```

If the device already holds a session from an earlier run, both arguments can be dropped.

Roughly 6–15 minutes overall (estimate), dominated by the first test.

> **Do not touch the device while it runs.** Your taps go to the same app the run is driving, so they change what gets recorded. Worse, they make the collected profile differ from one iteration to the next, and the run keeps repeating until it sees three *identical* iterations in a row — so interfering makes it take longer, which is usually what makes people interfere. Leave it alone and watch logcat instead.

## Phase 1 — build and install

Several minutes with nothing happening on the device. Gradle assembles `nonMinifiedRelease` of the app plus the `:baselineProfile` test APK, installs both, then instrumentation starts and the device wakes up. Minification is off for this variant, so the test tags survive.

## Phase 2 — on the device

Three tests run in a fixed order. Each repeats its whole sequence until the collected profile stops changing — **at least 3 times** (`stableIterations = 3`) — killing the app in between. You will see the splash screen over and over. That is the tool working, not a loop to interrupt.

The long journey is capped at `maxIterations = 6` so a profile that refuses to settle cannot drag the run out; the two startup tests keep the default ceiling of 15, since they take seconds each.

### 1. `journeysSignedIn` — the long one, ~60s per iteration

1. Launcher home screen.
2. Splash, then the sign-in screen. *(First iteration only — once a session exists, later iterations land on the dashboard and skip steps 3–5.)*
3. The sign-up form expands, then collapses.
4. **Email and password fill themselves in, with no keyboard.** ← see below
5. Sign-in is tapped. Wavy progress bar, then the dashboard with a "Login successful" snackbar.
6. Dashboard scrolls down and back to the top.
7. Bar chart steps back one month, forward one, then jumps to today.
8. Pie chart steps back one, switches to Annual, then back to Monthly.
9. Expenses tab. The list flings to the bottom and back up.
10. The letter `a` appears in the search field, the list narrows, then the query clears.
11. Filter sheet slides up (Label / Category / Date range). Category is tapped, the category grid opens, back dismisses it.
12. Filter sheet opens again, Date range is tapped, the date-range picker appears, back dismisses it. *Nothing is ever selected, so no filter is left applied.*
13. A transaction is long-pressed. Action sheet appears (Labels / Edit / Duplicate / Delete). **Edit** is tapped, the Add screen opens pre-filled, and the X closes it without saving.
14. Budget tab. The list scrolls down and back.
15. Profile tab. The account row expands and collapses, then Manage categories, Manage labels and Change password each open and close, then the currency sheet opens and closes.
16. Back to the dashboard, FAB tapped. The Add screen fills in with "Baseline profile" and 12.34, the expense/income switcher opens and closes, and the X closes the screen — **never the save tick**.
17. The app dies, launcher reappears, next iteration starts at step 2.

> **The moment that proves the fix.** At step 4 the credentials should appear **without the soft keyboard ever opening** — that means UiAutomator found the fields by resource-id and wrote to them through the accessibility action. If the keyboard springs up, or the fields stay empty and the run fails within ~20 seconds, the `testTagsAsResourceId` opt-in is not in the build you just installed.

### 2. `startupCold` — a few seconds per iteration

1. Launcher home screen.
2. Splash, then the dashboard.
3. The app dies. Repeat.

It lands on the dashboard rather than the sign-in screen because `journeysSignedIn` left a session behind — which is why the test order is pinned with `@FixMethodOrder(NAME_ASCENDING)`.

### 3. `startupFromShortcut`

1. Launcher home screen.
2. The app opens **straight onto the Add screen** in Expense mode. The dashboard never appears.
3. The process is killed, launcher returns.
4. The app opens straight onto the Add screen again, in Income mode.
5. The app dies. Repeat.

If the Add screen never appears, the log says `Add screen never appeared for com.frafio.myfinance.ADD_EXPENSE`. The shortcut only opens it for a signed-in user, so the session was lost — check that `journeysSignedIn` actually completed.

## Phase 3 — verify

Two files are written into the app source tree (`mergeIntoMain = true`):

```
app/src/main/generated/baselineProfiles/baseline-prof.txt
app/src/main/generated/baselineProfiles/startup-prof.txt
```

The real check is the relationship between them:

```
wc -l app/src/main/generated/baselineProfiles/*.txt
```

> **Success criterion.** Before the restructure both files were byte-identical at 27,169 lines, because every test set `includeInStartupProfile = true` and the startup profile swallowed the whole app. After this run `startup-prof.txt` should be **clearly smaller** than `baseline-prof.txt` — only what the two `startup*` tests touched. If they still match, the startup flags did not take effect.

Then measure the payoff. `StartupBenchmarks` covers launch; `ScrollBenchmarks` covers the scrolling the long journey exists to optimise:

```
.\gradlew.bat :baselineProfile:connectedBenchmarkReleaseAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.frafio.myfinance.baselineProfile.ScrollBenchmarks"
```

Compare `frameDurationCpuMs` P50 and P95 between the `CompilationNone` and `CompilationBaselineProfiles` variants. P95 is where a baseline profile shows up, because first-scroll jank is what it removes.

## If it stops

| What you'll see | What it means |
| --- | --- |
| `Auth screen not found: no node with resource-id 'email_field'` | The root `Surface` in `MainActivity` is missing `Modifier.semantics { testTagsAsResourceId = true }`, or you installed a build from before it was added. |
| `No signed-in session and no credentials` | No session on the device and neither `bpEmail` nor `bpPassword` was passed. |
| `Sign-in failed: the dashboard never appeared` | Wrong credentials, or no network. Check logcat unfiltered for the Firebase auth error. |
| `No transaction to open, skipping the edit journey` | Not a failure. The account has no expenses to long-press; the rest continues. |
| `Task 'generateReleaseBaselineProfile' not found` | Wrong task name. `mergeIntoMain = true` collapses the per-variant tasks into a single `generateBaselineProfile`. |

## Notes

- **Nothing here writes to Firestore.** Forms are filled but never saved; the transaction sheet's Delete, Duplicate and Labels entries are never tapped; category and label pickers are only reached through the filter sheet, where selection changes a local filter rather than the transaction.
- Step order is derived from the generator source, so it is exact. Durations are estimates — this was written from the code, not from a recorded run.
