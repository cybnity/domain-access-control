## PURPOSE
Presentation of the Access Control Management application domain, with mission to build and deliver testable CYBNITY software components and systems versions.

Feature specifications API versions (e.g interface versions) are delivered according to requirements and specification identified by MVF project line's prototyping results.

Feature implementations versions are designed and delivered as implementation software which extends the Foundation Core project.

### Sources Structure
Implementation components projects are structured and built according to standards:
- Maven: Java components using a [standard Maven project structure](https://maven.apache.org/guides/introduction/introduction-to-the-standard-directory-layout.html)
- NPM: JavaScript components
- Docker: system containers and images templates are managed by each project (e.g via auto-generated Dockerfile by Maven process)
- Helm: parameters and Kubernetes resources configuration using a [standard Helm structure](https://v2.helm.sh/docs/developing_charts/)

The implementation projects are supported by an Official TechStack version defining the authorized technologies used by this software project.

# USER INTERFACE COMPONENTS
The source codes managed in theses sub-projects are providing capabilities to final user via web interfaces (e.g visual interfaces and/or backend API services) which can enhance the Foundation Core project's capabilities layer:
- [User Interface API](access-control/ac-ui/ac-ui-api)

## RUNNABLE PACKAGED COMPONENTS
Several systems are built as executable modules, containerized (Docker images) and that are ready for deployment via provisioning management solution (e.g Helm charts):
- [Backend Server](access-control/ac-ui/ac-ui-system/ac-backend-server)
- [Frontend Server](access-control/ac-ui/ac-ui-system/ac-frontend-server)

# APPLICATION COMPONENTS
The source codes managed in theses sub-projects (Maven projects) are supporting the features (as micro-service components constituing the business capabilities provided by the application domain) provided by the bounded context:
- [Adapter API library](access-control/ac-adapter-api/)
- [Adapter Implementation library](access-control/ac-adapter-impl/)
- [Adapter Translator library](access-control/ac-translator/)
- [Domain Model library](access-control/ac-domain-model/)
- [Domain Service API library](access-control/ac-service-api/)
- [Domain Service Implementation module](access-control/ac-service-impl/)

## RUNNABLE PACKAGED COMPONENTS
Several systems are developed as executable modules, which are containerized and ready for deployment via provisioning management solution:
- [Domain Gateway Server](access-control/ac-system/ac-domain-gateway-server)
  - For example, to start auto-generated docker image (by Maven) into a Minikube platform, execute command line `kubectl run cybnity-ac-domain-gateway --image=cybnity/access-control-domain-gateway --image-pull-policy=Never`
- [Real-Time Stream Computation Unit](access-control/ac-system/ac-rts-computation-unit)
  - For example, to start docker image as Pod in Minikube, execute command line `kubectl run cybnity-ac-domain-rts-process --image=cybnity/access-control-process-module --image-pull-policy=Never`
