## PURPOSE
Discover here the Access Control Management application domain project, that have mission to build and deliver testable CYBNITY domain software components and systems versions.

The CYBNITY Access Control domain technical documentation includes many types of support deliverables produced during the software development life cycle.

You can find informations relative to software maintenance like:
- Design diagrams regarding software developed concepts and source codes
- Support to software build process and packaging
- Systems configuration and deployment procedures

# FUNCTIONAL VIEW (Use Cases)
## PURPOSE
- Show the functionalities of the system(s) as perceived by the external actors
- Exposes the requirements of the systems

### Usage
Formalizes software functional and/or technical analysis according to the functional and technical requirements.

Feature specifications API versions (e.g interface versions) are delivered according to requirements and specification identified by MVF project line's prototyping results.

### Artifacts
The managed source files are stored in the functional-view sub-folder like:
- Static aspects (structural diagrams): use cases
- Dynamic aspects (behavioral diagrams): interactions, statecharts, activities
- Model sub-packages:
  - Each context of the software (e.g Domain context) is described in a separate sub-package

# DESIGN VIEW (Logical Components)
## PURPOSE
- Sub-capture how the functionality is designed inside the domain contexts
- Logical view of systems and sub-systems

### Usage
Formalizes the specification of the software and sub-components produced during the solution analysis and technical design activities.

Feature implementations versions are designed and delivered as implementation software which extends the Foundation Core project.

### Artifacts
The managed source files are stored in the design-view sub-folder like:
- Static aspects (structural diagrams): classes, objects
- Dynamic aspects (behavioral diagrams): interactions, statecharts, activities, sequences

# PROCESS VIEW (Executions)
## PURPOSE
- Show the concurrency of the system(s)
- Encompasses the threads and processes that form the system's concurrency and synchronization mechanisms

### Usage
Describes execution models and synchronization rules, identified during the technical design phase and implementation rules definition.

### Artifacts
The managed source files are stored in the process-view sub-folder like:
- Static aspects: equals to design view's diagrams, with focus on the active classes that represent theses threads and processes
- Model sub-packages:
  - Performance
  - Scalability

# IMPLEMENTATION VIEW (Packaged Components)
## PURPOSE
- Show the organization of the core components and files (e.g source code, setting files)
- Packaging models and dependencies distribution
- Addresses the configuration management of the system's releases

### Usage
Formalizes the maintenance documentation aligned with source codes developed, including specificities regarding technologies (e.g language requirements) and frameworks (e.g implementation templates, protocols) used for implementation of the software.

### Artifacts
The managed source files are stored in the [implementation-view](docs/implementation-view) sub-folder like:
- Static aspects (structural diagrams): components, packages
- Dynamic aspects (behavioral diagrams): interactions, statecharts, activities
- Model sub-packages:
  - Implementation principles & models
  - Configuration-management
  - System-assembly

#### Source Codes Structures
Implementation components projects are structured and built according to standards:
- Maven: Java components using a [standard Maven project structure](https://maven.apache.org/guides/introduction/introduction-to-the-standard-directory-layout.html)
- NPM: JavaScript components
- Docker: system containers and images templates are managed by each project (e.g via auto-generated Dockerfile by Maven process)
- Helm: parameters and Kubernetes resources configuration using a [standard Helm structure](https://v2.helm.sh/docs/developing_charts/)

The implementation projects are supported by an Official TechStack version defining the authorized technologies used by this software project.

## UI COMPONENTS
The source codes managed in theses sub-projects are providing capabilities to final user via web interfaces (e.g visual interfaces and/or backend API services) which can enhance the Foundation Core project's capabilities layer:
- [User Interface API](/implementation-line/access-control/ac-ui/ac-ui-api/docs/README.md)

## APPLICATION COMPONENTS
The source codes managed in theses sub-projects (Maven projects) are supporting the features (as micro-service components constituing the business capabilities provided by the application domain) provided by the bounded context:
- [Adapter libraries](/implementation-line/access-control/ac-adapter)
  - Adapter API libraries
  - Adapter implementation components
- [Adapter translator libraries](/implementation-line/access-control/ac-translator)
  - UI translator library
  - Keycloak translator library
- [Domain model library](/implementation-line/access-control/ac-domain-model)
- [Domain service libraries](/implementation-line/access-control/ac-service)
  - Service API library
  - Service implementation module
- [System modules](/implementation-line/access-control/ac-system/docs)
  - Gateway application module
  - RTS computation unit

```mermaid
%%{
  init: {
    'theme': 'base',
    'themeVariables': {
        'background': '#ffffff',
        'fontFamily': 'arial',
        'fontSize': '13px',
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
        'textColor': '#fff',
        'lineColor': '#0e2a43',
        'nodeTextColor': '#fff',
        'nodeBorder': '#0e2a43',
        'noteTextColor': '#fff',
        'noteBorderColor': '#fff'
    },
    'flowchart': { 'curve': 'monotoneX', 'htmlLabels': 'true', 'wrappingWidth': '400' }
  }
}%%
flowchart LR
  access_control_rts_computation_unit("_&lt;&lt;System&gt;&gt;_<br>groupId: org.cybnity.application.access-control.**system**&nbsp;<br>artifactId: **process-module**")
  access_control_domain_gateway_server("_&lt;&lt;System&gt;&gt;_<br>groupId: org.cybnity.application.access-control.**system**&nbsp;<br>artifactId: **domain-gateway-server**")
  access_backend_server("_&lt;&lt;UI system&gt;&gt;_<br>groupId: org.cybnity.application.access-control.**ui.system**&nbsp;<br>artifactId: **backend-server**")
  access_frontend_server("_&lt;&lt;UI system&gt;&gt;_<br>groupId: org.cybnity.application.access-control.**ui.system**&nbsp;<br>artifactId: **frontend-server**")
  access_control_adapter_keycloak_impl("_&lt;&lt;Adapter&gt;&gt;_<br>groupId: org.cybnity.application.access-control.**adapters**&nbsp;<br>artifactId: **keycloak-impl**")
  access_control_adapter_admin_api("_&lt;&lt;Adapter API&gt;&gt;_<br>groupId: org.cybnity.application.access-control.**adapters**&nbsp;<br>artifactId: **admin-api**")
  access_control_adapter_keycloak_admin_impl("_&lt;&lt;Adapter&gt;&gt;_<br>groupId: org.cybnity.application.access-control.**adapters**&nbsp;<br>artifactId: **keycloak-admin-impl**")
  access_control_adapter_api("_&lt;&lt;Adapter API&gt;&gt;_<br>groupId: org.cybnity.application.access-control.**adapters**&nbsp;<br>artifactId: **api**")
  access_control_domain_model("_&lt;&lt;Domain model&gt;&gt;_<br>groupId: org.cybnity.application.**access-control**&nbsp;<br>artifactId: **domain**")
  access_control_service_api("_&lt;&lt;Service API&gt;&gt;_<br>groupId: org.cybnity.application.**access-control**&nbsp;<br>artifactId: **service-api**")
  access_control_ui_translator("_&lt;&lt;Translator&gt;&gt;_<br>groupId: org.cybnity.application.access-control.**translator**&nbsp;<br>artifactId: **ui**")
  access_control_ui_api("_&lt;&lt;UI API&gt;&gt;_<br>groupId: org.cybnity.application.access-control.**ui**&nbsp;<br>artifactId: **api**")
  access_control_service_impl("_&lt;&lt;Service&gt;&gt;_<br>groupId: org.cybnity.application.**access-control**&nbsp;<br>artifactId: **service-impl**")
  access_control_keycloak_translator("_&lt;&lt;Translator&gt;&gt;_<br>groupId: org.cybnity.application.access-control.**translator**&nbsp;<br>artifactId: **keycloak**")
  fwk_support("_&lt;&lt;Framework&gt;&gt;_<br>groupId: org.cybnity.**framework**&nbsp;<br>artifactId: **support**")
  fwk_domain("_&lt;&lt;Framework&gt;&gt;_<br>groupId: org.cybnity.**framework**&nbsp;<br>artifactId: **domain**")
  vertx_common("_&lt;&lt;Framework&gt;&gt;_<br>groupId: org.cybnity.**framework**&nbsp;<br>artifactId: **vertx-common**")
  redis_store("_&lt;&lt;Feature&gt;&gt;_<br>groupId: org.cybnity.features.technical.**persistence**&nbsp;<br>artifactId: **redis-store**")
  janusgraph_repository("_&lt;&lt;Feature&gt;&gt;_<br>groupId: org.cybnity.features.technical.**persistence**&nbsp;<br>artifactId: **janusgraph-repository**")
  redis_impl("_&lt;&lt;Adapter&gt;&gt;_<br>groupId: org.cybnity.infrastructure.integration.**uis.adapters**&nbsp;<br>artifactId: **redis-impl**")

  access_backend_server -.-> access_control_ui_api & access_control_ui_translator
  access_control_service_impl -.-> access_control_adapter_admin_api
  access_control_service_impl -.-> access_control_service_api & access_control_adapter_api & access_control_ui_api & access_control_domain_model
  access_control_adapter_keycloak_admin_impl -.-> access_control_adapter_admin_api
  access_control_adapter_keycloak_admin_impl -.-> access_control_adapter_keycloak_impl
  access_control_adapter_keycloak_impl -.-> access_control_adapter_api
  access_control_service_api -.-> access_control_ui_translator
  access_control_domain_gateway_server -.-> access_control_ui_translator
  access_control_rts_computation_unit -.-> access_control_adapter_keycloak_admin_impl & access_control_service_impl
  access_control_adapter_api -.-> access_control_ui_translator
  access_control_adapter_keycloak_impl -.-> access_control_keycloak_translator

  classDef module fill:#0e2a43, color:#fff
  classDef lib fill:#fff, stroke:##0e2a43, color:##0e2a43
  classDef external fill:#fff, stroke:#e5302a, color:#e5302a, stroke-dasharray: 5 5
  class access_control_ui_api,access_control_ui_translator,access_control_keycloak_translator,access_control_service_api,access_control_service_impl,access_control_domain_model,access_control_adapter_admin_api,access_control_adapter_api,access_control_adapter_keycloak_admin_impl,access_control_adapter_keycloak_impl lib;
  class access_backend_server,access_frontend_server,access_control_rts_computation_unit,access_control_domain_gateway_server module;
  class fwk_domain,redis_store,janusgraph_repository,redis_impl,vertx_common,fwk_support external;

```

## INFRASTRUCTURE COMPONENTS
The source code managed in the [Adapter translator libraries](/implementation-line/access-control/ac-adapter) area are about the infrastructure components supporting the integration capabilities:
- [Keycloak Admin Rest API adapter](/implementation-line/access-control/ac-adapter/ac-adapter-keycloak-admin-impl) to Keycloak server
- [Keycloak SSO API adapter](/implementation-line/access-control/ac-adapter/ac-adapter-keycloak-impl) to Keycloak SSO server

# DEPLOYMENT VIEW (Systems & Applications)
## PURPOSE
- Show the deployment of the systems in terms of physical architecture;
- Encompasses the node that form the system's hardware topology (e.g type of infrastructure components, network, virtual environments) on which the system executes (e.g resources requirements, runtime platform);
- Addresses the distribution (e.g flow opening), delivery (e.g procedures to respect), and installation (e.g resource prerequisites) of the parts that make up the physical system.

### Usage
Describes the environment(s), infrastructure and operating conditions required to install, activate and operate the systems safely.

### Artifacts
The managed source files are stored in the deployment-view sub-folder like:
- Static aspects (structural diagrams): components, deployment
- Model sub-packages:
  - Installation
    - Systems deployment
  - Delivery
  - System-distribution
  - System-topology

## DEPLOYABLE & RUNNABLE MODULES
Several systems are built as executable modules, containerized (Docker images) and that are ready for deployment via provisioning management solution (e.g Helm charts):
- [Gateway Server](/implementation-line/access-control/ac-system/ac-domain-gateway-server)
- [Process Server](/implementation-line/access-control/ac-system/ac-rts-computation-unit)
- [Backend Server](/implementation-line/access-control/ac-ui/ac-ui-system/ac-backend-server)
- [Frontend Server](/implementation-line/access-control/ac-ui/ac-ui-system/ac-frontend-server)

Several servers are developed as executable domain components, which are containerized and ready for deployment via provisioning management solution:
- Domain Gateway Server
  - For example, to start auto-generated docker image (by Maven) into a Kubernetes context, execute command line `kubectl run cybnity-ac-domain-gateway --image=cybnity/access-control-domain-gateway --image-pull-policy=Never`
- Real-Time Stream Computation Unit
  - For example, to start docker image as Pod in Kubernetes context, execute command line `kubectl run cybnity-ac-domain-rts-process --image=cybnity/access-control-process-module --image-pull-policy=Never`

### Reusable Provisioning System Projects
Perimeter: some infrastructure third-party software (e.g Keycloak, Postgresql, Redis, JanusGraph, Cassandra) are available on the market as template of provisioning helping to quickly customize the runtime (provisioning of pre-configured Docker image) into a Kubernetes platform. Some infrastructure systems are reused by CYBNITY as infrastructure systems with customization of the prepared templates of their images helmization.

Project type: Helm implementation structures.

Description: several generic infrastructure projects required by the CYBNITY implementation architecture are managed __into the CYBNITY helm charts repository__ supporting the provisioning of servers over Helm chart implementation.

The infrastructure servers reused by the Access Control domain are:
- SSO service: Keycloak server
- UIS service: Redis server
- Knowledge repository service: JanusGraph server with Cassandra
