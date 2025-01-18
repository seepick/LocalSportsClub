# LocalSportsClub

Communistic Custom Client for Urban Sports Club.

## Internal

* To package deployable artifacts, simply run `./gradlew package` and see the results
  in `build/composebinaries/main/...` (built seperately on macOS, Linux, Windows).

# TODOs

## Pre-Release

* 1: data cleaner tool: change EMS health studio: category from Fitness to EMS
    * also: Activity: "Elite HIIT &amp;amp;amp;amp;amp;amp; Strength"

## Release v1

* prefs: home city (complete list of options)
    * filter activity/freetraining/venues (info stored in DB?!)
    * make third party info syncer city-dependent
* prefs: USC creds
* prefs: gcal feature on/off (calendar ID; use Switch compose component)
* sync membership plan (infer limits)
* display version number
* online version checker
* auto-build/release script (incl. github); FULLY automated (build4k?!)
* design website + link to it in app
* user tests: go to friends, let them install, observe, get feedback (esp. on windows!)
* bigger PR: tell multiple friends, ask for feedback

## Release v2

* make scrollbar thingy brighter in dark mode
* after sync, display report what happened ++ big WARNING if new noshow was detected!
    * when no-show indicated, create big alert (request money get back if forgot to simply checkin but was there)
* table header has little gap above, one pixel "see through" -> remove it
* tooltips for table headers (some are not self-explanatory)

## Backlog

* enable up/down arrow for table; (would require a global listener, and changing focus...)
* feedback about sync progress while running (indicate current state, maybe even deterministic progress)
* make long venue texts "somehow" selectable; A) selectable text (toolitp?!) or B) click => copy clipboard
* if sorted by rating, then update rating, then need to be resorted (just that one which has been updated)
* when click on dropdown; select (dropdown closes); open again => click textfield no effect!
* ... no-show for freetraining possible?? try it yourself ;)
* spots left: make updateable (see SubEntityDetail view)
* use compose's snackbar (book/schedule, cancel, sync)
* notes with rich format text-editor (bold, italic, colors, fontsize)
* map component: https://wiki.openstreetmap.org/wiki/JMapViewer (compose google map only for android, not desktop)

### Later

* UI/ScreenTemplate: how to get V-scroll if use Column instead LazyColumn? (need weight 1.0f from Column to fill height)
* "hard sync" feature: get all venue details again (overwrite to get most recent description, opening times, etc...)
* DateParser dutch locale doesn't work when packaged as app...?!
* if textfield elipse ("foo...") => show tooltip full text
* could try to parse venue.openingTimes (standardized text): could shorten it ("Monday 10:00-20:00" -> "Mon 10-20");
  jaap-eden-ijsbaan has strange/long one
* IMPROVE: the syncer should not add the year information; return incomplete day+month only (let logic determine year)
* make rating UI a slider with custom renderer

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
* Text(style = MaterialTheme.typography.subtitle1)
