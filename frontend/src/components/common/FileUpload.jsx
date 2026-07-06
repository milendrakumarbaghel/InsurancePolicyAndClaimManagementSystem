import { useRef, useState } from "react";
import { UploadCloud, FileText } from "lucide-react";

export default function FileUpload({ onFileSelected, accept = ".pdf,.jpg,.jpeg,.png", disabled }) {
  const inputRef = useRef(null);
  const [dragOver, setDragOver] = useState(false);
  const [fileName, setFileName] = useState("");

  const handleFiles = (files) => {
    const file = files?.[0];
    if (!file) return;
    setFileName(file.name);
    onFileSelected(file);
  };

  return (
    <div
      onDragOver={(e) => {
        e.preventDefault();
        if (!disabled) setDragOver(true);
      }}
      onDragLeave={() => setDragOver(false)}
      onDrop={(e) => {
        e.preventDefault();
        setDragOver(false);
        if (!disabled) handleFiles(e.dataTransfer.files);
      }}
      onClick={() => !disabled && inputRef.current?.click()}
      className={`flex cursor-pointer flex-col items-center justify-center gap-2 rounded-xl border-2 border-dashed
        px-6 py-8 text-center transition-colors duration-200
        ${dragOver ? "border-harbor-500 bg-harbor-50 dark:bg-harbor-900/20" : "border-ink-300 dark:border-ink-700 hover:border-harbor-400"}
        ${disabled ? "opacity-50 cursor-not-allowed" : ""}`}
    >
      <input
        ref={inputRef}
        type="file"
        accept={accept}
        className="hidden"
        disabled={disabled}
        onChange={(e) => handleFiles(e.target.files)}
      />
      {fileName ? (
        <>
          <FileText className="h-8 w-8 text-harbor-500" />
          <p className="text-sm font-medium text-ink-700 dark:text-ink-200">{fileName}</p>
          <p className="text-xs text-ink-400">Click or drop to replace</p>
        </>
      ) : (
        <>
          <UploadCloud className="h-8 w-8 text-ink-400" />
          <p className="text-sm font-medium text-ink-600 dark:text-ink-300">
            Drop a file here, or <span className="text-harbor-500">browse</span>
          </p>
          <p className="text-xs text-ink-400">PDF, JPG or PNG</p>
        </>
      )}
    </div>
  );
}
