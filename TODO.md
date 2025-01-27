# TODO

## Next

* verifiedUscCredentials: instead "test connection" => "verify connection"
    * enable sync/book only if current creds == verified creds
    * same for GCal
* fetch activity days in parallel (syncProgress, display "Day 13", "Day 12",... counter like, as done with venues;
    * alternatively: send ~80 requests (each different page), for a single day ;)
* BUG: select search time; navigate other screen and back; time is gone!

## Backlog

* tooltips for table headers (some are not self-explanatory)
* if sorted by rating, then update rating, then need to be resorted (just that one which has been updated)
  day can have 66 pages!); and/or is it possible to increase page-size?!
* colored output for sync report (green for +, red for -)
* different icon for wishlisted
* make date search field more compact
* system test: book something which can't be bookedl parse response and show proper message
    * +6 reservations; +2 same day (visit limit); over checkin limit (end of period); over period limit (same venue per
      calendar month)
* enable up/down arrow for table; (would require a global listener, and changing focus...)
* make long venue texts "somehow" selectable; A) selectable text (toolitp?!) or B) click => copy clipboard
* third party syncer for cosmos-west
* ad snackbar: when type != info, make it duration=indef and closeable button (otherwise message gone without time to
  read it)

## Back-Backlog (unsorted)

* adaptive layout: depending on window size, change layout (add columns, change formatting)
* html click on image, zoom (https://codeconvey.com/html-image-zoom-on-click/)
* sync the membership tag (minimum one only) for venue&activity
* support custom plan; define everything yourself (as so many other plans are there; think of B2B)
* ... no-show for freetraining possible?? try it yourself ;)
* report overview like in app: monthly what visited, also show 6x per venue per (calendar! not period) month
* when text has emoji, then its height is much bigger; e.g. see in activities/freetrainings table, when there is only 1
  checked-in row
    * SOLUTION: make emoji same height as text (annoated text, and change font size? or make two Text() composables,
      separate for text/emoji?)
* maybe period restart day can be fetched via API (it is shown in app...)
* also sync for activities/freetrainings which plan applies
* how about plus checkins?
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
* uitest+koin (work in progress)

## Nope

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
