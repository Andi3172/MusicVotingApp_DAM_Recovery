# MusicVotingApp

A peer-to-peer music voting application for Android. Hosts (DJs) publish a playlist; clients connect, fetch that playlist, and cast live votes. All votes update in real time on the host, are persisted to SQLite, and can be reviewed in history or visualized in a chart.

---

## Table of Contents

1. [App Workflow](#app-workflow)  
2. [Features & Requirements Mapping](#features--requirements-mapping)  
3. [Setup & Running Two Emulators](#setup--running-two-emulators)  
4. [Project Structure](#project-structure)  
5. [Resources](#resources)  

---

## App Workflow

1. **Login**  
   - User enters **name** and selects **role** (`Host` or `Voter`).  
   - These are passed via `Intent` extras (Bundles) into `MainActivity`.

2. **Main Screen**  
   - Loads a **playlist** from `res/raw/songs.json`, removes duplicates, and stores in `SongRepository`.  
   - Shows the list in a **4-column table**: `Title | Artist | Genre | # Votes`.  
   - Host automatically starts a **ServerSocket** on port 8888.  
   - Voters see buttons to **Join Session** (launches `PeerClientActivity`).  
   - Common buttons:  
     - **View Vote History** → `HistoryActivity`  
     - **Show Vote Chart** → `ChartActivity`  
     - **Save Top Voted Song** → writes votes in SQLite  

3. **Host Socket Loop**  
   - **GET_SONGS** → serializes current `songList` as JSON over TCP.  
   - **VOTE:<title>:<voter>** → host parses, calls `Song.vote(voter)`, writes each vote to SQLite, then calls `adapter.notifyDataSetChanged()` to refresh the table.

4. **Client Screen**  
   - Enter host IP, tap **Join Session** → sends `GET_SONGS`, parses JSON, populates `ListView` in single-choice mode.  
   - Select one song, then tap **Confirm Vote** → sends `VOTE:<title>:<username>`.  
   - Optional polling can refresh the list every few seconds.

5. **HistoryActivity**  
   - Reads from SQLite table `votes(title, artist, voter)`, displays each vote in a scrollable log.  
   - **Clear History** button deletes all records.

6. **ChartActivity**  
   - Uses MPAndroidChart to render a bar chart of current vote counts from the live `songList`.  
   - **Reload Chart** button redraws with fresh data.

---

## Features & Requirements Mapping

| Req. # | Requirement                                                                                               | Implementation                                                                        |
|:------:|:----------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------|
| 1      | Classes with ≥4 fields, including a `List<>` field; operations on that list                               | `Song { title, artist, genre, List<String> voters }`; `vote()`, `hasVoted()`, scans. |
| 2      | Informational messages                                                                                   | Toasts for errors, confirmations, “Hosting…”, “Joined session…”, etc.                 |
| 3      | ≥4 Activities                                                                                            | `LoginActivity`, `MainActivity`, `PeerHostActivity` / socket in-host, `PeerClientActivity`, `HistoryActivity`, `ChartActivity`. |
| 4      | Transfer of params via Bundles                                                                            | `Intent.putExtra("username",…)`, `putExtra("role",…)` passed through all activities.  |
| 5      | Data retrieval & validation (input, simple & logical, explanatory errors)                                 | Checks for empty username, role, IP, song selection; toasts explain each error.       |
| 6      | Display list; handle selection; custom adapter with ≥3 controls                                          | Host & client both use `ListView`; client uses single-choice. `SongAdapter` with 4 `TextView`s. |
| 7      | Activity styling                                                                                         | Consistent padding, weights, color-coded buttons, symmetry across screens.            |
| 8      | Graphical data representation                                                                            | MPAndroidChart bar chart in `ChartActivity`.                                          |
| 9      | Network fetch & parse JSON (async)                                                                       | TCP socket on port 8888: `GET_SONGS` & `VOTE:` messages; JSON parsing with `org.json`; background `Thread`. |
| 10     | SQLite persistence (insert, select, display)                                                             | `VoteDBHelper` with `insertVote()`, `getAllVotes()`, displayed in `HistoryActivity`.   |

---

## Setup & Running Two Emulators

1. **Start two emulators** in Android Studio (e.g. AVD-5554 and AVD-5556).  
2. **Launch** `PeerHostActivity` on emulator-5554.  
3. On your dev machine’s terminal:

   ```bash
   adb devices
   # Identify the host emulator serial, e.g. emulator-5554
   adb -s emulator-5554 emu redir add tcp:8888:8888
4. On emulator-5555, open PeerClientActivity and enter host ip 10.0.2.2 and then tap Join Session -> Vote.
4. To remove the forward:
   ```bash
   adb -s emulator-5554 emu redir remove tcp:8888:8888
