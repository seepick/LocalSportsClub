# LocalSportsClub

Communistic Custom Client for Urban Sports Club.

## Internal

* To package deployable artifacts, simply run `./gradlew package` and see the results
  in `build/composebinaries/main/...` (built seperately on macOS, Linux, Windows).

# TODOs

## Next up

* venue links: only store one-way (no need to store it double; implicitly bi-directional)
* if venue.importantInfo == "-" then nullify
* sort case-IN-sensitive
* support sorting asc/desc
* BUG: if sorted by rating, then update rating, then need to be resorted (just that one which has been updated)
* description/info has "\n"... replace them
* linked venues
* official website as editable textfield
* finish rendering (editable) venue fields
* fine tune UI (table, details, colors)

## Big Features

* activities
* reserved activities (sync, overview; mark as reserved)
* usage (sync, UI indicator)
* reserve/cancel button

## Backlog

* enable up/down arrow for table
* sync free/"dropin" activities
* if textfield elipse ("foo...") => show tooltip full text
* migrate old content from AllFit

## Later

* sync with google calendar
* reserve/cancel request
* when no-show indicated, create big alert (request money get back if forgot to simply checkin but was there)
* workout metadata teacher (von partner site scrapen)
* alle QR codes von partner fotografieren, in app geben (prevent no-show-fee ;)
