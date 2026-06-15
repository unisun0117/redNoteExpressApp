import { useState } from "react";
import { api } from "../services/api";

interface Props {
  onStyleDetected: (profile: { tone: string; emoji_density: string; hook_patterns: string[] } | null) => void;
}

export function ViralAnalyzer({ onStyleDetected }: Props) {
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<string | null>(null);

  const analyze = async () => {
    if (!input.trim()) return;
    setLoading(true);
    setResult(null);
    try {
      const isUrl = input.startsWith("http://") || input.startsWith("https://");
      const data = await api.analyzeViral(
        isUrl ? { url: input } : { text: input }
      );
      setResult(`风格分析完成：语气-${data.tone}，emoji密度-${data.emoji_density}，句式-${data.avg_sentence_length}`);
      onStyleDetected(data);
    } catch {
      setResult("分析失败，请重试");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card">
      <div className="label" style={{ marginBottom: 8 }}>粘贴爆款文案链接/文本（可选，VIP功能）</div>
      <textarea
        className="textarea"
        placeholder="粘贴你刷到的爆款文案链接或全文..."
        value={input}
        onChange={(e) => setInput(e.target.value)}
        rows={3}
      />
      <button
        className="btn btn-outline"
        style={{ marginTop: 8 }}
        onClick={analyze}
        disabled={loading || !input.trim()}
      >
        {loading ? "分析中..." : "🔍 分析语言风格"}
      </button>
      {result && (
        <div style={{ marginTop: 8, fontSize: 13, color: "#666", padding: "8px 0" }}>{result}</div>
      )}
    </div>
  );
}
