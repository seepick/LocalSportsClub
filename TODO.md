# TODO

## v1.12.0

* for some categories, add associated emoji
* rethink color coding for both progress bars; make consistent!
* MonthlyVisitsModel.MAX_VISITS = 6, but must change according plan
    * S = 2x; M = 4x; L & XL = 6x; different per venue?
* if filter time, and change "from" time after "to" time, then adjust
* prefill search properly; e.g. distance < 3.0; checkins = 0
* table columns for favorited/wishlisted: make icons smaller; don't even render if disabled (less distraction, safe space)
* make all/most text selectable for copy'n'paste

## Backlog

* 🖋️ when cancel booking outside cancellation window: display warning-confirmation dialog
* 🎨 clear color coding: everything clickable is blue
* 🖋 go through venue/activity info/descriptive text details (on website); verify is all synced?
* Balanzs has two linked venues with identical name; it seems to cause issues...?!
    * het gymlokaal has two venues, with almost identical activity; it seems to cause issues...?!
* 🎨 if scroll through table with keys and hold, then short delay and burst
* string similarity search (search "foopar" and find "foobar" ); 'net.ricecode:string-similarity:1.0.0'
* 🎨 manually adjustable table column width (e.g. wanting to make name wider)
* 🎨 combine all venue info-texts into a single popup (info, times, description); too busy right now
* 🐞 while full sync, then sync single activity -> CRASH; simple solution: block whole UI on either sync
* 🖋 search filter for time only (evenings), without specifying day/date; in general think about using a ranged slider
* 🐞🎨while sync, Book button is disabled with wrong tooltip text ("Please verify USC login credentials")
* 🐞 after synced, trying to book, BookingService#bookOrCancelActivity lookup of activity returns null
* 🎨 make rating UI a slider with custom renderer
* 🖋 ad booking validation: the monthly limit (per venue) is venue- and plan-specific
    * e.g.: S=2, M=4, L/XL=8 .... or: L/XL=4 // also differentiate between B2C and B2B
    * most venues 6; but exceptions exist, e.g.: de nieuwe yogaschool (see mobile app, limits)

## New Features

* 🖋 sync also "plus checkins" (check app what's that again)
* 🖋 search fields provide count already; e.g. category "(20) EMS"
* 🖋 mark wishlisted-like (maybe a new label?) based on activity title (regexp like)
    * or maybe an adaptive "suggested" or "liked" venues/activities based on past bookings
* 🖋 could sync official rating (0.0-5.0 number and total count)
* 🖋️ "hard sync" feature: get all venue details again (overwrite to get most recent description, opening times, etc...)

## Later

* 🤖test fixtures from usc-client don't work; right now copy'n'pasted all
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

* investigate: room (flow-enabled sqlite MPP abstraction) instead exposed?
* switch to ZonedDate? UTC already for DNYS.
* display GCal name, once connected tested ok (would need to persist; when to fetch; baeh...)
* 🎨 notes with rich format text-editor (bold, italic, colors, fontsize)
* 🎨 adaptive layout: depending on window size, change layout (add columns, change formatting)
* 🖋 generate and install random UUID locally, and introduce heartbeat to server for usage statistics (opt-in in prefs)
* 🎨 when update venue leading to sort table change, then auto-scroll there
