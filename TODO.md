# TODO

## v1.16.0

* 🖋 USC API changed for visit limits!!!
* 🐞🖋 BUGFIX carousel image loading !!!

## v1.17.0

* 🖋️ post-process activity category; now it's a mess; use regexp on name to change properly (pilates, EMS, etc.)
* 🎨 rethink general highlight-color scheme (no more blue for reserved, as it is used for clickable indicator)
* 🖋 search venue by plan
* 🖋 ad DNYS auto-sync: use timezones to auto-adjust UTC to amsterdam+1/2
  orig.first.from == event.dateTimeRange.from.plusHours(2) &&

## Backlog

* 🎨 vertical space venue notes/tables size divider adjustable/draggable; if window height big, then space left blank (notes text has max height?)
* 🖋 auto-infer favorite category by looking at past activity checkins (usage stats' top categories)
* 🐞 while full sync, then sync single activity -> CRASH; simple solution: block whole UI on either sync
* 🐞 after synced, trying to book, BookingService#bookOrCancelActivity lookup of activity returns null
* 🎨 table column numberes bookings/activities/freetrainigs support visually a bit; e.g. if 0 then grey; if > 100 then bold
* 🤖 fix file resorver design issue (passing to error dialog for log retrieval...)
* 🤖 UI tests not working anymore... NoSuchMethodError :-/
* 🎨 make longtext (if clickable, as overspill) with some blue indicator ("..." or "[...]" or "more")

## Low Prio

* 🖋 autosync on startup checkbox in prefs
* 🤖test fixtures from usc-client don't work; right now copy'n'pasted all
* 🐞DateParser dutch locale doesn't work when packaged as app...?!
* 🎨don't display distance (column/search) if home coordinates not set
* 🖋 mark wishlisted-like (maybe a new label?) based on activity title (like remarks now)
* 🖋 prvoide an adaptive "suggested" or "liked" venues/activities based on past bookings
* 🖋 search filter for time only (evenings), without specifying day/date; in general think about using a ranged slider
* 🖋 highlight "info usage statistic" icon with red badge (away-clickable), if new penalty was synced
* 🎨 manually adjustable table column width (e.g. wanting to make name wider)
* 🖋️ FreetrainingRemarks
