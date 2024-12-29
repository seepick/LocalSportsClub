# LocalSportsClub

Communistic Custom Client for Urban Sports Club.

## Internal

* To package deployable artifacts, simply run `./gradlew package` and see the results
  in `build/composebinaries/main/...` (built seperately on macOS, Linux, Windows).

# TODOs

## Next up

* notes as richt format textfield
* prepare ACTIVITIES.TEACHER string?
* when click on rating textfield, already dropdown
* usage UI (progress bar like again)
* finish activities table
* implement freetrainings table
* don't dipslay activities in big table for A) hidden venues and B) in the past wasCheckedin
* table row color highlighted for favorited/wishlisted/booked (gradient for multiples)
* simple tables for activities/freetrainings in venues
* ... go to a freetraining, and sync checkedin things (got time info now! create synthetic activity ;)

## Backlog

* FEATURE: migrate old content from AllFit

## Later

* SYNC: currentYear is passed, but watch out for new year transition!
* UI: display linked venues (be aware, that each stored entity is a bi-directional link, although just stored 1-way)
* UI BUG: if sorted by rating, then update rating, then need to be resorted (just that one which has been updated)
* MIN: description/info has "\n"... replace them
* FEATURE: notes textarea (tabbed content)
* UI: fine tune UI (table, details, colors); use theme colors only (dark/light mode)
* UI: support sorting asc/desc
* FEATURE: reserve/cancel button
* UI: if textfield elipse ("foo...") => show tooltip full text
* enable up/down arrow for table (would require a global listener, and knowing which table got focus...)
* when no-show indicated, create big alert (request money get back if forgot to simply checkin but was there)
* make rating UI a slider with custom renderer

## Later Later

* sync with google calendar; on booking/cancellation
* sync workout metadata teacher (von partner site scrapen)
* use compose's snackbar to indicate notifications about sync
* (switch to ZonedDate?)

# Compose help:

* Official: https://developer.android.com/compose
* User Input: https://developer.android.com/develop/ui/compose/text/user-input
* Buttons: https://developer.android.com/develop/ui/compose/components/button
* Forms: https://medium.com/@anandgaur22/jetpack-compose-chapter-7-forms-and-user-input-in-compose-f2ce3e355356
