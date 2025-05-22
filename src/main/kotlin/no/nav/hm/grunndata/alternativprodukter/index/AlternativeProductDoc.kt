package no.nav.hm.grunndata.alternativprodukter.index

import io.micronaut.core.annotation.Introspected
import no.nav.hm.grunndata.rapid.dto.*
import java.time.LocalDateTime
import java.util.UUID

@Introspected
data class AlternativeProductDoc(
    override val id: String,
    val supplier: ProductSupplier,
    val title: String,
    val articleName: String,
    val attributes: AttributesDoc,
    val status: ProductStatus,
    val hmsArtNr: String? = null,
    val supplierRef: String,
    val isoCategory: String,
    val isoCategoryTitle: String? = null,
    val isoCategoryTitleShort: String? = null,
    val isoCategoryText: String? = null,
    val isoCategoryTextShort: String? = null,
    val seriesId: String? = null,
    val data: List<TechData> = emptyList(),
    val media: List<MediaDoc> = emptyList(),
    val created: LocalDateTime,
    val updated: LocalDateTime,
    val expired: LocalDateTime,
    val agreements: List<AgreementInfoDoc> = emptyList(),
    val hasAgreement: Boolean = false,
    val alternativeFor: Set<String> = emptySet(),
    val wareHouseStock: List<WareHouseStockDoc> = emptyList(),
): SearchDoc

@Introspected
data class ProductSupplier(val id: String, val identifier: String, val name: String)

data class AttributesDoc(
    val manufacturer: String? = null,
    val compatibleWith: CompatibleWith? = null,
    val keywords: List<String>? = null,
    val series: String? = null,
    val shortdescription: String? = null,
    val text: String? = null,
    val url: String? = null,
    val bestillingsordning: Boolean? = null,
    val digitalSoknad: Boolean? = null,
    val sortimentKategori: String? = null,
    val pakrevdGodkjenningskurs: PakrevdGodkjenningskurs? = null,
    val produkttype: Produkttype? = null,
    val tenderId: String? = null,
    val hasTender: Boolean? = null,
    val alternativeFor: AlternativeFor? = null,
    val egnetForKommunalTekniker: Boolean? = null,
    val egnetForBrukerpass: Boolean? = null,
)

data class MediaDoc(
    val uri: String,
    val priority: Int = 1,
    val type: MediaType = MediaType.IMAGE,
    val text: String? = null,
    val source: MediaSourceType = MediaSourceType.HMDB
)

data class AgreementInfoDoc(
    val id: UUID,
    val identifier: String? = null,
    val title: String? = null,
    val label: String,
    val rank: Int,
    val postNr: Int,
    val postIdentifier: String? = null,
    val postTitle: String? = null,
    val postId: UUID? = null,
    val refNr: String? = null,
    val reference: String,
    val published: LocalDateTime,
    val expired: LocalDateTime,
)

fun Attributes.toDoc(): AttributesDoc {
    return AttributesDoc(
        manufacturer = manufacturer,
        keywords = keywords,
        series = series,
        shortdescription = shortdescription,
        text = text,
        url = url,
        bestillingsordning = bestillingsordning,
        digitalSoknad = digitalSoknad,
        sortimentKategori = sortimentKategori,
        pakrevdGodkjenningskurs = pakrevdGodkjenningskurs,
        produkttype = produkttype,
        tenderId = tenderId,
        hasTender = hasTender,
        compatibleWith = compatibleWith,
        alternativeFor = alternativeFor,
        egnetForKommunalTekniker = egnetForKommunalTekniker,
        egnetForBrukerpass = egnetForBrukerpass
    )
}

@Introspected
data class WareHouseStock(
    val erPåLager: Boolean,
    val organisasjons_id: Long,
    val organisasjons_navn: String,
    val artikkelnummer: String,
    val artikkelid: Long,
    val fysisk: Int,
    val tilgjengeligatt: Int,
    val tilgjengeligroo: Int,
    val tilgjengelig: Int,
    val behovsmeldt: Int,
    val reservert: Int,
    val restordre: Int,
    val bestillinger: Int,
    val anmodning: Int,
    val intanmodning: Int,
    val forsyning: Int,
    val sortiment: Boolean,
    val lagervare: Boolean,
    val minmax: Boolean
)

@Introspected
data class WareHouseStockDoc(
    val location: String,
    val available: Int,
    val reserved: Int,
    val needNotified: Int,
    val minmax: Boolean,
    val updated: LocalDateTime = LocalDateTime.now()
)

fun StockQuantity.toDoc(dto: ProductStockDTO): WareHouseStockDoc = WareHouseStockDoc(
    location = location,
    available = available,
    reserved = reserved,
    needNotified = needNotified,
    minmax = minmax,
    updated = dto.updated
)

fun MediaInfo.toDoc(): MediaDoc = MediaDoc(
    uri = uri, priority = priority, type = type, text = text, source = source
)

fun AgreementInfo.toDoc(): AgreementInfoDoc = AgreementInfoDoc(
    id = id,
    identifier = identifier,
    title = title,
    label = title ?: "",
    rank = rank,
    postNr = postNr,
    postIdentifier = postIdentifier,
    postTitle = postTitle,
    postId = postId,
    refNr = refNr,
    reference = reference,
    expired = expired,
    published = published ?: LocalDateTime.now()
)


fun ProductRapidDTO.toDoc(
    isoCategoryService: IsoCategoryService,
    techLabelService: TechLabelService,
    alternativProdukterClient: AlternativProdukterClient
): AlternativeProductDoc = try {
    val (onlyActiveAgreements, previousAgreements) =
        agreements.partition {
            it.published!!.isBefore(LocalDateTime.now())
                    && it.expired.isAfter(LocalDateTime.now()) && it.status == ProductAgreementStatus.ACTIVE
        }
    val alternativeProdukterResponse = alternativProdukterClient.fetchAlternativProdukter(hmsArtNr!!)
    val iso = isoCategoryService.lookUpCode(isoCategory)
    AlternativeProductDoc(id = id.toString(),
        supplier = ProductSupplier(
            id = supplier.id.toString(), identifier = supplier.identifier, name = supplier.name
        ),
        title = title,
        articleName = articleName,
        attributes = attributes.toDoc(),
        status = status,
        hmsArtNr = hmsArtNr,
        supplierRef = supplierRef,
        isoCategory = isoCategory,
        isoCategoryTitle = iso?.isoTitle,
        isoCategoryTitleShort = iso?.isoTitleShort,
        isoCategoryText = iso?.isoText,
        isoCategoryTextShort = iso?.isoTextShort,
        seriesId = seriesUUID.toString(),
        data = techData,
        media = media.map { it.toDoc() }.sortedBy { it.priority },
        created = created,
        updated = updated,
        expired = expired,
        agreements = onlyActiveAgreements.map { it.toDoc() },
        hasAgreement = onlyActiveAgreements.isNotEmpty(),
        alternativeFor = alternativeProdukterResponse.alternatives.map { it }.toSet(),
        wareHouseStock = alternativeProdukterResponse.original.stockQuantity.map { it.toDoc(alternativeProdukterResponse.original)}
    )
} catch (e: Exception) {
    println("ERROR: $isoCategory")
    throw e
}

