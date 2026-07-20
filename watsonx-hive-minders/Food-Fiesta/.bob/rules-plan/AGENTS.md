# AGENTS.md — Plan Mode

This file provides architectural constraints for planning work in this repository.

## Non-Obvious Architectural Constraints

- **Auth is dual-track and session-based**: Spring Security handles route authorization only; actual credential validation is in `AdminController` (for admins) and `AdminController.userLogin()` (for users). Any authentication refactor must account for both tracks
- **`BCryptPasswordEncoder` is declared but unused** — passwords are plain text. Any security improvement must migrate existing data or handle both formats during transition
- **Session holds a live `User` JPA entity** (`session.getAttribute("loggedInUser")`) — if the session object becomes detached from the Hibernate session, lazy-loaded fields (`orders`) will throw `LazyInitializationException`. Any change to `User` fetch strategy affects session behavior
- **`CrudRepository` (not `JpaRepository`)** is used everywhere — adding sorting, pagination, or Specifications requires changing the repository interface inheritance
- **`Orders` uses `GenerationType.AUTO`** while all other entities use `IDENTITY` — this can cause sequence table conflicts when switching to PostgreSQL; normalize before production migration
- **`DataLoader` uses `count() == 0` guard** — seeding is additive on first run only; re-seeding requires clearing the DB or dropping the H2 in-memory state (auto on restart)
- **`AdminServices.update()` has a bug**: it iterates all admins to find a match but calls `save(admin)` without setting the ID on the passed-in admin object — the saved record may get a new ID. Fix before any admin update feature work
- **`UserServices.validateLoginCredentials()` loads all users** for comparison — not scalable; should be replaced with a `findUserByUemailAndUpassword` query method before adding significant user load
- **No `@Transactional` annotations anywhere** — all service methods run without explicit transaction boundaries; complex multi-save operations are at risk of partial commits
