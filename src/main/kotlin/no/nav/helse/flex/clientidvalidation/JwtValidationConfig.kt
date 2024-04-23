package no.nav.helse.flex.clientidvalidation

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("default")
@EnableJwtTokenValidation
class JwtValidationConfig
