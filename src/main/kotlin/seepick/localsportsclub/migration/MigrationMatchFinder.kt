package seepick.localsportsclub.migration

import seepick.localsportsclub.persistence.VenueDbo

data class LinkMapping(
    val partnerName: String,
    val venueSlug: String,
)

object MigrationMatchFinder {

    private val ignorePartners = setOf(
        "Circling Europe",
        "EMS by Excellence Skin",
        "Femme Gym",
        "Frans Otten Stadion Squash",
        "Freezlab - Hydroxy",
        "Freezlab - Red light therapy",
        "Freezlab - Tanning",
        "Freezlab whole body cryo",
        "Melt Mind & Life",
        "Only4Ladies",
        "Padel Dam",
        "Peakz Padel - Baanverhuur of Jack's Hustle",
        "Peakz Padel - Play & Learn",
        "Pilates 13 De Pijp",
        "SUP SUP CLUB - Markermeer EuroParcs",
        "SUP SUP CLUB - Poort van Amsterdam EuroParcs",
        "SUP SUP CLUB - Sloterplas Hotel Buiten",
        "SUP SUP CLUB - de Rijp EuroParcs",
        "SUP SUP CLUB - de Woudhoeve EuroParcs",
        "SUP Tropisch",
        "SUP&Meer supschool",
        "SwimEasy Amsterdam",
        "YogaToday",
        "Zest Your Life Test",
        "Zest your Life",
        "'t Klaslokaal", // Haarlem
        "Snap Fitness Breda", // Breda
    )

    private val createDeletedPartner = listOf(
        CreateMapping("Active Club"),
        CreateMapping(
            "Bluebirds Centrum",
            linkedVenueSlugs = listOf("bluebirds-oost", "bluebirds-west", "bluebirds-zuid")
        ),
        CreateMapping("CITY ALPS"),
        CreateMapping("Delight Yoga Amsterdam"),
        CreateMapping("Jumpsquare Trampoline Fitness"),
        CreateMapping("Sanctum"),
        CreateMapping("Shido Studio"),
        CreateMapping("SportCity Amsterdam Waterlooplein"),
        CreateMapping("Studio Balance"),
    ).associateBy { it.partnerName }

    private val linkPartnerToExistingVenue = listOf(
        LinkMapping("Aerials Amsterdam", "aerials-amsterdam-cla"),
        LinkMapping("Aikido with Shinyu Body & Mind", "shinyu-aikido-amsterdam-de-pijp-1"),
        LinkMapping("Canal SUP", "canalsup"),
        LinkMapping("Convert2Fit", "convert2fit-slotermeerlaan"),
        LinkMapping("Critical Alignment Yoga", "critical-alignment"),
        LinkMapping("Double Shift", "double-shift"),
        LinkMapping("EMS Health Studio - EMS training", "ems-health-studio"),
        LinkMapping("Frans Otten Padel", "frans-otten-padel"),
        LinkMapping("Fuse SUP", "canal-sup-x-fuse"),
        LinkMapping("Fysiotherapie en Training Amsterdam", "fysiotherapie-training-amsterdam"),
        LinkMapping("Hot Flow Yoga Zuid (Formerly: Hot Flow Yoga Amsterdam)", "hot-flow-yoga-zuid"),
        LinkMapping("Karunika Spiritual Center", "karunika-spiritual-center-studio-38-2"),
        LinkMapping("Movements Overtoom", "movements-overtoom"),
        LinkMapping("Odessa Amsterdam", "odessa-veemkade"),
        LinkMapping("Raiz Movement House", "anima-house"),
        LinkMapping("Relax Lounge", "relax-lounge-overtoom"),
        LinkMapping("SUP SUP CLUB - Apollo Hotel", "sup-sup-club-apollo"),
        LinkMapping("Soul Movement", "wilhelmina-gasthuisterrein"),
        LinkMapping("Sport Natural", "sport-natural"),
        LinkMapping("Svaha Yoga", "svaha-yoga-downtown-shala"),
        LinkMapping("TRIB3  Middenweg", "trib3-middenweg-1"),
        LinkMapping("TULA Yoga", "tula-yoga-westerpark"),
        LinkMapping("The Cosmos - West", "the-cosmos-west"),
        LinkMapping("The Cosmos - de Pijp", "the-cosmos-de-pijp"),
        LinkMapping("The Workout Studio A + B", "the-workout-lab-studio-b"),
        LinkMapping("Vondelgym Zuid - Fitness", "vondelgym-zuid-open-gym"),
        LinkMapping("Vondelgym Zuid - Groepslessen", "vondelgym-zuid"),
        LinkMapping("Yoga Spot Buitenveldert", "yoga-spot-olympisch-stadion"),
        LinkMapping("Yogaschool Noord", "yogaschool-noord-ndsm"),
        LinkMapping("bbb health boutique Amsterdam Jordaan", "bbb-health-boutique-jordaan"),
    ).associateBy { it.partnerName }

    fun find(partners: List<MigrationPartner>, venues: List<VenueDbo>): List<MigrationMatch> {
        val intermediate = partners.filter { !ignorePartners.contains(it.name) }.map { partner ->
            var matchType: MatchType? = null
            venues.firstOrNull { it.name == partner.name }?.also {
                matchType = MatchType.LinkToExistingVenue(it)
            }
            if (matchType == null) {
                linkPartnerToExistingVenue[partner.name]?.also { linkMapping ->
                    matchType = MatchType.LinkToExistingVenue(venues.singleOrNull { it.slug == linkMapping.venueSlug }
                        ?: error("Not found venue by slug: ${linkMapping.venueSlug}"))
                }
            }
            if (matchType == null) {
                createDeletedPartner[partner.name]?.also {
                    require(partner.checkins.isNotEmpty() || partner.dropins.isNotEmpty()) { "Meaningless partner to be created (it's empty): ${partner.name}" }
                    matchType = MatchType.CreateDeletedVenue(it)
                }
            }
            MigrationIntermediateMatch(partner, matchType)
        }
        val notFounds = intermediate.filter { it.matchType == null }
        if (notFounds.isNotEmpty()) {
            println("Failed to find matching partner-venue for ${notFounds.size} items:")
            notFounds.forEach {
                println("- [${it.partner.name}] => ${it.partner.locations.joinToString { "${it.zipCode} ${it.city} ${it.streetName} ${it.houseNumber} ${it.addition}" }}")
            }
            throw Exception("Failed! See details above...")
        }
        return intermediate.map { MigrationMatch(it.partner, it.matchType!!) }
    }
}

data class MigrationIntermediateMatch(
    val partner: MigrationPartner,
    var matchType: MatchType?,
)

data class MigrationMatch(
    val partner: MigrationPartner,
    val matchType: MatchType,
)

sealed interface MatchType {
    data class LinkToExistingVenue(val venueDbo: VenueDbo) : MatchType
    data class CreateDeletedVenue(val createMapping: CreateMapping) : MatchType
}

data class CreateMapping(
    val partnerName: String,
    val linkedVenueSlugs: List<String> = emptyList()
)

