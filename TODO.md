# TODO

## v1.14.0

* 🎨 enable tab focus cycling for remark dialog
* 🎨 rating dropdown vertical less space (A textrield in venue details and B dropdown itself)
* enhance usage statistics:
    * top: monthly
        * rename "venues monthly limit" to "this month's visits" and make it a table (per venue: title, datetime)
        * average/min/max visits per month
    * bottom: general
        * how many years/months active
        * top venues visited (your favorites, basically)

## v1.15.0

* 🎨 row bg color in small tables (activity/freetraining); esp. highlight booked ones (also in main/big table); rethink color scheme in general
* 🎨 RemarkRating will change bg color (together with favorited/wishlisted red/yellow; need to mix colors)
* 🎨 vertical space venue notes/tables size divider adjustable/draggable
* 🐞🎨 typing time range for activity search is buggy (too strict; do same as with double did)
* 🎨 make everything clickable is blue (and don't use blue for non-clickables)
* 🖋 autosync on startup checkbox in prefs
* 🎨 if scroll through table with keys and hold, then short delay and burst

## Backlog

* 🖋️ post-process activity category; now it's a mess; use regexp on name to change properly (pilates, EMS, etc.)
* 🖋️Balanzs has two linked venues with identical name; it seems to cause issues...?!
    * het gymlokaal has two venues, with almost identical activity; it seems to cause issues...?!
* 🐞 while full sync, then sync single activity -> CRASH; simple solution: block whole UI on either sync
* 🐞🎨while sync, Book button is disabled with wrong tooltip text ("Please verify USC login credentials")
* 🐞 after synced, trying to book, BookingService#bookOrCancelActivity lookup of activity returns null
* 🎨 table column numberes bookings/activities/freetrainigs support visually a bit; e.g. if 0 then grey; if > 100 then bold

## New Features

* 🖋️ FreetrainingRemarks
* 🖋 sync also "plus checkins" (check app what's that again)
* 🖋 search fields provide count already; e.g. category "(20) EMS"
* 🖋 mark wishlisted-like (maybe a new label?) based on activity title (regexp like)
    * or maybe an adaptive "suggested" or "liked" venues/activities based on past bookings
* 🖋 could sync official rating (0.0-5.0 number and total count)
* 🖋 search filter for time only (evenings), without specifying day/date; in general think about using a ranged slider
* 🎨 make rating UI a slider with custom renderer
* 🖋 highlight "info usage statistic" icon with red badge (away-clickable), if new penalty was synced
* 🎨 manually adjustable table column width (e.g. wanting to make name wider)

## Low Prio

* 🤖fix file resorver design issue (passing to error dialog for log retrieval...)
* 🤖test fixtures from usc-client don't work; right now copy'n'pasted all
* 🤖UI tests not working anymore... NoSuchMethodError :-/
* 🐞DateParser dutch locale doesn't work when packaged as app...?!
* 🎨don't display distance (column/search) if home coordinates not set
* 🎨 disable booking-button if class start is in the past

## Nope

* 🎨ScreenTemplate: how to get V-scroll if use Column instead LazyColumn? (need weight 1.0f from Column to fill height)
* 🤖the syncer should not add the year information; return incomplete day+month only (let logic determine year)
* 🎨could try to parse venue.openingTimes (standardized text): could shorten it ("Monday 10:00-20:00" -> "Mon 10-20");
* 🖋️MAP component: https://wiki.openstreetmap.org/wiki/JMapViewer (compose google map only for android, not desktop)
* 🤖RowScope.TableHeader missing VisualIndicator for bitmap and vector
* 🤖rework `workParallel` as service with interface, so can be mocked out easily during testing (?)
* 🖋️what if the membership.plan changes?! needs to be overwritten (startup, or pref change)
* 🤖when book activity which can't be booked, parse response and show proper message; not needed, just pass through msg
* 🤖 investigate: room (flow-enabled sqlite MPP abstraction) instead exposed?
* 🤖 switch to ZonedDate? UTC already for DNYS.
* 🖋 display GCal name, once connected tested ok (would need to persist; when to fetch; baeh...)
* 🎨 notes with rich format text-editor (bold, italic, colors, fontsize)
* 🎨 adaptive layout: depending on window size, change layout (add columns, change formatting)
* 🖋 generate and install random UUID locally, and introduce heartbeat to server for usage statistics (opt-in in prefs)
* 🎨 when update venue leading to sort table change, then auto-scroll there
* 🖋 string similarity search for "foopar" and find "foobar"
