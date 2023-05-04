# flexjar-backend

Applikasjon for lagring av brukerfeedback. Dataene lagres i en postgres database.

Feedbacken kommer fra ulike brukerrettede applikasjoner som er eid av team flex. F.eks. spinnsyn, sykepengesøknad og ditt sykefravær.

Formatet på feedbackend er relativt genereisk, for øyeblikket er APIet designet rundt å få en tekstlig kvalitative tilbakemelding fra brukeren.

Appen eksponerer et API til flexjar-frontend. Her kan medlemmer fra team flex se feedbacken. Det er også et eget API for å slette feedback som vi ikke ønsker å ha lagret, f.eks. på grunn av personopplysninger.

## Henvendelser

Spørsmål knyttet til koden eller prosjektet kan stilles til `flex@nav.no`

## For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen `#flex`.