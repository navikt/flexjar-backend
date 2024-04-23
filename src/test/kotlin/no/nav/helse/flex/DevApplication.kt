package no.nav.helse.flex

import io.github.serpro69.kfaker.Faker
import jakarta.annotation.PostConstruct
import no.nav.helse.flex.clientidvalidation.ClientIdValidation
import no.nav.helse.flex.repository.FeedbackDbRecord
import no.nav.helse.flex.repository.FeedbackRepository
import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@SpringBootApplication
class DevApplication

@Service
@Profile("dev")
class FakeClientIdValidation() : ClientIdValidation {
    override fun validateClientId(app: ClientIdValidation.NamespaceAndApp) {
    }

    override fun validateClientId(apps: List<ClientIdValidation.NamespaceAndApp>) {
    }
}

@Service
@Profile("dev")
class FakeTokenValidationContextHolder : TokenValidationContextHolder {
    override fun getTokenValidationContext(): TokenValidationContext {
        TODO("Not yet implemented")
    }

    override fun setTokenValidationContext(tokenValidationContext: TokenValidationContext?) {
        TODO("Not yet implemented")
    }
}

@Service
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

fun main(args: Array<String>) {
    PostgreSQLContainer14().apply {
        // Cloud SQL har wal_level = 'logical' på grunn av flagget cloudsql.logical_decoding i
        // naiserator.yaml. Vi må sette det samme lokalt for at flyway migrering skal fungere.
        withCommand("postgres", "-c", "wal_level=logical")
        start()
        System.setProperty("spring.datasource.url", "$jdbcUrl&reWriteBatchedInserts=true")
        System.setProperty("spring.datasource.username", username)
        System.setProperty("spring.datasource.password", password)
    }

    // Bruk port 80
    System.setProperty("server.port", "80")
    System.setProperty("spring.profiles.active", "dev")
    runApplication<DevApplication>(*args)
}
