# TODO

## v1.8.0

## Backlog

* 🤖replace release script with github workflow
* 🤖RowScope.TableHeader missing VisualIndicator for bitmap and vector
* 🐞️!!BUG in PAGEABLE.kt: not going beyond 20 pages!
    * while venue sync: not stepping over to next page when: page count > page hint (showMore logic doesn't work to skip over)
* 🖋️activity single-sync: sync per venue (for all activitites, not single); add tooltip
* 🖋️when search/filter for distance, provide only < and > (no =)
* 🎨every toast message should it closable (otherwise annoying); click on it to dismiss (no button needed)
* 🎨tooltips for table headers (symbols are not van-zelf-sprekend)
* 🎨make address right-clickable => dropdown: copy to clipboard
* 🎨tooltip when title of activity too long in detail view
* 🎨tooltip/notification message: make it away-clickable (maybe close button, maybe just click on it to dismiss)
* 🖋️store more data from `ActivityDetails` (spots left, etc.)
* 🖋️ (ALREADY DONE?!) search for activity teacher and description (free text search)
* 🎨enforce venue custom text min-height (so when vertical window gets smaller, it doesn't just disappear)
* 🖋️filter for time only (evenings), without specifying day/date
* 🖋️when cancel, get sure cancellation time window is considered
* 🐞 while full sync, then sync single activity -> CRASH; simple solution: block whole UI on either sync
* 🐞🎨while sync, Book button is disabled with wrong tooltip text ("Please verify USC login credentials")
* 🐞🎨if rating sorted, then update rating => resort! (just that one which has been updated)
* 🐞 after synced, trying to book, BookingService#bookOrCancelActivity lookup of activity returns null
* 🐞🎨in address: "&#039;" for "Thrive Yoga Outdoor Oosterpark" (but it DOES work with e.g. "Fitness Acadey - 's-Graves...")
* 🖋️incorporate cancellation_limit (fetch hour setting per venue; remove from activity; infer)

### Low Prio

* 🤖UI tests not working anymore... NoSuchMethodError :-/
* 🤖move all mock/dev/dummy things in src/test/kotlin; create LocalSportsClubDevApp as entry point
* 🤖rework `workParallel` as service with interface, so can be mocked out easily during testing (?)
* 🎨don't display distance (column/search) if home coordinates not set
* 🤖test fixtures from usc-client don't work; right now copy'n'pasted all
* 🎨when navigate with keyboard-arrows, ensure viewport visible, and scroll if necessary
* 🖋most venues montly limit of 6; but exceptions exist, e.g.: de nieuwe yogaschool (see mobile app, limits)
* 🖋also sync "plus checkins"
* 🖋display plan (it is now being used by USC)
* 🐞DateParser dutch locale doesn't work when packaged as app...?!
* 🖋️also fetch plan for venue (available in list, but not in detail response!)
* 🖋️"hard sync" feature: get all venue details again (overwrite to get most recent description, opening times, etc...)
* 🎨make rating UI a slider with custom renderer
* 🎨 ScreenTemplate: how to get V-scroll if use Column instead LazyColumn? (need weight 1.0f from Column to fill height)
* 🖋️MAP component: https://wiki.openstreetmap.org/wiki/JMapViewer (compose google map only for android, not desktop)
* 🎨could try to parse venue.openingTimes (standardized text): could shorten it ("Monday 10:00-20:00" -> "Mon 10-20");
* 🤖the syncer should not add the year information; return incomplete day+month only (let logic determine year)
* 🤖system test: book something which can't be booked parse response and show proper message
    * +6 bookings; +2 same day; over checkin limit (end of period); over period/veneu limit late cancellation
* 🖋️what if the membership.plan changes?! needs to be overwritten (startup, or pref change)

### Going Public

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
* notes with rich format text-editor (bold, italic, colors, fontsize)
* 🎨adaptive layout: depending on window size, change layout (add columns, change formatting)
