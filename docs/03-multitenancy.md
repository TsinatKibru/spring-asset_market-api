# Documentation: Multi-tenancy Isolation

This document explains how data isolation is achieved between different tenants.

## 1. Strategy: Discriminator Column
We use a shared database and shared schema strategy. Every tenant-specific table contains a `tenant_id` column used to filter data.

## 2. Implementation Details

### TenantAware Base Class
All entities that require tenant-specific isolation should extend the `TenantAware` abstract class.
- Defines the `tenant_id` column.
- Defines the Hibernate `@FilterDef` and `@Filter`.

### TenantContext
A utility class that uses `ThreadLocal` to store the `tenantId` of the currently authenticated user for the duration of the request.

### TenantFilter
A standard Spring Security filter that runs after authentication:
1. Extracts the user's `tenantId` from the database (based on the authenticated username).
2. Sets it in the `TenantContext`.
3. Ensures it is cleared after the request in a `finally` block.

### Hibernate Aspect (`HibernateConfig`)
To avoid manually enabling the filter in every service or repository, we use an Aspect-Oriented Programming (AOP) approach:
- Intercepts all repository method calls.
- Retrieves the `tenantId` from `TenantContext`.
- Enables the Hibernate `tenantFilter` on the current JPA session.

## 3. Benefits
- **Developer Experience**: Tenant filtering is automatic and transparent.
- **Security**: Harder to accidentally leak data between tenants as queries are scoped at the Hibernate level.
- **Performance**: Standard SQL filtering indexed on `tenant_id`.
