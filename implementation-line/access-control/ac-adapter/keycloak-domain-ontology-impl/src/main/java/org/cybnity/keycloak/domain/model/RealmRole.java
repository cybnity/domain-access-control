package org.cybnity.keycloak.domain.model;

/**
 * A realm role is global within a realm and applies to all clients in the realm. Realm role represents organization-wide permissions.
 * For example, type of role used for app user, offline access role, uma authorization, default realm admin roles.
 *
 * Best usages:
 * For roles that are truly global
 * When multiple applications share the same role semantics
 * When permissions are organization-wide
 *
 * Recommended: avoid using realm roles for app specific permissions.
 */
public interface RealmRole {
}
