## PURPOSE
Presentation of the infrastructure adaptation module allowing to make interactions with the Single-Sign On service provided by Keycloak system.

It's a specific client implementation module of CYBNITY infrastructure connector packaged as Java library which can be embedded by any CYBNITY other module.

It an implementation project of library providing scope of features relative to UAM (e.g authentication and authorization of a user or system into a Realm context) and IAM (e.g final user's account management regarding personal information) withtout any administration features allowed (see [ac-admin-api-keycloak-impl](../ac-admin-api-keycloak-impl) project supporting the administration features delivery).

| Cloudified As | Component Category               | Component Type      | Deployment Area      | Platform Type      |
|:--------------|:---------------------------------|:--------------------|:---------------------|:-------------------|
|               | CYBNITY Technical Service System | Application Service | CYBNITY Domains Area | IT & Data Platform |

# IMPLEMENTATION STACK
The main technologies set is:
- Java Library
- [Keycloak client](https://github.com/keycloak/keycloak-client?tab=readme-ov-file)

# KEYCLOAK INTEGRATION PROJECT
See the [documentation](keycloak-readme.md) regarding the development of provisioned Keycloak system for more detail about maintained Java project.
