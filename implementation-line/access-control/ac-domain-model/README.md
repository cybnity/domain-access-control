## PURPOSE
Presentation of the domain components.

# FUNCTIONAL VIEW


# DESIGN VIEW
Several components of specification or implementation are supporting the domain provided over the `org.cybnity.application.access-control.domain` project's main package.

|Class Type|Motivation|
| :-- | :-- |
|Account|Domain root aggregate object relative to a subject's usable account|
|OrganizationalStructure|Organizational structure (e.g company, association, group of companies, institution) who can have interactions with systems|
|Person|Physical social entity (e.g human person)|
|SmartSystem|Represent a software and/or hardware system (e.g autonomous accessory representing a person or organization)|

## STRUCTURE MODELS

### MODEL SUB-PACKAGE

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

# RELEASES HISTORY
- [V0 - FRAMEWORK changes list](v0-changes.md)

#
[Back To Home](/README.md)
