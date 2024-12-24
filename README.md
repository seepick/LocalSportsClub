# LocalSportsClub

Communistic Custom Client for Urban Sports Club.

## Internal

* To package deployable artifacts, simply run `./gradlew package` and see the results
  in `build/composebinaries/main/...` (built seperately on macOS, Linux, Windows).

# TODOs

## Next up

* enable CI
* !! new field: Activity.wasCheckedin
* !! sync activities not always next 14 days, but check the newest activity date, and sync from there
* !! sync past checkins (might require fetching old activities (not yet synced/in DB))
* MIN: change from/to to DateRange object
* MIN: description/info has "\n"... replace them
* MIN: if venue.importantInfo == "-" then nullify
* MIN: sort case-IN-sensitive

## Backlog

* UI: display usage UI (progress bar like again)
* UI: display linked venues (be aware, that each stored entity is a bi-directional link, although just stored 1-way)
* UI BUG: if sorted by rating, then update rating, then need to be resorted (just that one which has been updated)
* finish rendering (editable) venue fields
* UI: official website as editable textfield
* UI: fine tune UI (table, details, colors)
* UI: use theme colors only (dark/light mode)
* UI: support sorting asc/desc
* FEATURE: also sync free/"dropin" activities
* migrate old content from AllFit
* FEATURE: reserve/cancel button

## Later

* UI: if textfield elipse ("foo...") => show tooltip full text
* enable up/down arrow for table
* sync with google calendar
* reserve/cancel request
* when no-show indicated, create big alert (request money get back if forgot to simply checkin but was there)
* workout metadata teacher (von partner site scrapen)
* alle QR codes von partner fotografieren, in app geben (prevent no-show-fee ;)
* switch to ZonedDate
