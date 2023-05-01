# IA-2(10) SINGLE SIGN-ON

Feature Type: security control

Requirement: [defined specification](https://www.notion.so/cybnity/IA-2-10-Single-sign-on-c892bcc5227d45f99d23a4fade607e7d?pvs=4)

## OIDC Access control
OAuth2 and OIDC protocol are selected as main supported elements by the Access Control API adapter. DTO objects are exposed, manipulated and transformed by the AC adapter API component encapsulating the implementation coupling (e.g with Keycloak OIDC implementation classes in dependency like selected implementation technology as authorization server).

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

classDiagram
  IRefreshToken --|> ISecurityToken
  ISecurityToken <|.. IDToken
  IAccessToken --|> ISecurityToken

  note for IAccessToken "OAuth2 token type, digitally signed by the SSO realm"
  note for ISecurityToken "Signed artifact, base64 encoded value and including SSO server’s signature private key"
  note for IRefreshToken "OAuth2 protocol based"
  note for IDToken "OIDC protocol based, standardized JWT token by OpenID core fields"
  note for IAccreditation "Habilitation and roles allowed to a JWT token owner"

  IDToken o-- "0..*" IClaim : userClaims
  IDToken "0..1" --o JWTToken
  IDToken o-- "0..*" IAccreditation : userHabilitations
  SubjectAttribute *-- "1" ScopeAttribute : scope
  JWTToken "1" --* AccreditedUserToken

  class IRefreshToken {
    <<interface>>
  }
  class ISecurityToken {
    <<interface>>
    +base64TokenValue() String
    +plainTextTokenValue() String
    +signaturePrivateKey() String
    +hashCode() int
    +equals(Object token) boolean
  }
  class IAccessToken {
    <<interface>>
    +authorizedBy() IAuthorization
    +expireAt() OffsetDateTime
    +isSigned() boolean
    +tenant() EntityReference
    +type() String
  }
  class IDToken {
    <<abstract>>
    +IDToken(Collection~IClaim~ userClaims, Collection~IAccreditation~ userHabilitations, OffsetDateTime expiration, OffsetDateTime authentifiedAt)
    +IDToken()
    +expireAt() OffsetDateTime
    +createdAt() OffsetDateTime
    +authentifiedAt() OffsetDateTime
    ~isValid(IAccessToken access) boolean
    +userClaims() Collection~IClaim~
    +userHabilitations() Collection~IAccreditation~
  }
  class IClaim {
    <<interface>>
  }
  class IAccreditation {
    <<interface>>
    +userIdentity() Collection~SubjectAttribute~
  }
  class JWTToken {
    <<OAuth2 exchange format>>
    +userClaims() Collection~IClaim~
    +userHabilitations() Collection~IAccreditation~
    +hashCode() int
    +equals(Object event) boolean
  }
  class SubjectAttribute {
    <<abstract>>
    +SubjectAttribute(ScopeAttribute scope)
  }
  class ScopeAttribute {
    <<interface>>
    +name() String
  }
  class AccreditedUserToken {
	+AccreditedUserToken(Tenant tenant, String userIdentityId, String userAccountId, JWTToken originalToken, Collection~IClaim~ userClaims, Collection~IAccreditation~ userHabilitations)
  }

```
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

classDiagram
  note for InfrastructureModule "Users Interactions Space infrastructure"
  InfrastructureModule --|> ResourceServer
  DomainModule --|> ResourceServer
  ResourceServer <.. ResourceOwner : ownerOfProvidedContents

  note for DomainModule "Module implemented by a bounded context providing resources"
  note for ResourceServer "OAuth2 based"
  note for ResourceOwner "OAuthe2 based (e.g user’s browser)"

  class ResourceServer {
    <<abstract>>
    -label : String
    +ResourceServer(String name)
    +label() String
		+systemType()* SystemType
  }
  class InfrastructureModule {
		<<abstract>>
		+systemType() SystemType.INFRASTRUCTURE_MODULE
  }
  class DomainModule {
		<<abstract>>
		+systemType() SystemType.DOMAIN_MODULE
  }
  class ResourceOwner {
    <<interface>>
  }

```
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

classDiagram
	note for ExternalSystemClient "CYBNITY module (e.g JavaScript client-side web app)<br>executed on external system (e.g user laptop);<br>System out of the CYBNITY owned and protected area<br><br>"
  note for ServerSideModuleClient "CYBNITY frontend or backend modules like NodeJS web app,<br>Vert.x messaging gateway, CYBNITY domain module<br><br>"
  note for OAuthClient "Connector configuration reusable for exchange with authorization server"
	OAuthClient <|.. ExternalSystemClient
  ServerSideModuleClient ..|> OAuthClient
  OAuthClient ..> SystemType
  class ServerSideModuleClient {
		<<abstract>>
  }
  class ExternalSystemClient {
		<<abstract>>
  }
  class SystemType {
		<<enumeration>>
		CYBNITY_FRONTEND,
		CYBNITY_BACKEND,
		DOMAIN_MODULE,
		INFRASTRUCTURE_MODULE
  }
  class OAuthClient {
		<<interface>>
		+clientUID() String
		+ssoRedirectURL() URI
		+usableBy() SystemType
  }

```
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

classDiagram
  note for AuthorizationServer "OAuth2 server (e.g Keycloak) including access token generator,<br>user account credential (UAM) and user identity (IAM)<br><br>"
  AuthorizationServer ..|> Revocation : allow
  AuthorizationServer ..|> Token : provide
  UserInfoAPIEndpoint <|.. AuthorizationServer
  AuthorizationServer ..|> IAuthorization
  OpenIDServerDiscovery <|.. AuthorizationServer
  Introspection <|.. AuthorizationServer
  UserInfoAPIEndpoint ..> Permission
  SSOSession <.. AuthorizationServer : generate

  class AuthorizationServer {
	<<abstract>>
  }
  class Revocation {
	<<OIDC interface>>
	+delete(IRefreshToken token)
	+delete(IAccessToken token)
  }
  class Token {
	<<OIDC interface>>
	+getToken(String type) ISecurityToken
  }
  class OpenIDServerDiscovery {
	<<OIDC interface>>
  }
  class Introspection {
	<<OIDC interface>>
	+validate(IAccessToken token)
	+validate(IRefreshToken token)
  }
  class UserInfoAPIEndpoint {
	<<OIDC interface>>
	+userIdentifier() String
	+userPermissions() Permission[]
  }
  class Permission {
	<<interface>>
  }
  class IAuthorization {
	<<OIDC interface>>
	+getAccessToken(Credential[] userAuthenticationCredentials) IAccessToken
  }
  class SSOSession {
	<<interface>>
  }

```

#
[Back To View](README.md)
