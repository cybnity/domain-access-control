# AC-2(8) DYNAMIC ACCOUNT MANAGEMENT

Feature Type: security control

Requirement: [defined specification](https://www.notion.so/cybnity/AC-2-8-Dynamic-account-management-72d42a96723c48e5b254c59fd24b6bc8?pvs=4)

## Account Registration Flow

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
    },
    'sequence': {
		'mirrorActors': false
    }
  }
}%%

sequenceDiagram
  actor Person
  participant SignUpWebUI as <<Javascript View>><br>Sign Up UI
  participant AccessControlJSAdapter as <<JS Library>><br>AccessControlJSAdapter
  participant IdentityServer as <<Keycloak IAM>><br>IdentityServer
  participant AuthorizationServer as <<Keycloak UAM>><br>AuthorizationServer
  participant IAMDB as <<Keycloak Identities DB>><br>IdentityRepository
  participant UAMDB as <<Keycloak Accounts/Roles/SSOTokens DB>><br>AccountRepository
  participant AccessControlJavaAdapter as <<Keycloak Connector>><br>AccessControlJavaAdapter
  participant ACBackendServer as <<Reactive Backend Server>><br>ACBackendServer
  Person->>SignUpWebUI: signUp(tenantID, mailAddress, firstName, lastName...)
  SignUpWebUI->>AccessControlJSAdapter: createAccount(tenantID, identity...)
  AccessControlJSAdapter->>IdentityServer: addIdentity(tenantID, identity description)
  IdentityServer->>IAMDB: findIdentity(tenandID, identity...)
	alt "existing identity"
		IAMDB-->>IdentityServer: previous subject identity attributes
	else "unknow identity"
		IAMDB->>IAMDB: createIdentity(tenantID, identity...)
		IAMDB-->>IdentityServer: new subject identity attributes
		IdentityServer-->>AccessControlJavaAdapter: notify(new IdentityRegisteredEvent(tenantID, identity...))
		AccessControlJavaAdapter-)ACBackendServer: notify(identityRegisteredEvent)
		IdentityServer->>AuthorizationServer: addAccount(tenantID, identity)
		AuthorizationServer->>UAMDB: findAccount(tenantID, identity...)
		alt "existing account"
			alt "active account"
				par
					AuthorizationServer-->>IdentityServer: existing authenticated active account, roles and permissions
			  and
					AuthorizationServer-->>AccessControlJavaAdapter: notify(new UserAccountAuthentifiedEvent(tenantID, account...))
					AccessControlJavaAdapter-)ACBackendServer: notify(userAccountAuthentifiedEvent)
				end
				IdentityServer-->>AccessControlJSAdapter: existing authenticated account
				AccessControlJSAdapter-->>SignUpWebUI: account description
			else "deactived || expired account"
				par
					AuthorizationServer-->>IdentityServer: notify(new AccountDeactivedEvent(tenantID, account id))
					IdentityServer-->>AccessControlJSAdapter: notify(new UnusableExistingAccountEvent(tenantID, cause))
					AccessControlJSAdapter->>SignUpWebUI: notify(new AccountCreationRejectedEvent(tenantID, cause))
				and
					AuthorizationServer-->>AccessControlJavaAdapter: notify(new UnusableAccountAuthenticationAttemptedEvent(tenantID, account id))
					AccessControlJavaAdapter-)ACBackendServer: notify(unusableAccountAuthenticationAttemptedEvent)
				end
			end
		else "unknown account"
			AuthorizationServer->>UAMDB: createAccount(tenantID, identity..., default roles, default permissions...)
			par
				AuthorizationServer-->>IdentityServer: new user account
			  IdentityServer-->>AccessControlJSAdapter: assigned account
			  AccessControlJSAdapter-->>SignUpWebUI: account description
			and
				AuthorizationServer-->>AccessControlJavaAdapter: notify(new UserAccountRegisteredEvent(tenandID, account...))
				AccessControlJavaAdapter-)ACBackendServer: notify(userAccountCreatedEvent)
			end
		end
	end
	alt "success available account"
	  SignUpWebUI-->>Person: success registration notification
	else "failure event"
		SignUpWebUI-->>Person: rejection cause notification
	end

```

#
[Back To Home](README.md)
