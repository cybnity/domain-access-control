# AC-3 USER ACCESS ENFORCEMENT

Feature Type: security control

Requirement: [defined specification](https://www.notion.so/cybnity/AC-3-User-Access-Enforcement-d836d0fabdaf4fc5b6d22dcbd5e551f8?pvs=4)

## Authentication flow

```mermaid
%%{
  init: {
    'theme': 'base',
    'themeVariables': {
        'background': '#ffffff',
        'fontFamily': 'arial',
        'fontSize': '18px',
        'primaryColor': '#fff',
        'primaryBorderColor': '#0e2a43',
        'secondaryBorderColor': '#0e2a43',
        'tertiaryBorderColor': '#0e2a43',
        'edgeLabelBackground':'#0e2a43',
        'lineColor': '#0e2a43',
        'tertiaryColor': '#fff'
    }
  }
}%%

sequenceDiagram
	actor Person
	participant SignInWebUI as <<Javascript View>><br>Sign In UI
	participant AccessControlJSAdapter as <<JS Library>><br>AccessControlJSAdapter
  participant IdentityServer as <<Keycloak IAM>><br>IdentityServer
  participant AuthorizationServer as <<Keycloak UAM>><br>AuthorizationServer
	participant IAMDB as <<Keycloak Identities DB>><br>IdentityRepository
	participant UAMDB as <<Keycloak Accounts/Roles/SSOTokens DB>><br>AccountRepository
  participant AccessControlJavaAdapter as <<Keycloak Connector>><br>AccessControlJavaAdapter
	participant ACBackendServer as <<Reactive Backend Server>><br>ACBackendServer
	Person->>SignInWebUI: signIn(credentials)
	SignInWebUI->>AccessControlJSAdapter: authenticate(credentials[])
	AccessControlJSAdapter->>AuthorizationServer: getAccessToken(credentials[])
	AuthorizationServer->>UAMDB: findAccount(tenantID, account)
	alt "unactive || not found account"
		AuthorizationServer-->>AccessControlJSAdapter: notify(new ActiveAccountNotFoundEvent(tenantID))
		AccessControlJSAdapter-->>SignInWebUI: notify(activeAccountNotFoundEvent)
		SignInWebUI-->>Person: rejection cause notification
	else "found active account"
		AuthorizationServer->>AuthorizationServer: createSignedIDToken(user claims, user account habilitations, tenantID)
		AuthorizationServer->>UAMDB: saveToken(user token)
		par
			AuthorizationServer-->>AccessControlJSAdapter: authorized and signed token
			AccessControlJSAdapter-->>SignInWebUI: valid token usable to expiration time
			SignInWebUI-->>Person: authorized access notification
		and
			AuthorizationServer-->>AccessControlJavaAdapter: notify(new AuthorizedAccessEvent(user token))
			AccessControlJavaAdapter-)ACBackendServer: notify(authorizedAccess)
		end
	end

```

#
[Back To Home](README.md)
