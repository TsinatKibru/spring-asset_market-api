# Documentation: Security & Identity

This document describes the security implementation using Spring Security and JWT.

## 1. Authentication Flow
The system uses stateless JWT-based authentication.

1.  **Registration**: User registers via `POST /api/v1/auth/register` providing `username`, `email`, `password`, and `tenantId`.
2.  **Login**: User authenticates via `POST /api/v1/auth/login`.
3.  **Token Issuance**: On successful login, active JWT is returned.
4.  **Subsequent Requests**: User includes the JWT in the `Authorization: Bearer <token>` header.
5.  **Validation**: `AuthTokenFilter` intercepts requests, validates the JWT, and sets the security context.

## 2. Key Components
- **User Entity**: Stores user credentials, roles, and `tenantId`.
- **JwtUtils**: Handles creation, parsing, and validation of JWT tokens.
- **WebSecurityConfig**: Configures Spring Security to be stateless and defines public vs. protected routes.
- **UserDetailsServiceImpl**: Bridges our `User` entity with Spring Security's `UserDetails`.

## 3. Authorization
- **Roles**: Supported roles are `ROLE_USER` and `ROLE_ADMIN`.
- **Method Security**: Use `@PreAuthorize("hasRole('ADMIN')")` on controller methods to restrict access.

## 4. Multi-tenancy Integration
The `tenantId` is stored in the User entity and returned in the login response. It is a critical piece of information used in Phase 3 for data isolation.
