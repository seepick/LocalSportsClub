package seepick.localsportsclub.service

import java.io.File

// somewhat a hack :-/
object DummyFileResolver : FileResolver {
    override fun resolve(entry: FileEntry) = File("")
    override fun resolve(entry: DirectoryEntry) = File("")
}
