# TibiBalance 📱🧘‍♂️

> *Track your habits, emotions, and biometrics – even when the Wi‑Fi goes on holiday.*

---

## 🚀 Project Snapshot

| Layer      | Module    | Purpose                                                                         |
| ---------- | --------- | ------------------------------------------------------------------------------- |
| **UI**     | `:app`    | Jetpack Compose app, WorkManager jobs, Firebase Auth & Credential Manager       |
| **Data**   | `:data`   | Room + SQLCipher cache, Firestore sync, repositories & mappers, DI (Hilt + KSP) |
| **Domain** | `:domain` | 〈Pure Kotlin〉 value‑classes, enums, configs, entities, use cases                |
| **Core**   | `:core`   | JVM utilities, future home of shared extensions                                 |

Clean Architecture means the arrow only points **down**: `core → domain → data → app`.

---

## 🧩 Domain Model (TL;DR)

```
HabitId(value: String)        ActivityId(value: String)
└─ Habit                      └─ HabitActivity
   ├─ Session                     ├─ type: ActivityType
   ├─ Period                      ├─ timestamp: Instant
   ├─ Repeat (sealed)             └─ payload: Map<String,String>
   ├─ NotifConfig             
   ├─ ChallengeConfig?        User(uid) – settings, photoUrl, birthDate≥18
   └─ SyncMeta               EmotionEntry(date) 😄   DailyMetrics(date) 🏃
```

All models are `@Serializable` and use `Instant`/`LocalDate`/`LocalTime` from **kotlinx‑datetime**.

---

## 🗄️ Local DB (Room + SQLCipher)

* **Entities** mirror domain 1‑to‑1 (`HabitEntity`, `EmotionEntryEntity`, …)
* **TypeConverters** for value‑classes & enums – courtesy of `room.generateKotlin=true`.
* **SupportFactory** wires SQLCipher; passphrase stored via Android Keystore.
* **Migrations** placeholders are ready (`MIGRATION_1_2`, `MIGRATION_2_3`).

```kotlin
@Database(
    entities = [HabitEntity::class, HabitActivityEntity::class /* … */],
    version  = 3,
    exportSchema = true
)
abstract class AppDb : RoomDatabase() {
    abstract fun habitDao(): HabitDao
    // … five more DAOs
}
```

---

## ☁️ Firestore Layout

```
users/{uid}
 ├─ habits/{habitId}
 ├─ activities/{activityId}
 ├─ emotions/{date}
 ├─ metrics/{date}
 └─ onboarding (doc)
```

Sync policy = **Last‑Write‑Wins** via `updatedAt` (`SyncMeta`). Tombstones (`deletedAt≠null`) keep deletes consistent offline.

---

## 🔧 Build Stack

* **Gradle 8.12.1**  ·  **AGP 8.8.2**  ·  **Kotlin 2.0.21 (K2)**
* **Compose BOM 2025‑04‑01**
* **Hilt 2.56.2** (code‑gen via **KSP**, not KAPT – builds ≈ 40 % faster)
* **Room 2.7.1** with `room.generateKotlin=true`
* **Firebase BoM 33.13.0** (Auth + Firestore‑KTX)
* CI‑friendly thanks to a single `libs.versions.toml` catalog.

---

## 🏃‍♀️ Getting Started (Dev Workflow)

```bash
# Clone & sync sub‑modules (if any)
git clone https://github.com/eddn-dev/TibiBalanceApp.git
cd TibiBalanceApp

# Run the first build – gradle will fetch everything
./gradlew :app:installDebug

# Generate Dokka docs (optional but fancy)
./gradlew dokkaHtml
```

> **Pro tip 💡**: add `-Pksp.useKSP2=false` while Room 2.7 hates KSP2.

---

## ✅ Features Already Done

* Full domain model & kotlinx‑serialization docs
* Encrypted offline cache + Hilt DI
* Firestore sync repository with LWW conflict resolution & tombstones
* Periodic `SyncWorker` scaffold (15 min, exponential back‑off)
* Project compiles *warning‑free* on Android Studio Meerkat 2024.3.2

---

## 🔭 Roadmap (Next Sprint)

1. Finish `SyncWorker` wiring & manual **“Sync now”** button
2. Write use‑cases `CreateHabit`, `CompleteHabit`, `DeleteHabit`
3. In‑memory Room tests + Firestore emulator suite
4. Flesh out UI: habit list, editor wizard, emotional calendar
5. Real SQL migrations + instrumentation tests

*(If you read this far, grab a coffee – you earned it ☕)*

---

## 🛡️ Security & Privacy

* SQLCipher encryption key lives in Android Keystore
* Firebase Auth guards every remote read/write via UID rules
* GDPR/CCPA: export & delete endpoints planned in **Settings → Privacy**

---

## 🤝 Contributing

Pull Requests are welcome! Follow the existing module layering and remember:

> *With great value‑classes comes great type‑safety.*

Please run `./gradlew ktlintFormat` before pushing – our CI is picky.

---

## © 2025 The TibiBalance Team

Licensed under the MIT License.
