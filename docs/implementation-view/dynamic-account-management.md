# AC-2(8) DYNAMIC ACCOUNT MANAGEMENT

Feature Type: security control

Requirement: [defined specification](https://www.notion.so/cybnity/AC-2-8-Dynamic-account-management-72d42a96723c48e5b254c59fd24b6bc8?pvs=4)

Keycloack documentation:
- [Notified Events](https://wjw465150.gitbooks.io/keycloak-documentation/content/server_admin/topics/events/login.html)

## Tenant (Realm) Registration Flow

RealmRepresentation that is dynamically defined and include all the settings regarding the client scopes and security information required for future connections to Keycloak realm by the CYBNITY backend components (e.g client scopes).

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
  participant OrganizationRegistrationWebUI as <<Javascript View>><br>Organization Registration UI
  participant AccessControlJSAdapter as <<JS Library>><br>AccessControlJSAdapter
  participant ACBackendServer as <<Reactive Backend Server>><br>ACBackendServer
  participant ACDomainGatewayServer as <<Access Control Process Module>><br>ACDomainGatewayServer
  participant AccessControlJavaAdapter as <<Keycloak Connector>><br>AccessControlJavaAdapter
  participant IdentityServer as <<Keycloak IAM>><br>IdentityServer
  participant SignUpWebUI as <<Javascript View>><br>Sign Up UI
  participant DomainsInteractionSpace as <<DIS System>><br>DomainsInteractionSpace
  Person->>OrganizationRegistrationWebUI: signUp(organizationName)
  OrganizationRegistrationWebUI->>AccessControlJSAdapter: createOrganization(organizationName)
  AccessControlJSAdapter->>ACBackendServer: execute(new RegisterOrganization(organizationNaming))
  ACBackendServer->>ACDomainGatewayServer: execute(new RegisterOrganization(organizationNaming))
  ACDomainGatewayServer->>AccessControlJavaAdapter: findTenant(String organizationNaming)
  AccessControlJavaAdapter->>IdentityServer: realm(String realmName)
  alt "existing tenant"
	IdentityServer-->>AccessControlJavaAdapter: RealmResource (existing equals organization named)
	AccessControlJavaAdapter-->>ACDomainGatewayServer: Tenant(existing equals organization named)
	ACDomainGatewayServer-->>ACBackendServer: Tenant(existing equals organization named)
	ACBackendServer-->> AccessControlJSAdapter: rejected creation for cause of existing named organization
	AccessControlJSAdapter-->>OrganizationRegistrationWebUI: refused creation for cause
	OrganizationRegistrationWebUI-->>Person: rejection cause notification
  else "unknow tenant"
	IdentityServer-->>AccessControlJavaAdapter: unknown realm
	AccessControlJavaAdapter-->>ACDomainGatewayServer: none equals tenant
	ACDomainGatewayServer->>AccessControlJavaAdapter: createTenant(String organizationName)
	AccessControlJavaAdapter->>AccessControlJavaAdapter: buildRealmRepresentation(configurationSettings)
	AccessControlJavaAdapter->>IdentityServer: create(RealmRepresentation organizationRealm)
	IdentityServer-->>AccessControlJavaAdapter: success realm registration confirmation (and access control settings recorded)
	AccessControlJavaAdapter-->>ACDomainGatewayServer: new TenantRegistered(success created tenant description)
	par
		ACDomainGatewayServer->>DomainsInteractionSpace: execute(new AddTenant(tenant description, new TenantConnectorConfiguration(tenant regarding future Keycloack adapter client connections via dedicated realm's clientscope setting)))
	and
		ACDomainGatewayServer->>AccessControlJavaAdapter: findTenant(String organizationNaming)
		AccessControlJavaAdapter->>IdentityServer: realm(String realmName)
		IdentityServer-->>AccessControlJavaAdapter: RealmResource (existing equals organization named)
		AccessControlJavaAdapter-->>ACDomainGatewayServer: Tenant(found organization description)
		ACDomainGatewayServer->>ACDomainGatewayServer: prepare new OrganizationRegistered(new created tenant description) for domain storage notification and confirmation send
		par
			ACDomainGatewayServer->>DomainsInteractionSpace: [received keycloack admin event]/execute(new SaveRegisteredOrganization(Keycloack registered realm as Organization and Tenant)) into Access Control domain layer
		and
			ACDomainGatewayServer-->>ACBackendServer: OrganizationRegistered(tenantName...)
			ACBackendServer->>AccessControlJSAdapter: OrganizationRegistered(tenantName...)
			ACBackendServer-->>AccessControlJSAdapter: tenantID of created organization
			AccessControlJSAdapter-->>OrganizationRegistrationWebUI: tenantID
			OrganizationRegistrationWebUI-->>Person: success organization registration notification
			OrganizationRegistrationWebUI->>SignUpWebUI: show(tenantID)
		end
	end
  end

```

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
  participant AccessControlJavaAdapter as <<Keycloak Events Listener>><br>AccessControlJavaAdapter
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
		IdentityServer-->>AccessControlJavaAdapter: notify(RegisterEvent)
		AccessControlJavaAdapter-)ACBackendServer: notify(new IdentityRegisteredEvent(tenantID, identity...))
		IdentityServer->>AuthorizationServer: addAccount(tenantID, identity)
		AuthorizationServer->>UAMDB: findAccount(tenantID, identity...)
		alt "existing account"
			alt "active account"
				par
					AuthorizationServer-->>IdentityServer: existing authenticated active account, roles and permissions
			  and
					AuthorizationServer-->>AccessControlJavaAdapter: notify(Login)
					AccessControlJavaAdapter-)ACBackendServer: notify(new UserAccountAuthentifiedEvent(tenantID, account...))
				end
				IdentityServer-->>AccessControlJSAdapter: existing authenticated account
				AccessControlJSAdapter-->>SignUpWebUI: account description
			else "deactived || expired account"
				par
					AuthorizationServer-->>IdentityServer: notify(new AccountDeactivedEvent(tenantID, account id))
					IdentityServer-->>AccessControlJSAdapter: notify(new UnusableExistingAccountEvent(tenantID, cause))
					AccessControlJSAdapter->>SignUpWebUI: notify(new AccountCreationRejectedEvent(tenantID, cause))
				and
					AuthorizationServer-->>AccessControlJavaAdapter: notify(LoginError)
					AccessControlJavaAdapter-)ACBackendServer: notify(new UnusableAccountAuthenticationAttemptedEvent(tenantID, account id))
				end
			end
		else "unknown account"
			AuthorizationServer->>UAMDB: createAccount(tenantID, identity..., default roles, default permissions...)
			par
				AuthorizationServer-->>IdentityServer: new user account
			  IdentityServer-->>AccessControlJSAdapter: assigned account
			  AccessControlJSAdapter-->>SignUpWebUI: account description
			and
				AuthorizationServer-->>AccessControlJavaAdapter: notify(RegisterEvent)
				AccessControlJavaAdapter-)ACBackendServer: notify(new UserAccountRegisteredEvent(tenandID, account...))
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
[Back To View](README.md)
