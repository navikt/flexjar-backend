package no.nav.helse.flex

import no.nav.helse.flex.repository.FeedbackRepository
import no.nav.helse.flex.testoppsett.startPostgresContainer
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.MockMvcPrint
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import java.util.*

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

    @Autowired
    lateinit var feedbackRepository: FeedbackRepository

    @AfterAll
    fun `Vi resetter databasen`() {
        feedbackRepository.deleteAll()
    }

    companion object {
        init {
            startPostgresContainer()
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
    ): String =
        server
            .issueToken(
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

fun MockOAuth2Server.token(
    subject: String,
    issuerId: String,
    clientId: String = UUID.randomUUID().toString(),
    audience: String,
    claims: Map<String, Any> = mapOf("acr" to "idporten-loa-high"),
): String =
    this
        .issueToken(
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

fun MockOAuth2Server.buildAzureClaimSet(
    clientId: String,
    issuer: String = "azureator",
    azpName: String,
    audience: String = "flexjar-backend-client-id",
): String {
    val claims = HashMap<String, String>()
    claims.put("azp_name", azpName)
    return token(
        subject = "whatever",
        issuerId = issuer,
        clientId = clientId,
        audience = audience,
        claims = claims,
    )
}

fun FellesTestOppsett.skapAzureJwt(
    azpName: String = "dev-gcp:flex:flexjar-frontend",
    clientId: String = "flexjar-frontend-client-id",
) = server.skapAzureJwt(clientId = clientId, azpName = azpName)

fun MockOAuth2Server.skapAzureJwt(
    azpName: String = "dev-gcp:flex:flexjar-frontend",
    clientId: String = "flexjar-frontend-client-id",
) = buildAzureClaimSet(clientId = clientId, azpName = azpName)
