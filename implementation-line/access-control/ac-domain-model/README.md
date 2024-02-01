## PURPOSE
Presentation of the domain concerns and logical components supporting the Access Control bounded context at the model layer.

# FUNCTIONAL VIEW

```mermaid
%%{
  init: {
    'theme': 'base',
    'themeVariables': {
        'background': '#ffffff',
        'fontFamily': 'arial',
        'fontSize': '10px',
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
    'flowchart': { 'curve': 'monotoneX' }
  }
}%%
flowchart TB
  subgraph global
    direction TB
    id1(AUTHENTICATION)
    id1 -- type of --> id2(Identification)
    id1 -- is a --> id3(PROCESS)
    id1 -- yields --> id4(Security Context)
    id2 -- with a level of --> id5(Confidence)
    id3 -- that provides high levels of --> id5
    id3 -- verifies & test --> id6(CLAIMS)
    id5 -- about --> id6
    id3 -- implemented via --> id7(Mechanism)
    id7 -- evaluate & test --> id14(CREDENTIAL)

    id6 -- from or about an --> id22(ENTITY)
    id22 -- that is identifiable in an --> id29(PRINCIPAL)
    id22 -- can be a --> id23(Subject)
    id14 -- bind --> id6

    id14 -- expressed as --> id13
    id13(Factors) -- are --> id8(Ownership Factors) & id9(Knowledge Factors) & id10(Inheritance Factors)
    id8 -. examples .-> id11((Token)) & id12((Device))
    id9 -. examples .-> id15((Session<br>ID)) & id16((Password))
    id10 -. examples .-> id17((Camera)) & id18((Finger<br>Print))
    id14 -. example .-> id20((Digital<br>Certificate)) & id21((User ID<br>& Password))
    id23 -- can be --> id24(Process) & id25(Machine) & id26(Person) & id27(System) & id28(User)
    id23 -- has --> id19
    id14 -- bind --> id19(Identity)
  end
  classDef concern fill:#0e2a43, color:#fff
  classDef example fill:#e5302a, stroke:#e5302a, color:#fff
  class id1,id3,id6,id14,id22,id29 concern;
  class id11,id12,id15,id16,id17,id18,id20,id21 example;

```

# DESIGN VIEW
Several components of specification or implementation are supporting the domain provided over the `org.cybnity.application.access-control.domain` project's deliverable.

## STRUCTURE MODELS
Several sub-packages are implemented according to the specialization of sub-domains.

### IAM SUB-PACKAGE
The package `org.cybnity.accesscontrol.iam.domain.model` provide standard components regarding the access control of any type, as Identity & Access Management (IAM) features and domain objects.

| Class Type              | Motivation                                                                                                                  |
|:------------------------|:----------------------------------------------------------------------------------------------------------------------------|
| Account                 | Domain root aggregate object relative to a subject's usable account                                                         |
| OrganizationalStructure | Organizational structure (e.g company, association, group of companies, institution) who can have interactions with systems |
| Person                  | Physical social entity (e.g human person)                                                                                   |
| SmartSystem             | Represent a software and/or hardware system (e.g autonomous accessory representing a person or organization)                |

```mermaid
%%{
  init: {
    'theme': 'base',
    'themeVariables': {
        'background': '#ffffff',
        'fontFamily': 'arial',
        'fontSize': '18px',
        'primaryColor': '#fff',
        'primaryBorderColor': '#0e2a43',
        'secondaryBorderColor': '#0e2a43',
        'tertiaryBorderColor': '#0e2a43',
        'edgeLabelBackground':'#0e2a43',
        'lineColor': '#0e2a43',
        'tertiaryColor': '#fff'
    }
  }

}%%
classDiagram
    ChildFact <|-- Account
    Predecessors <.. Account : use
    VersionConcreteStrategy <.. Account : use
    VersionConcreteStrategy <.. OrganizationalStructure : use
    VersionConcreteStrategy <.. Person : use
    VersionConcreteStrategy <.. SmartSystem : use
    SocialEntity <|-- OrganizationalStructure
    SocialEntity <|-- Person
    SocialEntity <|-- SmartSystem

    class Account {
      -owner : EntityReference
      -tenant : EntityReference
      +Account(Entity predecessor, Identifier id, EntityReference accountOwnerIdentity, EntityReference tenantIdentity)
      +Account(Entity predecessor, LinkedHashSet~Identifier~ identifiers)
      +immutable() Serializable
      +versionHash() String
      +identified() Identifier
      +owner() EntityReference
      +tenant() EntityReference
      #generateIdentifierPredecessorBased(Entity predecessor, Identifier childOriginalId) Identifier
      #generateIdentifierPredecessorBased(Entity predecessor, Collection~Identifier~ childOriginalIds) Identifier
    }
    class OrganizationalStructure {
      +OrganizationalStructure(Entity predecessor, Identifier id)
      +OrganizationalStructure(Entity predecessor, LinkedHashSet~Identifier~ identifiers)
      +immutable() Serializable
    }
    class Person {
      +Person(Entity predecessor, Identifier id)
      +Person(Entity predecessor, LinkedHashSet~Identifier~ identifiers)
      +immutable() Serializable
    }
    class SmartSystem {
      +SmartSystem(Entity predecessor, Identifier id)
      +SmartSystem(Entity predecessor, LinkedHashSet~Identifier~ identifiers)
      +immutable() Serializable
    }

```
### AUTHORIZATION SUB-PACKAGE
The package `org.cybnity.accesscontrol.authorization.domain.model` provide components required to the authorization behaviors.

### CIAM SUB-PACKAGE
The package `org.cybnity.accesscontrol.ciam.domain.model` provide components required to manage Client Identity and Access Management (CIAM) of customer people requiring the support of authentication specific/enhanced use cases.

### PAM SUB-PACKAGE
The package `org.cybnity.accesscontrol.pam.domain.model` of elements required to implement the Privileged Access Management (PAM) regarding specific people, systems, processes that need to use specific access (e.g for administration action) based on robust means (e.g just-in-time access to a critical resource, remote access using encrypted gateway in place of password).

# IMPLEMENTATION VIEW
Presentation of the core components and files organization, packaging models and dependencies, and addressed configuration management of systems released. Globally this section give overview of technical components and structures implemented as domain layer.

- Structural diagrams regarding the domain model components matching the Keycloak domain elements
  - [CYBNITY Domain Model Mapping with Keycloak domain components](cybnity-keycloak-components-mapping.md)
- Behavioral diagrams regarding the interactions, states machines and activities provided by the model
- System assembly is mainly managed by Maven as a Java library artifact reused by the application service layer

# RELEASES HISTORY
- [V0 - FRAMEWORK changes list](v0-changes.md)

#
[Back To Home](/README.md)
