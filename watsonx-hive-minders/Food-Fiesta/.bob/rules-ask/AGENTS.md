# AGENTS.md — Ask Mode

This file provides context for answering questions about this repository.

## Non-Obvious Context

- The app class is named `FoodFrenzyApplication` (package/class name) but the project is branded and described as **Food Fiesta** — these are the same application
- Spring Security is present but does **not** perform authentication — it only controls route access. Actual login/logout is session-based and manually coded in `AdminController`
- `User` entity maps to the DB table `customer_info`, not `user` — relevant when inspecting H2 console
- `Orders` entity maps to `order_info` table
- There is only **one real test file**: `FoodFrenzyApplicationTests` — it only checks that the context loads
- The `loginCredentials` package holds plain DTOs (`AdminLogin`, `UserLogin`) used for Thymeleaf form binding — they are not entities and not persisted
- `count/Logic.java` is a standalone static utility class — it is not a Spring bean
- Google OAuth2 config keys are placeholders (`YOUR_CLIENT_ID`) in `application.properties` — the app starts fine without them because OAuth2 registration will be inactive if values are placeholder strings, but the `spring-boot-starter-oauth2-client` dependency is still on the classpath
- Template filenames are PascalCase (e.g. `Admin_Page`, `BuyProduct`) — controllers return these names as strings without `.html` extension
- The `count` package name is non-standard — it contains only `Logic.java`, a static helper for order total calculation
