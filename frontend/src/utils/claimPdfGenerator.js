import jsPDF from "jspdf";
import autoTable from "jspdf-autotable";

/* ─── Palette ─────────────────────────────────────────────────── */
const C = {
  primary:  [37,  99, 235],
  dark:     [15,  23,  42],
  muted:    [100, 116, 139],
  rowEven:  [248, 250, 252],
  rowOdd:   [255, 255, 255],
  white:    [255, 255, 255],
  success:  [22,  163,  74],
  danger:   [220,  38,  38],
  amber:    [217, 119,   6],
  purple:   [109,  40, 217],
  headBg:   [241, 245, 249],
  headText: [51,  65,  85],
};

/* ─── Status colour ────────────────────────────────────────────── */
function statusRGB(status) {
  switch (status) {
    case "APPROVED":
    case "RECOMMENDED_APPROVAL":  return C.success;
    case "REJECTED":
    case "RECOMMENDED_REJECTION": return C.danger;
    case "ASSIGNED":              return C.purple;
    case "UNDER_REVIEW":          return C.amber;
    case "SUBMITTED":             return C.primary;
    default:                      return C.muted;
  }
}

/* ─── Formatters ─────────────────────────────────────────────── */
// NOTE: jsPDF built-in fonts (helvetica) do NOT support the INR rupee
// symbol (U+20B9 ₹). We use the ASCII-safe "Rs." prefix instead.
function fmtCurrency(value) {
  if (value === null || value === undefined || isNaN(Number(value))) return "-";
  const n = new Intl.NumberFormat("en-IN", {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(Number(value));
  return "Rs. " + n;
}

function fmtDate(value) {
  if (!value) return "-";
  const d = new Date(value);
  if (isNaN(d.getTime())) return String(value);
  return d.toLocaleDateString("en-IN", { day: "2-digit", month: "short", year: "numeric" });
}

function fmtDateTime(value) {
  if (!value) return "-";
  const d = new Date(value);
  if (isNaN(d.getTime())) return String(value);
  return d.toLocaleString("en-IN", {
    day: "2-digit", month: "short", year: "numeric",
    hour: "2-digit", minute: "2-digit",
  });
}

function toTitleCase(value) {
  if (!value) return "-";
  return String(value)
    .toLowerCase()
    .split(/[\s_]+/)
    .map((w) => w.charAt(0).toUpperCase() + w.slice(1))
    .join(" ");
}

function safe(value) {
  if (value === null || value === undefined || value === "") return "-";
  return String(value);
}

/* ─── Page dimensions ────────────────────────────────────────── */
function pw(doc) { return doc.internal.pageSize.getWidth(); }
function ph(doc) { return doc.internal.pageSize.getHeight(); }

/**
 * If fewer than `minRemaining` mm are left on the current page,
 * add a new page and return the reset y position (20mm from top).
 * Otherwise return y unchanged.
 */
function checkNewPage(doc, y, minRemaining = 38) {
  if (y + minRemaining > ph(doc) - 18) {
    doc.addPage();
    return 20;
  }
  return y;
}

/* ─── Header banner ──────────────────────────────────────────── */
function drawHeader(doc) {
  const W = pw(doc);

  doc.setFillColor(...C.primary);
  doc.rect(0, 0, W, 30, "F");

  // Brand
  doc.setFont("helvetica", "bold");
  doc.setFontSize(16);
  doc.setTextColor(255, 255, 255);
  doc.text("Assurly", 14, 13);

  // Sub-tagline
  doc.setFont("helvetica", "normal");
  doc.setFontSize(8);
  doc.setTextColor(186, 210, 255);
  doc.text("Insurance Policy & Claim Management System", 14, 21);

  // Title (right)
  doc.setFont("helvetica", "bold");
  doc.setFontSize(11);
  doc.setTextColor(255, 255, 255);
  doc.text("CLAIM REPORT", W - 14, 13, { align: "right" });

  // Timestamp (right)
  doc.setFont("helvetica", "normal");
  doc.setFontSize(7.5);
  doc.setTextColor(186, 210, 255);
  doc.text("Generated: " + fmtDateTime(new Date().toISOString()), W - 14, 21, { align: "right" });

  // Bottom border of header
  doc.setDrawColor(186, 210, 255);
  doc.setLineWidth(0.3);
  doc.line(0, 30, W, 30);
}

/* ─── Section title strip ────────────────────────────────────── */
function sectionTitle(doc, text, y) {
  const W = pw(doc);

  doc.setFillColor(...C.headBg);
  doc.roundedRect(14, y - 4, W - 28, 9, 1.5, 1.5, "F");

  doc.setDrawColor(203, 213, 225);
  doc.setLineWidth(0.3);
  doc.roundedRect(14, y - 4, W - 28, 9, 1.5, 1.5, "S");

  doc.setFont("helvetica", "bold");
  doc.setFontSize(8.5);
  doc.setTextColor(...C.headText);
  doc.text(text.toUpperCase(), 19, y + 1.8);

  return y + 11;
}

/* ─── Status pill ────────────────────────────────────────────── */
function drawStatusBadge(doc, status, x, y) {
  const label = toTitleCase(status);
  doc.setFontSize(8);
  doc.setFont("helvetica", "bold");
  const tw = doc.getTextWidth(label);
  const px = 5, bh = 7, bw = tw + px * 2;
  const [r, g, b] = statusRGB(status);

  // tinted background
  doc.setFillColor(
    Math.round(r + (255 - r) * 0.82),
    Math.round(g + (255 - g) * 0.82),
    Math.round(b + (255 - b) * 0.82)
  );
  doc.roundedRect(x, y - bh + 1.5, bw, bh, 2, 2, "F");

  // border
  doc.setDrawColor(r, g, b);
  doc.setLineWidth(0.35);
  doc.roundedRect(x, y - bh + 1.5, bw, bh, 2, 2, "S");

  doc.setTextColor(r, g, b);
  doc.text(label, x + px, y + 0.2);
}

/* ─── Key-value detail table ─────────────────────────────────── */
// Uses alternateRowStyles ONLY — no didDrawCell so text is never overwritten.
function kvTable(doc, rows, startY) {
  autoTable(doc, {
    startY,
    margin: { left: 14, right: 14 },
    theme: "plain",
    styles: {
      fontSize: 9,
      cellPadding: { top: 3.2, bottom: 3.2, left: 5, right: 5 },
      lineColor: [226, 232, 240],
      lineWidth: 0.2,
    },
    columnStyles: {
      0: {
        fontStyle: "bold",
        textColor: C.muted,
        fillColor: C.rowEven,
        cellWidth: 62,
      },
      1: {
        textColor: C.dark,
        fillColor: C.rowOdd,
      },
    },
    // Alternate every pair row manually via willDrawCell
    body: rows,
    willDrawCell: (data) => {
      const isEven = data.row.index % 2 === 0;
      if (data.column.index === 0) {
        // label column always slight tint
        doc.setFillColor(...C.rowEven);
      } else {
        doc.setFillColor(isEven ? 255 : 248, isEven ? 255 : 250, isEven ? 255 : 252);
      }
    },
  });
  return doc.lastAutoTable.finalY + 10;
}

/* ─── Footer on every page ───────────────────────────────────── */
function addFooters(doc) {
  const total = doc.internal.getNumberOfPages();
  const W = pw(doc);
  const ph = doc.internal.pageSize.getHeight();

  for (let i = 1; i <= total; i++) {
    doc.setPage(i);

    doc.setDrawColor(203, 213, 225);
    doc.setLineWidth(0.4);
    doc.line(14, ph - 13, W - 14, ph - 13);

    doc.setFont("helvetica", "normal");
    doc.setFontSize(7.5);
    doc.setTextColor(...C.muted);
    doc.text("This document is system-generated and confidential.", 14, ph - 7);
    doc.text("Page " + i + " of " + total, W - 14, ph - 7, { align: "right" });
  }
}

/* ══════════════════════════════════════════════════════════════
   EXPORT 1 — Full Claim Detail PDF
   Used on ClaimDetailPage (all data already loaded)
   ══════════════════════════════════════════════════════════════ */
export function downloadClaimDetailPdf(claim, documents = [], history = []) {
  const doc = new jsPDF({ unit: "mm", format: "a4" });
  const W = pw(doc);
  let y = 37;

  drawHeader(doc);

  /* ── Hero amount + badge ── */
  doc.setFont("helvetica", "bold");
  doc.setFontSize(17);
  doc.setTextColor(...C.dark);
  doc.text(fmtCurrency(claim.claimAmount), 14, y);

  doc.setFont("helvetica", "normal");
  doc.setFontSize(9);
  doc.setTextColor(...C.muted);
  doc.text("Claim No: " + safe(claim.claimNumber), 14, y + 8);

  if (claim.claimStatus) {
    drawStatusBadge(doc, claim.claimStatus, W - 60, y + 3);
  }

  y += 17;
  doc.setDrawColor(203, 213, 225);
  doc.setLineWidth(0.4);
  doc.line(14, y, W - 14, y);
  y += 7;

  /* ── Claim Details ── */
  y = checkNewPage(doc, y);
  y = sectionTitle(doc, "Claim Details", y);
  const detailRows = [
    ["Claim Number",   safe(claim.claimNumber)],
    ["Claim Amount",   fmtCurrency(claim.claimAmount)],
    ["Status",         toTitleCase(claim.claimStatus)],
    ["Incident Date",  fmtDate(claim.incidentDate)],
    ["Customer",       safe(claim.customerName)],
    ["Policy Number",  safe(claim.policyNumber)],
    ["Assigned IOO",   safe(claim.assignedInsuranceOperationsOfficerName) || "Unassigned"],
    ["Reason",         safe(claim.claimReason)],
  ];
  if (claim.insuranceOperationsOfficerRemarks) {
    detailRows.push(["IOO Remarks", safe(claim.insuranceOperationsOfficerRemarks)]);
  }
  if (claim.adminRemarks) {
    detailRows.push(["Admin Remarks", safe(claim.adminRemarks)]);
  }
  y = kvTable(doc, detailRows, y);

  /* ── Plan & Coverage ── */
  if (claim.planDetails || claim.planSummary) {
    y = checkNewPage(doc, y);
    y = sectionTitle(doc, "Plan & Coverage", y);
    const planRows = [];
    if (claim.planDetails) {
      planRows.push(["Plan Name",       safe(claim.planDetails.planName)]);
      planRows.push(["Maximum Coverage", fmtCurrency(claim.planDetails.coverageAmount)]);
    }
    if (claim.planSummary) {
      planRows.push(["Previous Claims",    safe(claim.planSummary.totalPreviousClaims)]);
      planRows.push(["Previously Claimed", fmtCurrency(claim.planSummary.totalPreviousClaimAmount)]);
      planRows.push(["Remaining Coverage", fmtCurrency(claim.planSummary.remainingCoverage)]);
    }
    y = kvTable(doc, planRows, y);
  }

  /* ── Documents ── */
  y = checkNewPage(doc, y);
  y = sectionTitle(doc, "Supporting Documents", y);
  if (documents.length === 0) {
    doc.setFont("helvetica", "italic");
    doc.setFontSize(9);
    doc.setTextColor(...C.muted);
    doc.text("No documents attached.", 19, y + 2);
    y += 10;
  } else {
    autoTable(doc, {
      startY: y,
      margin: { left: 14, right: 14 },
      showHead: 'firstPage',
      head: [["#", "Document Name", "Type", "Uploaded On"]],
      body: documents.map((d, i) => [
        i + 1,
        safe(d.documentName),
        safe(d.documentType),
        fmtDateTime(d.uploadedDate),
      ]),
      headStyles: {
        fillColor: C.primary, textColor: C.white,
        fontSize: 8.5, fontStyle: "bold",
      },
      bodyStyles: { fontSize: 8.5, textColor: C.dark },
      alternateRowStyles: { fillColor: C.rowEven },
      columnStyles: { 0: { cellWidth: 10 } },
      styles: { cellPadding: { top: 3, bottom: 3, left: 4, right: 4 } },
    });
    y = doc.lastAutoTable.finalY + 10;
  }

  /* ── Prior Customer Claims ── */
  if (claim.customerClaimHistory?.length > 0) {
    y = checkNewPage(doc, y);
    y = sectionTitle(doc, "Customer's Prior Claims", y);
    autoTable(doc, {
      startY: y,
      margin: { left: 14, right: 14 },
      showHead: 'firstPage',
      head: [["Claim #", "Plan", "Amount", "Status"]],
      body: claim.customerClaimHistory.map((h) => [
        safe(h.claimNumber),
        safe(h.planName),
        fmtCurrency(h.claimedAmount),
        toTitleCase(h.claimStatus),
      ]),
      headStyles: {
        fillColor: C.primary, textColor: C.white,
        fontSize: 8.5, fontStyle: "bold",
      },
      bodyStyles: { fontSize: 8.5, textColor: C.dark },
      alternateRowStyles: { fillColor: C.rowEven },
      styles: { cellPadding: { top: 3, bottom: 3, left: 4, right: 4 } },
    });
  }

  addFooters(doc);
  doc.save("Claim_" + (claim.claimNumber || claim.claimId) + "_Report.pdf");
}

/* ══════════════════════════════════════════════════════════════
   EXPORT 2 — Per-row Summary PDF  (list pages)
   ══════════════════════════════════════════════════════════════ */
export function downloadClaimSummaryPdf(claim) {
  const doc = new jsPDF({ unit: "mm", format: "a4" });
  const W = pw(doc);
  let y = 37;

  drawHeader(doc);

  doc.setFont("helvetica", "bold");
  doc.setFontSize(17);
  doc.setTextColor(...C.dark);
  doc.text(fmtCurrency(claim.claimAmount), 14, y);

  doc.setFont("helvetica", "normal");
  doc.setFontSize(9);
  doc.setTextColor(...C.muted);
  doc.text("Claim No: " + safe(claim.claimNumber), 14, y + 8);

  if (claim.claimStatus) {
    drawStatusBadge(doc, claim.claimStatus, W - 60, y + 3);
  }

  y += 17;
  doc.setDrawColor(203, 213, 225);
  doc.setLineWidth(0.4);
  doc.line(14, y, W - 14, y);
  y += 7;

  y = sectionTitle(doc, "Claim Summary", y);
  const rows = [
    ["Claim Number",  safe(claim.claimNumber)],
    ["Claim Amount",  fmtCurrency(claim.claimAmount)],
    ["Status",        toTitleCase(claim.claimStatus)],
    ["Incident Date", fmtDate(claim.incidentDate)],
    ["Customer",      safe(claim.customerName)],
    ["Policy Number", safe(claim.policyNumber)],
    ["Assigned IOO",  safe(claim.assignedInsuranceOperationsOfficerName) || "Unassigned"],
    ["Assigned On",   fmtDate(claim.assignedAt)],
    ["Reason",        safe(claim.claimReason)],
  ];
  kvTable(doc, rows, y);

  addFooters(doc);
  doc.save("Claim_" + (claim.claimNumber || claim.claimId) + "_Summary.pdf");
}

/* ══════════════════════════════════════════════════════════════
   EXPORT 3 — Bulk Claims Table PDF  (admin / agent toolbar)
   Landscape for more columns
   ══════════════════════════════════════════════════════════════ */
export function downloadClaimsListPdf(claims, title = "Claims Report") {
  if (!claims || claims.length === 0) return;

  const doc = new jsPDF({ unit: "mm", format: "a4", orientation: "landscape" });
  const W = pw(doc);
  let y = 37;

  drawHeader(doc);

  doc.setFont("helvetica", "bold");
  doc.setFontSize(13);
  doc.setTextColor(...C.dark);
  doc.text(title, 14, y);

  doc.setFont("helvetica", "normal");
  doc.setFontSize(8.5);
  doc.setTextColor(...C.muted);
  doc.text("Total records: " + claims.length + "   |   Report date: " + fmtDate(new Date().toISOString()), 14, y + 8);
  y += 17;

  autoTable(doc, {
    startY: y,
    margin: { left: 14, right: 14 },
    showHead: 'firstPage',
    head: [["#", "Claim #", "Customer", "Policy #", "Amount (Rs.)", "Incident Date", "Assigned IOO", "Status"]],
    body: claims.map((c, i) => [
      i + 1,
      safe(c.claimNumber),
      safe(c.customerName),
      safe(c.policyNumber),
      fmtCurrency(c.claimAmount),
      fmtDate(c.incidentDate),
      safe(c.assignedInsuranceOperationsOfficerName) || "Unassigned",
      toTitleCase(c.claimStatus),
    ]),
    headStyles: {
      fillColor: C.primary, textColor: C.white,
      fontSize: 8.5, fontStyle: "bold",
    },
    bodyStyles: { fontSize: 8.5, textColor: C.dark },
    alternateRowStyles: { fillColor: C.rowEven },
    styles: { cellPadding: { top: 3, bottom: 3, left: 4, right: 4 } },
    columnStyles: { 0: { cellWidth: 8 } },
  });

  addFooters(doc);
  doc.save("Claims_Report_" + new Date().toISOString().slice(0, 10) + ".pdf");
}
