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
specification components or implementation components hosting behaviour required by the domain promise.

Some structural elements already provided by the Keycloak domain library are manipulated to reused existing
capabilities (e.g OAuth features; security concerns of the Identity Management).

Tactically, when a mapping of behavior and/or data is managed between Keycloak components and CYBNITY domain model
components, an encapsulation approach is primary selected.

### RBAC Implementation model & supported concepts
The Role-Based Access Control ([RBAC](https://en.wikipedia.org/wiki/Role-based_access_control)) is supported as default implementation model.

#### Inventoried Identities, Systems and Critical Assets
Key types of subject requiring roles and permissions support for a domain model (e.g; according to a tenant scope) are:
- User types:
  - Human (e.g; end-user account like security team member of an organization, or privileged administrator)
  - Service (e.g; transversal account used by CYBNITY systems to ensure their integration coupling; external account allowing 3rd-party system communication)
  - AI Agent (e.g; CYBNITY AI agent deployed into an organization context and executing protection activities)
  
- System types:
  - Infrastructure resource (e.g; storage area or persistence system; network equipment)
  - Security tool (e.g; standalone device deployed into a protected zone as security control system)
  - Application (e.g; API Endpoint exposing websocket or JSON-RPC for AI Agents, or CYBNITY domain application component executing process)

#### Defined key Roles and Naming Convention
The definition of a default consistent naming scheme for generic support of roles is based:
- by type with naming convention based on template `<<system type>>-<<system label>>-<<custom logical name>>`
  - Web endpoint role (e.g; "endpoint-web-reactive-server", "endpoint-reactive-backend-server" "endpoint-ai-mcp-server")
  - Infrastructure role (e.g; "store-janusgraph-knr", "store-redis-uis", "sso-keycloak-service")
  - Application module role (e.g; "process-module-access-control", "ui-module-access-control")

- by function with naming convention based on template `<<function category name>>-<<responsibility name>>`
  - Tenant user role (e.g; "tenant-enduser", "tenant-manager", "tenant-enduser-security-CISO")
  - Security representative role (e.g; "security-CISO", "security-ISO", "security-risk-analyst", "security-controls-manager" or any role defined by a ISMS framework implemented in CYBNITY software suite instance by an organization)
  - System maintainer role (e.g; "maintenance-devops" as CYBNITY systems DevOps; "maintenance-deployer" or "maintenance-auditor")
  - Solution administrator role (e.g; "administration-contents-manager" about the contents perimeter regarding a CYBNITY tenant)
  - Tester role (e.g; "quality-functional-validator", "quality-technical-performance-reviewer")
  - Developer role (e.g; "dev-application-developer")
  - CI/CD role (e.g; "tooling-application-deployer", "tooling-environment-preparer")
  
- by environment with naming convention based on template `<<environment name>>-<<activity type>>`
  - Production context role (e.g; "prod-read-only", "prod-deploy", "prod-roolback")
  - Quality context role (e.g; "qa-functional-acceptance", "qa-version-change-execution")
  - Development context role (e.g; "dev-server-start", "dev-app-version-installation")
  
- by combination with naming convention based on template `<<context label>>-<<responsibility name>>`

### Designed key roles
Generic and extensible implementation components are designed into the domain model for allow creation and maintenance of roles required by RBAC concepts.

|Class|Description|
|:----|:----------|
|Role|Generic role managed into a context (e.g; that could be assigned to a service account about a system application layer)|

#

[Back To Home](README.md)
