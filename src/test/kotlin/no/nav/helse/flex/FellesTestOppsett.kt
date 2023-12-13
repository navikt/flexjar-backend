package no.nav.helse.flex

import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.testcontainers.containers.PostgreSQLContainer
import java.util.*

private class PostgreSQLContainer14 : PostgreSQLContainer<PostgreSQLContainer14>("postgres:14-alpine")

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureObservability
@EnableMockOAuth2Server
@SpringBootTest(classes = [Application::class])
@AutoConfigureMockMvc(print = MockMvcPrint.NONE, printOnlyOnFailure = false)
abstract class FellesTestOppsett {
    @Autowired
    lateinit var server: MockOAuth2Server

    @Autowired
    lateinit var mockMvc: MockMvc

    companion object {
        init {
            PostgreSQLContainer14().apply {
                // Cloud SQL har wal_level = 'logical' på grunn av flagget cloudsql.logical_decoding i
                // naiserator.yaml. Vi må sette det samme lokalt for at flyway migrering skal fungere.
                withCommand("postgres", "-c", "wal_level=logical")
                start()
                System.setProperty("spring.datasource.url", "$jdbcUrl&reWriteBatchedInserts=true")
                System.setProperty("spring.datasource.username", username)
                System.setProperty("spring.datasource.password", password)
            }
        }
    }

    fun tokenxToken(
        fnr: String = "12345678910",
        audience: String = "flexjar-backend-client-id",
        issuerId: String = "tokenx",
        clientId: String = "dev-gcp:flex:spinnsyn-frontend",
        claims: Map<String, Any> =
            mapOf(
                "acr" to "idporten-loa-high",
                "idp" to "idporten",
                "client_id" to clientId,
                "pid" to fnr,
            ),
    ): String {
        return server.issueToken(
            issuerId,
            clientId,
            DefaultOAuth2TokenCallback(
                issuerId = issuerId,
                subject = UUID.randomUUID().toString(),
                audience = listOf(audience),
                claims = claims,
                expiry = 3600,
            ),
        ).serialize()
    }
}

fun MockOAuth2Server.token(
    subject: String,
    issuerId: String,
    clientId: String = UUID.randomUUID().toString(),
    audience: String,
    claims: Map<String, Any> = mapOf("acr" to "idporten-loa-high"),
): String {
    return this.issueToken(
        issuerId,
        clientId,
        DefaultOAuth2TokenCallback(
            issuerId = issuerId,
            subject = subject,
            audience = listOf(audience),
            claims = claims,
            expiry = 3600,
        ),
    ).serialize()
}

fun FellesTestOppsett.buildAzureClaimSet(
    subject: String,
    issuer: String = "azureator",
    audience: String = "flexjar-backend-client-id",
): String {
    val claims = HashMap<String, String>()

    return server.token(
        subject = "Vi sjekker azp",
        issuerId = issuer,
        clientId = subject,
        audience = audience,
        claims = claims,
    )
}

fun FellesTestOppsett.skapAzureJwt(subject: String = "flexjar-frontend-client-id") = buildAzureClaimSet(subject = subject)
