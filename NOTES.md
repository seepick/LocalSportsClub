# Notes

* Download count for each version (GitHub release): https://qwertycube.com/github-release-stats/

## Exposed

* add: `addLogger(StdOutSqlLogger)` in transaction block

## Compose

* material guide: https://m3.material.io/
* Official: https://developer.android.com/compose
* Components: https://developer.android.com/develop/ui/compose/components
    * User Input: https://developer.android.com/develop/ui/compose/text/user-input
    * Buttons: https://developer.android.com/develop/ui/compose/components/button
* Forms: https://medium.com/@anandgaur22/jetpack-compose-chapter-7-forms-and-user-input-in-compose-f2ce3e355356
* Text(style = MaterialTheme.typography.subtitle1)
* Icons = https://fonts.google.com/icons

# Non-Todos

* 🖋 support for corporate (non-private) accounts
* 🎨disable booking-button if class start is in the past
* 🖋 search fields provide count already; e.g. category "(20) EMS" (performance impact?!)
* 🖋 could sync official rating (0.0-5.0 number and total count)
* 🎨 make rating UI a slider with custom renderer
* 🎨ScreenTemplate: how to get V-scroll if use Column instead LazyColumn? (need weight 1.0f from Column to fill height)
* 🤖the syncer should not add the year information; return incomplete day+month only (let logic determine year)
* 🎨could try to parse venue.openingTimes (standardized text): could shorten it ("Monday 10:00-20:00" -> "Mon 10-20");
* 🖋️MAP component: https://wiki.openstreetmap.org/wiki/JMapViewer (compose google map only for android, not desktop)
* 🤖RowScope.TableHeader missing VisualIndicator for bitmap and vector
* 🤖rework `workParallel` as service with interface, so can be mocked out easily during testing (?)
* 🖋️what if the membership.plan changes?! needs to be overwritten (startup, or pref change)
* 🤖when book activity which can't be booked, parse response and show proper message; not needed, just pass through msg
* 🤖 investigate: room (flow-enabled sqlite MPP abstraction) instead exposed?
* 🤖 switch to ZonedDate? UTC already for DNYS.
* 🖋 display GCal name, once connected tested ok (would need to persist; when to fetch; baeh...)
* 🎨 notes with rich format text-editor (bold, italic, colors, fontsize)
* 🎨 adaptive layout: depending on window size, change layout (add columns, change formatting)
* 🖋 generate and install random UUID locally, and introduce heartbeat to server for usage statistics (opt-in in prefs)
* 🎨 when update venue leading to sort table change, then auto-scroll there
* 🖋 string similarity search for "foopar" and find "foobar"
* 🎨 enable tab focus cycling for remark dialog (textfield consumes tab... not worth it anyways)
