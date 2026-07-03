## PURPOSE

This documentation presents the mapping of concerns, components (e.g services, capabilities providers) and object
types (e.g entities, structural elements) managed by the Access Control domain model and eventually linked to specific implementation
models.

The AC domain model is encapsulating any object implementation model reused as technical solution, via the integration
of external API elements (e.g services libraries, naming convention).

# OBJECT MODELS

## AC-DOMAIN-MODEL PROJECT

Deliverable: `org.cybnity.application.access-control:domain` java library.

Goal: several CYBNITY domain objects are exposed to other Access Control domain elements (e.g service layer) as
specification components or implementation components hosting behaviors required by the domain promise.

Some structural elements already provided by the Keycloak domain library are manipulated to reused existing
capabilities (e.g OAuth features; security concerns of the Identity Management).

Tactically, when a mapping of behaviors and/or data is managed between Keycloak components and CYBNITY domain model
components, an encapsulation approach is primary selected.

#

[Back To Home](/README.md)
