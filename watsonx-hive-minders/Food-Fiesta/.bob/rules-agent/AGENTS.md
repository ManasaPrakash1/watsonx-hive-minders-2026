# AGENTS.md — Agent (Coding) Mode

This file provides coding-specific guidance for agents working in this repository.

## Non-Obvious Coding Rules

- **Never use `@Service`** for service classes — the project uses `@Component` on all service beans (`UserServices`, `AdminServices`, `ProductServices`, `OrderServices`)
- **Always cast `findAll()` to `List`** explicitly — repositories extend `CrudRepository`, not `JpaRepository`, so `findAll()` returns `Iterable<T>`
- **Preserve the `totalAmmout` typo** in `Orders` entity and everywhere it is referenced — changing it breaks JPA column mapping
- **`BCryptPasswordEncoder` bean exists but is wired to nothing** — do not assume passwords are hashed; plain-text comparison is used in both `AdminServices.validateAdminCredentials()` and `UserServices.validateLoginCredentials()`
- **Use `Logic.countTotal(price, quantity)`** from `com.example.demo.count` for order total calculation — do not inline price × quantity
- **Update operations use `@GetMapping`** (not `@PutMapping` or `@PostMapping`) — this is the established pattern for all `updating*` routes
- **Session keys are typed** — `"loggedInAdmin"` holds a `String` (email), `"loggedInUser"` holds a `User` object; always cast accordingly
- **Admin `update()` in `AdminServices`** sets the ID on a loop iteration but calls `save(admin)` without setting `admin.setAdminId(id)` — if fixing this, set the ID on the admin object before saving
- **`springdoc.packages-to-scan=com.example.demo`** is set — Swagger picks up all controllers automatically; no additional config needed for new controllers
- **CSRF is disabled** — no CSRF tokens needed in forms
- When adding new entities, map to explicit table names with `@Table(name = "...")` — existing entities do not rely on default Hibernate naming
