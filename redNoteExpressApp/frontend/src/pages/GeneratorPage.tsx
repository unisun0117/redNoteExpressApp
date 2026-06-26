import { useState } from "react";
import { api } from "../services/api";
import { useAuth } from "../hooks/useAuth";
import { ImageUpload } from "../components/ImageUpload";
import { StylePanel } from "../components/StylePanel";
import type { StyleConfig } from "../components/StylePanel";
import { ResultView } from "../components/ResultView";
import { ViralAnalyzer } from "../components/ViralAnalyzer";

const DEFAULT_CONFIG: StyleConfig = {
  template: "简约风",
  track: "美食",
  emojiEnabled: true,
  subtitlesEnabled: true,
};

export function GeneratorPage() {
  const { user, refreshUser } = useAuth();
  const [images, setImages] = useState<File[]>([]);
  const [keywords, setKeywords] = useState("");
  const [storeName, setStoreName] = useState("");
  const [storeAddress, setStoreAddress] = useState("");

  const saved = localStorage.getItem("style_config");
  const [styleConfig, setStyleConfig] = useState<StyleConfig>(
    saved ? { ...DEFAULT_CONFIG, ...JSON.parse(saved) } : DEFAULT_CONFIG
  );

  const [generating, setGenerating] = useState(false);
  const [result, setResult] = useState<any>(null);
  const [error, setError] = useState<string | null>(null);

  const handleGenerate = async () => {
    if (!keywords.trim() && images.length === 0) {
      setError("请至少上传一张图片或输入关键词");
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
      if (images.length > 0) fd.append("image", images[0]);

      const data = await api.generate(fd);
      if (data.title) {
        setResult(data);
        refreshUser(); // 生成成功后刷新点数显示
      } else {
        setError(data.detail || "生成失败，请重试");
      }
    } catch {
      setError("网络错误，请检查后端服务是否启动");
    } finally {
      setGenerating(false);
    }
  };

  return (
    <div className="page">
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 16 }}>
        <h1 className="page-title" style={{ marginBottom: 0 }}>🍠 红薯快写</h1>
        {user && (
          <span style={{ fontSize: 13, color: "#999" }}>
            剩余：{user.credits_remaining} 次
          </span>
        )}
      </div>

      <ImageUpload images={images} onImagesChange={setImages} />

      <div className="card" style={{ marginTop: 12 }}>
        <div className="form-group">
          <div className="label">关键词</div>
          <input
            className="input"
            placeholder="例如：咖啡店 brunch 推荐"
            value={keywords}
            onChange={(e) => setKeywords(e.target.value)}
          />
        </div>
        <div className="form-group">
          <div className="label">店铺名称（可选）</div>
          <input
            className="input"
            placeholder="店名"
            value={storeName}
            onChange={(e) => setStoreName(e.target.value)}
          />
        </div>
        <div className="form-group">
          <div className="label">店铺地址（可选）</div>
          <input
            className="input"
            placeholder="地址"
            value={storeAddress}
            onChange={(e) => setStoreAddress(e.target.value)}
          />
        </div>
      </div>

      <StylePanel config={styleConfig} onChange={setStyleConfig} />

      <ViralAnalyzer onStyleDetected={(profile) => profile && setStyleConfig((c) => ({ ...c, template: "深度测评风" }))} />

      {error && (
        <div style={{ color: "#e74c3c", fontSize: 14, marginTop: 12, padding: "8px 12px", background: "#fff0f0", borderRadius: 8 }}>
          {error}
        </div>
      )}

      <button
        className="btn btn-primary"
        style={{ marginTop: 16 }}
        onClick={handleGenerate}
        disabled={generating}
      >
        {generating ? "生成中..." : "✨ 一键生成小红书文案"}
      </button>

      {result && <ResultView article={result} />}
    </div>
  );
}
