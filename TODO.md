# TODO

## v1.19.0

* change scoring colors: -1.0 red, +1.0 green, 0.0 nothing/null; +0.1 already greenish! (nothing yellow!)
* 🎨 autoscroll on navigation for SelectSearchOption (e.g. categories)
* 🎨 height of v-scrollbars minus table header

## v2.[0-4].0

## Tech Backlog

ASK AI for code review. find hotspots.

* 🤖 UI tests not working anymore... NoSuchMethodError :-/
* 🐞 after synced, trying to book, BookingService#bookOrCancelActivity lookup of activity returns null
* 🐞 while full sync, then sync single activity -> CRASH; simple solution: block whole UI on either sync
* 🤖 fix file resorver design issue; passing to error dialog for log retrieval; always assume PROD ;)
* 🎨 vertical space venue notes/tables: calc notes text area height dynamically; fill activity table height (if activities existing); if no activities, then fill whole space with notes text area
    * 🎨 vertical space venue notes/tables size divider adjustable/draggable; if window height big, then space left blank (notes text has max height?)
* 🐞 DateParser dutch locale doesn't work when packaged as app...?!
* 🤖the syncer should not add the year information; return incomplete day+month only (let logic determine year)
* 🤖RowScope.TableHeader missing VisualIndicator for bitmap and vector
* 🤖rework `workParallel` as service with interface, so can be mocked out easily during testing (?)
* 🤖when book activity which can't be booked, parse response and show proper message; not needed, just pass through msg
* 🤖 investigate: room (flow-enabled sqlite MPP abstraction) instead exposed?
* 🤖 test fixtures from usc-client don't work; right now copy'n'pasted all
* 🎨 improve tab focus cycling; e.g. see remark dialog
