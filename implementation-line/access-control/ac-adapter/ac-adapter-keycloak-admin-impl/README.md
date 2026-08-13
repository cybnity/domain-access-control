## PURPOSE
Presentation of the infrastructure adaptation module allowing to make interactions with the UIAM solution (Single-Sign On service, and embedded IAM capabilities) provided by Keycloak system.

It's a specific client implementation module of CYBNITY infrastructure connector packaged as Java library which can be embedded by securized CYBNITY other module (e.g component of Access Control domain).

It an implementation project of library providing scope of features relative to administration and supervision of UAM (e.g authentication and authorization settings management) and IAM (e.g realm management regarding multi-tenants).

| Cloudified As | Component Category               | Component Type      | Deployment Area      | Platform Type      |
|:--------------|:---------------------------------|:--------------------|:---------------------|:-------------------|
|               | CYBNITY Technical Service System | Application Service | CYBNITY Domains Area | IT & Data Platform |

# IMPLEMENTATION STACK
The main technologies set is:
- Java Library
- [Keycloak client](https://github.com/keycloak/keycloak-client?tab=readme-ov-file)

## Adaptation Components Dependencies
```mermaid
%%{
  init: {
    'theme': 'base',
    'themeVariables': {
        'background': '#ffffff',
        'fontFamily': 'arial',
        'fontSize': '14px',
        'primaryColor': '#fff',
        'primaryTextColor': '#0e2a43',
        'primaryBorderColor': '#0e2a43',
        'secondaryColor': '#fff',
        'secondaryTextColor': '#fff',
        'secondaryBorderColor': '#fff',
        'tertiaryColor': '#fff',
        'tertiaryTextColor': '#fff',
        'tertiaryBorderColor': '#fff',
        'edgeLabelBackground':'#fff',
        'lineColor': '#0e2a43',
        'titleColor': '#fff',
        'textColor': '#0e2a43',
        'lineColor': '#0e2a43',
        'nodeTextColor': '#0e2a43',
        'nodeBorder': '#0e2a43',
        'noteTextColor': '#0e2a43',
        'noteBorderColor': '#0e2a43'
    },
    'flowchart': { 'curve': 'basis' },
    'class': {
        'hideEmptyMembersBox': 'true'
    }
  }
}%%

classDiagram
  class ac_aaa["ac-adapter-admin-api"]
  class kac["keycloak-admin-client"]
  class ac_aki["ac-adapter-keycloak-impl"]
  class ac_aa["ac-adapter-api"]
  class ac_akai["ac-adapter-keycloak-admin-impl"]
  class kautc["keycloak-authz-client"]
  class ktr["keycloak-translator"]
  class kdo["keycloak-domain-ontology-impl"]
  class kccs["keycloak-client-common-synced"]
  class ac_dmm["ac-domain-model"]
  ac_akai ..> ac_aaa
  ac_akai ..> kac
  ac_akai ..> ac_aki
  ac_aaa ..> ac_aa
  ac_aki ..> kautc
  ac_aki ..> ktr
  ac_aki ..> ac_aa
  ktr ..> kdo
  ktr ..> ac_aa
  ac_aa ..> ac_dmm
  kdo ..> kccs
```

