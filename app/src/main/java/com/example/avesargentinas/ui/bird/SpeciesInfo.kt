package com.example.avesargentinas.ui.bird

import java.util.Locale

enum class PhotoSex { MACHO, HEMBRA }

data class SpeciesSource(
    val id: String,
    val title: String,
    val organization: String,
    val url: String,
    val consultedOn: String
)

data class SpeciesPhoto(
    val imagePath: String,
    val sex: PhotoSex?,
    val representative: Boolean,
    val author: String?,
    val license: String?,
    val sourceIds: List<String>
)

data class SpeciesConservation(
    val category: String,
    val assessmentYear: Int?,
    val sourceIds: List<String>
)

data class SpeciesInfo(
    val speciesKey: String,
    val commonName: String? = null,
    val scientificName: String? = null,
    val englishName: String? = null,
    val family: String? = null,
    val group: String? = null,
    val order: String? = null,
    val argentineRegions: String? = null,
    val description: String? = null,
    val habitat: String? = null,
    val diet: String? = null,
    val seasonalMovements: String? = null,
    val conservationGlobal: SpeciesConservation? = null,
    val conservationNational: SpeciesConservation? = null,
    val fieldSources: Map<String, List<String>> = emptyMap(),
    val photos: List<SpeciesPhoto> = emptyList()
) {
    fun photoForVariant(variant: String?): SpeciesPhoto? {
        val value = variant?.trim().orEmpty()
        if (value.isEmpty()) return photos.singleOrNull { it.representative }
        val sex = when (value.lowercase(Locale.ROOT)) {
            "macho" -> PhotoSex.MACHO
            "hembra" -> PhotoSex.HEMBRA
            else -> return null
        }
        return photos.singleOrNull { it.sex == sex }
    }
}

data class SpeciesInfoCatalog(
    val sources: Map<String, SpeciesSource> = emptyMap(),
    val species: Map<String, SpeciesInfo> = emptyMap(),
    val issues: List<String> = emptyList()
) {
    fun find(scientificName: String): SpeciesInfo? = species[
        scientificName.lowercase(Locale.ROOT).replace(' ', '_').replace('-', '_')
    ]
}
