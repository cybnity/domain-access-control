# Access Control domain: initial roadmap (draft)

> **Status: draft proposal for Olivier Lemée to review.** Neither the items nor their order are
> approved commitments. Both are inferred from the code as it stands on `main` (merged into
> `feature-237` at `df70a3a`). There are no dates on purpose.

## Where things stand

The one end-to-end use case in progress is **tenant (organization) registration**: a user asks
to register an organization through the UI backend, the command crosses the Users Interactions
Space (Redis), and the RTS computation unit's `TenantRegistration` service records a `Tenant`.
The pieces exist on every layer, but they are not yet connected end to end:

- The service creates the tenant in the domain store only. Realm creation in Keycloak is a
  placeholder (`TenantRegistration.java:214`, `:221`), although the adapter can already create a
  realm: `AccessAdminAdapterKeycloakImpl.createTenant(...)` (`ac-adapter-keycloak-admin-impl`,
  line 238) is implemented, but nothing calls it and it has no tests.
- The service holds an `ISSOAdminAdapter`. That interface declares no realm-management methods
  of its own: it only inherits the lifecycle and health methods of `ICleanup` and `IHealthControl`
  (`ac-adapter-admin-api/.../ISSOAdminAdapter.java:11`). The realm operations (`createTenant`,
  `deleteTenant`, `findTenantByLabel`) are on `IAccessAdminAdapter`, which the service doesn't use.
- The UI backend answers every registration request with a hard-coded success response for an
  organization named `"CYBNITY"` (`DomainPublicAPIMessagesContentBasedRouter.java:203-209`,
  `mockedResponse(...)` at `:270`). The UIS consumers that should collect the real result are
  empty (`:124`, `:133`).
- Several registration tests pass without checking anything. The four use-case tests in
  `ac-rts-computation-unit/.../TenantRegistrationUseCaseTest.java` flag their checkpoints
  unconditionally (for example lines 115-117) after a `TODO coding of use case test`. The
  backend integration test doesn't start the process module or the domain gateway it says it
  needs (`PublicTenantRegistrationUseCaseIntegrationTest.java:58`, `:61`).

## Proposed outcomes, in order

The order follows code dependencies where there are some (2 needs 1 to be trusted, 3 needs 2
to have something real to return). Putting 4 last is a judgement call: the code doesn't force it.

### 1. Registration tests tell the truth

- **Why:** while use-case tests pass with no assertions, a green CI run (`java-project-ci.yml`
  runs `mvn ... test`) says nothing about whether registration works. Every later item needs a
  test that can fail.
- **Progress looks like:** each `TenantRegistrationUseCaseTest` case either asserts the
  behaviour named in its comments (tenant actioned, tenant ID returned, SSO admin event received)
  or is visibly disabled with the reason. The Keycloak setup/teardown TODOs in
  `ac-service-impl/.../TenantRegistrationUseCaseIntegrationTest.java:73`, `:81` are resolved, or
  the tests are scoped so they don't depend on a running Keycloak.
- **Open question:** should these tests run against a real Keycloak (container) in CI, or stop
  at the adapter boundary?

### 2. Registering a tenant provisions its Keycloak realm

- **Why:** the documented registration flow (`docs/implementation-view/dynamic-account-management.md`,
  AC-2(8)) has a tenant map to a dedicated realm. Today a registered tenant has no realm, so it
  has no SSO context and no accounts can be created in it.
- **Progress looks like:** registering a new organization creates a matching realm through the
  existing `createTenant(...)`, and the tenant event carries its connection settings. A test
  covers the new-realm case and the realm-already-exists case.
- **Builds on:** the existing `AccessAdminAdapterKeycloakImpl`. It doesn't need a new adapter.
- **Open decisions:**
  - Should `TenantRegistration` depend on `IAccessAdminAdapter`, or should `ISSOAdminAdapter`
    gain operations?
  - What should happen when the realm creation fails after the tenant is stored?
  - Should the "existing tenant, check SSO configuration" step (`TenantRegistration.java:155`)
    be asynchronous, as its comment proposes?
  - Is the use case "realm created directly by an administrator in Keycloak"
    (`TenantRegistration.java:97`) in scope?
  - The re-assignment rule for an inactive tenant with the same name is coded in the service,
    but the matching RTS test is a placeholder (`TenantRegistrationUseCaseTest.java:129`). The
    rule needs Olivier's confirmation.

### 3. The public API returns the real registration result

- **Why:** a UI caller is currently told that registration succeeded for `"CYBNITY"`, whatever
  it asked for and whatever happened downstream. This is the visible gap for any front-end work.
- **Progress looks like:**
  - The backend's UIS consumers collect the domain's response event by correlation ID and
    reply with it.
  - `mockedResponse(...)` is removed.
  - `PublicTenantRegistrationUseCaseIntegrationTest` starts the process module and the gateway,
    and asserts the organization name and tenant ID it gets back.
- **Depends on:** item 2 for a meaningful result. The plumbing could start in parallel against
  the current domain-store-only behaviour.
- **Open decision:** the TODO at `DomainPublicAPIMessagesContentBasedRouter.java:203` asks
  whether `vertx.redis` usage is still needed once the UIS adapter library observes responses.

### 4. Rejected and failed messages are kept, not only logged

- **Why:** `TenantRegistrationActivator` sends invalid and unprocessable commands only to the
  log, with TODOs to feed the invalid-message and dead-letter channels (`:95`, `:108`). The
  backend's `onInvalidChannelEntry` also only logs. The code comments name these cases as
  possible security events, so dropping them loses evidence an operator or a detection rule
  would need.
- **Progress looks like:** invalid and dead-letter messages appear on named UIS channels with
  their cause, and a test checks each path.
- **Open decision:** the channel names and the retention policy, plus whether these channels
  are owned by this domain or by the framework (`org.cybnity.framework`).

## Noted, not proposed as roadmap items yet

- The SSO adapters' `enable()`, `disable()` and `resume()` are empty in both
  `SSOAdapterKeycloakImpl` and `SSOAdminAdapterKeycloakImpl`. Whether they matter depends on how
  the process module manages adapter lifecycle, which this draft didn't establish.
- `RealmDefaultComplementaryResourcesProvider.java:370`: the `use-tenant` role isn't yet added
  to the `ui-layer-system-roles` client scope. It may surface while doing item 2.
- `ac-backend-server/.../backend/deprecated/` still holds handlers with open TODOs. Removing or
  keeping them is a clean-up decision, not a roadmap outcome.
- The other documented controls (AC-3, AC-3(7), AC-4, AC-24(1), IA-2(10) in
  `docs/implementation-view/README.md`) weren't assessed for implementation status in this draft.
  Their requirements live in Notion, which this draft didn't read.
