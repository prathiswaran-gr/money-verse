# MoneyVerse — Product Roadmap + Sub-project 1 (Foundation) Design Spec

## Context

New Kotlin Android money-tracking app ("MoneyVerse") targeting a Play Store launch in India. The repo is empty (no commits on `master`).
Requested features: manual expense entry, auto-capture of transactions from notifications/SMS/email, a "Buy or Wait" AI advisor,
family budgets, a subscription tracker with keep/cancel/snooze reminders, and 4 analytics views. Firebase provides storage/backend;
RevenueCat handles premium subscriptions.

The scope covers six independent subsystems, so it is split into sub-projects. Each gets its own spec, plan, and implementation cycle.
This document records the agreed product decisions and full design for **Sub-project 1: Foundation**.

---

## 1. Product decisions (all sub-projects)

| Area | Decision |
|---|---|
| Goal | Production Play Store app |
| Market | India: INR, UPI apps (GPay/PhonePe/Paytm), Indian bank alerts |
| Auto-capture sources | Notification listener (launch), SMS and Gmail API (behind Remote Config flags until Google approves the restricted SMS permission and Gmail OAuth verification + CASA) |
| Parsing | On-device regex/template parsers. Only unparseable messages, redacted, go to a Gemini fallback |
| Capture UX | Auto-add as `UNREVIEWED`, then a review inbox. Notification action for quick categorize. Dedupe across sources |
| Buy-or-Wait bot | Budget-aware advisor: reads budgets, month spend, upcoming subs, goals → Buy / Wait (until date) / Skip + reasoning, with chat follow-ups |
| LLM | Gemini via Firebase AI Logic, protected by App Check |
| Family | Txns private by default; user tags a txn to a group. Groups have shared budgets. Roles: owner/admin/member |
| Subscriptions | Manual + auto-detect from recurring txns. Reminder N days before renewal: Keep / Cancel / Snooze (1d/3d/custom). Cancel = mark ended + deep-link to provider |
| Analytics | 4 views: category breakdown, trend over time, budget vs actual, merchant/payment-mode/family split |
| Premium (RevenueCat) | Bot (3 free chats/mo teaser), Family groups, Budget-vs-actual + merchant/family analytics, Sub auto-detect |
| Free | Manual entry, auto-capture, category + trend analytics, manual subs + reminders, budgets, CSV export |
| Pricing | Monthly + yearly, 7-day trial on yearly (e.g. ₹99/mo, ₹799/yr; final prices set in Play Console) |
| Family billing | Owner's Premium unlocks group features for all members (max ~6). Personal premium features still need the member's own sub |
| Auth | Google Sign-In only (Credential Manager + Firebase Auth) |
| Local data | Room is the source of truth, synced to Firestore via WorkManager |
| Team | Solo dev + Claude, MVP first |

## 2. Roadmap

| # | Sub-project | Release |
|---|---|---|
| 1 | **Foundation**: modules, auth, Room + Firestore sync, manual txns, categories, budgets, RevenueCat paywall + entitlement mirror, settings, account deletion | MVP |
| 2 | Auto-capture: notification listener, parser engine, dedupe, review inbox | MVP |
| 3 | Subscriptions: manual + reminders/snooze, then auto-detect (premium) | MVP |
| 4 | Analytics: 4 views on Room aggregations | MVP |
| 5 | Family groups: invites, roles, shared budgets, realtime listeners, owner entitlement | Post-MVP |
| 6 | Buy-or-Wait bot | Post-MVP |
| 2b/2c | SMS capture, Gmail capture (once Google approvals land) | Post-MVP |

---

## 3. Foundation design

### 3.1 Stack
- Kotlin 2.x, Jetpack Compose + Material 3, Navigation Compose (type-safe routes), minSdk 26, targetSdk = latest stable
- Hilt, Coroutines + Flow, Room (KSP), WorkManager, DataStore
- Firebase: Auth, Firestore, Remote Config, App Check (Play Integrity), Crashlytics, Analytics, Cloud Functions (TypeScript)
- RevenueCat Purchases SDK. Entitlement `premium`, offering `default` (monthly, yearly + 7d trial)
- Gradle: `build-logic` convention plugins, `gradle/libs.versions.toml`

### 3.2 Clean architecture: feature × layer modules
```
:app                                   Hilt root, nav graph, MainActivity
:core:common                           Result/DomainError, dispatchers, UUIDv7, Money (pure Kotlin)
:core:database                         Room DB, migrations, DAOs registry
:core:firebase                         Auth, Firestore, Remote Config, App Check providers
:core:billing                          RevenueCat wrapper
:core:designsystem                     theme, components
:core:testing                          fakes, test rules
:feature:auth:{domain,data,presentation}
:feature:txn:{domain,data,presentation}
:feature:category:{domain,data,presentation}
:feature:budget:{domain,data,presentation}
:feature:paywall:{domain,data,presentation}
:feature:settings:presentation
:sync                                  SyncWorker + Syncer multibinding
functions/                             Cloud Functions (TS)
```
- Convention plugins: `moneyverse.jvm.domain` (pure `kotlin("jvm")`), `moneyverse.android.data`, `moneyverse.android.presentation`, `moneyverse.android.application`
- Dependency rule, enforced by Gradle: `presentation → domain`, `data → domain`, `domain → :core:common` only (no Android)
- Cross-feature access only through domain interfaces. Example: the budget domain depends on the txn domain's `TransactionRepository` interface, never on another feature's data or presentation module

### 3.3 Data model
Conventions:
- Money is `Long` minor units (paise) plus a `currency` field (default `"INR"`)
- IDs are client-generated UUIDv7
- Time: `occurredAt` (epoch ms UTC) + `zoneId`; `createdAt`, `updatedAt`, `deletedAt?` (soft delete)
- Room-only column: `syncState` (`SYNCED|PENDING|FAILED`)

Entities:
- **Transaction**: id, userId, amountMinor, currency, type (`EXPENSE|INCOME`), categoryId, merchant?, note?, paymentMode (`UPI|CARD|CASH|NETBANKING|WALLET|OTHER`), occurredAt, zoneId, source (`MANUAL|NOTIFICATION|SMS|GMAIL`), reviewState (`REVIEWED|UNREVIEWED`), groupId?, dedupeKey?, timestamps
- **Category**: id, ownerId? (null = system seed), name, iconKey, colorHex, type, sortOrder, archived
- **Budget**: id, ownerId, scope (`PERSONAL|GROUP`), groupId?, categoryId? (null = overall), limitMinor, period (`WEEKLY|MONTHLY`), startDate, rollover=false
- **UserProfile**: uid, displayName, email, photoUrl, defaultCurrency, createdAt
- **Entitlement** (read-only mirror): uid, premium, expiresAt, source

The `source`, `reviewState`, `groupId`, and `dedupeKey` fields exist now so sub-projects 2 and 5 need no schema migration.

Firestore:
```
users/{uid}                          profile
users/{uid}/transactions/{id}
users/{uid}/categories/{id}
users/{uid}/budgets/{id}
entitlements/{uid}                   written only by Cloud Function
groups/{gid}/...                     reserved (sub-project 5)
```
Security rules: owner-only access to the `users/{uid}` subtree; `entitlements/{uid}` is read-own and clients cannot write it; type/range validation on amounts and enums.

### 3.4 Sync
- **Write:** UseCase → Repository → Room write (`PENDING`, `updatedAt=now`) → enqueue unique `SyncWorker` (CONNECTED constraint, exponential backoff). UI observes Room Flows only.
- **Push:** batch PENDING rows into a Firestore `WriteBatch` (≤500 ops) with `serverUpdatedAt = serverTimestamp()`, then mark `SYNCED`. Transient errors retry. A rules rejection marks the row `FAILED` and logs a Crashlytics non-fatal.
- **Pull:** on app foreground and after each push, query `serverUpdatedAt > cursor` per collection and upsert into Room. Cursors live in DataStore. No realtime listeners for personal data (keeps read costs down).
- **Conflicts:** last-write-wins on `updatedAt`. A tombstone beats edits older than itself. Tombstones are purged after 30 days.
- **First login / new device:** full pull with progress UI before the home screen. Seed default categories if the remote has none.
- **Logout:** if PENDING rows exist, warn and require confirmation; then clear Room + DataStore.
- **Account deletion (Play requirement):** callable Function `deleteAccount` deletes the Firestore subtree + Auth user; the client wipes local data. A web deletion-request URL is also provided for Data Safety.
- **Extensibility:** each `:feature:x:data` provides an `XSyncer` (local + remote data sources) via a Hilt `@IntoSet` multibinding. One `SyncWorker` runs all syncers.

### 3.5 Auth + billing
- Credential Manager `GetGoogleIdOption` → Firebase `GoogleAuthProvider`. `AuthRepository.authState: Flow<SignedOut|SignedIn(uid)>` gates the nav graph.
- On sign-in, `Purchases.logIn(uid)` makes the RevenueCat app user ID equal the Firebase uid. On logout, `Purchases.logOut()`.
- `BillingRepository` (`:core:billing`): `entitlement: Flow<Entitlement>`, `offerings()`, `purchase(pkg, activity)`, `restore()`.
- UI gating uses RevenueCat `CustomerInfo` (cached, fast). The server source of truth is `entitlements/{uid}`, written by the `revenuecatWebhook` Function (shared-secret auth header, idempotent on event id).
- Domain `FeatureGate.isUnlocked(Feature): Flow<Boolean>`. `Feature` = `BOT, FAMILY, ADV_ANALYTICS, SUB_DETECT`, defined now for later sub-projects.
- Paywall: custom Compose screen with monthly/yearly packages, trial badge, restore, terms/privacy links.

### 3.6 Screens
1. Sign-in: Google button + privacy blurb
2. Home: month summary (spent/income/top budget), txns grouped by day, FAB
3. Add/Edit txn bottom sheet: amount keypad, type toggle, category grid, payment-mode chips, date, merchant, note; delete on edit
4. Categories: list, add/edit/archive
5. Budgets: list with progress bars, add/edit (overall or per category, weekly/monthly)
6. Paywall
7. Settings: profile, currency (INR, display only in v1), premium status + manage subscription, CSV export, logout, delete account

### 3.7 Error handling
- Domain returns `Result<T, DomainError>`; `DomainError` = `Network | Unauthorized | Validation(field) | NotFound | Billing(code) | Unknown`
- The data layer maps Firebase/RevenueCat/Room exceptions to `DomainError`. SDK exceptions never reach the domain.
- ViewModels expose `StateFlow<UiState>` plus one-shot events (snackbar). Being offline is not an error: show a subtle "pending sync" badge.
- Billing: user-cancel is silent; pending purchase shows a "processing" state; restore shows a result toast.
- Crashlytics non-fatals for sync `FAILED` and `Unknown` mappings.

### 3.8 Testing
- domain: JUnit5 + fakes covering use cases, money math, budget progress/period boundaries (IST month edges)
- data: Room in-memory DAO tests (Robolectric), repositories with fake remote, sync LWW and tombstone tests
- Firestore rules: Firebase Emulator + `@firebase/rules-unit-testing`
- presentation: ViewModel tests (Turbine), Compose UI tests for add-txn and budget flows, Roborazzi screenshots for designsystem
- functions: emulator tests for `revenuecatWebhook` → entitlement doc and for `deleteAccount`
- CI (GitHub Actions): ktlint + detekt, unit tests, `assembleDebug`, rules tests on every PR
- TDD throughout

### 3.9 Out of scope for Foundation
Auto-capture, subscriptions, analytics screens beyond the home summary, family groups, bot, multi-currency conversion, TRANSFER type, accounts/wallets entity.

---

## 4. Verification (Foundation complete when)
- `./gradlew ktlintCheck detekt testDebugUnitTest assembleDebug` passes
- Firebase Emulator suite: rules tests + Functions tests pass
- Manual run on device/emulator against the Firebase emulators:
  - Google sign-in → categories seeded → add/edit/delete txn → budget progress updates
  - Airplane mode: add txn → shows pending → reconnect → syncs. Reinstall → full pull restores data
  - RevenueCat sandbox (Play license tester): purchase yearly with trial → `premium` unlocks, `entitlements/{uid}` doc written by webhook; restore works
  - Delete account → Firestore subtree + Auth user removed, local wiped
- Gradle dependency rule check: domain modules have no Android deps (build fails otherwise)
