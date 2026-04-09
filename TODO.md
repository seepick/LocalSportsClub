# TODO 2.x

## v2.0.0 - submodules; simple ones

* 🤖 introduce submodules: app, ui, domain, persistence (keep pretty empty at first, most remains in app)

* Geographic Coordinates Stored as String in the Persistence Layer
    * VenueDbo declares val latitude: String and val longitude: String, and VenueDboTable maps them to VARCHAR(16). Every use of coordinates (e.g., in DataStorage.calculateLocatioAndDistance) requires an explicit .toDouble() conversion, and Venue.toDbo() converts back with .toString().
    * The service-layer Location data class correctly uses Double. This type inconsistency at the persistence boundary is error-prone and prevents the database engine from performing range queries on numeric columns.
    * => Add a Liquibase migration to convert the existing VARCHAR columns to REAL.
    * => Change VenueDbo.latitude/longitude to Double and update VenueDboTable to use Exposed's double("LATITUDE") / double("LONGITUDE") columns

* move Hardcoded Venue IDs to usc-client library (Venue.kt)

* delete scratch code (Venue.kt)
    * => place them in src/test or a dedicated scratch file

## v2.1.0 - UI tests; simple ones

* 🤖 fix UI tests, make run local & CI (derbauer); NoSuchMethodError; write "almost" e2e test for happy paths
* 🤖 test fixtures from usc-client don't work; right now copy'n'pasted all

* 🤖 fix file resorver design issue; passing to error dialog for log retrieval; always assume PROD ;)

* Cryptographically Weak Credential Encryption
    * Encrypter.kt derives an AES key solely from the OS username padded with the character 'x' to reach 32 bytes.
    * It calls Cipher.getInstance("AES") with no mode or padding specified, which defaults to AES/ECB/PKCS5Padding. ECB mode encrypts each 16-byte block independently and produces deterministic ciphertext — a well-known cryptographic weakness.
    * There is no salt, no random IV, and no key-derivation function (KDF). The scheme provides only superficial obfuscation, not real security.
    * => ... good enough?!
    * => Use AES/GCM/NoPadding with a randomly generated 96-bit IV stored alongside each ciphertext. Derive the key with PBKDF2 (available in the JDK) from a stable device-local secret. For a desktop application, the proper solution is to delegate to the OS keychain entirely (macOS Keychain, Windows Credential Manager, libsecret on Linux), which avoids maintaining a custom crypto scheme altogether.

## v2.2.0 - sync

* E2E tests first
* move code to submodule (domain or custom "sync" one?!)

* SyncReporter Mixes Sync Domain Logic with Compose UI Rendering
    * lives in the sync package and implements SyncerListener — a pure domain/infrastructure concern.
    * Yet it imports androidx.compose.* (layout, text, color) and defines a buildContent(): @Composable () -> Unit method that constructs the sync-result UI directly inside the sync class.
    * This is a direct layer violation: a sync infrastructure class is responsible for building a UI composable.
    * => Move the composable rendering logic into the view layer (e.g., a new SyncReportView.kt).
    * => SyncReporter should expose only the plain SyncReport data class; a view composable can observe and render it.

* SyncerListener Interface Violates the Interface Segregation Principle
    * 9 abstract methods spanning three unrelated concerns:
    * (1) venue lifecycle events (onVenueDbosAdded, onVenueDbosMarkedDeleted, onVenueDbosMarkedUndeleted)
    * (2) activity events (onActivityDbosAdded, onActivityDboUpdated, onActivityDbosDeleted),
    * (3) freetraining events (onFreetrainingDbosAdded, onFreetrainingDboUpdated, onFreetrainingDbosDeleted)
    * ... Implementors (SyncReporter or UsageStorage) that only care about a subset must provide empty no-op bodies for all unrelated methods.
    * => Split into three focused interfaces: VenueSyncListener, ActivitySyncListener, FreetrainingSyncListener.
    * => Optionally provide a combined SyncerListener that extends all three for backwards compatibility. This immediately makes it visible which events a class actually handles and prevents no-op drift.

* 🤖 the syncer should not add the year information; return incomplete day+month only (let logic determine year)
* 🤖 rework `workParallel` as service with interface, so can be mocked out easily during testing (?)
* 🐞 after synced, trying to book, BookingService#bookOrCancelActivity lookup of activity returns null
* 🐞 while full sync, then sync single activity -> CRASH; simple solution: block whole UI on either sync
* 🐞 DateParser dutch locale doesn't work when packaged as app...?!

## v2.3.0 - submodule layering

* finish extraction of code into submodules

* split Domain Model (all 4 core domain entities) from Compose UI
    * using mutableStateOf, mutableStateListOf, derivedStateOf; Color, SpanStyle, Lsc, TableItemBgColor
    * => split into Venue-VenueUi (Activity, Freetraining)

* split DataStorage (violating SRP)
    * (a) in-memory data cache for venues, activities, and freetrainings
    * (b) SyncerListener consuming raw DBO events from the sync layer;
    * (c) domain object factory (mapping *Dbo → domain model);
    * (d) event broadcaster with its own DataStorageListener dispatch loop;
    * (e) database read coordinator calling venueRepo, activityRepo
    * => DomainObjectFactory (DBO→model)
    * => extract DataStorageListenerDispatcher
    * => ... keep DataStorage as a thin orchestrator

* Infrastructure Bootstrapping Mixed into the Compose Entry Point
    * LocalSportsClub.kt contains the entire application startup wiring inside the Compose application {} block
    * manual listener-registration loops for SyncerListener, DataStorageListener, SyncProgressListener, and ApplicationLifecycleListener are all called inline
    * This mixes UI composition with dependency wiring and makes the startup sequence hard to read, impossible to test, and fragile
    * the comment // has to be first ;) for mainViewModel registration is a classic ordering-dependency code smell.
    * => Extract a dedicated ApplicationBootstrapper class
    * receives all services and view models and calls registerListener() / wire() in one place
    * Call bootstrapper.wire() from the windowOpened handler
    * The Compose block should only declare the window and surface.

## v2.4.0 - misc

* avoid !!-usage: SinglesService.kt, BookingValidator.kt, ScreenViewModel.kt, SyncProgressThreaded
    * => Replace !! with requireNotNull(x) { "Descriptive message" }
    * => or use safe-call operator combined with meaningful fallback logic

* 🤖 investigate with spike: room (flow-enabled sqlite MPP abstraction) instead exposed?

* ...
* ...
* ...
