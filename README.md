# LocalSportsClub

Communistic Custom Client for Urban Sports Club.

## Internal

* To package deployable artifacts, simply run `./gradlew package` and see the results
  in `build/composebinaries/main/...` (built seperately on macOS, Linux, Windows).

# TODOs

## Next up

* rename "isScheduled" to "isBooked"
* write venue.notes when venue is synced via links, or via DataRescuer; prefill it from system, so user will see it ;)
* !! sync past checkins (might require fetching old activities (not yet synced/in DB)); new field: Activity.wasCheckedin
* MIN: description/info has "\n"... replace them
* MIN: if venue.importantInfo == "-" then nullify
* MIN: sort case-IN-sensitive
* cleanup tasktags

## Backlog

* FEATURE: introduce tabbed content: activities
* FEATURE: notes textarea (tabbed content)
* UI: display usage UI (progress bar like again)
* UI: display linked venues (be aware, that each stored entity is a bi-directional link, although just stored 1-way)
* UI BUG: if sorted by rating, then update rating, then need to be resorted (just that one which has been updated)
* UI: finish rendering (editable) venue fields; official website as editable textfield
* FEATURE: also sync free/"dropin" activities
* FEATURE: migrate old content from AllFit

## Later

* UI: support sorting asc/desc
* FEATURE: reserve/cancel button
* UI: fine tune UI (table, details, colors); use theme colors only (dark/light mode)
* UI: if textfield elipse ("foo...") => show tooltip full text
* enable up/down arrow for table
* when no-show indicated, create big alert (request money get back if forgot to simply checkin but was there)

## Later Later

* sync with google calendar
* workout metadata teacher (von partner site scrapen)
* (switch to ZonedDate?)
