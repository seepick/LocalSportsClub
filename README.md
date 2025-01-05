# LocalSportsClub

Communistic Custom Client for Urban Sports Club.

## Internal

* To package deployable artifacts, simply run `./gradlew package` and see the results
  in `build/composebinaries/main/...` (built seperately on macOS, Linux, Windows).

# TODOs

## Next up

* real system test: book/cancel feature
* full sync, and migrate AllFit data
* // hide partners: femme*, Ladies Only, padel, SUP
* in activity table, DO show visited count column. important. same for freetraining.

## Backlog

* UI: fine tune UI (table, details, colors); use theme colors only (dark/light mode)
* UI: table row color highlighted for favorited/wishlisted/booked (gradient for multiples)
* view only either Activity OR Freetraining (not both at the same time)
* FEATURE: display linked venues (be aware, that each stored entity is a bi-directional link, although just stored
  1-way)
* MIN: description/info has "\n"... replace them
* FEATURE: support sorting asc/desc
* notes: show scrollbar if necessary

## Later

* display venue address + link to google maps
* FEATURE: sync workout metadata teacher (von partner site scrapen)
* UI BUG: if sorted by rating, then update rating, then need to be resorted (just that one which has been updated)
* enable up/down arrow for table (would require a global listener, and knowing which table got focus...)
* UI: if textfield elipse ("foo...") => show tooltip full text
* when no-show indicated, create big alert (request money get back if forgot to simply checkin but was there)
* FEATURE: sync with google calendar; on booking/cancellation
* use compose's snackbar to indicate notifications about sync
* "light sync" to only sync spots
* activity search: fav, wish, booked, rating, category
* freetraining search: date, category, rating, fav, wish
* venue search: fav, wish, rating, hidden, activities.count, checkins.count, bookings.count, category

## Later Later

* map component: https://wiki.openstreetmap.org/wiki/JMapViewer (compose google map only for android, not desktop)
* SYNC: currentYear is passed, but watch out for new year transition!
* make rating UI a slider with custom renderer
* notes with rich format text-editor (bold, italic, colors, fontsize)
* (switch to ZonedDate?)
* investigate: room (flow-enabled sqlite MPP abstraction) instead exposed?

# Compose help:

* Official: https://developer.android.com/compose
* User Input: https://developer.android.com/develop/ui/compose/text/user-input
* Buttons: https://developer.android.com/develop/ui/compose/components/button
* Forms: https://medium.com/@anandgaur22/jetpack-compose-chapter-7-forms-and-user-input-in-compose-f2ce3e355356
