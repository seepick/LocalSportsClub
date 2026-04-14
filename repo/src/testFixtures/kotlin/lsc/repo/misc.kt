package lsc.repo

// TODO make generic and reuse
fun ActivityStateDbo.someOther() = ActivityStateDbo.entries.toSet().minus(this).random()
