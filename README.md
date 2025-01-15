# LocalSportsClub

Communistic Custom Client for Urban Sports Club.

## Internal

* To package deployable artifacts, simply run `./gradlew package` and see the results
  in `build/composebinaries/main/...` (built seperately on macOS, Linux, Windows).

# TODOs

## Pre-Release

* UI: dropdown menu needs better colors; also seems they "block", once expanded?!
* UI: dropdown select for date (instead only via arrow navigation)
* FEATURE: display linked venues (be aware that each stored entity is a bi-directional link, although just stored 1-way)
* MIN: description/info has \n, and \" ... replace them
* FEATURE: support sorting asc/desc
* ... no-show for freetraining possible?? try it yourself ;)
* notes: show scrollbar if necessary
* after sync, display report what happened ++ big WARNING if new noshow was detected!
    * when no-show indicated, create big alert (request money get back if forgot to simply checkin but was there)
* UI BUG: if sorted by rating, then update rating, then need to be resorted (just that one which has been updated)
* UI: if textfield elipse ("foo...") => show tooltip full text
* UI: use space for activities/freetraining sub-table ONLY up to rows (if only 1 row, use only that space, but NOT more)

## Release v1

* prefs: home city (make list more complete)
* make third party info syncer optional (match with configured city)
* prefs: USC creds
* prefs: gcal feature on/off (calendar ID)
* sync membership plan (infer limits)
* display version number
* online version checker
* auto-build/release script (incl. github); FULLY automated
* design website + link to it in app
* user tests: go to friends, let them install, observe, get feedback (esp. on windows!)
* bigger PR: tell multiple friends, ask for feedback

## Post-Release

* enable up/down arrow for table (would require a global listener, and knowing which table got focus...)
* DateParser dutch locale doesn't work when packaged as app...?!
* feedback about sync progress while running (indicate current state, maybe even deterministic progress)
* UI/ScreenTemplate: how to get V-scroll if use Column instead LazyColumn? (need weight 1.0f from Column to fill height)
* IMPROVE: the syncer should not add the year information; return incomplete day+month only (let logic determine year)
* spots left: make updateable (see SubEntityDetail view)
* make rating UI a slider with custom renderer
* use compose's snackbar (book/schedule, cancel, sync)
* map component: https://wiki.openstreetmap.org/wiki/JMapViewer (compose google map only for android, not desktop)
* notes with rich format text-editor (bold, italic, colors, fontsize)

## Nope

* investigate: room (flow-enabled sqlite MPP abstraction) instead exposed?
* (switch to ZonedDate?)
* maybe use 'net.ricecode:string-similarity:1.0.0'

# Compose help:

* Official: https://developer.android.com/compose
* Components: https://developer.android.com/develop/ui/compose/components
    * User Input: https://developer.android.com/develop/ui/compose/text/user-input
    * Buttons: https://developer.android.com/develop/ui/compose/components/button
* Forms: https://medium.com/@anandgaur22/jetpack-compose-chapter-7-forms-and-user-input-in-compose-f2ce3e355356
