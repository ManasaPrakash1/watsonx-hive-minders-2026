# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Commands

```powershell
# Run app (Windows)
.\mvnw.cmd spring-boot:run

# Run all tests
.\mvnw.cmd test

# Run a single test class
.\mvnw.cmd test -Dtest=FoodFrenzyApplicationTests

# Package (skip tests)
.\mvnw.cmd clean package -DskipTests
```

> Tests require Spring context (`@SpringBootTest`) and Google OAuth2 credentials — the `YOUR_CLIENT_ID`/`YOUR_CLIENT_SECRET` placeholders in `application.properties` will cause the context to fail if OAuth2 auto-configuration is triggered. Keep these as-is for local H2 runs.

## Architecture

- Base package: `com.example.demo` (app name is `FoodFrenzy` in code, `Food Fiesta` in branding)
- Layer order: `controllers` → `services` → `repositories` → `entities`
- Repositories extend `CrudRepository` (not `JpaRepository`) — `findAll()` returns `Iterable`, always cast to `List`
- Services annotated `@Component` (not `@Service`)
- Session-based custom auth lives alongside Spring Security — **Spring Security only guards routes**, actual login validation is done manually in `AdminController` and `UserController`, storing objects in `HttpSession`

## Auth & Session — Critical

- **Admin auth**: `AdminController.getAllData()` validates via `AdminServices.validateAdminCredentials()`, stores email string in session as `"loggedInAdmin"`
- **User auth**: `AdminController.userLogin()` validates via `UserServices.validateLoginCredentials()`, stores full `User` object in session as `"loggedInUser"`
- **Passwords are stored in plain text** for Admin. User passwords are also plain text — `BCryptPasswordEncoder` bean exists in `SecurityConfig` but is **not applied** anywhere in the current codebase
- OAuth2 Google login creates a User with `upassword = "OAUTH_USER"` and `unumber = 0L` as placeholders — see `OAuth2Controller`
- CSRF is **disabled** globally in `SecurityConfig`

## Entity / DB Conventions

- `User` maps to table `customer_info` (not `user` or `users`)
- `Orders` maps to table `order_info`
- `Orders.totalAmmout` — note the **typo** (double `m`) — preserve it in all code touching this field
- `User.u_id` — field name uses underscore; getter/setter are `getU_id()` / `setU_id()`
- `Orders` uses `GenerationType.AUTO`; all others use `GenerationType.IDENTITY`
- `DataLoader` seeds 5 products and 1 admin on startup (guarded by `count() == 0`)

## Order Calculation

- `Logic.countTotal(price, quantity)` in `com.example.demo.count` is the only calculation utility — use it for total amount, do not inline the multiplication

## Routing Conventions

- Public routes (no auth required): `/`, `/home`, `/login`, `/register`, `/adminLogin`, `/userLogin`, `/products`, `/location`, `/about`
- All CRUD mutations that redirect back to admin dashboard return `"redirect:/admin/services"`
- Update operations use `@GetMapping` with path variable (not `@PutMapping`) — this is intentional

## Swagger / OpenAPI

- Scanned package: `com.example.demo` (set in `application.properties`)
- Every controller method should have `@Operation(summary=..., description=...)` and every controller class `@Tag(name=..., description=...)`

## Templates & Static Assets

- Thymeleaf templates in `src/main/resources/templates/` use PascalCase filenames (e.g. `Admin_Page.html`, `BuyProduct.html`)
- Static CSS files in `src/main/resources/static/css/` mirror template names
- Static assets served under `/css/**`, `/JavaScript/**`, `/Images/**` — these paths are whitelisted in `SecurityConfig`
