# TODO

## v1.19.0

* 🖋 global remarks (category / activity / teacher)
    * [✅] persistence layer
    * [✅] UI for managing remarks in prefs (reuse RemarksView)
    * [✅] transform DBOs to business objects (sealed type)
    * [✅] service business logic to update remarks (simple reset ~~figure out add/remove/update logic~~)
    * [ ] category annotated string with color (in table, etc.)
    * [ ] extend activity/venue score based on category ratings (needs to passed in, externally calced)
        * local takes precedence over global remark

## Backlog

* ad adapative suggestions: remove those already used
* could provide ultra long list to global remark for teacher/activity, and make suggest "prefilter it dynamically"
* 🎨 autoscroll on navigation for SelectSearchOption (e.g. categories)
* 🖋 maybe make activity&freetraining search isHidden implicit search filter visible; just as did with venue.isDeleted
* 🖋 provide custom emoji for venues (just a simple string, which is added to a venue's name)
* 🎨 make date search options less wide
* 🎨 height of v-scrollbars minus table header
* 🎨 vertical space venue notes/tables: calc notes text area height dynamically; fill activity table height (if activities existing); if no activities, then fill whole space with notes text area
    * 🎨 vertical space venue notes/tables size divider adjustable/draggable; if window height big, then space left blank (notes text has max height?)
* 🖋 maybe share same remarks (teacher, activity) for all linked venues
* 🐞 after synced, trying to book, BookingService#bookOrCancelActivity lookup of activity returns null
* 🐞 while full sync, then sync single activity -> CRASH; simple solution: block whole UI on either sync
* 🤖 fix file resorver design issue; passing to error dialog for log retrieval; always assume PROD ;)
* 🖋 mark wishlisted-like (maybe a new label?) based on activity title (like remarks now)
* 🖋 search filter for time only (evenings), without specifying day/date; in general think about using a ranged slider
