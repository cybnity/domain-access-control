## PURPOSE
This documentation presents the mapping of concerns, components (e.g services, capabilities providers) and object types (e.g entities, structural elements) managed by the Access Control domain model and the Keycloak implementation model.

The AC domain model is encapsulating the Keycloak implementation model reused as technical solution, via the integration of Keycloak API elements (e.g services libraries, naming convention).

# OBJECT MODELS
## AC-DOMAIN-MODEL PROJECT
Deliverable: `org.cybnity.application.access-control:domain` java library
Goal: several CYBNITY domain objects are exposed to other Access Control domain elements (e.g service layer) as specification components or implementation components hosting behaviors required by the domain promise.

Some structural elements already provided by the Keycloak domain library are manipulated to reused existing capabilities (e.g OAuth features; security concerns of the Identity Management).

Tactically, when a mapping of behaviors and/or data is managed between Keycloak components and CYBNITY domain model components, an encapsulation approach is primary selected.

When a specific enhancement of the Keycloak domain model doesn't make sens in terms of quality or addionnal required feature, some standard design patterns (e.g interpreter, mediator, adaptor) are implemented to reduce the proliferation of direct dependency of Keycloak domain model to external other components (e.g CYBNITY domain service layer).

Documentation: [Keycloak client javadoc](https://www.keycloak.org/docs-api/21.0.1/javadocs/index.html)

## DOMAIN OBJECTS MAPPING
The conceptual mapping of main concerns supported by the CYBNITY domain model and Keycloak model is presented here to understand the several terms that are used into each side's technical documentation.

|CYBNITY Object Type|Description|KEYCLOAK Object Type|Description|
| :-- | :-- | :-- | :-- |
|Account|Set of credentials (e.g cryptographic keys that enable the subject to sign or encrypt data) to an owner (e.g organization's member), according to a role (e.g user principal), with accorder privileges (e.g regarding systems and/or capabilities) in the frame of a context (e.g specific Tenant scope). Domain root aggregate object relative to a subject's usable account|User|Registred identity of a user|
|OrganizationalStructure|Represent a company, association, group of companies, or institution who can have interactions with systems| | |
|Person|Physical social entity (e.g human person) who can have interactions with systems| | |
|SmartSystem|Software and/or hardware system (e.g autonomous accessory representing a person or organization) who can have interactions with systems|Client|Client adapter allowing authorization management and representing a system; include settings regarding permissions allowed to adapter. Can be linked to a ServiceAccount regarding group of permissions allowed|
| | |ServiceAccount|Group of shared permissions accorging to allowed role(s)|
| | |Permission|Privilege allowed regarding a subject|
| | |Role|Logical role (e.g applicative role) allowing assigning of permissions set|

#
[Back To Home](/README.md)