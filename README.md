# LocalSportsClub

Communistic Custom Client for Urban Sports Club.

## Internal

* To package deployable artifacts, simply run `./gradlew package` and see the results
  in `build/composebinaries/main/...` (built seperately on macOS, Linux, Windows).

# TODOs

## Next up

* all ViewModels, invoking logic: surround with backgroundTask wrapper
* real system test: book/cancel activity&freetraining

## Backlog

* ... no-show for freetraining possible?? try it yourself ;)
* UI: fine tune UI (table, details, colors); use theme colors only (dark/light mode)
* UI: table row color highlighted for favorited/wishlisted/booked (gradient for multiples)
* FEATURE: display linked venues (be aware, that each stored entity is a bi-directional link, although just stored
  1-way)
* MIN: description/info has "\n"... replace them
* FEATURE: support sorting asc/desc
* notes: show scrollbar if necessary
* after sync, display report what happened ++ big WARNING if new noshow was detected!

## Later

* display venue address + link to google maps
* FEATURE: sync workout metadata teacher (von partner site scrapen)
* UI BUG: if sorted by rating, then update rating, then need to be resorted (just that one which has been updated)
* enable up/down arrow for table (would require a global listener, and knowing which table got focus...)
* UI: if textfield elipse ("foo...") => show tooltip full text
* when no-show indicated, create big alert (request money get back if forgot to simply checkin but was there)
* IMPROVE: the syncer should not add the year information; return incomplete day+month only (let logic determine proper
  year)
* UI: use space for activities/freetraining sub-table ONLY up to row count (if only 1 row, use only that space, but NOT
  more)
* UI: ad ScreenTemplate: how to get vertical scroll if use Column instead LazyColumn? (need weight 1.0f from Column to
  fill height)

## Later Later

* use compose's snackbar to indicate notifications about sync
* use geo-location to pre-calculate distance
* use 'net.ricecode:string-similarity:1.0.0'
* "light sync" to only sync spots
* map component: https://wiki.openstreetmap.org/wiki/JMapViewer (compose google map only for android, not desktop)
* SYNC: currentYear is passed, but watch out for new year transition!
* make rating UI a slider with custom renderer
* notes with rich format text-editor (bold, italic, colors, fontsize)
* (switch to ZonedDate?)
* investigate: room (flow-enabled sqlite MPP abstraction) instead exposed?
* DateParser dutch locale doesn't work when packaged as app...?!

# Compose help:

* Official: https://developer.android.com/compose
* User Input: https://developer.android.com/develop/ui/compose/text/user-input
* Buttons: https://developer.android.com/develop/ui/compose/components/button
* Forms: https://medium.com/@anandgaur22/jetpack-compose-chapter-7-forms-and-user-input-in-compose-f2ce3e355356
