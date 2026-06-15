import { useRef, useState } from "react";

interface Props {
  images: File[];
  onImagesChange: (files: File[]) => void;
  multiple?: boolean;
  maxCount?: number;
}

export function ImageUpload({ images, onImagesChange, multiple = false, maxCount = 50 }: Props) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [dragOver, setDragOver] = useState(false);

  const handleFiles = (fileList: FileList | null) => {
    if (!fileList) return;
    const newFiles = Array.from(fileList).filter(
      (f) => f.type === "image/jpeg" || f.type === "image/png" || f.type === "image/webp"
    );
    if (multiple) {
      const combined = [...images, ...newFiles].slice(0, maxCount);
      onImagesChange(combined);
    } else {
      onImagesChange(newFiles.slice(0, 1));
    }
  };

  const removeImage = (index: number) => {
    onImagesChange(images.filter((_, i) => i !== index));
  };

  return (
    <div>
      <div
        className={`image-upload-zone${dragOver ? " drag-over" : ""}`}
        onClick={() => inputRef.current?.click()}
        onDragOver={(e) => { e.preventDefault(); setDragOver(true); }}
        onDragLeave={() => setDragOver(false)}
        onDrop={(e) => { e.preventDefault(); setDragOver(false); handleFiles(e.dataTransfer.files); }}
      >
        <div className="upload-icon">📷</div>
        <div className="upload-text">
          {multiple ? "点击或拖拽上传图片（最多50张）" : "点击或拖拽上传图片"}
        </div>
        <div style={{ fontSize: 12, color: "#bbb", marginTop: 4 }}>
          支持 JPG / PNG / WEBP
        </div>
      </div>
      <input
        ref={inputRef}
        type="file"
        accept="image/jpeg,image/png,image/webp"
        multiple={multiple}
        style={{ display: "none" }}
        onChange={(e) => handleFiles(e.target.files)}
      />
      {images.length > 0 && (
        <div className="image-preview-grid">
          {images.map((file, i) => (
            <div key={i} className="image-preview-item">
              <img src={URL.createObjectURL(file)} alt="" />
              <button className="remove-btn" onClick={() => removeImage(i)}>✕</button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
