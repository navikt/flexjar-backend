package no.nav.helse.flex.utvikling

import io.github.serpro69.kfaker.Faker
import jakarta.annotation.PostConstruct
import no.nav.helse.flex.repository.FeedbackDbRecord
import no.nav.helse.flex.repository.FeedbackRepository
import no.nav.helse.flex.serialisertTilString
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

@Component
@Profile("dev")
class TestdataGenerator {
    @Autowired
    lateinit var feedbackRepository: FeedbackRepository

    @PostConstruct
    fun generate() {
        System.out.println("Generating testdata")
        val faker = Faker()

        val antallFeedback = 342

        val apper = listOf("sykepengesoknad-frontend", "ditt-sykefravaer", "spinnsyn-frontend")

        for (i in 1..antallFeedback) {
            fun feedbackTekst(): String {
                if (faker.random.nextInt(100) < 20) return faker.breakingBad.character()
                if (faker.random.nextInt(100) < 20) return faker.dumbAndDumber.quotes()
                if (faker.random.nextInt(100) < 10) return faker.cowboyBebop.quote()
                if (faker.random.nextInt(100) < 10) return faker.beer.brand()
                if (faker.random.nextInt(100) < 20) return faker.military.spaceForceRank()

                return ""
            }

            feedbackRepository.save(
                FeedbackDbRecord(
                    opprettet = OffsetDateTime.now().minusDays(faker.random.nextLong(500L)),
                    feedbackJson =
                        mapOf(
                            "feedback" to feedbackTekst(),
                            "svar" to if (faker.random.nextBoolean()) "JA" else "NEI",
                            "team" to if (faker.random.nextBoolean()) "flex" else "teamsykmelding",
                            "feedbackId" to faker.beer.name(),
                        ).serialisertTilString(),
                    team = if (faker.random.nextBoolean()) "flex" else "teamsykmelding",
                    app = apper[faker.random.nextInt(0, apper.size - 1)],
                    tags =
                        if (faker.random.nextBoolean()) {
                            listOf(
                                faker.color.name(),
                            ).joinToString(",")
                        } else if (faker.random.nextBoolean()) {
                            listOf(
                                faker.color.name(),
                                faker.color.name(),
                            ).joinToString(",")
                        } else {
                            null
                        },
                ),
            )
        }
    }
}
