## PURPOSE
This documentation presents the mapping of concerns, components (e.g services, capabilities providers) and object types (e.g entities, structural elements) managed by the Access Control domain model and the Keycloak implementation model.

The AC domain model is encapsulating the Keycloak implementation model reused as technical solution, via the integration of Keycloak API elements (e.g services libraries, naming convention).

# OBJECT MODEL
Several CYBNITY domain objects are exposed to other Access Control domain elements (e.g service layer) as specification components or implementation components hosting behaviors required by the domain promise.

Some structural elements already provided by the Keycloak domain library (e.g Java client) are manipulated to reused existing capabilities (e.g OAuth features; security concerns of the Identity Management).

Tactically, when a mapping of behaviors and/or data is managed between Keycloak components and CYBNITY domain model components, an encapsulation approach is primary selected.

When a specific enhancement of the Keycloak domain model doesn't make sens in terms of quality or addionnal required feature, some standard design patterns (e.g interpreter, mediator, adaptor) are implemented to reduce the proliferation of direct dependency of Keycloak domain model to external other components (e.g CYBNITY domain service layer).

## DOMAIN OBJECTS MAPPING
The conceptual mapping of main concerns supported by the CYBNITY domain model and Keycloak model is presented here to understand the several terms that are used into each side's technical documentation.

|CYBNITY Object Type|Description|Keycloak Object Type|Description|
| :-- | :-- | :-- | :-- |
|Account| | | |
|OrganizationalStructure| | | |
|Person| | | |
|SmartSystem| | | |



#
[Back To Home](/README.md)
