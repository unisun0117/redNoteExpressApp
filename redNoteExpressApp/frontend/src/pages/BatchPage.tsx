import { useState } from "react";
import { api } from "../services/api";
import { ImageUpload } from "../components/ImageUpload";
import { StylePanel } from "../components/StylePanel";
import type { StyleConfig } from "../components/StylePanel";

const DEFAULT_CONFIG: StyleConfig = {
  template: "简约风",
  track: "美食",
  emojiEnabled: true,
  subtitlesEnabled: true,
};

export function BatchPage() {
  const [images, setImages] = useState<File[]>([]);
  const [keywords, setKeywords] = useState("");
  const [storeName, setStoreName] = useState("");
  const [storeAddress, setStoreAddress] = useState("");
  const [styleConfig, setStyleConfig] = useState<StyleConfig>(DEFAULT_CONFIG);
  const [generating, setGenerating] = useState(false);
  const [results, setResults] = useState<any[]>([]);
  const [error, setError] = useState<string | null>(null);

  const handleBatchGenerate = async () => {
    if (images.length === 0) {
      setError("请上传至少一张图片");
      return;
    }
    setError(null);
    setGenerating(true);

    try {
      const fd = new FormData();
      fd.append("keywords", keywords);
      fd.append("style_template", styleConfig.template);
      fd.append("track", styleConfig.track);
      fd.append("emoji_enabled", String(styleConfig.emojiEnabled));
      fd.append("subtitles_enabled", String(styleConfig.subtitlesEnabled));
      fd.append("store_name", storeName);
      fd.append("store_address", storeAddress);
      images.forEach((img) => fd.append("images", img));

      const data = await api.batchGenerate(fd);
      if (data.batch_id) {
        setResults([{ id: data.batch_id, status: "生成中...", total: data.total || images.length }]);
      }
    } catch {
      setError("批量生成请求失败");
    } finally {
      setGenerating(false);
    }
  };

  return (
    <div className="page">
      <h1 className="page-title">批量生成（VIP）</h1>

      <ImageUpload images={images} onImagesChange={setImages} multiple maxCount={50} />

      <div className="card" style={{ marginTop: 12 }}>
        <div className="form-group">
          <div className="label">关键词</div>
          <input
            className="input"
            placeholder="例如：咖啡店探店"
            value={keywords}
            onChange={(e) => setKeywords(e.target.value)}
          />
        </div>
        <div className="form-group">
          <div className="label">店铺名称</div>
          <input
            className="input"
            placeholder="店名"
            value={storeName}
            onChange={(e) => setStoreName(e.target.value)}
          />
        </div>
        <div className="form-group">
          <div className="label">店铺地址</div>
          <input
            className="input"
            placeholder="地址"
            value={storeAddress}
            onChange={(e) => setStoreAddress(e.target.value)}
          />
        </div>
      </div>

      <StylePanel config={styleConfig} onChange={setStyleConfig} />

      {error && (
        <div style={{ color: "#e74c3c", fontSize: 14, marginTop: 12, padding: "8px 12px", background: "#fff0f0", borderRadius: 8 }}>
          {error}
        </div>
      )}

      <button
        className="btn btn-primary"
        style={{ marginTop: 16 }}
        onClick={handleBatchGenerate}
        disabled={generating}
      >
        {generating ? "生成中..." : `📦 批量生成（${images.length}/50 张）`}
      </button>

      {results.length > 0 && (
        <div style={{ marginTop: 16 }}>
          {results.map((r, i) => (
            <div key={i} className="card" style={{ fontSize: 14 }}>
              <div style={{ fontWeight: 600 }}>批次: {r.id}</div>
              <div style={{ color: "#999" }}>状态: {r.status} | 总数: {r.total}</div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
