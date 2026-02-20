# TODO

## v1.10.0

* 🖋also sync "plus checkins"
* 🐞🎨in address: "&#039;" for "Thrive Yoga Outdoor Oosterpark" (but it DOES work with e.g. "Fitness Acadey - 's-Graves...")
* 🖋 new venue search filter: is-auto-sync, is-deleted
* 🎨enforce venue custom text min-height (so when vertical window gets smaller, it doesn't just disappear)
* 🖋most venues montly limit of 6; but exceptions exist, e.g.: de nieuwe yogaschool (see mobile app, limits)

## Backlog

* !  when sort table, then by default DESC initially! (not asc)
* search fields provide count already; e.g. category "(20) EMS"
* manually adjustable table column width (e.g. wanting to make name wider)
* table columns for favorited/wishlisted: make icons smaller; don't even render if disabled (less distraction, safe space)
* clear color coding: everything clickable is blue
* make all/most text selectable for copy'n'paste
* if scroll through table with keys and hold, then short delay and burst
* ad booking validation: the monthly limit (per venue) is venue-specific
    * e.g.: S=2, M=4, L/XL=8 .... or: L/XL=4 // also differentiate between B2C and B2B
* add booking warning if 0 spots; general rule: never block action, only inform (we might be wrong, and worst-case getting server error, no harm being done)
* 🖋display cancellation limit; fetch hour setting per venue; remove from activity; infer
    * 🖋️when cancel booking, get sure cancellation time window is considered
* 🐞🎨if rating sorted, then update rating => resort! (just that one which has been updated)
* 🤖test fixtures from usc-client don't work; right now copy'n'pasted all
* 🎨make rating UI a slider with custom renderer
* 🐞 while full sync, then sync single activity -> CRASH; simple solution: block whole UI on either sync
* 🐞🎨while sync, Book button is disabled with wrong tooltip text ("Please verify USC login credentials")
* 🐞 after synced, trying to book, BookingService#bookOrCancelActivity lookup of activity returns null
* 🖋️"hard sync" feature: get all venue details again (overwrite to get most recent description, opening times, etc...)
* 🖋️filter for time only (evenings), without specifying day/date

## Later

* 🤖UI tests not working anymore... NoSuchMethodError :-/
* 🤖move all mock/dev/dummy things in src/test/kotlin (leftovers?)
* 🤖rework `workParallel` as service with interface, so can be mocked out easily during testing (?)
* 🤖RowScope.TableHeader missing VisualIndicator for bitmap and vector
* 🎨don't display distance (column/search) if home coordinates not set
* 🐞DateParser dutch locale doesn't work when packaged as app...?!
* 🎨 ScreenTemplate: how to get V-scroll if use Column instead LazyColumn? (need weight 1.0f from Column to fill height)
* 🖋️MAP component: https://wiki.openstreetmap.org/wiki/JMapViewer (compose google map only for android, not desktop)
* 🎨could try to parse venue.openingTimes (standardized text): could shorten it ("Monday 10:00-20:00" -> "Mon 10-20");
* 🤖the syncer should not add the year information; return incomplete day+month only (let logic determine year)
* 🤖system test: book something which can't be booked parse response and show proper message
    * +6 bookings; +2 same day; over checkin limit (end of period); over period/veneu limit late cancellation
* 🖋️what if the membership.plan changes?! needs to be overwritten (startup, or pref change)

## Nope

* for website: html click on image, zoom (https://codeconvey.com/html-image-zoom-on-click/)
* investigate: room (flow-enabled sqlite MPP abstraction) instead exposed?
* (switch to ZonedDate?)
* maybe use 'net.ricecode:string-similarity:1.0.0'
* display GCal name, once connected tested ok (would need to persist; when to fetch; baeh...)
* notes with rich format text-editor (bold, italic, colors, fontsize)
* 🎨adaptive layout: depending on window size, change layout (add columns, change formatting)
