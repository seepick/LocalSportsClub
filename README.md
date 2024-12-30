# LocalSportsClub

Communistic Custom Client for Urban Sports Club.

## Internal

* To package deployable artifacts, simply run `./gradlew package` and see the results
  in `build/composebinaries/main/...` (built seperately on macOS, Linux, Windows).

# TODOs

## Next up

* BUGFIX: venue search doesnt work
* usage UI (progress bar like again)
* don't dipslay activities in big table for A) hidden venues and B) in the past wasCheckedin
* UI: display linked venues (be aware, that each stored entity is a bi-directional link, although just stored 1-way)

## Backlog

* transaction scope around whole sync process
* FEATURE: migrate old content from AllFit; implement venue-lookup logic

## Later

* table row color highlighted for favorited/wishlisted/booked (gradient for multiples)
* SYNC: currentYear is passed, but watch out for new year transition!
* when click rating textfield, also expand dropdown
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
* notes as richt format textfield

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
