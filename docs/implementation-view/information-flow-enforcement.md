# AC-4 INFORMATION FLOW ENFORCEMENT

Feature Type: security control

Requirement: [defined specification](https://www.notion.so/cybnity/AC-4-Information-Flow-Enforcement-35a6c4aca8c547ed98da42dce281fc9a?pvs=4)

## Authorization flow
Multiple OIDC algorithm implementations are existing that are materialized as AuthorizationFlow types.

The ImplicitFlow (direct come back of access tokens without usage of the token API; none client recorded, no refresh token) is voluntary not retained by CYBNITY.

Generally used for JavaScript application without backend module, this approach is not implemented by CYBNITY software suite.

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
  AuthorizationFlow <|-- AuthorizationCodeFlow
  note for AuthorizationCodeFlow "For mobile application, web app, backend<br>token API used, 2 steps<br><br>"
  class AuthorizationFlow {
	<<interface>>
  }
  class AuthorizationCodeFlow {
  }

```

## Access authorization types
Define the several types of authorizations which are supported by the Access Control Process Module and that can be managed (e.g requested, accepted, rejected) by a stakeholder (e.g CISO of security team).

Based on OAuth2 standard, the ResourceOwnerCredential authorization type is voluntary not supported by the CYBNITY software (cause: not strongly secure).

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
  AuthorizationType <|-- AuthorizationCode
  Implicit --|> AuthorizationType
  ClientCredentials --|> AuthorizationType

  note for AuthorizationCode "Usable by server-side client application (e.g front UI or backend server module)"
  note for ClientCredentials "Usable only when client is equals to resource owner (without authorization to obtain from user)"
  note for Implicit "Usable by client-side (e.g web browser javascript module).<br>Be carefull, type of authorization artifact (e.g access token)<br>that can be intercepted if none security measure is implemented<br><br>"

  class AuthorizationType {
  }
  class AuthorizationCode {
  }
  class Implicit {
  }
  class ClientCredentials {
  }

```

#
[Back To Home](README.md)
