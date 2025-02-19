# TODO

## Next

* new tab "Other", with summaries/report/usage statistics/overview (no shows/late cancellation)
    * report overview like in app: monthly what visited, also show 6x per venue per (calendar! not period) month
* also sync "plus checkins"
* notes with rich format text-editor (bold, italic, colors, fontsize)

## Backlog

* also fetch plan for venue (available in list, but not in detail response!)
* adaptive layout: depending on window size, change layout (add columns, change formatting)
* make rating UI a slider with custom renderer
* enable/disable specific 3rd party sync in prefs (make it city aware)
    * third party syncer for: cosmos-west, balanzcs, white door yoga
* "hard sync" feature: get all venue details again (overwrite to get most recent description, opening times, etc...)
* BUG: DateParser dutch locale doesn't work when packaged as app...?!

### Low Prio

* system test: book something which can't be booked parse response and show proper message
    * +6 bookings; +2 same day; over checkin limit (end of period); over period/veneu limit late cancellation
* if rating sorted, then update rating => resort! (just that one which has been updated)
* ... no-show for freetraining possible?? try it yourself ;)
* UI/ScreenTemplate: how to get V-scroll if use Column instead LazyColumn? (need weight 1.0f from Column to fill height)
* map component: https://wiki.openstreetmap.org/wiki/JMapViewer (compose google map only for android, not desktop)
* could try to parse venue.openingTimes (standardized text): could shorten it ("Monday 10:00-20:00" -> "Mon 10-20");
* IMPROVE: the syncer should not add the year information; return incomplete day+month only (let logic determine year)
* uitest+koin (work in progress)
* spots left: make updateable (see SubEntityDetail view)

### Public

* if it is first time started, disable all screens; only prefs. to enter credentials (wizard)
* maybe period restart day can be fetched via API (it is shown in app...)
* support custom plan; define everything yourself (as so many other plans are there; think of B2B)
* tooltips for table headers (some are not self-explanatory)

## Nope

* for website: html click on image, zoom (https://codeconvey.com/html-image-zoom-on-click/)
* investigate: room (flow-enabled sqlite MPP abstraction) instead exposed?
* (switch to ZonedDate?)
* maybe use 'net.ricecode:string-similarity:1.0.0'
* display GCal name, once connected tested ok (would need to persist; when to fetch; baeh...)

# External

* user tests: go to friends, let them install, observe, get feedback (esp. on windows!)
* contact someone: is it ok what i'm doing?

## Todos for others

* NOPE: make preferences plan a dropdown, with an option "custom" to custom define the usage limits

## Beta Testers

* serieke
* jasmin

# Info

## Exposed

* add: `addLogger(StdOutSqlLogger)` in transaction block

## Compose

* material guide: https://m3.material.io/
* Official: https://developer.android.com/compose
* Components: https://developer.android.com/develop/ui/compose/components
    * User Input: https://developer.android.com/develop/ui/compose/text/user-input
    * Buttons: https://developer.android.com/develop/ui/compose/components/button
* Forms: https://medium.com/@anandgaur22/jetpack-compose-chapter-7-forms-and-user-input-in-compose-f2ce3e355356
* Text(style = MaterialTheme.typography.subtitle1)
* Icons = https://fonts.google.com/icons
