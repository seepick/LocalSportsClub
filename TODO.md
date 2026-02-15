# TODO

## v1.7.0

* upgrade dependency versions
* remove all task tags (resolve or put in here)
* store more data from `ActivityDetails` (spots left, etc.)

## Backlog

* activity single-sync: sync for whole venue (all activities)
* enforce venue custom text min-height (so when vertical window gets smaller, it doesn't just disappear)
* every toast message should it closable (otherwise annoying); click on it to dismiss (no button needed)
* when search/filter for distance, provide only < and > (no =)
* when cancel, get sure cancellation time window is considered
* BUG: when do big sync, then sync single activity -> CRASH; simple solution: block whole UI on either sync
* search for activity teacher and description (free text search)
* don't display distance (column/search) if home coordinates not set
* delete custom 3rd party fetchers
* activity single-sync: tooltip for sync button
* activity single-sync: sync per venue (for all activitites)
* BUGFIX! venue sync bug! not stepping over to next page when: page count > page hint (showMore logic doesn't work to
  skip over)
* UI-FIX: while sync, Book button is disabled with wrong tooltip text ("Please verify USC login credentials")
* BUGFIX: after synced, trying to book, BookingService#bookOrCancelActivity lookup of activity returns null
* tooltips for table headers (symbols are not van-zelf-sprekend)
* make address right-clickable => dropdown: copy to clipboard
* UI-FIX in address: "&#039;" for "Thrive Yoga Outdoor Oosterpark" (but it DOES work with e.g. "Fitness Acadey - '
  s-Graves...")
* filter for time only (evenings), without specifying day/date
* tooltip when title of activity too long in detail view
* tooltip/notification message: make it away-clickable (maybe close button, maybe just click on it to dismiss)

### Low Prio

* activity single-sync: sync (free) spots
* when navigate with keyboard-arrows, ensure viewport visible, and scroll if necessary
* in prefs, select sync of teachers for specific venues by slug, comma separated
* most venues montly limit of 6; but exceptions exist, e.g.: de nieuwe yogaschool (see mobile app, limits)
* also sync "plus checkins"
* notes with rich format text-editor (bold, italic, colors, fontsize)
* display plan (it is now being used by USC)
* BUG: DateParser dutch locale doesn't work when packaged as app...?!
* also fetch plan for venue (available in list, but not in detail response!)
* "hard sync" feature: get all venue details again (overwrite to get most recent description, opening times, etc...)
* make rating UI a slider with custom renderer
* adaptive layout: depending on window size, change layout (add columns, change formatting)
* system test: book something which can't be booked parse response and show proper message
    * +6 bookings; +2 same day; over checkin limit (end of period); over period/veneu limit late cancellation
* if rating sorted, then update rating => resort! (just that one which has been updated)
* UI/ScreenTemplate: how to get V-scroll if use Column instead LazyColumn? (need weight 1.0f from Column to fill height)
* map component: https://wiki.openstreetmap.org/wiki/JMapViewer (compose google map only for android, not desktop)
* could try to parse venue.openingTimes (standardized text): could shorten it ("Monday 10:00-20:00" -> "Mon 10-20");
* IMPROVE: the syncer should not add the year information; return incomplete day+month only (let logic determine year)
* uitest+koin (work in progress)
* spots left: make updateable (see SubEntityDetail view)

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
