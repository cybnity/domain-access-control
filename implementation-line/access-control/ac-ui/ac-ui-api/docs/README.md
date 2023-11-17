## PURPOSE
The space presents the implementation view of the developed components of Access Control User Interface API.

# DOMAIN API OBJECTS
Presentation of the matrix of objects manipulated over the UI API with events supported by the domain UI layer:
- Relative to the CQRS implementation
  - CommandName: referential of Command types (CQRS element)
  - QueryName: referential of Query types (CQRS element)
  - AttributeName: referential of attributes managed as DTO into the CQRS elements
- Relative to the Domain-Driven-Design architecture
  - DomainEventType: referential of DomainEvent types

## EVENTS
### Organization Registration
Presentation of the incoming messages treated by the capability and reactive state machine executed between involved components.

| CAPABILITY PROCESS STATE               | ORGANIZATION_REGISTRATION_SUBMITTED (Event)                | ORGANIZATION_REGISTRATION_REJECTED (Event) | TENANT_CREATED (Event)        |
|:---------------------------------------|:-----------------------------------------------------------|:-------------------------------------------|:------------------------------|
| Not started                            | Awaiting organization registration<br>RegisterOrganization |                                            |                               |
| Awaiting organization registration     |                                                            |                                            | Awaiting account registration |
| Awaiting account registration          |                                                            |                                            |                               |
| Awaiting account owner mail validation |                                                            |                                            |                               |
| Awaiting tenant activation             |                                                            |                                            |                               |
| Completed                              |                                                            |                                            |                               |

#
[Back To Home](/README.md)
