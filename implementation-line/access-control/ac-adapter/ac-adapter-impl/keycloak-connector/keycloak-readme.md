## PURPOSE
Presentation of the minimum configuration actions to implement into a started Keycloak instance (provided as access-control-sso-system module hosted into a Kubernetes cluster).

# PREREQUISITE
- A Keycloack server instance is running (started via Helm chart into a K8s cluster), supported by a Postgresql service (storage of user accounts)
- An admin account is automatically created and ready for use (authentication ready from user=admin and password=admin) as defined in Helm project settings (see access-control-sso-system's [values.yaml file](/implementation-line/systems/charts/access-control-sso-system/values.yaml))
- The Keycloak http access is forwarded from external of Cluster via an active Linux process ('kubectl port-forward --namespace default svc/access-control-sso-system 8080:81' shell command executed)
- The Keycloak web application instance is accessible from web browser at http://127.0.01:8080/ via account:
    - login = admin
    - password = admin

# INTEGRATION WITH FRONTEND UI
Manage several tasks from the accessed Keycloak's adminisation console.

## REALM REGISTRATION
Create a Realm similar to a TenantId relative to an organization (e.g named cybnity) from the top-left select menu with click on `Create Realm` button and switch on the new created real area.

### Realm configuration
From **Realm Settings** created:
- Frontend URL:
  - Define the external (e.g url and port exposed outside the K8s cluster) of Keycloak realm
- Security Defenses to configure the Clickjacking security
  - Default `SAMEORIGIN` value of **X-Frame-Options**
    - See https://datatracker.ietf.org/doc/html/rfc7034#section-2.2.1 for more details about X-Frame-Options
    - See https://wjw465150.gitbooks.io/keycloak-documentation/content/server_admin/topics/threat/clickjacking.html for help about mitigation of Clickjacking
  - Default `frame-src 'self'; frame-ancestors 'self'; object-src 'none';` value of **Content-Security-Policy**
    - See https://www.w3.org/TR/CSP/#directive-frame-src about frame-src to restrict the URLS which may be loaded into nested browsing contexts
    - See https://www.w3.org/TR/CSP/#directive-frame-ancestors about frame-ancestors to define the URLs which can embed the resource using frame of iframe
    - See https://www.w3.org/TR/CSP/#directive-object-src about object-src to restrict URLS from which plugin context may be loaded

## SYSTEMS' CLIENTS REGISTRATION

### Web Reactive Frontend System client
#### Creation
Register a new Keycloak client dedicated to frontend module (allowing user authentication from web browser when access to web UI provided by the **web-reactive-frontend-system** that is running into the K8s cluster):
- Navigate to **Clients** and use **Create client** button for add a new client
- Set client to create's description about web app source to integrate with Keycloak login:
  - General Settings
    - Client type: `OpenID Connect`
      (allows Clients to verify the identity of the End-User based on the authentication performed by an Authorization Server)
    - Client ID: `web-reactive-frontend-system`
      (specifies ID referenced in URI and tokens)
    - Name: `Web Reactive Frontend Client`
      (specifies display name of the client)
    - Description: `OpenID client supporting the user interface frontend systems`
    - Always display in console: `ON`
      (always list this client in the Account Console, even if the user does not have an active session)
  - Capability config
    - Client authentication: `OFF`
      (this defines the type of the OIDC client. When it's ON, the OIDC type is set to confidential access type. When it's OFF, it is set to public access type; mandatory defined as public allowing authentication via JS client executed on client-side via web browser of frontend UI)
    - Authorization: `OFF`
      (Enable/Disable fine-grained authorization support for a client)
    - Authentication flow:
      - Standard Flow: `ON`
        (enables standard OpenID Connect redirect based authentication with authorization code. In terms of OpenID Connect or OAuth2 specifications, this enables support of 'Authorization Code Flow' for this client)
      - Direct access grants Enabled: `ON`
        (means that client has access to username/password of user and exchange it directly with Keycloak server for access token. In terms of OAuth2 specification, this enables support of 'Resource Owner Password Credentials Grant' for this client)

- Complete the created client details
  Don't forget to SAVE any changed information via **Save** button!
  - Settings
    - Access settings
      - Root URL: `${authBaseUrl}`
        (defined according to the external port exposed by the web-reactive-frontend-system module executed into the K8s cluster. Root URL appended to relative URLs)
      - Home URL: `/realms/CYBNITY/account/`
      - Valid Redirect URIs: `/*`
        (valid URI pattern a browser can redirect to after a successful login. Simple wildcards are allowed such as `http://example.com/*`. Relative path can be specified too such as `/my/relative/path/*`. Relative paths are relative to the client root URL, or if none is specified the auth server root URL is used)
      - Valid post logout redirect URIs: `+`
      - Web Origins: `+`
        (allowed CORS origins. To permit all origins of Valid Redirect URIs, add '+')
      - Admin URL: `${authBaseUrl}`
        (URL to the admin interface of the client. Set this if the client supports the adapter REST API. This REST API allows the auth server to push revocation policies and other administrative tasks. Usually this is set to the base URL of the client)
    - Login settings
      - Login theme: `base`
    - Logout settings
      - Front channel logout: `ON`
      - Backchannel logout session required: `ON`

#### Applicative role definition
From **Roles** section, add a new standard role named `user` described as `standard user role of the frontend user interface`.

From **Realm Roles** menu, add a new realm role named `tenant-user` via the **Create role** button:
- Role name: `tenant-user`
- Description: `Standard role of a user role (e.g frontend web user interface, backend api system) authorized to be used into the CYBNITY tenant context`

A composite role is a role that has one or more additional roles associated with it. When a composite role is mapped to a user, the user gains the roles associated with the composite role.

From new created realm role, define associated roles (filter by clients) via the **Action > Add associated roles** top-right button:
- Find and select the `web-reactive-frontend-system` item
- Assign it to the `tenant-user` account type

#### Client scope creation
If there are many applications to secure and register within the organization (e.g multi tenant), it can become tedious to configure role scope mappings for each of these systems' clients. Keycloak allows to define a shared client configuration in an entity called a client scope.
To get client roles as a custom key in the JWT token, add client scope to put client roles in access token.

From **Client Scopes**, create a new scope via the **Create client scope** button:
- Name: `ui-layer-systems-roles`
  (name of the client scope. Must be unique in the realm. Name should not contain space characters as it is used as value of scope parameter)
- Description: `Shared configuration of clients used by systems executed in the UI layer`
- Type: `Default`
- Protocol: `OpenID Connect`
  (SSO protocol configuration is being supplied by this client scope)
- Display on consent screen: `ON`
- Include in token scope: `ON`
  (when on, the name of this client scope will be added to the access token property 'scope' as well as to the Token Introspection Endpoint response. If off, this client scope will be omitted from the token and from the Token Introspection Endpoint response)

#### Generated setting files
The generated client setting resulting of this settings should be equals (see it via the top-right **Action > Export** menu) to [client configuration file](web-reactive-frontend-system-keycloak.json).

### Reactive Backend System client
Register a new Keycloak client dedicated to backend messaging gateway module (allowing gateway api usage of services since the web browser UI components, and that are exposed by the **reactive-backend-system** that is running into the K8s cluster):
- Navigate to **Clients** and use **Create client** button for add a new client
- Set client to create's description about web app source to integrate with Keycloak sso checking:
  - General Settings
    - Client type: `OpenID Connect`
      (allows Clients to verify the identity of the api users based on the authentication performed by an Authorization Server)
    - Client ID: `reactive-backend-system`
      (specifies ID referenced in URI and tokens)
    - Name: `Reactive Backend Client`
      (specifies display name of the client)
    - Description: `OpenID client supporting the api exposure of backend messaging gateway systems`
    - Always display in console: `ON`
      (always list this client in the Account Console, even if the user does not have an active session)
  - Capability config
    - Client authentication: `ON`
      (this defines the type of the OIDC client. When it's ON, the OIDC type is set to confidential access type reserved to only server-side system instances)
    - Authorization: `ON`
      (Enable/Disable fine-grained authorization support for a client)
    - Authentication flow:
      - Standard Flow: `ON`
        (enables standard OpenID Connect redirect based authentication with authorization code. In terms of OpenID Connect or OAuth2 specifications, this enables support of 'Authorization Code Flow' for this client)
      - Direct access grants Enabled: `ON`
        (means that client has access to username/password of user and exchange it directly with Keycloak server for access token. In terms of OAuth2 specification, this enables support of 'Resource Owner Password Credentials Grant' for this client)
      - Implicit flow: `ON`
        (enables support for OpenID Connect redirect based authentication without authorization code, in terms of OpenID Connect or OAuth2 specifications)
      - Service accounts roles: `ON`
        (allows to authenticate this client to Keycloak and retrieve access token dedicated to this client. In terms of OAuth2 specification, this enables support of 'Client Credentials Grant' for this client)

- Complete the created client details
  Don't forget to SAVE any changed information via **Save** button!
  - Settings
    - Access settings
      - Root URL: `${authBaseUrl}`
        (defined according to the external port exposed by the reactive-backend-system module executed into the K8s cluster. Root URL appended to relative URLs)
      - Home URL: `/realms/CYBNITY/account/`
      - Valid Redirect URIs: `/*`
        (valid URI pattern a browser can redirect to after a successful login. Simple wildcards are allowed such as `http://example.com/*`. Relative path can be specified too such as `/my/relative/path/*`. Relative paths are relative to the client root URL, or if none is specified the auth server root URL is used)
      - Valid post logout redirect URIs: `+`
      - Web Origins: `+`
        (allowed CORS origins. To permit all origins of Valid Redirect URIs, add '+')
      - Admin URL: `${authBaseUrl}`
        (URL to the admin interface of the client. Set this if the client supports the adapter REST API. This REST API allows the auth server to push revocation policies and other administrative tasks. Usually this is set to the base URL of the client)
    - Login settings
      - Login theme: `base`
    - Logout settings
      - Front channel logout: `OFF`
      - Backchannel logout session required: `ON`
    - Advanced
      - OpenID Connect Compatibility Modes
        - Use refresh tokens: `ON`
          (when is on, a refresh_token will be created and added to the token response. If this is off then no refresh_token will be generated)
      - Advanced settings
        - Access token lifespan: `Expires in 10 minutes`
        (max time before an access token is expired. This value is recommended to be short relative to the SSO timeout)
      - Authentication flow overrides
        - Direct grant flow: `direct grant`
        (define the flow you want to use for direct grant authentication)
    - Credentials
      - Client Authenticator: `Client Id and Secret`
      - Generate client secret and registration access token from dedicated buttons
    - Client scopes
      - Set assigned type to the reactive-backend-system-dedicated item

#### Client roles mapping
From **Client Scopes > ui-layer-systems-roles**:
- Mappers
  - Create a new mapper since the **Configure a new mapper** button:
    - Choose `User Client Role` mapper type
    - Name: `ui-clients-role`
    - Client ID: `web-reactive-frontend-system`
      (client ID for role mappings. Just client roles of this client will be added to the token. If this is unset, client roles of all clients will be added to the token)
    - Multivalued: `ON`
    - Token Claim Name: `resource_access.role`
      (name of the claim to insert into the token. This can be a fully qualified name like 'address.street'. In this case, a nested json object will be created)
    - Claim JSON Type: `String`
    - Add to ID token: `ON`
    - Add to access token: `ON`
    - Add to userinfo: `ON`

  - Create a new mapper since the **Add mapper > By configuration** button:
    - Choose `User Realm Role`
    - Name: `ui-realm-role`
    - Multivalued: `ON`
    - Token Claim Name: `realm.role`
    - Claim JSON Type: `String`
    - Add to ID token: `ON`
    - Add to access token: `ON`
    - Add to userinfo: `ON`
- Scope
  - Make new assignment of `tenant-user` role to `ui-layer-systems-roles` account

From **Clients > web-reactive-frontend-system > Client Scopes** panel, add the custom scope previously created via the **Add client scope** button:
- Select `ui-layer-systems-roles` from the items list, and assign it as `Default`

Now, we can get the client roles from the JWT token with **resource_access.role** key, allowing to enable/disable web-reactive-frontend-system's view (e.g UI component, functions visibility) to particular roles received from the token.

From **Clients > reactive-backend-system > Client Scopes** panel, add the custom scope previously created via the **Add client scope** button:
- Select `ui-layer-systems-roles` from the items list, and assign it as `Default`

#### Applicative role definition
From **Real roles** menu, complete the **tenant-user** existing composite realm role, with definition of additional associated role (filter by clients) via the **Associated roles** section's **Assign role** button:
- Find and select the `reactive-backend-system` item
- Assign it to the `tenant-user` account type

#### Generated setting files
The generated client setting resulting of this settings should be equals (see it via the top-right **Action > Export** menu) to [client configuration file](reactive-backend-system-keycloak.json).

### Tests

#### Tester account
By default, new created Realm has none user.

Create a test account (e.g dedicated to the frontend application test) declared regarding a tester allowing to use the web-reactive-frontend-system web UI:
- Add a user account sample from the **Users** menu as:
  - Username: `tester`
  - User enabled: `true`
- From **Users > tester** account item:
  - **Details** section:
    - Toggle the **Email verified** as `ON` and save (simulate that email address verification procedure have been performed with success without need of mail smtp server setting ;) )
    - Define a First and last names
  - **Credentials** section:
    - Set a password for the user account and toggle `Temporary` to `OFF`
    - Validate password creation
  - **Role Mapping** section:
      - Assign the `tenant-user` realm role to the user allowing him to have automatically assigned mapped role defined for each client

#### User account authentication check
When disconnected of any user account:
- Open `http://localhost:8080/realms/cybnity/account/` from a web browser:
  - The account management UI is shown including panels of Keycloak features
  - Click on `Personal info` for try to manage personal basic informations
    - The standard Sign page is shown allowing to authenticate the test user account
    - Re-use the Tester account credential for try authentication
    - When authenticated, the personal information screen is shown with success
