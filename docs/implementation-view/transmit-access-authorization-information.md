# AC-24(1) TRANSMIT ACCESS AUTHORIZATION INFORMATION

Feature Type: security control

Requirement: [defined specification](https://www.notion.so/cybnity/AC-24-1-Transmit-access-authorization-information-3bf2f16555d849979d90cb324d4ed007?pvs=4)

## Access control enforcement flow
Specification of the synchronization of the authenticated user account role/accreditations on the Users Interactions Space based on the first presented access token to the backend messaging gateway.

It allow autonomous sso check by any capability/application module when the user’s JWT token (which does not include the current/real-time managed user’s roles/accreditations managed by stakeholders domain) is presented to them, and allow to check quickly the authorized RBAC or ABAC to requested resource (e.g materialized by request command including the JWT in event transport enveloppe).

This decoupled JWT access token and role/accreditions reduce the risk of extraction/change/view of current user’s roles/permission when the JWT is out-of server (e.g stored in user’s browser side) and/or by an intermediary server (e.g network proxy providing JWT reference to browser for retrieve it from stored session).

The JWT shared end-to-end between systems only include SSO validated token (is user authenticated before expiration time?), and real-time managed accreditations are only stored temporary into the Users Interactions Space for authorization checks by any server-side module during resources accesses by account’s owner.

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
        'secondaryColor': '#0e2a43',
        'tertiaryBorderColor': '#0e2a43',
        'edgeLabelBackground':'#0e2a43',
        'lineColor': '#0e2a43',
        'tertiaryColor': '#fff'
    }
  }
}%%

sequenceDiagram
	actor ExternalSystem
	participant ACBackendServer as <<Reactive Backend Server>><br>ACReactiveMessagingGW
	participant UISAdapter as <<Redis Connector>><br>UISAdapter
	participant UsersInteractionsSpace as <<Real-Time Redis Streamed Event Store>><br>UsersInteractionsSpace
	participant AccreditedUserToken
	participant AccessControlGatewayServer as <<Domain Change Events Validators>><br>AccessControlGatewayServer
	participant JWTToken
	participant ACJWTCachingSecurityCapability as <<Java Controller>><br>ACJWTCachingSecurityCapability
	participant ResponsibilitiesCapability as <<Capability Domain>><br>Stakeholders&Responsibilities

	ExternalSystem-)ACBackendServer: notify(new AuthorizedAccessEvent(user token))
	alt "invalid | no signed token"
		Note right of ACBackendServer: Ignore and create security incident
	else
		ACBackendServer->>UISAdapter: notify(authorizedAccessEvent)
		UISAdapter-)UsersInteractionsSpace: append(authorizedAccessEvent, accessControlDomain topic...)
		UsersInteractionsSpace-)AccessControlGatewayServer: notify(authorizedAccessEvent)
		AccessControlGatewayServer->>JWTToken: checkConformity()
		alt "expired token"
			AccessControlGatewayServer-)UsersInteractionsSpace: add(new SecurityTokenExpiredEvent(JWTToken))
			Note right of UsersInteractionsSpace: clean old cached token version
		else "SecurityToken is signed & valid"
			AccessControlGatewayServer-)UsersInteractionsSpace: append(new ValidSecurityTokenCheckedEvent(JWTToken, tenantID))
			UsersInteractionsSpace-)ACJWTCachingSecurityCapability: notify(validSecurityTokenCheckedEvent)
			Note right of JWTToken: parse JWT to build cached version of token owner's accreditations
			ACJWTCachingSecurityCapability->>JWTToken: userIdentity()
			JWTToken-->>ACJWTCachingSecurityCapability: subject attributes describing token owner's identity
			ACJWTCachingSecurityCapability-)UsersInteractionsSpace: append(new FindUserHabilitationsQuery(tenantID, userIdentity))
			UsersInteractionsSpace-)ResponsibilitiesCapability: notify(findUserHabilitationsQuery)
			ResponsibilitiesCapability->>ResponsibilitiesCapability: findExistingAccreditations(userIdentity)
			alt "existing role(s), habilitation(s)"
				ResponsibilitiesCapability->>AccreditedUserToken: new AccreditedUserToken(JWTToken.hashCode(), tenantID, userIdentityId.hashCode(), accountId.hashCode(), jwtToken, userClaims[], userHabilitations[])
			else "none role or habilitation defined"
				Note right of ResponsibilitiesCapability: Initialize Subject Access Request (SAR)<br>to security team's manager
				ResponsibilitiesCapability->>ResponsibilitiesCapability: definedDefaultHabilitations(userIdentity)
				ResponsibilitiesCapability->>AccreditedUserToken: new AccreditedUserToken(tenantID, userIdentityId.hashCode(), accountId.hashCode(), jwtToken, userClaims[], userHabilitations[])
			end
			ResponsibilitiesCapability-)UsersInteractionsSpace: save(accreditedUserToken)
			ResponsibilitiesCapability-)UsersInteractionsSpace: save(new AccreditedUserTokenCreatedEvent(tenantId, accreditedUserToken))
			Note right of UsersInteractionsSpace: Token enhanced of accreditations is cached per user, and ready to be read only by any other component before its expiration date
		end
	end

```

#
[Back To Home](README.md)
