# TODO

* 🎨 rethink general highlight-color scheme

## v1.16.0

* BUGFIX carousel image loading!
* 🎨 render faved/wished icons in big tables next to venue name (change font color; annotated string)
* 🎨 ad search-category/plan (mini-tables): remove black border
* 🎨 ad search-category: add padding left
* 🎨 make km distance indicator less prominent; remove black border, make even more transparent/alpha
* 🎨 make everything clickable is blue (and don't use blue for non-clickables)
    * add:
        * venue image, for carousal, always draw blue border
        * scroll bar drag component
        * blue border around tables (mini tables, in search, big ones, detail-slim ones)
        * every unselected checkbox
        * every textfield and dropdown
        * many search fields...
        * make longtext (if clickable, as overspill) with some blue indicator ("..." or "[...]" or "more"?)
    * remove:
        * booking/reserved color code
    * PLUS: make input bg consistent (now it's gray AND white)
    * PLUS: when hover over input-able component, change appearance (more blue; like table hover)
* 🎨 vertical space venue notes/tables size divider adjustable/draggable
* 🖋️ post-process activity category; now it's a mess; use regexp on name to change properly (pilates, EMS, etc.)
* 🐞🎨while sync, Book button is disabled with wrong tooltip text ("Please verify USC login credentials")
* 🖋 ad DNYS auto-sync: use timezones to auto-adjust UTC to amsterdam+1/2
  orig.first.from == event.dateTimeRange.from.plusHours(2) &&
* 🖋️Balanzs has two linked venues with identical name; it seems to cause issues...?!
    * het gymlokaal has two venues, with almost identical activity; it seems to cause issues...?!

## Backlog

* 🖋 auto-infer favorite category by looking at past activity checkins (usage stats' top categories)
* 🐞 while full sync, then sync single activity -> CRASH; simple solution: block whole UI on either sync
* 🖋 autosync on startup checkbox in prefs
* 🐞 after synced, trying to book, BookingService#bookOrCancelActivity lookup of activity returns null
* 🎨 table column numberes bookings/activities/freetrainigs support visually a bit; e.g. if 0 then grey; if > 100 then bold
* 🖋 sync also "plus checkins" (check app what's that again)
* 🤖fix file resorver design issue (passing to error dialog for log retrieval...)
* 🤖UI tests not working anymore... NoSuchMethodError :-/

## Low Prio

* 🤖test fixtures from usc-client don't work; right now copy'n'pasted all
* 🐞DateParser dutch locale doesn't work when packaged as app...?!
* 🎨don't display distance (column/search) if home coordinates not set
* 🖋 mark wishlisted-like (maybe a new label?) based on activity title (like remarks now)
* 🖋 prvoide an adaptive "suggested" or "liked" venues/activities based on past bookings
* 🖋 search filter for time only (evenings), without specifying day/date; in general think about using a ranged slider
* 🖋 highlight "info usage statistic" icon with red badge (away-clickable), if new penalty was synced
* 🎨 manually adjustable table column width (e.g. wanting to make name wider)
* 🖋️ FreetrainingRemarks
