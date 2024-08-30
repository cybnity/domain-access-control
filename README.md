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
  - API library
  - Implementation library
- Adapter Translator libraries
  - UI translator
  - Keycloak translator
- [Domain Model library](/implementation-line/access-control/ac-domain-model)
- Domain Service API library
- Domain Service Implementation module

## INFRASTRUCTURE COMPONENTS
The source code managed in this area are about the infrastructure components supporting the features and applications modules.
- Keycloak SSO server

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
- [Backend Server](/implementation-line/access-control/ac-ui/ac-ui-system/ac-backend-server)
- [Frontend Server](/implementation-line/access-control/ac-ui/ac-ui-system/ac-frontend-server)

Several servers are developed as executable domain components, which are containerized and ready for deployment via provisioning management solution:
- Domain Gateway Server
  - For example, to start auto-generated docker image (by Maven) into a Minikube platform, execute command line `kubectl run cybnity-ac-domain-gateway --image=cybnity/access-control-domain-gateway --image-pull-policy=Never`
- Real-Time Stream Computation Unit
  - For example, to start docker image as Pod in Minikube, execute command line `kubectl run cybnity-ac-domain-rts-process --image=cybnity/access-control-process-module --image-pull-policy=Never`

### Reusable Provisioning System Projects
Perimeter: some infrastructure third-party software (e.g Keycloak, Postgresql) are available on the market as template of provisioning helping to quickly customize the runtime (provisioning of pre-configured Docker image) into a Kubernetes platform. Some infrastructure components are reused by CYBNITY as infrastructure systems with customization of the prepared templates of their images helmization.

Project type: Helm implementation structures.

Description: several generic infrastructure projects required by the CYBNITY implementation architecture are managed into the helm charts repository supporting the provisioning of Keycloak server over Helm chart implementation.
