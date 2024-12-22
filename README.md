# LocalSportsClub

Communistic Custom Client for Urban Sports Club.

## Internal

* To package deployable artifacts, simply run `./gradlew package` and see the results
  in `build/composebinaries/main/...` (built seperately on macOS, Linux, Windows).

# TODOs

## Big Features

* link partner venues
* activities
* reserved activities overview
* usage
* reserve/cancel button

## Next up

* mutable venue object
* sort case-IN-sensitive
* enable up/down arrow for table
* support sorting asc/desc
* BUG: if sorted by rating, then update rating, then need to be resorted (just that one which has been updated)
* description/info has "\n"... replace them
* sync more venue details
* linked venues
* official website as editable textfield
* finish rendering (editable) venue fields
* fine tune UI (table, details, colors)
* dry run venue sync; optimizations

## Backlog

* free trainings
* if textfield elipse ("foo...") => show tooltip full text
* sync activities ("workouts")
* sync reservations
* mark reserved activities
* show usage indicator
* migrate old content from AllFit

## Later

* sync with google calendar
* reserve/cancel request
* when no-show indicated, create big alert (request money get back if forgot to simply checkin but was there)
* workout metadata teacher (von partner site scrapen)
* alle QR codes von partner fotografieren, in app geben (prevent no-show-fee ;)
