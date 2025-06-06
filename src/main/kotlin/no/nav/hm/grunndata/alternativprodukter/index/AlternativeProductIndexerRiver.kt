package no.nav.hm.grunndata.alternativprodukter.index

import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Requires
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.KafkaRapid
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.River
import no.nav.hm.grunndata.rapid.dto.ProductRapidDTO
import no.nav.hm.grunndata.rapid.dto.ProductStatus
import no.nav.hm.grunndata.rapid.dto.rapidDTOVersion
import no.nav.hm.grunndata.rapid.event.EventName
import no.nav.hm.grunndata.rapid.event.RapidApp
import no.nav.hm.rapids_rivers.micronaut.RiverHead
import org.slf4j.LoggerFactory

@Context
@Requires(bean = KafkaRapid::class)
class AlternativeProductIndexerRiver(
    river: RiverHead,
    private val objectMapper: ObjectMapper,
    private val alternativeProductIndexer: AlternativeProductIndexer

): River.PacketListener {
    companion object {
        private val LOG = LoggerFactory.getLogger(AlternativeProductIndexerRiver::class.java)
    }

    init {
        LOG.info("Using Rapid DTO version $rapidDTOVersion")
        river
            .validate { it.demandValue("createdBy", RapidApp.grunndata_db)}
            .validate { it.demandAny("eventName", listOf(EventName.syncedRegisterProductV1)) }
            .validate { it.demandKey("payload")}
            .validate { it.demandKey("dtoVersion")}
            .register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val dtoVersion = packet["dtoVersion"].asLong()
        if (dtoVersion > rapidDTOVersion) LOG.warn("this event dto version $dtoVersion is newer than our version: $rapidDTOVersion")
        val dto = objectMapper.treeToValue(packet["payload"], ProductRapidDTO::class.java)
        if (dto.status == ProductStatus.DELETED) {
            LOG.info("deleting product id: ${dto.id} hmsnr: ${dto.hmsArtNr}")
            alternativeProductIndexer.delete(dto.id)
        }
        else {
            LOG.info("indexing product id: ${dto.id} hmsnr: ${dto.hmsArtNr}, " +
                    "disabled until we get wareHouseStock calls sorted out from oebs")
        }
    }
}
