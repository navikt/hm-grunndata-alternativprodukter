package no.nav.hm.grunndata.alternativprodukter.index

import io.micronaut.core.annotation.Introspected
import java.time.LocalDateTime
import java.util.UUID
import no.nav.hm.grunndata.alternativprodukter.alternative.AlternativeAndProductStockService.ProductStockAlternatives
import no.nav.hm.grunndata.alternativprodukter.stock.ProductStockDTO
import no.nav.hm.grunndata.alternativprodukter.stock.WarehouseStock
import no.nav.hm.grunndata.rapid.dto.AgreementInfo
import no.nav.hm.grunndata.rapid.dto.AlternativeFor
import no.nav.hm.grunndata.rapid.dto.Attributes
import no.nav.hm.grunndata.rapid.dto.CompatibleWith
import no.nav.hm.grunndata.rapid.dto.IsoCategoryDTO
import no.nav.hm.grunndata.rapid.dto.MediaInfo
import no.nav.hm.grunndata.rapid.dto.MediaSourceType
import no.nav.hm.grunndata.rapid.dto.MediaType
import no.nav.hm.grunndata.rapid.dto.PakrevdGodkjenningskurs
import no.nav.hm.grunndata.rapid.dto.ProductAgreementStatus
import no.nav.hm.grunndata.rapid.dto.ProductRapidDTO
import no.nav.hm.grunndata.rapid.dto.ProductStatus
import no.nav.hm.grunndata.rapid.dto.Produkttype
import no.nav.hm.grunndata.rapid.dto.TechData

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
data class WareHouseStockDoc(
    val locationId: Int,
    val location: String,
    val amountInStock: Int,
    val available: Int,
    val reserved: Int,
    val needNotified: Int,
    val minmax: Boolean,
    val backOrders: Int,
    val intRequest: Int,
    val request: Int,
    val orders: Int,
    val inStock: Boolean,
    val physical: Int,

    val updated: LocalDateTime = LocalDateTime.now()
)

fun WarehouseStock.toDoc(dto: ProductStockDTO): WareHouseStockDoc = WareHouseStockDoc(
    locationId = locationId,
    location = location,
    available = available,
    amountInStock = amountInStock,
    reserved = reserved,
    needNotified = needNotified,
    minmax = minmax,
    backOrders = backOrders,
    request = request,
    intRequest = intRequest,
    orders = orders,
    physical = physical,
    inStock = inStock,
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

fun ProductRapidDTO.toDoc(iso: IsoCategoryDTO, productStockAlternatives: ProductStockAlternatives): AlternativeProductDoc {
        val (onlyActiveAgreements, previousAgreements) =
            agreements.partition {
                it.published!!.isBefore(LocalDateTime.now())
                        && it.expired.isAfter(LocalDateTime.now()) && it.status == ProductAgreementStatus.ACTIVE
            }
        return AlternativeProductDoc(id = id.toString(),
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
            alternativeFor = productStockAlternatives.alternatives.map { it }.toSet(),
            wareHouseStock = productStockAlternatives.original.warehouseStock.map { it.toDoc(productStockAlternatives.original)}
        )
    }


