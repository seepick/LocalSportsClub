package lsc.domain.repo

import lsc.domain.model.Venue
import lsc.repo.VenueDbo

public interface VenueDomainRepo {
    /** Doesn't do any filtering, not even the deleted ones. */
    fun selectAllAnywhere_NOPE(): List<VenueDbo>
    fun selectAllAnywhere(): List<Venue>

//    fun selectAllByCity(cityId: Int): List<VenueDbo>
//    fun insert(venue: VenueDbo): VenueDbo
//    fun update(venue: VenueDbo): VenueDbo
//    fun selectById(id: Int): VenueDbo?
//    fun selectBySlug(slug: String): VenueDbo?
}
