# TODO

## v1.18.0

* 🎨 rating table col, bg colored red-green based on rating (1-5; 0 = transparent)
* 🖋 research auto-update for macos (create sample project)

## Backlog

* 🎨 improve colors
    * change hover color, when e.g. yellow-greenish, not really visible (more contrast)
    * in dark theme almost invisible
* 🎨 improve remark dialog suggest dropdown UX (add trailing icon instead)
* 🎨 height of v-scrollbars minus table header
* 🤖 change Rating to proper enum
* 🎨 autoscroll on navigation for SelectSearchOption (e.g. categories)
* 🖋 give category ratings (define in prefs), and then extend activity/venue ratings based on that
    * OR: auto-infer favorite category + annotated string with color
        * check venue rating and those which are hidden (if all from same category -> decrease score)
        * look at past activity checkins (usage stats' top categories)
* 🖋 provide custom emoji for venues (just a simple string, which is added to a venue's name)
* 🖋 trim activity.name [ _ . * ⭐︎ ] multiple times (when transform from DBO to entity; or directly on sync when insert in DB?)
    * not [ + ] (too many 60+ etc
    * see venue "Change with pilates", has an emoji as a prefix :-/
* 🎨 render activity state in SubEntityDetails view (booked already; checkedin, and explain emojis for latecancel/noshow); also use color for checkedin/booked/cancel&noshow
* 🖋 maybe make activity&freetraining search isHidden implicit search filter visible; just as did with venue.isDeleted
* 🖋 customize about dialog (app description, github url; all content copyright USC)
* 🎨 vertical space venue notes/tables: calc notes text area height dynamically; fill activity table height (if activities existing); if no activities, then fill whole space with notes text area
* 🎨 vertical space venue notes/tables size divider adjustable/draggable; if window height big, then space left blank (notes text has max height?)
* 🎨 make date search options less wide
* 🤖 fix file resorver design issue; passing to error dialog for log retrieval; always assume PROD ;)
* 🐞 after synced, trying to book, BookingService#bookOrCancelActivity lookup of activity returns null
* 🐞 while full sync, then sync single activity -> CRASH; simple solution: block whole UI on either sync
* 🖋 category new property: activitiesCount

## Low Prio

* 🤖 UI tests not working anymore... NoSuchMethodError :-/
* 🎨 make longtext (if clickable, as overspill) with some blue indicator ("..." or "[...]" or "more")
* 🎨 redo carousel view: on click, load data in bg, and slide images left/right (like website does)
    * plus: if go beyond end, start from beginning; and vice versa
* 🎨 idea search panel: only show emoji, when hover show label-text, when click also show inputs
* 🖋 autosync on startup checkbox in prefs
* 🤖 test fixtures from usc-client don't work; right now copy'n'pasted all
* 🐞 DateParser dutch locale doesn't work when packaged as app...?!
* 🎨 don't display distance (column/search) if home coordinates not set
* 🖋 mark wishlisted-like (maybe a new label?) based on activity title (like remarks now)
* 🖋 prvoide an adaptive "suggested" or "liked" venues/activities based on past bookings
* 🖋 search filter for time only (evenings), without specifying day/date; in general think about using a ranged slider
* 🖋 highlight "info usage statistic" icon with red badge (away-clickable), if new penalty was synced
* 🎨 manually adjustable table column width (e.g. wanting to make name wider)
* 🖋️ FreetrainingRemarks
* 🎨 adjust dialog size dynamically based on window size (while resizing)
