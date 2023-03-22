## PURPOSE
Presentation of the domain components.

# FUNCTIONAL VIEW


# DESIGN VIEW
Several components of specification or implementation are supporting the domain.

|Class Type|Motivation|
| :-- | :-- |
|Command|Imperative and identifiabl element that is a request for the system to perform a task|


## STRUCTURE MODELS

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

    class IVersionable {
        <<interface>>
        +versionUID() Long
    }

```

### Sub-Packages
See the presentation of detailed structure models implemented into the sub-packages.

# RELEASES HISTORY
- [V0 - FRAMEWORK changes list](v0-changes.md)
