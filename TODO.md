# TODO

## v1.17.0

* 🎨 rethink general highlight-color scheme (no more blue for reserved, as it is used for clickable indicator)
* 🖋 ad DNYS auto-sync: use timezones to auto-adjust UTC to amsterdam+1/2
  orig.first.from == event.dateTimeRange.from.plusHours(2) &&
* 🖋 venue detail, visit limit, table value can be "Classic Not included"

## Backlog

* 🖋 in search.category, only show those where there would be activities existing to book
    * this "bug" is due to past-checkin activities, and those where venue is hidden (implicit search criteria)
* 🖋 before booking, fetch activity details (teacher persisted!)
* 🎨 less vspace use for inputs in remarks dialog
* 🎨 idea search panel: only show emoji, when hover show label-text, when click also show inputs
* 🎨 vertical space venue notes/tables size divider adjustable/draggable; if window height big, then space left blank (notes text has max height?)
* 🖋 auto-infer favorite category by looking at past activity checkins (usage stats' top categories); annotated string with color
    * also check venues which are hidden, if all from same category -> decrease score
* 🐞 while full sync, then sync single activity -> CRASH; simple solution: block whole UI on either sync
* 🐞 after synced, trying to book, BookingService#bookOrCancelActivity lookup of activity returns null
* 🎨 table column numberes bookings/activities/freetrainigs support visually a bit; e.g. if 0 then grey; if > 100 then bold
* 🤖 fix file resorver design issue (passing to error dialog for log retrieval...)
* 🤖 UI tests not working anymore... NoSuchMethodError :-/
* 🎨 make longtext (if clickable, as overspill) with some blue indicator ("..." or "[...]" or "more")
* 🎨 redo carousel view: on click, load data in bg, and slide images left/right (like website does)
    * plus: if go beyond end, start from beginning; and vice versa
* 🎨 make date search options less wide

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
