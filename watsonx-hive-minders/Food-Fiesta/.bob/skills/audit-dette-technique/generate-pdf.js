#!/usr/bin/env node
/**
 * audit-dette-technique — PDF generation script
 *
 * Generates docs/audit/dette_technique.pdf from environment variables
 * populated by Bob during Step 11 of the audit-dette-technique skill.
 * Output directory is controlled by AUDIT_OUTPUT_DIR (default: docs/audit).
 *
 * Zero external dependencies — uses only Node.js built-ins.
 * Produces a valid PDF 1.4 file with:
 *   - Page 1 : Cover page — project name, date, KPI summary, stacked bar
 *   - Page 2 : Grouped bar chart — dette par catégorie (jours + €)
 *   - Page 3 : Findings summary table + top-3 actions
 *
 * Usage (called by Bob in Step 11.2) — use resolved $nodeExe if node not in PATH:
 *   $env:AUDIT_PROJECT    = "Food Fiesta"
 *   $env:AUDIT_DATE       = $(Get-Date -Format 'yyyy-MM-dd')
 *   $env:AUDIT_DAYS       = "29.1"
 *   $env:AUDIT_EUROS      = "20270"
 *   $env:AUDIT_SECURITY   = "17.2"
 *   $env:AUDIT_TESTS      = "2.0"
 *   $env:AUDIT_ARCH       = "5.3"
 *   $env:AUDIT_OPS        = "2.6"
 *   $env:AUDIT_QUALITY    = "2.0"
 *   $env:AUDIT_CRITICAL   = "3"
 *   $env:AUDIT_HIGH       = "4"
 *   $env:AUDIT_COVERAGE   = "60"
 *   $env:AUDIT_DAYRATE    = "550"
 *   $env:AUDIT_OUTPUT_DIR = "docs/audit"
 *   & $nodeExe "$env:USERPROFILE\.bob\skills\audit-dette-technique\generate-pdf.js"
 */

'use strict';

const fs   = require('fs');
const path = require('path');

// ─── Read env vars ────────────────────────────────────────────────────────────

const e = process.env;
const project  = e.AUDIT_PROJECT  || 'Projet inconnu';
const date     = e.AUDIT_DATE     || new Date().toISOString().slice(0, 10);
const days     = parseFloat(e.AUDIT_DAYS     || '0');
const euros    = parseFloat(e.AUDIT_EUROS    || '0');
const dayRate  = parseFloat(e.AUDIT_DAYRATE  || '550');
const critical = parseInt(e.AUDIT_CRITICAL   || '0', 10);
const high     = parseInt(e.AUDIT_HIGH       || '0', 10);
const coverage = parseFloat(e.AUDIT_COVERAGE || '0');

// Differentiated day-rates per category (mirrors SKILL.md Step 5)
const ratesByCat = {
  securite:     parseFloat(e.AUDIT_RATE_SECURITY || '800'),
  tests:        parseFloat(e.AUDIT_RATE_TESTS    || '450'),
  architecture: parseFloat(e.AUDIT_RATE_ARCH     || '600'),
  ops:          parseFloat(e.AUDIT_RATE_OPS      || '550'),
  qualite:      parseFloat(e.AUDIT_RATE_QUALITY  || '500'),
};

const cats = [
  { label: 'Securite',     key: 'securite',     days: parseFloat(e.AUDIT_SECURITY || '0'), color: [207, 34, 46]   },
  { label: 'Tests',        key: 'tests',        days: parseFloat(e.AUDIT_TESTS    || '0'), color: [188, 76, 0]    },
  { label: 'Architecture', key: 'architecture', days: parseFloat(e.AUDIT_ARCH     || '0'), color: [154, 103, 0]   },
  { label: 'OPS',          key: 'ops',          days: parseFloat(e.AUDIT_OPS      || '0'), color: [59, 130, 212]  },
  { label: 'Qualite',      key: 'qualite',      days: parseFloat(e.AUDIT_QUALITY  || '0'), color: [124, 92, 216]  },
];

// Pre-compute euros per category using differentiated rates
cats.forEach(c => { c.euros = c.days * ratesByCat[c.key]; });

// ─── Number formatting (ASCII only — no Unicode non-breaking spaces) ──────────
// toLocaleString('fr-FR') produces \u202F thin-space separators which corrupt
// the PDF latin1 stream. This formatter uses plain ASCII space instead.
function fmtNum(n) {
  return Math.round(n).toString().replace(/\B(?=(\d{3})+(?!\d))/g, ' ');
}
function fmtEur(n) { return fmtNum(n) + ' EUR'; }

// ─── PDF primitives ───────────────────────────────────────────────────────────

/**
 * Minimal PDF 1.4 builder — no fonts embedded beyond the 14 standard ones,
 * no images, pure vector graphics + text.
 */
class PdfBuilder {
  constructor() {
    this.buf      = [];   // raw bytes as strings
    this.objects  = [];   // byte offsets of each object
    this.pages    = [];   // page object IDs
    this.pageStreams = []; // { id, streamId, w, h }
    this.nextId   = 1;
    this._write('%PDF-1.4\n');
    this._write('%\xE2\xE3\xCF\xD3\n'); // binary comment
  }

  // ── low-level ──

  _write(s) { this.buf.push(s); }

  _byteOffset() { return this.buf.reduce((a, s) => a + Buffer.byteLength(s, 'binary'), 0); }

  _startObj(id) {
    while (this.objects.length < id) this.objects.push(null);
    this.objects[id - 1] = this._byteOffset();
    this._write(`${id} 0 obj\n`);
  }

  _endObj() { this._write('endobj\n'); }

  _reserveId() { return this.nextId++; }

  // ── document structure ──

  addPage(width = 595, height = 842) {
    const pageId  = this._reserveId();
    const streamId = this._reserveId();
    this.pages.push({ id: pageId, streamId, width, height });
    return { pageId, streamId, width, height };
  }

  setPageStream(streamId, content) {
    const bytes = Buffer.from(content, 'latin1').length;
    this.pageStreams.push({ streamId, content, bytes });
  }

  // ── finalise ──

  build(outputPath) {
    const catalogId  = this._reserveId();
    const pagesId    = this._reserveId();
    const fontId     = this._reserveId();
    const fontBoldId = this._reserveId();

    // Font objects
    this._startObj(fontId);
    this._write('<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>\n');
    this._endObj();

    this._startObj(fontBoldId);
    this._write('<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>\n');
    this._endObj();

    // Page streams
    for (const ps of this.pageStreams) {
      this._startObj(ps.streamId);
      this._write(`<< /Length ${ps.bytes} >>\nstream\n`);
      this._write(ps.content);
      this._write('\nendstream\n');
      this._endObj();
    }

    // Page objects
    for (const p of this.pages) {
      this._startObj(p.id);
      this._write(
        `<< /Type /Page /Parent ${pagesId} 0 R ` +
        `/MediaBox [0 0 ${p.width} ${p.height}] ` +
        `/Contents ${p.streamId} 0 R ` +
        `/Resources << /Font << /F1 ${fontId} 0 R /F2 ${fontBoldId} 0 R >> >> >>\n`
      );
      this._endObj();
    }

    // Pages dict
    this._startObj(pagesId);
    this._write(`<< /Type /Pages /Kids [${this.pages.map(p => `${p.id} 0 R`).join(' ')}] /Count ${this.pages.length} >>\n`);
    this._endObj();

    // Catalog
    this._startObj(catalogId);
    this._write(`<< /Type /Catalog /Pages ${pagesId} 0 R >>\n`);
    this._endObj();

    // Cross-reference table
    const xrefOffset = this._byteOffset();
    const allIds = this.nextId - 1;
    this._write(`xref\n0 ${allIds + 1}\n`);
    this._write('0000000000 65535 f \n');
    for (let i = 0; i < allIds; i++) {
      const off = this.objects[i];
      this._write(off != null ? `${String(off).padStart(10, '0')} 00000 n \n` : '0000000000 65535 f \n');
    }

    // Trailer
    this._write(`trailer\n<< /Size ${allIds + 1} /Root ${catalogId} 0 R >>\n`);
    this._write(`startxref\n${xrefOffset}\n%%EOF\n`);

    fs.mkdirSync(path.dirname(outputPath), { recursive: true });
    fs.writeFileSync(outputPath, this.buf.join(''), 'binary');
  }
}

// ─── PDF drawing helpers ──────────────────────────────────────────────────────

function pdfText(x, y, size, font, text, colorRgb) {
  const [r, g, b] = (colorRgb || [31, 35, 40]).map(v => (v / 255).toFixed(3));
  const safe = String(text).replace(/\\/g, '\\\\').replace(/\(/g, '\\(').replace(/\)/g, '\\)');
  return `BT /F${font} ${size} Tf ${r} ${g} ${b} rg ${x} ${y} Td (${safe}) Tj ET\n`;
}

function pdfRect(x, y, w, h, fillRgb, strokeRgb) {
  let s = '';
  if (fillRgb) {
    const [r, g, b] = fillRgb.map(v => (v / 255).toFixed(3));
    s += `${r} ${g} ${b} rg ${x} ${y} ${w} ${h} re f\n`;
  }
  if (strokeRgb) {
    const [r, g, b] = strokeRgb.map(v => (v / 255).toFixed(3));
    s += `${r} ${g} ${b} RG 0.5 w ${x} ${y} ${w} ${h} re S\n`;
  }
  return s;
}

function pdfLine(x1, y1, x2, y2, rgb, width) {
  const [r, g, b] = (rgb || [200, 200, 200]).map(v => (v / 255).toFixed(3));
  return `${r} ${g} ${b} RG ${width || 0.5} w ${x1} ${y1} m ${x2} ${y2} l S\n`;
}

// ─── Page builders ────────────────────────────────────────────────────────────

function buildCoverPage(W, H) {
  const totalEuros = fmtNum(euros);
  let s = '';

  // Top accent bar
  s += pdfRect(0, H - 48, W, 48, [31, 35, 40]);

  // Title
  s += pdfText(40, H - 33, 18, 2, 'Audit Dette Technique', [255, 255, 255]);
  s += pdfText(W - 140, H - 33, 10, 1, `Genere le ${date}`, [180, 180, 180]);

  // Project name
  s += pdfText(40, H - 90, 14, 2, project, [31, 35, 40]);
  s += pdfLine(40, H - 96, W - 40, H - 96, [229, 231, 235], 0.8);

  // Subtitle
  s += pdfText(40, H - 115, 10, 1, 'Rapport combine business + technique | Taux journalier : ' + dayRate + ' EUR/j', [87, 96, 106]);

  // ── KPI boxes ──
  const kpis = [
    { label: 'Dette totale',     value: `${days} jours`,    color: [207, 34, 46]  },
    { label: 'Cout estime',      value: `${totalEuros} EUR`,  color: [207, 34, 46]  },
    { label: 'Findings crit/haut', value: `${critical + high}`, color: [188, 76, 0] },
    { label: 'Couverture tests', value: `${coverage} %`,    color: coverage < 20 ? [207, 34, 46] : [26, 127, 55] },
  ];

  const kpiY = H - 210;
  const kpiW = (W - 80 - 30) / 4;
  kpis.forEach((k, i) => {
    const kx = 40 + i * (kpiW + 10);
    s += pdfRect(kx, kpiY, kpiW, 70, [247, 248, 250], [229, 231, 235]);
    s += pdfText(kx + 8, kpiY + 50, 8, 1, k.label, [87, 96, 106]);
    // value — truncate if needed
    const valStr = String(k.value).slice(0, 18);
    const valSize = valStr.length > 10 ? 13 : 16;
    s += pdfText(kx + 8, kpiY + 28, valSize, 2, valStr, k.color);
  });

  // ── Stacked horizontal bar ──
  const barY  = kpiY - 80;
  const barH  = 28;
  const barW  = W - 80;
  const total = cats.reduce((a, c) => a + c.days, 0) || 1;
  let cx = 40;
  s += pdfText(40, barY + barH + 10, 9, 2, 'Repartition de la dette (jours)', [31, 35, 40]);
  for (const cat of cats) {
    const w = Math.round((cat.days / total) * barW);
    if (w < 2) continue;
    s += pdfRect(cx, barY, w, barH, cat.color);
    cx += w;
  }

  // Legend
  let legX = 40;
  const legY = barY - 18;
  for (const cat of cats) {
    s += pdfRect(legX, legY + 3, 8, 8, cat.color);
    s += pdfText(legX + 11, legY + 3, 7.5, 1, `${cat.label} ${cat.days}j`, [87, 96, 106]);
    legX += 95;
  }

  // ── Methodology note ──
  s += pdfLine(40, barY - 36, W - 40, barY - 36, [229, 231, 235], 0.5);
  s += pdfText(40, barY - 52, 8, 1, 'Methodologie : analyse statique du code source. Voir rapport HTML pour le detail complet.', [87, 96, 106]);
  s += pdfText(40, barY - 64, 8, 1, `Taux journalier : ${dayRate} EUR/j | Marge : +/-30%`, [87, 96, 106]);

  // Footer
  s += pdfRect(0, 0, W, 28, [247, 248, 250]);
  s += pdfLine(0, 28, W, 28, [229, 231, 235], 0.5);
  s += pdfText(40, 10, 8, 1, 'audit-dette-technique skill | IBM Bob', [87, 96, 106]);
  s += pdfText(W - 80, 10, 8, 1, 'Page 1 / 3', [87, 96, 106]);

  return s;
}

function buildChartPage(W, H) {
  let s = '';

  // Header bar
  s += pdfRect(0, H - 40, W, 40, [31, 35, 40]);
  s += pdfText(40, H - 26, 13, 2, 'Dette par categorie — jours & euros', [255, 255, 255]);
  s += pdfText(W - 100, H - 26, 9, 1, date, [180, 180, 180]);

  // ── Vertical grouped bar chart ──
  const chartLeft = 90;
  const chartRight = W - 40;
  const chartTop = H - 70;
  const chartBottom = 80;
  const chartH = chartTop - chartBottom;
  const chartW = chartRight - chartLeft;

  const maxDays  = Math.max(...cats.map(c => c.days), 1);
  const maxEuros = Math.max(...cats.map(c => c.euros), 1);

  const groupW = chartW / cats.length;
  const barGroupPad = 8;
  const barW = (groupW - barGroupPad * 2 - 4) / 2;

  // Grid lines (5 horizontal)
  for (let i = 0; i <= 5; i++) {
    const gy = chartBottom + (i / 5) * chartH;
    s += pdfLine(chartLeft, gy, chartRight, gy, [229, 231, 235], 0.4);
    const labelDays  = ((maxDays  * (5 - i)) / 5).toFixed(1);
    const labelEuros = fmtNum((maxEuros * (5 - i)) / 5);
    s += pdfText(chartLeft - 84, gy - 4, 7, 1, `${labelDays}j`, [87, 96, 106]);
    s += pdfText(chartLeft - 84, gy - 14, 6.5, 1, `${labelEuros} EUR`, [154, 103, 0]);
  }

  // Axes
  s += pdfLine(chartLeft, chartBottom, chartLeft, chartTop, [31, 35, 40], 0.8);
  s += pdfLine(chartLeft, chartBottom, chartRight, chartBottom, [31, 35, 40], 0.8);

  // Bars
  cats.forEach((cat, i) => {
    const gx = chartLeft + i * groupW + barGroupPad;

    // Days bar (left in group)
    const daysH = (cat.days / maxDays) * chartH;
    s += pdfRect(gx, chartBottom, barW, daysH, cat.color);
    s += pdfText(gx + 2, chartBottom + daysH + 4, 7.5, 2, `${cat.days}j`, cat.color);

    // Euros bar (right in group, lighter shade) — uses differentiated rate per category
    const eur = cat.euros;
    const eurH = (eur / maxEuros) * chartH;
    const lighterColor = cat.color.map(v => Math.min(255, v + 60));
    s += pdfRect(gx + barW + 4, chartBottom, barW, eurH, lighterColor);
    const eurLabel = fmtNum(eur);
    s += pdfText(gx + barW + 4, chartBottom + eurH + 4, 6, 1, `${eurLabel}E`, lighterColor);

    // Category label
    s += pdfText(gx, chartBottom - 18, 7.5, 2, cat.label, [31, 35, 40]);
  });

  // Legend
  const legY = chartBottom - 42;
  s += pdfRect(chartLeft, legY, 10, 8, [87, 96, 106]);
  s += pdfText(chartLeft + 13, legY, 8, 1, 'Barre gauche = jours de dette', [87, 96, 106]);
  s += pdfRect(chartLeft + 180, legY, 10, 8, [154, 103, 0]);
  s += pdfText(chartLeft + 193, legY, 8, 1, 'Barre droite = cout EUR (taux differencie par categorie)', [87, 96, 106]);

  // Footer
  s += pdfRect(0, 0, W, 28, [247, 248, 250]);
  s += pdfLine(0, 28, W, 28, [229, 231, 235], 0.5);
  s += pdfText(40, 10, 8, 1, 'audit-dette-technique skill | IBM Bob', [87, 96, 106]);
  s += pdfText(W - 80, 10, 8, 1, 'Page 2 / 3', [87, 96, 106]);

  return s;
}

function buildSummaryPage(W, H) {
  let s = '';

  // Header
  s += pdfRect(0, H - 40, W, 40, [31, 35, 40]);
  s += pdfText(40, H - 26, 13, 2, 'Synthese des findings & priorites', [255, 255, 255]);
  s += pdfText(W - 100, H - 26, 9, 1, date, [180, 180, 180]);

  // ── Findings table ──
  const rows = [
    ['Severite',   'Nb findings', 'Dette (j)', 'Cout EUR'],
    ['CRITIQUE',   String(critical), `${(critical * 3).toFixed(1)}`, fmtEur(critical * 3 * dayRate)],
    ['HAUTE',      String(high),     `${(high * 1.5).toFixed(1)}`,   fmtEur(high * 1.5 * dayRate)],
    ['TOTAL SECU', String(critical + high), `${cats[0].days}`,       fmtEur(cats[0].days * cats[0].euros / (cats[0].days || 1))],
  ];

  const tableTop = H - 70;
  const colW = [(W - 80) * 0.28, (W - 80) * 0.2, (W - 80) * 0.2, (W - 80) * 0.32];
  const rowH = 22;

  rows.forEach((row, ri) => {
    const ry = tableTop - ri * rowH;
    const bg = ri === 0 ? [31, 35, 40] : ri % 2 === 0 ? [255, 255, 255] : [247, 248, 250];
    const fg = ri === 0 ? [255, 255, 255] : [31, 35, 40];
    let cx = 40;
    row.forEach((cell, ci) => {
      s += pdfRect(cx, ry - rowH + 4, colW[ci] - 2, rowH - 2, bg, [229, 231, 235]);
      s += pdfText(cx + 4, ry - rowH + 10, ri === 0 ? 8 : 8.5, ri === 0 ? 2 : 1, cell, fg);
      cx += colW[ci];
    });
  });

  // ── Top 3 actions ──
  const actY = tableTop - rows.length * rowH - 30;
  s += pdfLine(40, actY + 14, W - 40, actY + 14, [229, 231, 235], 0.5);
  s += pdfText(40, actY, 10, 2, 'Top 3 actions prioritaires', [31, 35, 40]);

  const top3 = [
    { rank: '#1', sev: 'CRITIQUE', roi: '6x', title: 'Cabler BCryptPasswordEncoder pour hacher les mots de passe', effort: '2j' },
    { rank: '#2', sev: 'CRITIQUE', roi: '6x', title: 'Supprimer le mot de passe admin123 code en dur',            effort: '0.5j' },
    { rank: '#3', sev: 'HAUTE',    roi: '3x', title: 'Desactiver la console H2 hors profil local',                effort: '0.5j' },
  ];

  top3.forEach((a, i) => {
    const ay = actY - 26 - i * 48;
    const sevColor = a.sev === 'CRITIQUE' ? [207, 34, 46] : [188, 76, 0];
    s += pdfRect(40, ay - 6, W - 80, 40, [247, 248, 250], [229, 231, 235]);
    s += pdfText(48, ay + 20, 10, 2, a.rank, [87, 96, 106]);
    s += pdfRect(68, ay + 14, 42, 12, sevColor);
    s += pdfText(70, ay + 16, 7, 2, a.sev, [255, 255, 255]);
    s += pdfText(118, ay + 20, 9, 2, a.title.slice(0, 65), [31, 35, 40]);
    s += pdfText(48, ay + 6, 8, 1, `Effort : ${a.effort}  |  ROI : ${a.roi}`, [87, 96, 106]);
  });

  // Note de cloture
  const noteY = actY - 26 - top3.length * 48 - 24;
  s += pdfLine(40, noteY + 10, W - 40, noteY + 10, [229, 231, 235], 0.5);
  s += pdfText(40, noteY - 4, 8, 1, 'Rapport complet : docs/audit/dette_technique.html  |  Baseline : docs/audit/dette_technique_baseline.json', [87, 96, 106]);
  s += pdfText(40, noteY - 18, 8, 1, 'Relancer /audit-dette-technique apres remediation pour mesurer l\'evolution.', [87, 96, 106]);

  // Footer
  s += pdfRect(0, 0, W, 28, [247, 248, 250]);
  s += pdfLine(0, 28, W, 28, [229, 231, 235], 0.5);
  s += pdfText(40, 10, 8, 1, 'audit-dette-technique skill | IBM Bob', [87, 96, 106]);
  s += pdfText(W - 80, 10, 8, 1, 'Page 3 / 3', [87, 96, 106]);

  return s;
}

// ─── Main ─────────────────────────────────────────────────────────────────────

function main() {
  const outputDir  = process.env.AUDIT_OUTPUT_DIR || 'docs/audit';
  const outputPath = path.join(process.cwd(), outputDir, 'dette_technique.pdf');

  const pdf = new PdfBuilder();
  const W = 595, H = 842; // A4 portrait

  // Page 1 — cover + KPIs + stacked bar
  const p1 = pdf.addPage(W, H);
  pdf.setPageStream(p1.streamId, buildCoverPage(W, H));

  // Page 2 — grouped vertical bar chart (days + euros)
  const p2 = pdf.addPage(W, H);
  pdf.setPageStream(p2.streamId, buildChartPage(W, H));

  // Page 3 — findings table + top-3 actions
  const p3 = pdf.addPage(W, H);
  pdf.setPageStream(p3.streamId, buildSummaryPage(W, H));

  pdf.build(outputPath);
  console.log(`PDF saved: ${outputPath}`);
}

main();
