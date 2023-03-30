# PURPOSE
Presentation of design models and documentation regarding the Access Control Adaptations sub-projects.

The main principles and specifications supported by the API are:
- OAuth2 protocol defined by [RFC6749](https://tools.ietf.org/html/rfc6749) to securize the APIs access (e.g messaging gateway systems) without user authentication (only based on authorization)
- [OpenID Connect](https://openid.net/connect/) identity layer (more detail on [Core documentation website](https://openid.net/specs/openid-connect-core-1_0.html) which enhance the OAuth2 standard to securize authentication with user identity check (UIAM)

OpenID Connect is selected as default implementation supported by the Access Control Adapter projects (api and impl libraries).

More detail about supported protocols and formats can be found on:
- [JSON Web Token (JWT) RFC 7519](https://tools.ietf.org/html/rfc7519)
- ID Token are used to support the exchanged data integrity (signature and encryption functions)

# ACCESS CONTROL ADAPTER API
This is a **specification sub-project** defining the capabilities and DTO object which can be reuse to have interaction with the Access Control domain (e.g from other domains, from UI layer).


# ACCESS CONTROL ADAPTER IMPL
This is an **implementation sub-project** defining the capabililties implementation of `ac-adapter-api` which encapsulate the integration with compatible third-party solutions (e.g Keycloak) as a client adapter.

The implementation library built by this sub-project is a client adapter which can be reuse by any other CYBNITY applicative or technical component that require interaction with the UIAM module deployed into a CYBNITY software suite environment.


## Integration
The default integration implemented is based on Keycloak (see [Keycloak documentation](https://www.keycloak.org/docs/latest/securing_apps/) for developer additional help and [managed token types](https://www.keycloak.org/docs/latest/securing_apps/#_token-exchange)).

Specific OpenID connect libraries are encapsulated/reused by CYBNITY application components according to:
- Keycloak JavaScript adapter: by web UI executed into a user's HTML5/JS browser instance (e.g accessing to Keycloak authorization server over a reverse proxy service, using access token reference and session id token) and CYBNITY UI element.
- Keycloak Node.js adapter: by server-side front end user interface providers (e.g domain ui modules) developed with Node.js, which requires support of accesses control (e.g to provide a server-side capability, view or static contents).
- Keycloak Java adapter: by server-side java components requiring accesses control support and users identities management.

## Provisioning
The setting resources (e.g parameters to connect with the third-party solution) required during an instantiation of an adapter is not managed into this project, but is dynamically read (e.g via environment variable read, or injection of resources by the application modules using the adapter instance) during the runtime.

None statefull configuration shall be implemented (e.g no Maven filtered files) into this sub-project.
