## PURPOSE
The space presents the implementation view of the developed components.

# SOURCE ARCHITECTURE MAP
The [architecture map](ARCHITECTURE.md) describes the code as it is today, read from the Maven POMs and the Java sources, for maintainers and builders who need to find their way. It keeps observed behaviour separate from the intent described in this documentation, and it doesn't propose future work.

In synthesis:
- **Modules:** under `implementation-line/access-control/`, the modules are grouped into domain model, translators, adapters, services, systems and UI. Each module's role and in-repo dependencies are listed in the map.
- **Runnable systems:** three Vert.x systems, each with a main verticle and Docker packaging declared in its POM:
  - Domain gateway (`AccessControlDomainIOGateway`)
  - Process module (`AccessControlDomainProcessModule`)
  - UI backend (`AccessControlReactiveMessagingGateway`)

  Every other module is a library. `ac-frontend-server` is excluded from the build.
- **Tenant registration:** this is the one use case wired across all layers. It runs from UI backend, through Redis streams, the gateway and the process module, to `TenantRegistration`, which stores the tenant in Redis and JanusGraph. Some segments are still mocked or missing:
  - The backend replies with a fixed response.
  - Nothing collects the domain's result.
  - Keycloak realm creation is not called.
- **Integrations:** Redis (messaging and event store), JanusGraph (read model), Keycloak (SSO and realm administration) and Vert.x (runtime).
- **Build and tests:** the Maven stage profiles (`-Dstage=commit`, `integration-quality`, …) and CI workflows are mapped. The map also lists current gaps and the places where this documentation and the code disagree.

# IMPLEMENTATION PRINCIPLES & MODELS
Presentation of dynamic aspects regarding interactions between components, diagrams regarding statemachines and/or processed activities.
## COMPONENTS COLLABORATION
Presentation of the strategy of integration developed between the implementation components to ensure their collaboration via standardized patterns at:
- User systems integration models
- [Application systems integration models](/implementation-line/access-control/ac-system/docs)


## SECURITY FEATURES & CONTROLS
|Control ID|Control Label|Implementation Models|
|:---|:---|:---|
|AC-2(8)|[Dynamic account management](dynamic-account-management.md)|- Account registration flow<br>|
|AC-3|[User access enforcement](user-access-enforcement.md)|- Authentication flow<br>|
|AC-3(7)|[Role-based access control](role-based-access-control.md)|- Authorization policy strategy<br>|
|AC-4|[Information flow enforcement](information-flow-enforcement.md)|- Authorization flow<br>- Access authorization types|
|AC-24(1)|[Transmit access authorization information](transmit-access-authorization-information.md)|- Access control enforcement flow<br>|
|IA-2(10)|[Single sign-on](single-sign-on.md)|- OIDC access control<br>|

# CONFIGURATION MANAGEMENT
Presentation of the specific configuration artifacts.

# SYSTEM ASSEMBLY
Presentation of static aspects relative to the produced components and their packaging models (e.g structural diagrams).

#
[Back To Home](/README.md)
