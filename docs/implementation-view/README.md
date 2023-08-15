## PURPOSE
The space presents the implementation view of the developed components.

# SECURITY CONTROLS
|Control ID|Control Label|Implementation Models|
|:---|:---|:---|
|AC-2(8)|[Dynamic account management](dynamic-account-management.md)|- Account registration flow<br>|
|AC-3|[User access enforcement](user-access-enforcement.md)|- Authentication flow<br>|
|AC-3(7)|[Role-based access control](role-based-access-control.md)|- Authorization policy strategy<br>|
|AC-4|[Information flow enforcement](information-flow-enforcement.md)|- Authorization flow<br>- Access authorization types|
|AC-24(1)|[Transmit access authorization information](transmit-access-authorization-information.md)|- Access control enforcement flow<br>|
|IA-2(10)|[Single sign-on](single-sign-on.md)|- OIDC access control<br>|

# INTEGRATION WITH KEYCLOAK EVENTS
Several type of events are promoted by Keycloak during its running, that can be listened by Access Control modules for detection and collaborative actions to perform into the Access Control domain:
- [Error events](https://www.keycloak.org/docs-api/22.0.1/javadocs/org/keycloak/events/Errors.html)
- [User events](https://www.keycloak.org/docs-api/22.0.1/javadocs/org/keycloak/events/Event.html)
- [Admin events](https://www.keycloak.org/docs-api/22.0.1/javadocs/org/keycloak/events/admin/ResourceType.html) that represents Keycloak resource types
- [Operation type events](https://www.keycloak.org/docs-api/22.0.1/javadocs/org/keycloak/events/admin/OperationType.html)

#
[Back To Home](/README.md)
