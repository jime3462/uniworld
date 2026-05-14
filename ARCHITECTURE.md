# UniWorld Architecture

## Vision Statement
For music listeners who want an accessible and customizable online music experience, UniWorld, is a web-based music streaming service that provides instant music playback, playlist management, song search that leads to the discovery of new music. Unlike large commercial streaming services, this product delivers a seamless and personalized, and enjoyable music listening experience to users.

## User Stories
As a new user, I want to register with email and password so that I can create a music account.

As a registered user, I want to log in securely so that I can access my music account.

As a user, I want o browse songs by title, artist, or album so that I can discover new music.

As a user, I want to play a song instantly so that I can listen to it without downloading.

As a user, I want to create a playlist so that can organize my favorite songs.

As a user, I want add or remove songs from my playlist so that I can manage my music collection.

## 1. System Overview
UniWorld is a full-stack music web application composed of:
- Angular frontend (`uniworld-frontend`) for UI, routing, playback controls, and API integration.
- Spring Boot backend (`uniworld-backend`) for authentication, authorization, search, and music domain CRUD APIs.
- H2 in-memory database for users, playlists, songs, albums, and artists.

## 2. Technology Stack
- Frontend: Angular 20, TypeScript, RxJS
- Backend: Java 21, Spring Boot 4, Spring Security (JWT), Spring Data JPA
- Database: H2 (in-memory)
- Auth: JWT Bearer tokens
- Media metadata ingestion: mp3agic (startup music import)

## 3. C4 Model

### 3.1 C4 Context Diagram
```mermaid
C4Context
    title UniWorld - System Context

    Person(user, "Listener", "Searches music, plays songs, and manages playlists")
    Person(admin, "Admin", "Manages catalog and moderation actions")

    System(uniworld, "UniWorld", "Music discovery, playback metadata, and playlist management")

    System_Ext(localMedia, "Local Music Asset Folder", "Audio and cover assets used by the app")

    Rel(user, uniworld, "Uses", "HTTPS")
    Rel(admin, uniworld, "Admin operations", "HTTPS")
    Rel(uniworld, localMedia, "Imports metadata from audio files", "Filesystem")
```

### 3.2 C4 Container Diagram
```mermaid
C4Container
    title UniWorld - Container Diagram

    Person(user, "User")

    System_Boundary(uniworld_boundary, "UniWorld") {
        Container(frontend, "uniworld-frontend", "Angular + TypeScript", "SPA for auth, search, playlist, profile, and playback UI")
        Container(backend, "uniworld-backend", "Spring Boot", "REST API, JWT security, domain logic")
        ContainerDb(db, "H2 Database", "H2", "Stores users, playlists, songs, albums, artists")
        Container(media, "Music/Cover Assets", "Static files", "Audio files and album covers")
    }

    Rel(user, frontend, "Uses", "Browser")
    Rel(frontend, backend, "Calls REST API", "JSON/HTTP")
    Rel(frontend, media, "Loads audio and image assets", "HTTP")
    Rel(backend, db, "Reads/Writes", "JPA")
    Rel(backend, media, "Imports metadata at startup", "Filesystem")
```

### 3.3 C4 Component Diagram (Backend)
```mermaid
C4Component
    title UniWorld Backend - Component Diagram

    Container_Boundary(backend, "uniworld-backend") {
        Component(authController, "AuthController", "REST Controller", "Register/login/me endpoints")
        Component(playlistController, "PlaylistController", "REST Controller", "Playlist CRUD and ownership checks")
        Component(searchController, "SearchController", "REST Controller", "Keyword search across song/artist/album")
        Component(songController, "SongController", "REST Controller", "Song CRUD and retrieval")

        Component(authService, "AuthService", "Service", "Registration/login orchestration")
        Component(jwtService, "JwtService", "Security Service", "JWT issue and validation")
        Component(jwtFilter, "JwtAuthenticationFilter", "Security Filter", "Extracts and validates bearer token")
        Component(securityConfig, "SecurityConfig", "Configuration", "Auth rules, CORS, stateless security")
        Component(musicInitializer, "MusicLibraryInitializer", "Startup Component", "Imports metadata from local music files")

        Component(repoLayer, "Repository Layer", "Spring Data JPA", "UserRepository, PlaylistRepository, SongRepository, ArtistRepository, AlbumRepository")
    }

    Rel(authController, authService, "Uses")
    Rel(authService, jwtService, "Issues token with claims")
    Rel(authService, repoLayer, "Reads/Writes users")

    Rel(playlistController, repoLayer, "Reads/Writes playlists, songs, users")
    Rel(searchController, repoLayer, "Reads songs, artists, albums")
    Rel(songController, repoLayer, "Reads/Writes songs")

    Rel(securityConfig, jwtFilter, "Registers filter")
    Rel(jwtFilter, jwtService, "Validates token")

    Rel(musicInitializer, repoLayer, "Bootstraps catalog metadata")
```

## 4. Domain Class Diagram
```mermaid
classDiagram
direction LR

class User {
  +Long userID
  +String name
  +String email
  +String password
  +String role
}

class Playlist {
  +Long playlistID
  +String name
  +Boolean isPublic
  +String coverImage
}

class Song {
  +Long songID
  +String title
  +String genre
  +String keyScale
  +int tempo
  +int duration
  +String audioFile
}

class Album {
  +Long albumID
  +String title
  +String genre
  +int releaseYear
  +String coverImage
}

class Artist {
  +Long artistID
  +String name
  +String genre
  +String image
}

User "1" --> "0..*" Playlist : owns
User "0..*" --> "0..1" Song : currentSong
Playlist "0..*" --> "0..*" Song : contains
Song "0..*" --> "1" Album : belongsTo
Song "0..*" --> "0..*" Artist : performedBy
Artist "1" --> "0..*" Album : creates
```

## 5. Sequence Diagrams

### 5.1 User Login Flow
```mermaid
sequenceDiagram
    autonumber
    actor U as User
    participant FE as Angular Frontend
    participant API as AuthController
    participant SVC as AuthService
    participant AUTH as AuthenticationManager
    participant JWT as JwtService
    participant DB as UserRepository/H2

    U->>FE: Submit identifier + password
    FE->>API: POST /api/auth/login
    API->>SVC: login(request)
    SVC->>DB: findByEmail or findByName
    DB-->>SVC: User record
    SVC->>AUTH: authenticate(email, password)
    AUTH-->>SVC: success
    SVC->>JWT: generateToken(role, userID)
    JWT-->>SVC: signed JWT
    SVC-->>API: AuthResponse(token, profile)
    API-->>FE: 200 OK
    FE-->>FE: store token in localStorage
```

### 5.2 Search Flow
```mermaid
sequenceDiagram
    autonumber
    actor U as User
    participant FE as Angular Frontend
    participant API as SearchController
    participant SR as SongRepository
    participant AR as ArtistRepository
    participant ALR as AlbumRepository
    participant DB as H2

    U->>FE: Enter keyword and submit
    FE->>API: GET /api/search?keyword=...
    API->>SR: findAll()
    SR->>DB: SELECT songs
    DB-->>SR: Song rows
    SR-->>API: Songs

    API->>AR: findAll()
    AR->>DB: SELECT artists
    DB-->>AR: Artist rows
    AR-->>API: Artists

    API->>ALR: findAll()
    ALR->>DB: SELECT albums
    DB-->>ALR: Album rows
    ALR-->>API: Albums

    API-->>FE: SearchResultResponse
    FE-->>U: Render grouped results
```

### 5.3 Playlist Update with Authorization
```mermaid
sequenceDiagram
    autonumber
    actor U as User
    participant FE as Angular Frontend
    participant INT as AuthInterceptor
    participant SEC as JwtAuthenticationFilter
    participant API as PlaylistController
    participant UR as UserRepository
    participant PR as PlaylistRepository
    participant SR as SongRepository
    participant DB as H2

    U->>FE: Edit playlist and save
    FE->>INT: PUT /api/playlists/{id}
    INT->>INT: Attach Authorization: Bearer token
    INT->>SEC: Send request

    SEC->>SEC: Validate JWT
    SEC-->>API: Authenticated principal

    API->>UR: findByEmail(authentication.name)
    UR->>DB: SELECT user
    DB-->>UR: user

    API->>PR: findById(id)
    PR->>DB: SELECT playlist
    DB-->>PR: playlist

    API->>API: Verify owner == current user
    API->>SR: findAllById(songIds)
    SR->>DB: SELECT songs
    DB-->>SR: songs

    API->>PR: save(updated playlist)
    PR->>DB: UPDATE playlist + join table
    DB-->>PR: persisted

    API-->>FE: PlaylistResponse
    FE-->>U: Updated playlist shown
```

### 5.4 Song Playback Metadata Flow
```mermaid
sequenceDiagram
    autonumber
    actor U as User
    participant FE as Angular Frontend
    participant API as SongController
    participant SR as SongRepository
    participant DB as H2
    participant B as Browser Audio Element
    participant AS as Frontend Static Assets

    U->>FE: Click play on a song
    FE->>API: GET /api/songs/{id}
    API->>SR: findById(id)
    SR->>DB: SELECT song
    DB-->>SR: song (includes audioFile path)
    SR-->>API: Song
    API-->>FE: Song JSON

    FE-->>B: audio.src = song.audioFile
    B->>AS: GET /assets/music/...file...
    AS-->>B: Audio bytes
    B-->>U: Playback starts
```

## 6. Key Architectural Decisions
- Stateless authentication with JWT; frontend stores token and sends it through an interceptor.
- Global security policy requires authentication for all API routes except auth, error, and H2 console.
- Catalog bootstrap imports and updates song metadata from local audio assets at backend startup.
- Playlists are ownership-protected at the controller level (only owner can update/delete).
- Search currently performs in-memory filtering over repository data (`findAll()` then filter in Java).

## 7. Challenges Faced
- H2 is in-memory and reset-prone, which made it harder to keep test data stable across runs.
- Search relied on in-memory filtering over repository data, so performance and scalability became a concern as the catalog grew.
- Serving audio and cover assets directly was straightforward for development, but it introduced production deployment concerns.
- Authorization checks were handled in controllers, which made security logic more repetitive and harder to centralize.
- Importing music metadata at startup introduced edge cases around missing tags, invalid file formats, and cover-image fallback handling.

## 8. Pairwise Testing Plan

Pairwise testing complements MC/DC coverage by testing interactions between available inputs in current code paths.

### 8.1 Test Domains with Available Pairwise Factors

#### Authentication
Available factors: endpoint (`/register`, `/login`, `/me`) x identifier type (email, username) x password state (valid, blank, wrong) x authentication presence (present, missing)
Example:

| Endpoint | Identifier | Password | Authentication | Expected Result |
|---|---|---|---|---|
| `/api/auth/register` | email | valid | missing | PASS (creates user) |
| `/api/auth/register` | email | blank | missing | FAIL 400 |
| `/api/auth/login` | username | valid | missing | PASS (token returned) |
| `/api/auth/login` | username | wrong | missing | FAIL 401 |
| `/api/auth/me` | n/a | n/a | present | PASS (current user) |
| `/api/auth/me` | n/a | n/a | missing | FAIL 401 |

#### Playlist Management
Available factors: caller relationship (owner, non-owner, unauthenticated) x operation (`create`, `update`, `delete`) x `songIds` state (valid, empty, invalid) x `isPublic` value (true, false, null)
#### Example:

| Caller | Operation | `songIds` | `isPublic` | Expected Result |
|---|---|---|---|---|
| owner | create | valid | true | PASS 201 |
| owner | create | empty | null | PASS 201 (defaults public) |
| owner | update | invalid | false | FAIL 400 |
| non-owner | update | valid | true | FAIL 403 |
| owner | delete | empty | n/a | PASS 204 |
| unauthenticated | delete | n/a | n/a | FAIL 401 |

#### Search and Discovery
Available factors: keyword validity (valid, blank) x keyword pattern (exact, partial, special chars, unicode) x expected match bucket (songs, artists, albums, none)
#### Example:

| Keyword | Pattern | Expected Bucket | Expected Result |
|---|---|---|---|
| `jazz` | exact | songs | PASS (songs list non-empty) |
| `art` | partial | artists | PASS (artist match) |
| `rock` | exact | albums | PASS (album match) |
| `@@@` | special chars | none | PASS (empty lists) |
| `cafe` | unicode/locale variant | songs or artists | PASS (if data exists) |
| `` | blank | n/a | FAIL 400 |

#### Playback and Player
Available factors: queue size (empty, single, multiple) x repeat mode (`off`, `all`, `one`) x shuffle (`on`, `off`) x action (`nextSong`, `previousSong`, `onAudioEnded`, `onSeek`)
#### Example:

| Queue | Repeat | Shuffle | Action | Expected Result |
|---|---|---|---|---|
| empty | off | off | `nextSong` | PASS (no-op) |
| single | one | off | `onAudioEnded` | PASS (restarts current song) |
| single | off | off | `onAudioEnded` | PASS (stops at end) |
| multiple | off | off | `nextSong` | PASS (moves to next index) |
| multiple | all | on | `previousSong` | PASS (selects valid random/prev index) |
| multiple | off | off | `onSeek` | PASS (updates current time) |

### 8.2 Benefits

This version uses only factors that map directly to existing controller methods and player component methods, avoiding unimplemented dimensions such as server-side search filters or sort parameters.

### 8.3 Implementation

Generate pairwise cases from the available factors with PICT, then run them as parameterized tests in JUnit 5 (`AuthService`, `PlaylistController`, `SearchController`) and Jasmine (`Player`).
