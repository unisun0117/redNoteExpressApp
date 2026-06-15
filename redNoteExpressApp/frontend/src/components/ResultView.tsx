interface Props {
  article: {
    title: string;
    intro: string;
    sections: { subtitle: string; content: string }[];
    summary: string;
    store_info: string;
  };
}

export function ResultView({ article }: Props) {
  if (!article) return null;

  const copyText = () => {
    const text = [
      article.title,
      "",
      article.intro,
      "",
      ...article.sections.map((s) => (s.subtitle ? `${s.subtitle}\n${s.content}` : s.content)),
      "",
      article.summary,
      "",
      article.store_info,
    ].join("\n");
    navigator.clipboard.writeText(text);
  };

  return (
    <div className="article-preview">
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 12 }}>
        <span style={{ fontSize: 13, color: "#999" }}>生成结果</span>
        <button className="copy-btn" onClick={copyText}>📋 复制全文</button>
      </div>

      <div className="article-title">{article.title}</div>
      <div className="article-intro">{article.intro}</div>

      {article.sections?.map((section, i) => (
        <div key={i} className="article-section">
          {section.subtitle && (
            <div className="article-section-subtitle">{section.subtitle}</div>
          )}
          <div className="article-section-content">{section.content}</div>
        </div>
      ))}

      <div className="article-summary">{article.summary}</div>
      {article.store_info && <div className="article-store">{article.store_info}</div>}
    </div>
  );
}
