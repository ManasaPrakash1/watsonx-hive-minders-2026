---
name: audit-dette-technique
description: Use when the user wants to audit technical debt, code quality, vulnerabilities, or migration complexity of an existing project — produces a combined business + tech report with an executive dashboard, prioritized refactoring list, temporal tracking baseline, and proactive alerts, saved to docs/audit/dette_technique.html and docs/audit/dette_technique.pdf.
---

# Audit de la Dette Technique

Follow these steps **in order**. Do not skip or reorder steps. The final deliverables are:
1. An HTML report saved as `docs/audit/dette_technique.html` (rendered as a `create_html_artifact`)
2. A PDF report saved as `docs/audit/dette_technique.pdf` with a visual debt chart (€ and days)
3. A baseline snapshot saved to `docs/audit/dette_technique_baseline.json` for future temporal tracking

---

## Step 0 — Capture the current date

Before doing anything else, resolve today's date by running:

```powershell
Get-Date -Format 'yyyy-MM-dd'
```

Store the output as `TODAY` (e.g. `2025-09-12`). Use this value — **not any hardcoded date** — in
every place a date appears: the HTML report header, the baseline JSON `"date"` field, the PDF
`$env:AUDIT_DATE` variable, and all delta comparisons. Never write a literal date like `2025-07-16`
into the output files.

---

## Step 1 — Discover project layout

Use `list_files` (top-level) then `glob` to map:
- All source files (`**/*.java`, `**/*.ts`, `**/*.py`, `**/*.js`, etc. — adapt to the language)
- Build descriptor (`pom.xml`, `package.json`, `requirements.txt`, `go.mod`, etc.)
- Config / secrets files (`*.properties`, `*.env`, `*.yml`, `*.yaml`)
- Test files (`*Test*.java`, `*.spec.ts`, `*.test.js`, etc.)
- CI/CD files (`.github/`, `Dockerfile`, `buildspec.yml`, etc.)

Store the inventory in memory. Calculate:
- **Total source files**, **total lines** (use `execute_command` with PowerShell `Get-ChildItem -Recurse | Measure-Object -Line` per extension group)
- **Test ratio** = test files / source files
- **Languages detected**

---

## Step 2 — Run the automated analysis script

Execute the bundled analysis script using the **same Node.js detection logic as Step 11.3** — do NOT assume `node` is in PATH:

```powershell
$nodeExe = if (Get-Command node -ErrorAction SilentlyContinue) { "node" } else {
  @(
    "C:\Program Files\nodejs\node.exe",
    "C:\Program Files (x86)\nodejs\node.exe",
    "$env:APPDATA\nvm\current\node.exe"
  ) | Where-Object { Test-Path $_ } | Select-Object -First 1
}
if ($nodeExe) {
  & $nodeExe "$env:USERPROFILE\.bob\skills\audit-dette-technique\analyze.js"
}
```

If `$nodeExe` is null (Node unavailable), perform the equivalent checks manually using `grep` and `read_file` — the script logic is documented inline in `analyze.js` for that fallback.

Parse the JSON output. It contains:
- `securityFindings[]` — each entry has `{ file, line, rule, severity, description, snippet }`
- `complexityHotspots[]` — files with high cyclomatic complexity estimates
- `codeSmells[]` — patterns like long methods, God classes, dead code
- `architectureIssues[]` — God classes, missing @Transactional, direct repo access from controllers
- `migrationRisks[]` — outdated deps, deprecated APIs, known migration blockers
- `meta` — `{ totalFiles, coverage: { testFiles, sourceFiles, ratio }, findingCounts }`

---

## Step 3 — Deep-read the top-5 critical files

From the hotspots identified in Step 2, read the **5 highest-risk files** in full using `read_file`.
For each, manually assess:
1. **Security issues** — plain-text passwords, SQL injection risk, missing input validation, session fixation, CSRF, exposed secrets
2. **Architecture issues** — God class, missing abstraction layer, tight coupling, circular deps
3. **Maintainability** — magic numbers, dead code, missing error handling, inconsistent naming
4. **Migration complexity** — deprecated framework APIs, hard-coded config, vendor lock-in

---

## Step 4 — Read build & dependency descriptor

Read the build file (e.g. `pom.xml`, `package.json`) in full. For each dependency, note:
- Current version
- Whether a major upgrade is available (apply general knowledge)
- Known CVEs at the declared version (apply general knowledge)
- Upgrade effort estimate (Low / Medium / High)

---

## Step 5 — Compute the debt model

### Differentiated day-rates by category (Option B)

Apply a **differentiated rate per category** reflecting the actual market profile required to fix each type of debt.
If the user overrides a rate (e.g. "utilise un taux sécurité de €X/jour"), update only that category.

| Category | Default day-rate | Rationale |
|---|---|---|
| **Sécurité** (critical + high findings, hardcoded secrets) | **€800/j** | Senior dev or security consultant required |
| **Architecture** (God class, tight coupling, transaction issues) | **€600/j** | Senior / tech lead profile |
| **Qualité** (code smells, Optional.get(), logging, validation) | **€500/j** | Mid-level developer |
| **Tests** (missing coverage per 10% below 80% target) | **€450/j** | Junior to mid-level developer |
| **OPS** (CI/CD stages, config, dependencies) | **€550/j** | DevOps / mid-level developer |
| Medium/low security findings | **€600/j** | Mid-level developer with security awareness |

### Effort table (days — unchanged)

| Category | Unit | Effort/unit |
|---|---|---|
| Critical security finding | per finding | 3 days |
| High security finding | per finding | 1.5 days |
| Medium security finding | per finding | 0.5 days |
| Low security finding | per finding | 0.1 days |
| Architectural issue (God class, tight coupling) | per class | 2 days |
| Code smell (long method, magic number, dead code) | per file | 0.3 days |
| Missing test coverage (per 10% below 80% target) | per 10% gap | 1 day |
| Outdated major dependency | per dep | 1.5 days |
| Outdated minor/patch dependency | per dep | 0.2 days |
| Missing CI/CD stage (test, lint, SAST) | per missing stage | 0.5 days |
| Hardcoded secret or config | per occurrence | 0.5 days |
| Missing documentation / Swagger | per endpoint | 0.1 days |

### Calculation rules

- **Cost per category (€)** = days_in_category × day-rate_for_category
- **Total debt (€)** = sum of all category costs (not days × a single flat rate)
- **Total debt (days)** = sum of all days (unchanged — for planning purposes)
- **Breakdown** must show both days AND € per category separately
- **Prioritized action list** = items ranked by `(severity × effort_reduction_days × category_rate) / fix_cost`
- In the HTML report, always display the rate used per category next to its cost figure
- In the baseline JSON, store `ratesByCategoryEuros` with the five rates used

---

## Step 6 — Build the temporal baseline

Create `docs/audit/dette_technique_baseline.json` using `write_file`:

```json
{
  "date": "<TODAY — value from Step 0>",
  "version": "1.0",
  "project": "<detected project name>",
  "totalDebtDays": <number>,
  "totalDebtEuros": <number>,
  "ratesByCategoryEuros": {
    "security": 800,
    "architecture": 600,
    "quality": 500,
    "tests": 450,
    "ops": 550
  },
  "breakdown": {
    "security":     { "days": <number>, "euros": <number> },
    "architecture": { "days": <number>, "euros": <number> },
    "quality":      { "days": <number>, "euros": <number> },
    "tests":        { "days": <number>, "euros": <number> },
    "ops":          { "days": <number>, "euros": <number> }
  },
  "criticalFindings": <count>,
  "highFindings": <count>,
  "testCoverageRatio": <0-1 float>,
  "topRiskyFiles": ["file1", "file2", "file3"]
}
```

On subsequent audit runs:
1. Load the previous baseline from `docs/audit/dette_technique_baseline.json` (if it exists)
2. Compute **delta values** (improvement or regression per category)
3. **Auto-increment the version** field: parse the previous `"version"` as a float, add 1.0, format as `"X.0"` (e.g. `"6.0"` → `"7.0"`). If no previous baseline exists, start at `"1.0"`.
4. Display temporal evolution in the report.

---

## Step 7 — Proactive alerts

Apply these alert rules and flag any that trigger:

| Alert | Condition |
|---|---|
| 🔴 CRITIQUE — Mots de passe en clair | Plain-text password comparison found in services |
| 🔴 CRITIQUE — Secret exposé | OAuth/API key hardcoded or in tracked config |
| 🟠 URGENT — Module sur le chemin critique | A file has >5 findings AND >10 inbound references |
| 🟠 URGENT — Dette de sécurité > 30j | Security debt alone exceeds 30 days |
| 🟡 ATTENTION — Dépendance sans patch depuis 2+ ans | Dep with known CVE and no patch release in 2+ years |
| 🟡 ATTENTION — Couverture de tests < 20% | Test ratio is below 0.20 |
| 🟡 ATTENTION — Couplage fort détecté | A controller directly accesses a repository (skips service layer) |
| 🔵 INFO — Prochaine migration majeure | A core dependency is 1 major version behind (e.g. Spring Boot 2→3) |

For each alert, produce a **one-paragraph impact narrative** written for a non-technical audience
(CTO / product owner), explaining the business risk.

---

## Step 8 — Prioritized refactoring backlog

Produce a ranked backlog of at most **15 action items** formatted as:

```
#1  [CRITIQUE] Hacher les mots de passe utilisateur
    Effort : 2j | Économie de dette : 5j | ROI : 2.5×
    Fichier(s) : UserServices.java, AdminServices.java
    Détail : Les mots de passe sont comparés en clair. Utiliser BCryptPasswordEncoder
             déjà déclaré dans SecurityConfig mais jamais câblé.
```

Each item must include:
- Rank and severity label
- Title (action-oriented, in French)
- Effort / Debt reduction / ROI ratio
- Affected files
- One-paragraph technical detail

---

## Step 9 — Generate and save the HTML report

Build the full HTML report as a self-contained string (inline all CSS — no external assets).

The HTML report **must** contain the following sections in this order:

### 9.0 Executive summary banner (very top, above KPIs)
A single high-contrast coloured banner — **one sentence** — giving an immediate verdict for a non-technical reader:
- 🔴 If critical findings > 0: `"⚠️ ACTION REQUISE — X finding(s) critique(s) détecté(s). Intervention recommandée sous 2 semaines."`
- 🟠 If no criticals but high findings > 2: `"🟠 VIGILANCE — Pas de critique, mais X problèmes importants à planifier ce sprint."`
- 🟢 If criticals = 0 and high ≤ 2: `"✅ BONNE SANTÉ — Aucun finding critique. Continuer à surveiller la dette tests."`

### 9.1 Executive dashboard (top of page)
- 4 KPI tiles: **Total dette (j)**, **Total dette (€)**, **Findings critiques**, **Ratio tests**
- Horizontal stacked bar chart (SVG) showing debt breakdown by category
- Temporal delta row (if baseline existed): green/red arrows per category

### 9.2 Alerts banner
List all triggered alerts from Step 7 with colour-coded badges and their business narrative.

### 9.3 Prioritized backlog
Full ranked list from Step 8 in a styled table.

### 9.4 Security findings detail
Table: File | Line | Rule | Severity | Description — sortable by severity.

### 9.5 Dependency audit
Table: Dependency | Current version | Latest known | CVE count | Upgrade effort.

### 9.6 Architecture & quality findings
Per-file summary of code smells, complexity hotspots, and architectural issues.

### 9.7 Methodology note
Short paragraph explaining the cost model, day-rate used, and instructions for re-running the audit.

### 9.8 — Save to disk first, then render

**Do these two actions in order:**

1. Use `write_file` to save the complete HTML string to `docs/audit/dette_technique.html`
   (create `docs/audit/` if it doesn't exist).
2. Use `create_html_artifact` with `id: "dette_technique_report"` to render the same HTML in the chat.

Both outputs must be identical — do not generate two different versions.

---

## Step 10 — Save the baseline to disk

Write the baseline JSON from Step 6 to `docs/audit/dette_technique_baseline.json` using `write_file`.

---

## Step 11 — Generate the PDF report

This step produces `docs/audit/dette_technique.pdf` — a self-contained PDF with a full-page debt
visualization chart and a condensed written summary.

### 11.1 — Write the PDF generation script

Use `write_file` to save the script `.bob/skills/audit-dette-technique/generate-pdf.js` **only if
it does not already exist** (check with `glob` first). The script is already bundled in the skill
directory alongside this SKILL.md — if present, skip directly to 11.2.

Ensure the `docs/audit/` directory exists before running the script (write_file creates parent directories automatically when writing the HTML and baseline in Steps 9 and 10, so it should already exist by this point).

### 11.2 — Run the script

Pass the computed audit data as environment variables so the script can render the chart without
re-reading source files:

```powershell
$env:AUDIT_PROJECT         = "<project name>"
$env:AUDIT_DATE            = $(Get-Date -Format 'yyyy-MM-dd')
$env:AUDIT_DAYS            = "<totalDebtDays>"
$env:AUDIT_EUROS           = "<totalDebtEuros>"
$env:AUDIT_SECURITY        = "<security days>"
$env:AUDIT_TESTS           = "<tests days>"
$env:AUDIT_ARCH            = "<architecture days>"
$env:AUDIT_OPS             = "<ops days>"
$env:AUDIT_QUALITY         = "<quality days>"
$env:AUDIT_CRITICAL        = "<criticalFindings count>"
$env:AUDIT_HIGH            = "<highFindings count>"
$env:AUDIT_COVERAGE        = "<testCoverageRatio as percentage, e.g. 60>"
$env:AUDIT_DAYRATE         = "550"
$env:AUDIT_RATE_SECURITY   = "800"
$env:AUDIT_RATE_ARCH       = "600"
$env:AUDIT_RATE_OPS        = "550"
$env:AUDIT_RATE_QUALITY    = "500"
$env:AUDIT_RATE_TESTS      = "450"
$env:AUDIT_OUTPUT_DIR      = "docs/audit"
# Utiliser $nodeExe résolu par la détection Step 11.3 (pas 'node' directement)
& $nodeExe "$env:USERPROFILE\.bob\skills\audit-dette-technique\generate-pdf.js"
```

The script writes `docs/audit/dette_technique.pdf` and prints `PDF saved: docs/audit/dette_technique.pdf`
on success.

### 11.3 — Détection de Node.js et fallback

Node.js peut être installé sans être dans le PATH de la session PowerShell courante.
Appliquer la séquence de détection suivante **dans cet ordre** :

1. **Tenter `node`** directement :
   ```powershell
   node --version 2>$null
   ```
2. **Si introuvable, chercher le chemin standard Windows** :
   ```powershell
   $nodePath = @(
     "C:\Program Files\nodejs\node.exe",
     "C:\Program Files (x86)\nodejs\node.exe",
     "$env:APPDATA\nvm\current\node.exe",
     "$env:ProgramFiles\nodejs\node.exe"
   ) | Where-Object { Test-Path $_ } | Select-Object -First 1
   ```
3. **Si toujours introuvable, scanner le PATH étendu** :
   ```powershell
   $nodePath = Get-ChildItem "C:\Program Files", "C:\Program Files (x86)", $env:LOCALAPPDATA `
     -Recurse -Filter "node.exe" -ErrorAction SilentlyContinue |
     Select-Object -First 1 -ExpandProperty FullName
   ```

**Pour lancer le script**, utiliser `$nodeExe` résolu :
```powershell
$nodeExe = if (Get-Command node -ErrorAction SilentlyContinue) { "node" } else { $nodePath }
if ($nodeExe) {
    & $nodeExe "$env:USERPROFILE\.bob\skills\audit-dette-technique\generate-pdf.js"
} else {
    # → fallback message ci-dessous
}
```

**Si Node.js est vraiment introuvable** après toutes ces tentatives, dire à l'utilisateur :
> "Le PDF n'a pas pu être généré car Node.js n'est pas détecté. Le rapport HTML complet est
> disponible à `docs/audit/dette_technique.html` — ouvrez-le dans un navigateur et utilisez
> Fichier → Imprimer → Enregistrer en PDF pour obtenir le PDF manuellement."

### 11.4 — Final confirmation message

Tell the user:
- ✅ HTML report: `docs/audit/dette_technique.html`
- ✅ PDF report:  `docs/audit/dette_technique.pdf` (or fallback message)
- ✅ Baseline:    `docs/audit/dette_technique_baseline.json`
- Re-running the skill after remediation will show temporal delta (improvements or regressions)

---

## Notes for the analyst

- **This skill works on any project** — adapt file extensions and build tool parsing to the detected stack.
- **Day-rate override**: if the user says "utilise un taux de €X/jour pour [catégorie]", update only that category's rate in the table before Step 5. If no category is specified, apply the rate to all categories.
- **Scope restriction**: if the user says "audite seulement le module X", restrict Steps 1–4 to that subtree.
- **French output**: all findings titles, alert messages, backlog items, and narrative text must be in **French**. Code identifiers stay as-is.
- **Minimal speculation**: only report findings that are grounded in code read in Steps 3 and 4. Do not hallucinate CVEs — use qualitative labels ("version ancienne, risque potentiel") when exact CVE data is unavailable.
