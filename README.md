# LocalSportsClub

Communistic Custom Client for Urban Sports Club.

## Internal

* To package deployable artifacts, simply run `./gradlew package` and see the results in `build/composebinaries/main/...` (built seperately on macOS, Linux, Windows).

# TODOs

* switch to latest dep versions (ktor 3)
* syncing of data
* persistence
* simple UI:
  * workouts table
  * search for text (title, partner, teacher), filter date
  * button to go to website
* ? koin + compose?
* when no-show indicated, create big alert (request money get back if forgot to simply checkin but was there)
* workout metadata teacher (von partner site scrapen)
* alle QR codes von partner fotografieren, in app geben (prevent no-show-fee ;)