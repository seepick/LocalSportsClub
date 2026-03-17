# TODO

## v1.15.0

* 🎨 remark auto suggest dropdown, filtered by input, select to autofill
* 🎨 row bg color in small tables (activity/freetraining); esp. highlight booked ones (also in main/big table); rethink color scheme in general
* 🐞🎨 typing time range for activity search is buggy (too strict; do same as with double did)
* 🎨 if scroll through table with keys and hold, then short delay and burst
* 🎨 teacher name in different color

## Backlog

* 🎨 make everything clickable is blue (and don't use blue for non-clickables)
* 🖋 autosync on startup checkbox in prefs
* 🎨 RemarkRating will change bg color (together with favorited/wishlisted red/yellow; need to mix colors)
* 🎨 vertical space venue notes/tables size divider adjustable/draggable
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
