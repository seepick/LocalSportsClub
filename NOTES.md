# Notes

* Download count for each version (GitHub release): https://qwertycube.com/github-release-stats/

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
* check: val assetFile = Path.of(System.getProperty("compose.application.resources.dir")).resolve("myFile.png")

# Backlog of moved out Todos

* 🎨 notes with rich format text-editor (bold, italic, colors, fontsize)
* 🎨 could provide ultra long list to global remark for teacher/activity, and make suggest "prefilter it dynamically"
* 🎨 make date search options less wide
* 🖋 share same remarks (teacher, activity) for all linked venues
* 🖋 provide custom emoji for venues (just a simple string, which is added to a venue's name)
* 🖋 mark wishlisted-like (maybe a new label?) based on activity title (like remarks now)
* 🖋 search filter for time only (evenings), without specifying day/date; in general think about using a ranged slider
* 🖋 maybe make activity&freetraining search isHidden implicit search filter visible; just as did with venue.isDeleted
* 🖋 category new property: activitiesCount
* 🎨 redo carousel view: on click, load data in bg, and slide images left/right (like website does)
    * plus: if go beyond end, start from beginning; and vice versa
* 🎨 search panel save space by only showing emoji, when hover show label-text, when click also show inputs
* 🖋 highlight "info usage statistic" icon with red badge (away-clickable), if new penalty was synced
* 🖋️ FreetrainingRemarks
* 🎨 adjust dialog size dynamically based on window size (while resizing)
* 🎨 disable booking-button if class start is in the past
* 🖋 search fields provide count already; e.g. category "(20) EMS" (performance impact?!)
* 🤖 ScreenTemplate: how to get V-scroll if use Column instead LazyColumn? (need weight 1.0f from Column to fill height)
* 🖋 generate and install random UUID locally, and introduce heartbeat to server for usage statistics (opt-in in prefs)
* 🎨 when update venue leading to sort table change, then auto-scroll there (?)

## Only for others

* 🖋 what if the membership.plan changes?! needs to be overwritten (startup, or pref change)
* 🤖 switch to ZonedDate? UTC already for DNYS.
* 🎨 don't display distance (column/search) if home coordinates not set
* 🖋 support for corporate (non-private) accounts
* 🖋 customize about dialog (app description, github url; all content copyright USC)
* 🎨 adaptive layout (laptop vs smartphone): depending on window size, change layout (add columns, change formatting)
* 🖋 display GCal name, once connected tested ok (would need to persist; when to fetch; baeh...)

## Dont want it

* 🖋 could sync official rating (0.0-5.0 number and total count)
* 🖋 auto infer category rating: check venue rating and those which are hidden (if all from same category -> decrease score); look at past activity checkins (usage stats' top categories)
* 🖋 autosync on startup checkbox in prefs

## Too much effort / Not worth it

* 🎨 manually adjustable table column width (e.g. wanting to make name wider)
* 🖋 could try to parse venue.openingTimes (standardized text): could shorten it ("Monday 10:00-20:00" -> "Mon 10-20");
* 🎨 rating as custom rendered star symbols (not emoji) and use custom color (1 star = red, 5 stars = green)
* 🎨 make longtext (if clickable, as overspill) with some blue indicator ("..." or "[...]" or "more")
* 🎨 make rating UI a slider with custom renderer
* 🖋 string similarity search for "foopar" and find "foobar" (performance impact too bad if not done properly!)
* 🖋 provide an adaptive "suggested" or "liked" venues/activities based on past bookings
* 🖋️ MAP component: https://wiki.openstreetmap.org/wiki/JMapViewer (compose google map only for android, not desktop)
* 🖋 research auto-update for macos (create sample project; jdeploy, or old update4j) ... way too sophisticated/complex
