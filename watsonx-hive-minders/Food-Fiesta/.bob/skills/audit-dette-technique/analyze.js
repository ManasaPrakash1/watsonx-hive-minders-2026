#!/usr/bin/env node
/**
 * audit-dette-technique — Static analysis script
 *
 * Scans the current working directory for security, quality, and dependency
 * issues. Outputs a single JSON object to stdout. Bob reads this output in
 * Step 2 of the skill.
 *
 * Usage:  & $nodeExe "$env:USERPROFILE\.bob\skills\audit-dette-technique\analyze.js"
 *         (use resolved $nodeExe from Step 11.3 detection if 'node' is not in PATH)
 * Output: JSON → { securityFindings, complexityHotspots, codeSmells, architectureIssues, migrationRisks, meta }
 *
 * Fallback: if Node is unavailable, the skill instructions document the
 *           equivalent manual grep checks for each rule.
 */

'use strict';

const fs   = require('fs');
const path = require('path');

// ─── Config ──────────────────────────────────────────────────────────────────

const SOURCE_EXTENSIONS = ['.java', '.ts', '.js', '.py', '.go', '.cs', '.kt', '.rb', '.php'];
const IGNORED_DIRS      = new Set(['node_modules', '.git', 'target', 'build', 'dist', '.bob', '__pycache__']);
const ROOT              = process.cwd();

// ─── Security rules ───────────────────────────────────────────────────────────

const SECURITY_RULES = [
  // Authentication & passwords
  // SEC-001: matches .equals(password) AND getXxx().equals(someVar) — escaped dot, case-insensitive
  { id: 'SEC-001', severity: 'CRITIQUE', pattern: /\bequals\s*\(\s*password\s*\)|\bgetPassword\(\)\.equals|getUpassword\(\)\.equals|getAdminPassword\(\)\.equals/i, description: 'Comparaison de mot de passe en clair (sans hachage)' },
  // SEC-002: matches setXxxPassword("literal") AND password = "literal" AND passwordEncoder unused patterns
  { id: 'SEC-002', severity: 'CRITIQUE', pattern: /setAdminPassword\s*\(\s*["'][^"']{1,60}["']\s*\)|setUpassword\s*\(\s*["'][^"']{1,60}["']\s*\)|password\s*=\s*["'][^"']{1,60}["']/i, description: 'Mot de passe potentiellement codé en dur' },
  { id: 'SEC-003', severity: 'HAUTE',    pattern: /client[_-]?secret\s*=\s*["'][^"']{4,}["']/i,  description: 'Secret OAuth/API potentiellement codé en dur' },
  { id: 'SEC-004', severity: 'HAUTE',    pattern: /YOUR_CLIENT_SECRET|YOUR_CLIENT_ID/,            description: 'Placeholder de secret OAuth non remplacé' },
  // Injection
  { id: 'SEC-005', severity: 'HAUTE',    pattern: /createNativeQuery|createQuery.*\+/,            description: 'Risque d\'injection SQL (concaténation dans une requête)' },
  { id: 'SEC-006', severity: 'HAUTE',    pattern: /Runtime\.getRuntime\(\)\.exec|ProcessBuilder/, description: 'Exécution de commande OS (risque d\'injection de commande)' },
  // Session & CSRF
  { id: 'SEC-007', severity: 'MOYENNE',  pattern: /csrf.*disable\(\)|\.csrf\(csrf -> csrf\.disable/i, description: 'CSRF désactivé globalement' },
  { id: 'SEC-008', severity: 'MOYENNE',  pattern: /session\.setAttribute.*password/i,            description: 'Mot de passe stocké en session' },
  // Input validation
  { id: 'SEC-009', severity: 'MOYENNE',  pattern: /@RequestParam(?!.*@Valid).*String/,            description: 'Paramètre de requête sans validation (@Valid manquant)' },
  // Logging
  { id: 'SEC-010', severity: 'FAIBLE',   pattern: /System\.out\.println|console\.log.*password/i, description: 'Journalisation potentiellement sensible via stdout' },
  // Headers
  { id: 'SEC-011', severity: 'FAIBLE',   pattern: /frameOptions.*disable/i,                      description: 'Protection X-Frame-Options désactivée' },
];

// ─── Code smell rules ─────────────────────────────────────────────────────────

const SMELL_RULES = [
  { id: 'SMELL-001', pattern: /\.get\(\)\s*;/g,          description: 'Optional.get() sans isPresent() — risque NullPointerException' },
  { id: 'SMELL-002', pattern: /catch\s*\(\s*Exception\s/,description: 'Catch trop large (Exception générique)' },
  { id: 'SMELL-003', pattern: /TODO|FIXME|HACK|XXX/,      description: 'Commentaire technique laissé dans le code' },
  { id: 'SMELL-004', pattern: /\bnew\s+\w+\(\)\s*;\s*$/m, description: 'Instanciation directe (préférer injection de dépendances)' },
  { id: 'SMELL-005', pattern: /System\.exit/,             description: 'System.exit() dans le code applicatif' },
];

// ─── File helpers ─────────────────────────────────────────────────────────────

function walkSync(dir, results = []) {
  let entries;
  try { entries = fs.readdirSync(dir, { withFileTypes: true }); }
  catch { return results; }
  for (const e of entries) {
    if (IGNORED_DIRS.has(e.name)) continue;
    const full = path.join(dir, e.name);
    if (e.isDirectory()) walkSync(full, results);
    else if (SOURCE_EXTENSIONS.includes(path.extname(e.name))) results.push(full);
  }
  return results;
}

function readLines(filePath) {
  try { return fs.readFileSync(filePath, 'utf8').split('\n'); }
  catch { return []; }
}

function relPath(p) { return p.replace(ROOT + path.sep, '').replace(/\\/g, '/'); }

// ─── Analysis passes ──────────────────────────────────────────────────────────

function scanSecurity(files) {
  const findings = [];
  for (const file of files) {
    // Skip test files — password literals in tests are expected (not real secrets)
    if (/[Tt]est|\.spec\.|\.test\./.test(relPath(file))) continue;
    const lines = readLines(file);
    for (let i = 0; i < lines.length; i++) {
      const line = lines[i];
      for (const rule of SECURITY_RULES) {
        // Always reset lastIndex BEFORE test() to avoid skipping matches on subsequent lines
        if (rule.pattern.global) rule.pattern.lastIndex = 0;
        if (rule.pattern.test(line)) {
          if (rule.pattern.global) rule.pattern.lastIndex = 0;
          findings.push({
            file: relPath(file),
            line: i + 1,
            rule: rule.id,
            severity: rule.severity,
            description: rule.description,
            snippet: line.trim().slice(0, 120),
          });
        }
      }
    }
  }
  return findings;
}

function scanSmells(files) {
  const findings = [];
  for (const file of files) {
    const lines = readLines(file);
    const fullText = lines.join('\n');
    for (const rule of SMELL_RULES) {
      // Line-level match
      for (let i = 0; i < lines.length; i++) {
        // Reset lastIndex BEFORE test() for global patterns
        if (rule.pattern.global) rule.pattern.lastIndex = 0;
        if (rule.pattern.test(lines[i])) {
          if (rule.pattern.global) rule.pattern.lastIndex = 0;
          findings.push({
            file: relPath(file),
            line: i + 1,
            rule: rule.id,
            description: rule.description,
            snippet: lines[i].trim().slice(0, 120),
          });
          break; // one finding per file per rule
        }
      }
      if (rule.pattern.global) rule.pattern.lastIndex = 0;
    }
  }
  return findings;
}

function estimateComplexity(files) {
  const hotspots = [];
  for (const file of files) {
    const lines = readLines(file);
    let score = 0;
    for (const line of lines) {
      // Count decision points as a proxy for cyclomatic complexity
      if (/\bif\b|\belse\b|\bfor\b|\bwhile\b|\bcase\b|\bcatch\b|\b&&\b|\|\|/.test(line)) score++;
    }
    const methodCount = lines.filter(l => /\s(public|private|protected)\s.*\(/.test(l)).length;
    const lineCount   = lines.length;

    // Lower thresholds to catch mid-size God classes (e.g. AdminController at 199 lines)
    if (score > 10 || lineCount > 150 || methodCount > 8) {
      hotspots.push({
        file:        relPath(file),
        lineCount,
        decisionPoints: score,
        methodCount,
        complexityScore: score + Math.floor(lineCount / 50) + methodCount,
        risk: score > 30 || lineCount > 400 ? 'ÉLEVÉ' : score > 15 || lineCount > 200 ? 'MOYEN' : 'FAIBLE',
      });
    }
  }
  return hotspots.sort((a, b) => b.complexityScore - a.complexityScore).slice(0, 10);
}

// ─── Dependency scanner ───────────────────────────────────────────────────────

function scanMavenDeps(pomPath) {
  const risks = [];
  if (!fs.existsSync(pomPath)) return risks;
  const content = fs.readFileSync(pomPath, 'utf8');

  // Detect Spring Boot parent version
  const sbMatch = content.match(/<artifactId>spring-boot-starter-parent<\/artifactId>\s*<version>([\d.]+)<\/version>/);
  if (sbMatch) {
    const [major] = sbMatch[1].split('.').map(Number);
    risks.push({
      dependency: 'spring-boot-starter-parent',
      currentVersion: sbMatch[1],
      latestKnown: '3.4.x',
      cveCount: major < 3 ? '⚠️ Plusieurs CVE connues (versions < 3.x)' : 'Aucune critique connue',
      upgradeEffort: major < 3 ? 'ÉLEVÉ — migration majeure' : 'FAIBLE',
      notes: major < 3 ? 'Migration Spring Boot 2→3 requise (Jakarta EE, changements de config)' : 'Version récente.',
    });
  }

  // Extract all <dependency> blocks
  const depBlockPattern = /<dependency>([\s\S]*?)<\/dependency>/g;
  let match;
  while ((match = depBlockPattern.exec(content)) !== null) {
    const block = match[1];
    const artMatch     = block.match(/<artifactId>(.*?)<\/artifactId>/);
    const versionMatch = block.match(/<version>(.*?)<\/version>/);
    if (!artMatch) continue;

    const artifact = artMatch[1];
    const version  = versionMatch ? versionMatch[1] : 'géré par parent';

    // Flag known risky artifacts
    const knownRisks = {
      'h2':              { cveNote: 'CVE-2022-45868 (H2 Console RCE) si console activée',  effort: 'FAIBLE — patcher à 2.2+' },
      'log4j':           { cveNote: 'CVE-2021-44228 (Log4Shell) si version < 2.15',         effort: 'CRITIQUE — mettre à jour immédiatement' },
      'jackson-databind':{ cveNote: 'Plusieurs CVE sur versions < 2.13 (désérialisation)',   effort: 'MOYEN' },
      'spring-security': { cveNote: 'Vérifier CVE-2023-34034 / CVE-2022-22965',             effort: 'FAIBLE si géré par parent Boot 3' },
    };

    const known = Object.entries(knownRisks).find(([k]) => artifact.includes(k));
    if (known) {
      risks.push({
        dependency:    artifact,
        currentVersion: version,
        latestKnown:   'Voir Maven Central',
        cveCount:      known[1].cveNote,
        upgradeEffort: known[1].effort,
        notes:         '',
      });
    }
  }
  return risks;
}

function scanNpmDeps(pkgPath) {
  const risks = [];
  if (!fs.existsSync(pkgPath)) return risks;
  try {
    const pkg = JSON.parse(fs.readFileSync(pkgPath, 'utf8'));
    const deps = { ...pkg.dependencies, ...pkg.devDependencies };
    for (const [name, version] of Object.entries(deps)) {
      const v = version.replace(/[\^~>=<]/g, '');
      const major = parseInt(v.split('.')[0], 10);
      if (!isNaN(major) && major < 1) {
        risks.push({ dependency: name, currentVersion: version, latestKnown: '?', cveCount: 'Version pre-stable (0.x)', upgradeEffort: 'MOYEN', notes: '' });
      }
    }
  } catch { /* ignore parse errors */ }
  return risks;
}

// ─── Test coverage estimate ───────────────────────────────────────────────────

function estimateTestCoverage(files) {
  const testFiles   = files.filter(f => /[Tt]est|\.spec\.|\.test\./.test(f));
  const sourceFiles = files.filter(f => !/[Tt]est|\.spec\.|\.test\./.test(f));
  const ratio = sourceFiles.length > 0 ? testFiles.length / sourceFiles.length : 0;
  return { testFiles: testFiles.length, sourceFiles: sourceFiles.length, ratio: Math.round(ratio * 100) / 100 };
}

// ─── Architecture checks ──────────────────────────────────────────────────────

function scanArchitecture(files) {
  const issues = [];
  for (const file of files) {
    const rel  = relPath(file);
    const text = readLines(file).join('\n');

    // Controller directly accessing repository (skips service layer)
    if (/Controller/.test(rel) && /Repository/.test(text) && /Autowired/.test(text)) {
      if (/private\s+\w*Repository\s+\w+/.test(text)) {
        issues.push({ file: rel, type: 'COUPLAGE FORT', description: 'Un contrôleur injecte directement un repository (couche service contournée)' });
      }
    }

    // God class heuristic: >300 lines AND >15 methods
    const lines   = text.split('\n');
    const methods = lines.filter(l => /\s(public|private|protected)\s.*\(/.test(l)).length;
    if (lines.length > 300 && methods > 15) {
      issues.push({ file: rel, type: 'GOD CLASS', description: `Classe volumineuse (${lines.length} lignes, ${methods} méthodes) — candidat à la décomposition` });
    }

    // Missing @Transactional on services that perform any mutation (save/delete/update)
    const hasMutations = /\.save\(|\.delete\(|\.deleteById\(/.test(text);
    if (/Service/.test(rel) && hasMutations && !/@Transactional/.test(text)) {
      issues.push({ file: rel, type: 'COHÉRENCE TRANSACTIONNELLE', description: 'Service avec mutations (save/delete) sans @Transactional — risque d\'incohérence de données' });
    }
  }
  return issues;
}

// ─── Main ─────────────────────────────────────────────────────────────────────

function main() {
  const files = walkSync(ROOT);

  const securityFindings  = scanSecurity(files);
  const codeSmells        = scanSmells(files);
  const complexityHotspots= estimateComplexity(files);
  const architectureIssues= scanArchitecture(files);
  const coverage          = estimateTestCoverage(files);

  const mavenRisks = scanMavenDeps(path.join(ROOT, 'pom.xml'));
  const npmRisks   = scanNpmDeps(path.join(ROOT, 'package.json'));
  const migrationRisks = [...mavenRisks, ...npmRisks];

  const meta = {
    scannedAt:   new Date().toISOString(),
    totalFiles:  files.length,
    coverage,
    findingCounts: {
      critique: securityFindings.filter(f => f.severity === 'CRITIQUE').length,
      haute:    securityFindings.filter(f => f.severity === 'HAUTE').length,
      moyenne:  securityFindings.filter(f => f.severity === 'MOYENNE').length,
      faible:   securityFindings.filter(f => f.severity === 'FAIBLE').length,
    },
  };

  const output = {
    meta,
    securityFindings,
    complexityHotspots,
    codeSmells,
    architectureIssues,
    migrationRisks,
  };

  process.stdout.write(JSON.stringify(output, null, 2) + '\n');
}

main();
