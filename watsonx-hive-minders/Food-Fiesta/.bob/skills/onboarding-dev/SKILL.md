---
name: onboarding-dev
description: Use when a new developer joins a project and needs to understand it — generates a complete onboarding document (purpose, how to start, architecture, CI/CD, project health) saved as docs/onboarding/devOnboarding.pdf, writes an architecture diagram to docs/onboarding/architecture.md, then automatically runs the technical debt audit to give the new developer a full picture of the project's health.
---

# Developer Onboarding

You are helping a new developer get up to speed on the current project. Follow these steps in order.

## Step 1 — Explore the project structure

Use `list_files` on the root directory (non-recursive first, then recursive on `src/` or equivalent source folder). Also read:
- `README.md` or `readme.md` if it exists
- `pom.xml`, `package.json`, `build.gradle`, `Cargo.toml`, `go.mod`, or whichever build manifest is present
- The main application entry point (e.g. `main()`, `index.ts`, `app.py`)
- `application.properties`, `.env.example`, `config/` files

Also look for CI/CD and deployment files — read them if they exist:
- `.github/workflows/*.yml` — GitHub Actions pipelines
- `Dockerfile` and `docker-compose.yml` — containerisation and service dependencies
- `buildspec.yml`, `appspec.yml` — AWS CodeBuild / CodeDeploy pipelines
- `azure-pipelines.yml`, `.gitlab-ci.yml`, `Jenkinsfile` — other CI systems
- `entrypoint.sh`, `start.sh` — custom container startup scripts
- Any `ENV` variables declared in the Dockerfile or overridden in docker-compose

The output folder for all generated files is always `docs/onboarding`. Create it if it does not exist (write_file creates parent directories automatically). Use this path consistently for all output files in this skill run.

## Step 2 — Understand authentication and security

Look for security configuration files (e.g. `SecurityConfig.java`, `auth/`, `middleware/`). Note:
- How users and admins authenticate
- What routes are public vs protected
- Whether sessions, JWT, or OAuth are used

## Step 3 — Map the architecture

Read every layer of the application:
- **Controllers / routes** — what endpoints exist and what they do
- **Services / business logic** — how the core logic is organized
- **Repositories / data access** — how the app reads and writes data
- **Entities / models** — what the data looks like, including DB table names
- **Config / startup** — any seed data, scheduled jobs, or special beans

## Step 4 — Generate the onboarding document and save it as PDF

Using everything discovered, compose the full onboarding content covering these six sections:

### Section 1 — Purpose
Answer: *What is this application for? Who uses it and what problem does it solve?*
- Describe the business domain
- List the main user roles (e.g. admin, customer)
- Summarize the core features

### Section 2 — How to Start the Application
Answer: *How does a developer run this project locally?*
- List all prerequisites (language runtime, tools, versions)
- Give exact commands to run the app (copy-pasteable)
- List all local URLs and what they are for (app, API docs, DB console, etc.)
- Include any default credentials seeded on startup
- Mention environment variables or config files that must be set

### Section 3 — Application Architecture
Answer: *How is the codebase structured and how do the pieces fit together?*
- Describe each architectural layer (controllers, services, repositories, entities, config)
- Describe the authentication mechanism in detail (is it Spring Security? sessions? JWT? custom?)
- Describe the data model (entities, relationships, DB table names if non-default)
- Describe the request lifecycle (browser → controller → service → repository → DB → response)
- Flag any non-obvious patterns, naming quirks, or gotchas discovered during investigation

### Section 4 — Architecture Diagram

Produce a **flow vertical HTML diagram** (Option 2 format) embedded directly in the HTML document.
Do NOT use Mermaid, do NOT use ASCII art, do NOT use a plain table.

The diagram is a vertical stack of colored `<div>` blocks — one per architectural layer — connected by labeled arrows. Each block contains named component chips. Follow this exact HTML/CSS structure:

```html
<div class="arch-flow">

  <!-- ═══════════════════════════════════════════════════════════════
       INSTRUCTIONS FOR BOB — populate ALL chips below with the ACTUAL
       components of THIS project discovered in Steps 1–3.
       - Delete any layer block that does not exist in this project.
       - Add extra layers if needed (Cache, Message Queue, API Gateway…).
       - Never leave a placeholder like "[ControllerName]" in the output.
       ═══════════════════════════════════════════════════════════════ -->

  <!-- Layer: Client
       Adapt the rendering technology to the actual stack:
       "React SPA", "Vue.js", "Angular", "Thymeleaf (SSR)", "Mobile app"… -->
  <div class="arch-layer layer-browser">
    <div class="arch-layer-title">🌐 Client / Browser</div>
    <div class="arch-chips">
      <!-- Replace with actual rendering tech and client type found in the project -->
      <span class="chip chip-browser">[actual rendering — e.g. React SPA / Thymeleaf SSR]</span>
      <span class="chip chip-browser">[client type — e.g. HTML Forms / REST / GraphQL]</span>
    </div>
  </div>

  <!-- Replace arrow label with actual protocol: HTTP / WebSocket / gRPC… -->
  <div class="arch-arrow">↓ <span>[e.g. HTTP Request]</span></div>

  <!-- Layer: Security
       Remove this entire block if the project has no security layer.
       Add chip-warn on any security gap found in Steps 1–3. -->
  <div class="arch-layer layer-security">
    <!-- Rename title to actual mechanism: Spring Security / JWT Middleware / Auth0… -->
    <div class="arch-layer-title">🔒 [Security mechanism name]</div>
    <div class="arch-chips">
      <!-- One chip per security feature. chip-warn = known gap. -->
      <span class="chip chip-security">[e.g. JWT / Session auth / OAuth2]</span>
    </div>
  </div>

  <div class="arch-arrow">↓ <span>[e.g. Requêtes autorisées]</span></div>

  <!-- Layer: Entry point
       Rename to match the project: "Controllers" / "Routes" / "Handlers" /
       "Resolvers" / "Lambda Functions"… -->
  <div class="arch-layer layer-ctrl">
    <div class="arch-layer-title">🎮 [Entry layer — e.g. Controllers / Routes]</div>
    <div class="arch-chips">
      <!-- One chip per controller/route found. chip-warn = God class, stub, known issue. -->
      <span class="chip chip-ctrl">[ActualControllerName1]</span>
      <span class="chip chip-ctrl">[ActualControllerName2]</span>
    </div>
  </div>

  <div class="arch-arrow">↓ <span>[e.g. Service calls]</span></div>

  <!-- Layer: Business logic
       Rename: "Services" / "Use Cases" / "Domain" / "Actions"…
       Note non-standard annotations on the title if applicable. -->
  <div class="arch-layer layer-svc">
    <div class="arch-layer-title">⚙️ [Logic layer — e.g. Services / Use Cases]</div>
    <div class="arch-chips">
      <!-- One chip per service/use-case. chip-warn = bug, missing annotation… -->
      <span class="chip chip-svc">[ActualServiceName1]</span>
      <span class="chip chip-svc">[ActualServiceName2]</span>
    </div>
  </div>

  <div class="arch-arrow">↓ <span>[e.g. JPA / ORM / Raw SQL / HTTP]</span></div>

  <!-- Layer: Data access
       Rename: "Repositories" / "DAOs" / "Models" / "Adapters"…
       Remove if the service layer accesses data directly. -->
  <div class="arch-layer layer-repo">
    <div class="arch-layer-title">🗄️ [Data access — e.g. Repositories / DAOs]</div>
    <div class="arch-chips">
      <!-- One chip per repository/DAO found. -->
      <span class="chip chip-repo">[ActualRepositoryName1]</span>
      <span class="chip chip-repo">[ActualRepositoryName2]</span>
    </div>
  </div>

  <div class="arch-arrow">↓ <span>[e.g. SQL / NoSQL / HTTP]</span></div>

  <!-- Layer: Data stores
       List every database, cache, queue, or external API.
       For relational DBs add one chip per table: table_name ── EntityName -->
  <div class="arch-layer layer-db">
    <div class="arch-layer-title">💾 [Data stores — e.g. Database / Cache / Queue]</div>
    <div class="arch-chips">
      <!-- One chip per data store. For SQL tables: "table_name ── EntityName" -->
      <span class="chip chip-db">[e.g. PostgreSQL / MongoDB / Redis]</span>
    </div>
  </div>

  <!-- Optional blocks — include ONLY if they exist in the project.
       Examples: Session store, Message broker, External APIs, CDN. -->
  <!--
  <div class="arch-arrow">↕ <span>[e.g. Session / State]</span></div>
  <div class="arch-layer layer-session">
    <div class="arch-layer-title">💡 [State mechanism — e.g. HTTP Session / JWT Claims]</div>
    <div class="arch-chips">
      <span class="chip chip-session">[key] → [type and description]</span>
    </div>
  </div>
  -->

</div>
```

Include the following CSS inline in the document's `<style>` block (add it alongside the existing styles — do not replace them):

```css
/* ── Architecture flow diagram ── */
.arch-flow { display:flex; flex-direction:column; align-items:center; gap:0; margin:24px 0; }
.arch-layer { width:100%; max-width:700px; border-radius:10px; padding:14px 18px; margin:0; }
.arch-layer-title { font-size:12px; font-weight:700; text-transform:uppercase; letter-spacing:.07em; margin-bottom:10px; }
.arch-chips { display:flex; flex-wrap:wrap; gap:6px; }
.chip { border-radius:5px; padding:3px 10px; font-size:12px; font-family:monospace; font-weight:600; }
.chip-warn { outline:2px solid #f6ad55; outline-offset:1px; }
.arch-arrow { text-align:center; font-size:20px; color:#a0aec0; line-height:1; padding:6px 0; width:100%; max-width:700px; }
.arch-arrow span { display:block; font-size:11px; color:#a0aec0; margin-top:1px; }

/* Layer background colors */
.layer-browser  { background:#edf2f7; border:2px solid #a0aec0; }
.layer-security { background:#fff5f5; border:2px solid #fc8181; }
.layer-ctrl     { background:#ebf8ff; border:2px solid #63b3ed; }
.layer-svc      { background:#f0fff4; border:2px solid #68d391; }
.layer-repo     { background:#faf5ff; border:2px solid #b794f4; }
.layer-db       { background:#fffaf0; border:2px solid #f6ad55; }
.layer-session  { background:#fefcbf; border:2px solid #f6e05e; }

/* Chip colors matching their layer */
.chip-browser  { background:#e2e8f0; color:#2d3748; }
.chip-security { background:#fed7d7; color:#c53030; }
.chip-ctrl     { background:#bee3f8; color:#2c5282; }
.chip-svc      { background:#c6f6d5; color:#22543d; }
.chip-repo     { background:#e9d8fd; color:#44337a; }
.chip-db       { background:#feebc8; color:#7b341e; }
.chip-session  { background:#fef9c3; color:#713f12; }
```

**Rules for chip content:**
- Use the **exact component name** (never abbreviate: `UserServices` not `UserServ.`)
- Add `chip-warn` class AND an `⚠️` emoji on any chip that represents a known issue (God class, bug, stub, security gap)
- Add a brief parenthetical note after the name if relevant: `AdminServices ⚠️ bug update()`, `OrderController (stub vide)`
- DB chips should map: `table_name ── EntityName`
- If a component uses a non-standard annotation (e.g. `@Component` instead of `@Service`), note it on the layer title, not on each chip

### Section 5 — CI/CD & Deployment
Answer: *How is this application built, tested, and deployed?*

Only include sub-sections for tools that are actually present in the project (skip any that were not found during Step 1).

- **Continuous Integration** — describe the CI pipeline (GitHub Actions, GitLab CI, Jenkins, etc.):
  - What triggers it (push, pull request, specific branches)
  - What steps it runs (build, test, lint, package)
  - Which Java/Node/Python version it uses in CI
- **Docker** — if a `Dockerfile` is present:
  - Describe the build stages (e.g. multi-stage: build → run)
  - List the base images used
  - List all `ENV` variables declared in the Dockerfile with their default values
  - Note any security measures (non-root user, minimal runtime image)
  - Describe what `docker-compose.yml` starts (services, ports, dependencies, health checks)
- **Cloud / CD pipeline** — if `buildspec.yml`, `appspec.yml`, or equivalent is present:
  - Name the cloud provider and service (e.g. AWS CodeBuild, CodeDeploy)
  - Describe the build phases and artifacts produced
- **Environment variables to set for production** — compile a table of all env vars discovered across Dockerfile, docker-compose, and CI files, with their purpose and default values
- **Health check endpoint** — if a health or ping endpoint exists (e.g. `/api/health`, `/actuator/health`), mention its URL and what it returns

### Section 6 — Project Health & Technical Debt Tracking

Add a final section explaining to the new developer how to monitor the project's health over time:

- A short paragraph explaining that a technical debt audit will be automatically run at the end of this onboarding (Step 7), and that its report will be available in the `docs/audit/` folder once complete
- The exact command to re-run the audit at any time:
  > Type **"lance un audit de dette technique"** in a new Bob conversation on this project to regenerate the full audit report with temporal delta (improvement or regression vs. the previous baseline)
- A reminder that the baseline JSON (`docs/audit/dette_technique_baseline.json`) stores the reference snapshot — re-running the audit will automatically compare against it and show what has improved or regressed
- The list of audit deliverables that will be produced:
  - `docs/audit/dette_technique.html` — interactive HTML report with KPI dashboard and prioritized backlog
  - `docs/audit/dette_technique.pdf` — PDF version with debt visualization chart
  - `docs/audit/dette_technique_baseline.json` — temporal tracking baseline

### Section 7 — Known Gotchas & Contribution Guide

Add two subsections that a new developer needs before writing their first line of code:

**Known Gotchas** — list every non-obvious pattern discovered during Steps 1–3 that would trip up a new developer. Examples of what to include:
- Intentional typos or naming quirks in entities (e.g. a field with a double letter — never "fix" it, it maps to a DB column)
- Annotations used against convention (e.g. `@Component` instead of `@Service` on service classes — works but is intentional)
- Unsafe patterns that are known but not yet fixed (e.g. `Optional.get()` without guard — throws `NoSuchElementException` if ID missing)
- Routes that use the wrong HTTP verb by design (e.g. mutation endpoints mapped to `@GetMapping`)
- Hardcoded credentials in seed data (e.g. default admin password — note it, don't change it without updating the team)
- Session attributes stored as non-standard types (e.g. a full JPA entity in session vs. a simple String)

**Contribution Guide** — give the new developer the minimum they need to contribute safely:
- How to run the test suite locally: `./mvnw test` (or equivalent)
- The minimum test pass rate expected before pushing (e.g. "all existing tests must pass")
- Branching convention observed in the project (detect from git history or CI config — e.g. "feature branches merged to main via PR" or "direct push to main")
- How to run the app locally before pushing (exact command)
- Which CI checks will run automatically on push (from the CI config discovered in Step 1)
- One-line reminder: **"If you add a new service method, add a matching test in the corresponding `*Test.java` file"**

### Saving as PDF

**Step A — Retrieve the current date**

Run this command to get today's date as static text to embed in the HTML cover:

```powershell
Get-Date -Format "dd MMMM yyyy"
```

Store the output. Do NOT use `<script>document.write(...)</script>` — Edge headless does not execute JavaScript when printing to PDF and the date would appear blank.

**Step B — Detect the PDF engine**

First detect the operating system, then check for available browsers accordingly.

**On Windows** — run this PowerShell command:

```powershell
$edge86 = "C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
$edge64 = "C:\Program Files\Microsoft\Edge\Application\msedge.exe"
$chrome = "C:\Program Files\Google\Chrome\Application\chrome.exe"
if (Test-Path $edge86) { Write-Output "EDGE:$edge86" }
elseif (Test-Path $edge64) { Write-Output "EDGE:$edge64" }
elseif (Test-Path $chrome) { Write-Output "CHROME:$chrome" }
else { Write-Output "NONE" }
```

**On Linux/macOS** — run this shell command:

```bash
if command -v google-chrome &>/dev/null; then echo "CHROME:$(command -v google-chrome)"
elif command -v chromium-browser &>/dev/null; then echo "CHROME:$(command -v chromium-browser)"
elif command -v chromium &>/dev/null; then echo "CHROME:$(command -v chromium)"
elif command -v google-chrome-stable &>/dev/null; then echo "CHROME:$(command -v google-chrome-stable)"
else echo "NONE"
fi
```

- If the result starts with `EDGE:` or `CHROME:` — store the path and use it in Step D.
- If the result is `NONE` — skip Steps C and D, keep the HTML file, and tell the developer to print it manually (see fallback message below).

**Step C — Write the HTML file**

Write the complete self-contained HTML to `docs/onboarding/devOnboarding.html` using `write_file`. The HTML must:
- Use inline CSS only (no external stylesheets)
- Use a clean, readable font (system-ui or Arial), font-size 14px, max-width 900px, centered
- Render headings, paragraphs, tables, and `<pre><code>` blocks with good contrast and spacing
- Include all six sections as HTML — convert Markdown-style tables to `<table>` elements, code blocks to `<pre><code>`, bold/italic to `<strong>`/`<em>`
- Include a cover header with the project name, "Developer Onboarding Guide", and the generation date as **static text** from Step A

**Step D — Convert to PDF and verify**

**On Windows** — run:

```powershell
& "<engine-path>" --headless --disable-gpu --print-to-pdf="$((Get-Location).Path)\docs\onboarding\devOnboarding.pdf" --no-pdf-header-footer "$((Get-Location).Path)\docs\onboarding\devOnboarding.html"
```

Verify:
```powershell
Test-Path "$((Get-Location).Path)\docs\onboarding\devOnboarding.pdf"
```

**On Linux/macOS** — run:

```bash
"<engine-path>" --headless --disable-gpu --no-sandbox \
  --print-to-pdf="$(pwd)/docs/onboarding/devOnboarding.pdf" \
  --no-pdf-header-footer \
  "$(pwd)/docs/onboarding/devOnboarding.html"
```

Verify:
```bash
test -f "$(pwd)/docs/onboarding/devOnboarding.pdf" && echo "True" || echo "False"
```

After the command completes, **verify the PDF was created**:

- If `True` — keep the HTML file as well. Both `devOnboarding.pdf` and `devOnboarding.html` will be available in `docs/onboarding/`.
- If `False` — do NOT delete the HTML file. Warn the developer that PDF generation failed and the HTML is available instead.

**Fallback message if no PDF engine or PDF generation failed:**
> ⚠️ Le PDF n'a pas pu être généré automatiquement. Le fichier HTML est disponible à `docs/onboarding/devOnboarding.html` — ouvrez-le dans un navigateur et utilisez **Fichier → Imprimer → Enregistrer en PDF** pour obtenir le PDF manuellement.

## Step 4b — Check for existing onboarding document

Before writing any file, check if `docs/onboarding/devOnboarding.pdf` already exists:

```powershell
Test-Path "docs/onboarding/devOnboarding.pdf"
```

- If `True` — warn the developer:
  > ℹ️ Un document d'onboarding existe déjà (`docs/onboarding/devOnboarding.pdf`). Il va être remplacé par une version mise à jour.
- If `False` — proceed silently.

## Step 5 — Write the architecture diagram

Write the architecture to `docs/onboarding/architecture.md` using `write_file`.

Since the HTML flow diagram (Section 4) does not render in Markdown, the `.md` file uses a **simplified table format** — one row per layer.

**⚠️ Populate every cell with the ACTUAL components of THIS project discovered in Steps 1–3. Never leave placeholder names like `[actual rendering tech]` in the output — replace them with the real names found in the codebase.**

```markdown
# Architecture Diagram

> Auto-generated by the onboarding-dev skill.
> Full interactive diagram: see `docs/onboarding/devOnboarding.pdf`

| Couche | Composants | Notes |
|---|---|---|
| 🌐 Client | [actual rendering tech, e.g. React SPA / Thymeleaf SSR] | [rendering model] |
| 🔒 [Security name] | [actual security components found] | [any known gaps ⚠️] |
| 🎮 [Entry layer name] | [actual controller / route / handler names] | [God classes, stubs ⚠️] |
| ⚙️ [Logic layer name] | [actual service / use-case names] | [non-standard patterns, bugs ⚠️] |
| 🗄️ [Data access name] | [actual repository / DAO names] | [ORM type, quirks] |
| 💾 [Data stores] | [actual DB engines and table→entity mappings] | [dev vs prod engine if different] |
| 💡 [State/Session] | [actual session keys or JWT claims, if any] | [types stored] |

## Flux de requête

`[Client] → [Security] → [Entry layer] → [Logic layer] → [Data access] → [Database] → [Response]`

## Documents liés

- Onboarding complet : `docs/onboarding/devOnboarding.pdf`
- Audit de dette : `docs/audit/dette_technique.html`
```

**Adaptation rules:**
- Remove any row whose layer does not exist in the project
- Add extra rows for Message Queue, Cache, External APIs, CDN if present
- Use layer names matching actual project conventions (e.g. "Routes" for Express, "Models" for Django)

## Step 6 — Confirm onboarding completion

Tell the developer:
- ✅ The full onboarding document has been saved to `docs/onboarding/devOnboarding.pdf` and `docs/onboarding/devOnboarding.html` (or HTML only if PDF generation failed)
- ✅ The architecture diagram has been saved to `docs/onboarding/architecture.md`
- Point them to the default credentials and first URL to open
- Tell them the technical debt audit is now starting automatically (Step 7)

## Step 7 — Run the technical debt audit

Activate the `audit-dette-technique` skill now using the `use_skill` tool.

If `use_skill` returns that the skill is already active in this conversation (cannot be re-activated), tell the developer:
> ℹ️ L'audit de dette technique est déjà actif dans cette conversation. Pour obtenir le rapport d'audit complet, lance une **nouvelle conversation Bob** sur ce projet et tape : **"lance un audit de dette technique"**

Otherwise, the audit will automatically:
- Scan the codebase for security, architecture, quality, and dependency findings
- Compute the total technical debt in days and euros
- Generate prioritized alerts and a refactoring backlog
- Save the full audit report to `docs/audit/dette_technique.html`, `docs/audit/dette_technique.pdf`, and `docs/audit/dette_technique_baseline.json`

The new developer will have both their onboarding guide and a full picture of the project's health in a single run.
