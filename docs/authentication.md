# Autenticación y autorización

Modelo de identidad del `catalog-service`: roles, usuarios, clientes de Keycloak,
flujos de token por llamador y mapeo a los endpoints del servicio.

Realm de Keycloak: **`ecommerce`** (puerto `8080`). Servicio: puerto `8081`.

> La definición del realm `ecommerce` (roles, clientes, client scopes, usuarios)
> vive en el proyecto **`keycloak-local`** (`realm/ecommerce-realm.json`), que
> levanta Keycloak con Docker. Este repositorio no contiene la definición de realm;
> este documento describe el modelo que esa definición debe cumplir.

## Roles del realm

| Rol       | Quién                                          | Alcance en catalog-service |
|-----------|------------------------------------------------|----------------------------|
| `ADMIN`   | Backoffice (persona)                           | Lectura + escritura completa |
| `CUSTOMER`| Consumidor del storefront                      | Lectura pública (sin token) |
| `SELLER`  | Vendedor (uso futuro: su propio catálogo)      | Lectura pública             |
| `SUPPORT` | Soporte                                        | Lectura pública             |

## Clientes de Keycloak

| Cliente            | Tipo         | Flujo            | Uso |
|--------------------|--------------|------------------|-----|
| `ecommerce-frontend` | Público    | `authorization_code` + PKCE | SPA del storefront; **nunca** `grant_type=password` |
| `ecommerce-api`    | Confidencial | `client_credentials` (service account) | Machine-to-machine / scripts |

`ecommerce-api` tiene habilitado `directAccessGrantsEnabled: true` **solo** para
testing local con `grant_type=password`; en entornos desplegados debe deshabilitarse.

## Scopes (machine-to-machine)

| Scope          | Efecto                                  |
|----------------|-----------------------------------------|
| `catalog:read` | GET sobre `/api/v1/products/**`        |
| `catalog:write`| POST/PUT/DELETE/PATCH sobre `/api/v1/products/**` |

El token de un service account solo vale para autorizar scopes, nunca roles.

## Mapeo de claims

- Roles humanos: `realm_access.roles` → `ROLE_<rol>` (ej. `ROLE_ADMIN`).
- Scopes: claim `scope` → `SCOPE_<scope>` (ej. `SCOPE_catalog:write`).
- El servicio valida el claim `aud` (debe contener `catalog-service`) además de
  firma, issuer y timestamp. Keycloak añade `catalog-service` a `aud` mediante el
  client scope `aud-catalog-service` (mapper `oidc-audience-mapper`).

## Reglas de autorización por ruta

| Ruta                        | Métodos                | Requisito |
|-----------------------------|------------------------|-----------|
| `/api/v1/products/**`       | GET                    | público   |
| `/api/v1/products/**`       | POST/PUT/DELETE/PATCH  | `ROLE_ADMIN` o `SCOPE_catalog:write` |
| `/v3/api-docs/**`, `/swagger-ui/**` | GET          | público (solo dev) |
| `/management/**` (actuator) | GET                    | público (local) |
| resto                       | —                      | autenticado |

## Flujos por llamador

### Frontend (storefront)
`authorization_code` + PKCE con `ecommerce-frontend`. El token lleva
`realm_access.roles`; un usuario `CUSTOMER` solo lee (GET es público), un `ADMIN`
puede escribir.

### Persona con curl / Swagger
`grant_type=password` contra `ecommerce-api` (solo dev local). No confundir con
machine-to-machine: para escribir se necesita rol `ADMIN`.

### Máquina (M2M)
`client_credentials` con `ecommerce-api`; los scopes `catalog:read`/`catalog:write`
se configuran en el client scope del cliente, no por usuario.

## Matriz endpoint × llamador

| Llamador                        | GET | POST/PUT/DELETE/PATCH |
|---------------------------------|-----|-----------------------|
| Sin token                       | ✅  | ❌ 401 |
| Frontend `CUSTOMER`             | ✅  | ❌ 403 |
| Frontend `ADMIN`                | ✅  | ✅   |
| Máquina con `catalog:read`      | ✅  | ❌ 403 |
| Máquina con `catalog:write`     | ✅  | ✅   |

## Buenas prácticas aplicadas

- `client_secret` vía secret de despliegue (variable de entorno), no en repos.
- Frontend nunca usa `grant_type=password`; el cliente público no tiene
  `directAccessGrantsEnabled`.
- Separación de responsabilidades: roles para humanos, scopes para máquinas.
- Validación de `aud` para evitar el reenvío de tokens emitidos para otra API.
- `SessionCreationPolicy.STATELESS`; sin estado de sesión en el recurso.
