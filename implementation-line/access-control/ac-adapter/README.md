# PURPOSE
Presentation of design models and documentation regarding the Access Control Adaptations sub-projects.

The main principles and specifications supported by the API are:
- OAuth2 protocol defined by [RFC6749](https://tools.ietf.org/html/rfc6749) to securize the APIs access (e.g messaging gateway systems) without user authentication (only based on authorization)
- [OpenID Connect](https://openid.net/connect/) identity layer (more detail on [Core documentation website](https://openid.net/specs/openid-connect-core-1_0.html) which enhance the OAuth2 standard to securize authentication with user identity check (UIAM)

OpenID Connect is selected as default implementation supported by the Access Control Adapter projects (api and impl libraries).

More detail about supported protocols and formats can be found on:
- [JSON Web Token (JWT) RFC 7519](https://tools.ietf.org/html/rfc7519)
- ID Token are used to support the exchanged data integrity (signature and encryption functions)

# ACCESS CONTROL APIs
These are **specification sub-projects** defining the capabilities and DTO object which can be reuse to have interaction with the Access Control domain (e.g from other domains, from UI layer).

Several dedicated libraries are maintained according to their authorized usage into the CYBNITY other systems:
- [ac-adapter-api](ac-adapter-api): User Authentication & Authorization Management (UAM) interface contracts regarding common services provided as __common Access Control capabilities__. Can include specification perimeter relative to specific user types covering the concepts of CIAM (Customer Identity & Access Management) and common IAM (Identity & Access Management; usable for human or system identification or personal information management relative to authorized accounts). For example, this library provide authentication of user and authorization features allowing adding of Single-Sign On capability to any CYBNITY system endpoints (e.g from a domain gateway).
- [ac-adapter-admin-api](ac-adapter-admin-api): Identify & Access Management (IAM) interface contracts regarding administration and/or supervision services relative to access controls (e.g creation of realms for multi-tenants support; automated initialization of SSO client per real ensuring dynamic configuration of UIAM for dedicated company). The provided services are focused on administration features of SSO system (e.g Keycloak).

# ACCESS CONTROL ADAPTER IMPLs
These are **implementation sub-projects** defining the capabilities realization of AC Adapter APIs, which encapsulate the integration with compatible third-party solutions (e.g Keycloak) as client adapter impl components.

## KEYCLOAK INTEGRATION
The default integration implemented is based on Keycloak (see [Keycloak documentation](https://www.keycloak.org/docs/latest/securing_apps/) for developer additional help and [managed token types](https://www.keycloak.org/docs/latest/securing_apps/#_token-exchange)).

Specific OpenID connect libraries are encapsulated/reused by CYBNITY application components according to software language supported:
- Keycloak JavaScript adapters: by web UI executed into a user's HTML5/JS browser instance (e.g accessing to Keycloak authorization server over a reverse proxy service, using access token reference and session id token) and CYBNITY UI element.
- Keycloak Node.js adapters: by server-side front end user interface providers (e.g domain ui modules) developed with Node.js, which requires support of accesses control (e.g to provide a server-side capability, view or static contents).
- Keycloak Java adapters: by server-side java components requiring access control support and users identities management. It manages the coupling with Keycloak server-side services and exchanged objects via the Keycloak Java APIs (see library api [javadoc](https://www.keycloak.org/docs-api/25.0.4/javadocs/index.html) which provide abstract and implementation components used by the security domains provided by Keycloak solution.

### Java Connectors
These implementation sub-projects build client adapters which can be reuse by any other CYBNITY applicative or technical component that require interaction with the UIAM module deployed into a CYBNITY software suite environment:
- [ac-adapter-keycloak-impl](ac-adapter-keycloak-impl): implementation components that are compatible to Keycloak endpoints and API, and support realization of the capabilities (e.g communication protocol, data transformation) with contribution/delegation of Keycloak server system. This __implementation library__ is supporting the scope of `ac-adapter-api` as __common features__ usable from any CYBNITY architecture layer. For example, the authentication features are realized by this library in a SSO protocol implemented for authentication and authorization of a user or system.
- [ac-adapter-keycloak-admin-impl](ac-adapter-keycloak-admin-impl): implementation components that are managing the integration and support with Keycloak Admin REST API. This __implementation library__ is supporting the scope of `ac-adapter-admin-api` as __IAM administration features__ usable only from CYBNITY Access Control domain that is deployed into secure layers (e.g server-side system executed and deployed into protected area). For example, this library is usefull for TenantRegistrationService ensuring the creation of independant Realm scopes dedicated per organization.

### Javascript Connectors

### Node.js Connectors

# INTEGRATION PROTOCOL TRANSLATORS

## JAVA TRANSLATION COMPONENTS
### Domain Objects Translation
The [ac-translator/keycloak-translator](/implementation-line/access-control/ac-translator/keycloak-translator) project manage the hidding and mapping of the Keycloak API library's data structures (e.g representing DTO exposed by the Keycloak API's services) with components external (e.g CYBNITY applicative module of other domains which have interactions with the Keycloak UIAM system) to the CYBNITY Access Control domain.

# ENVIRONMENT PROVISIONING
The setting resources (e.g parameters to connect with the third-party solution) required during the instantiation of an adapter is not managed into its dedicated project, but is __dynamically read__ (e.g via environment variables read, or through injection of resources by the application modules using the adapter instance) during the runtime.

None statefull configuration shall be implemented (e.g no Maven filtered files) into any component sub-project. See `iac-helm-charts` CYBNITY repository dedicated to the Infrastructure-As-Code build and delivery, including the implementation of settings required by CYBNITY systems or components.

## KEYCLOAK PROVISIONING
See the [documentation](ac-adapter-keycloak-impl/keycloak-readme.md) regarding the development of provisioned Keycloak system during development phase.

