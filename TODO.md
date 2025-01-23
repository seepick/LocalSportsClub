# TODO

## External

* user tests: go to friends, let them install, observe, get feedback (esp. on windows!)
* contact someone: is it ok what i'm doing?

## Release v1

* !! "Premium OneFit" has 18 total check-ins per month; not same as "Premium" which has 14

## Release v2

* use compose's snackbar for: book/schedule, cancel, sync)
* use same icon for both web links (the arrow button into "USC Site" button)
* description for critical alignment has \"
* maybe period restart day can be fetched via API (it is shown in app...)
* when sync for other city; store separate last sync
* how to use in memory for SQLite?! "jdbc:sqlite:" or "jdbc:sqlite::memory:" wont work!
* display GCal name, once connected tested ok
* make scrollbar thingy brighter in dark mode
* GCal trash creds file if: com.google.api.client.auth.oauth2.TokenResponseException: 400 Bad Request
    * { "error": "invalid_grant", "error_description": "Token has been expired or revoked." }
* next to book/cancel button: checkbox for google calendar entry management
* in preferences: disable auto version check

## Backlog

* ad usage: display how many reservations max
* system test: book something which can't be booked (different limits); parse response and show proper message
* after sync, display report what happened ++ big WARNING if new noshow was detected!
    * when no-show indicated, create big alert (request money get back if forgot to simply checkin but was there)
* tooltips for table headers (some are not self-explanatory)
* enable up/down arrow for table; (would require a global listener, and changing focus...)
* if sorted by rating, then update rating, then need to be resorted (just that one which has been updated)
* report overview like in app: monthly what visited, also show 6x per venue per (calendar! not period) month
* make long venue texts "somehow" selectable; A) selectable text (toolitp?!) or B) click => copy clipboard
* ... no-show for freetraining possible?? try it yourself ;)

### Later

* feedback about sync progress while running (indicate current state, maybe even deterministic progress)
* spots left: make updateable (see SubEntityDetail view)
* notes with rich format text-editor (bold, italic, colors, fontsize)
* map component: https://wiki.openstreetmap.org/wiki/JMapViewer (compose google map only for android, not desktop)
* biggest PR: print stickers, put next to offical QR codes (ask venue before)
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

# Info

## Exposed

* add: `addLogger(StdOutSqlLogger)` in transaction block

## Compose

* Official: https://developer.android.com/compose
* Components: https://developer.android.com/develop/ui/compose/components
    * User Input: https://developer.android.com/develop/ui/compose/text/user-input
    * Buttons: https://developer.android.com/develop/ui/compose/components/button
* Forms: https://medium.com/@anandgaur22/jetpack-compose-chapter-7-forms-and-user-input-in-compose-f2ce3e355356
* Text(style = MaterialTheme.typography.subtitle1)
