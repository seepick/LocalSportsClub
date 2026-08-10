# TODO 2.x

* add dependency from root to { domain-model, domain-repo, domain-logic }
* step-by-step/incremental (!!!) replace from *Dbo to *Model
* later add modules: view-common (domain independent), view-controller, view-model, view-compose (top most)

Loading carousel images failed (venue was deleted)

LSC version 2.1.0

com.github.seepick.uscclient.UscException: Expected status 200 OK but was [301 Moved Permanently] for: https://urbansportsclub.com/en/venues/vitalit-ferrier-reformer-pilates-studio. Response body was:
at com.github.seepick.uscclient.shared.Http_utilsKt.requireStatusOk(http_utils.kt:50)
at com.github.seepick.uscclient.shared.Http_utilsKt.safeAny(http_utils.kt:93)
at com.github.seepick.uscclient.shared.Http_utilsKt.access$safeAny(http_utils.kt:1)
at com.github.seepick.uscclient.shared.Http_utilsKt$safeAny$1.invokeSuspend(http_utils.kt)
at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:34)
at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:100)
at kotlinx.coroutines.internal.LimitedDispatcher$Worker.run(LimitedDispatcher.kt:124)
at kotlinx.coroutines.scheduling.TaskImpl.run(Tasks.kt:89)
at kotlinx.coroutines.scheduling.CoroutineScheduler.runSafely(CoroutineScheduler.kt:586)
at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.executeTask(CoroutineScheduler.kt:820)
at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.runWorker(CoroutineScheduler.kt:717)
at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.run(CoroutineScheduler.kt:704)

LOG:
11:47:17.035 s.l.s.s.AbstractSearch.182@[AWT-EventQueue-0] DEBUG-ActivitySearch resetting search with options: hidden, Search  
11:47:17.152 s.l.s.s.StringSearchOption.35@[AWT-EventQueue-0] DEBUG-Set search terms to: [ref]  
11:47:17.153 s.l.s.s.AbstractSearch.182@[AWT-EventQueue-0] DEBUG-ActivitySearch resetting search with options: hidden, Search  
11:47:17.243 s.l.s.s.StringSearchOption.35@[AWT-EventQueue-0] DEBUG-Set search terms to: [refo]  
11:47:17.243 s.l.s.s.AbstractSearch.182@[AWT-EventQueue-0] DEBUG-ActivitySearch resetting search with options: hidden, Search  
11:47:17.349 s.l.s.s.StringSearchOption.35@[AWT-EventQueue-0] DEBUG-Set search terms to: [refor]  
11:47:17.349 s.l.s.s.AbstractSearch.182@[AWT-EventQueue-0] DEBUG-ActivitySearch resetting search with options: hidden, Search  
11:47:17.981 s.l.s.s.StringSearchOption.35@[AWT-EventQueue-0] DEBUG-Set search terms to: []  
11:47:17.981 s.l.s.s.AbstractSearch.182@[AWT-EventQueue-0] DEBUG-ActivitySearch resetting search with options: hidden, Search  
11:47:21.317 s.l.s.s.SearchOption.219@[AWT-EventQueue-0] DEBUG-BooleanSearchOption - updateEnabled(isEnabled=false, suppressReset=false)  
11:47:21.317 s.l.s.s.AbstractSearch.182@[AWT-EventQueue-0] DEBUG-VenueSearch resetting search with options: Search  
11:47:22.487 s.l.v.c.Utils.69@[AWT-EventQueue-0] DEBUG-Executing task...  
11:47:24.705 s.l.v.v.d.CarouselViewModel.47@[AWT-EventQueue-0] DEBUG-onVenueDetailImageClicked(Venue[id=407, slug=vitalit-ferrier-reformer-pilates-studio, name=Vitalité Ferrier Reformer Pilates Studio, rating=Rating0])  
11:47:24.747 s.l.v.c.Utils.69@[DefaultDispatcher-worker-3] DEBUG-Executing task...  
11:47:24.747 s.l.v.v.d.CarouselViewModel.60@[DefaultDispatcher-worker-3] DEBUG-Loading carousel images for: Vitalité Ferrier Reformer Pilates Studio  
11:47:24.747 c.g.s.u.v.VenueHttpApi.71@[DefaultDispatcher-worker-3] DEBUG-Fetching details for: [vitalit-ferrier-reformer-pilates-studio]  
11:47:24.972 s.l.v.c.Utils.52@[DefaultDispatcher-worker-3] ERROR-Executing task failed! com.github.seepick.uscclient.UscException: Expected status 200 OK but was [301 Moved Permanently] for: https://urbansportsclub.com/en/venues/vitalit-ferrier-reformer-pilates-studio. Response body was:
at com.github.seepick.uscclient.shared.Http_utilsKt.requireStatusOk(http_utils.kt:50)
at com.github.seepick.uscclient.shared.Http_utilsKt.safeAny(http_utils.kt:93)
at com.github.seepick.uscclient.shared.Http_utilsKt.access$safeAny(http_utils.kt:1)
at com.github.seepick.uscclient.shared.Http_utilsKt$safeAny$1.invokeSuspend(http_utils.kt)
at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:34)
at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:100)
at kotlinx.coroutines.internal.LimitedDispatcher$Worker.run(LimitedDispatcher.kt:124)
at kotlinx.coroutines.scheduling.TaskImpl.run(Tasks.kt:89)
at kotlinx.coroutines.scheduling.CoroutineScheduler.runSafely(CoroutineScheduler.kt:586)
at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.executeTask(CoroutineScheduler.kt:820)
at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.runWorker(CoroutineScheduler.kt:717)
at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.run(CoroutineScheduler.kt:704)

## v2.1.0 - UI tests; simple ones

* ad repo module: mark all public/internal; restructure packages
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
* no DBO stuff in domain or view (portnadapter); define interfaces in domain with clean domain objects

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

* configure detekt
* configure logging for liquibase (seems logs before config happens; maybe revert to XML nevertheless?)
* ... 3/3
