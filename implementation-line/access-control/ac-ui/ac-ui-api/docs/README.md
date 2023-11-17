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

| CAPABILITY PROCESS STATE               | ORGANIZATION_REGISTRATION_SUBMITTED (Event)                | ORGANIZATION_REGISTRATION_REJECTED (Event)  | TENANT_CREATED (Event)        | ACCOUNT REGISTERED (Event)             | ACCOUNT ACTIVATED (Event)   |
|:---------------------------------------|:-----------------------------------------------------------|:--------------------------------------------|:------------------------------|:---------------------------------------|:----------------------------|
| Not started                            | Awaiting organization registration<br>RegisterOrganization | InvalidOperationException                   | InvalidOperationException     | InvalidOperationException              | InvalidOperationException   |
| Awaiting organization registration     | InvalidOperationException                                  | RejectOrganizationRegistration<br>Completed | Awaiting account registration | InvalidOperationException              | InvalidOperationException   |
| Awaiting account registration          | InvalidOperationException                                  | InvalidOperationException                   | RegisterAccount               | Awaiting account owner mail validation | InvalidOperationException   |
| Awaiting account owner mail validation | InvalidOperationException                                  | InvalidOperationException                   | InvalidOperationException     | ActivateRegisteredAccount              | Awaiting tenant activation  |
| Awaiting tenant activation             | InvalidOperationException                                  | InvalidOperationException                   | InvalidOperationException     | InvalidOperationException              | ActivateTenant<br>Completed |
| Completed                              | InvalidOperationException                                  | InvalidOperationException                   | InvalidOperationException     | InvalidOperationException              | InvalidOperationException   |

#
[Back To Home](/README.md)
