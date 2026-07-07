/**
 * Converts an array of objects to a CSV string and triggers a browser download.
 *
 * @param {string}   filename  – Name of the downloaded file (without extension).
 * @param {Array}    data      – Array of row objects.
 * @param {Array}    columns   – Array of { key, header, format? } describing each column.
 *                                `key` is the property name on the row object.
 *                                `header` is the column label in the CSV header.
 *                                `format` (optional) is a function (value, row) => string.
 */
export function exportToCSV(filename, data, columns) {
  if (!data || data.length === 0) return;

  const escapeCsv = (value) => {
    if (value === null || value === undefined) return "";
    const str = String(value);
    // Wrap in quotes if value contains comma, newline, or double-quote
    if (str.includes(",") || str.includes("\n") || str.includes('"')) {
      return `"${str.replace(/"/g, '""')}"`;
    }
    return str;
  };

  const headerRow = columns.map((col) => escapeCsv(col.header)).join(",");

  const rows = data.map((row) =>
    columns
      .map((col) => {
        const raw = row[col.key];
        const value = col.format ? col.format(raw, row) : raw;
        return escapeCsv(value);
      })
      .join(",")
  );

  const csv = [headerRow, ...rows].join("\n");
  const blob = new Blob(["\uFEFF" + csv], { type: "text/csv;charset=utf-8;" });
  const url = URL.createObjectURL(blob);

  const link = document.createElement("a");
  link.href = url;
  link.download = `${filename}.csv`;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
}
