import { useState, useEffect } from "react";
import { api } from "../services/api";

export type StyleConfig = {
  template: string;
  track: string;
  emojiEnabled: boolean;
  subtitlesEnabled: boolean;
}

interface Props {
  config: StyleConfig;
  onChange: (config: StyleConfig) => void;
}

const DEFAULT_TEMPLATES = [
  { id: "简约风", name: "简约风" },
  { id: "复古风", name: "复古风" },
  { id: "幽默风", name: "幽默风" },
  { id: "深度测评风", name: "深度测评风" },
];

const DEFAULT_TRACKS = [
  { id: "美食", name: "美食" },
  { id: "运动", name: "运动" },
  { id: "摄影", name: "摄影" },
  { id: "萌宠", name: "萌宠" },
  { id: "家居", name: "家居" },
  { id: "美妆", name: "美妆" },
  { id: "数码", name: "数码" },
  { id: "母婴", name: "母婴" },
  { id: "餐饮", name: "餐饮" },
];

export function StylePanel({ config, onChange }: Props) {
  const [templates, setTemplates] = useState(DEFAULT_TEMPLATES);
  const [tracks, setTracks] = useState(DEFAULT_TRACKS);

  useEffect(() => {
    api.getStyles().then((data) => {
      if (data.templates) setTemplates(data.templates);
      if (data.tracks) setTracks(data.tracks);
    }).catch(() => {});
  }, []);

  const update = (partial: Partial<StyleConfig>) => {
    onChange({ ...config, ...partial });
    localStorage.setItem("style_config", JSON.stringify({ ...config, ...partial }));
  };

  return (
    <div className="card">
      <div className="form-group">
        <div className="label">文章模板</div>
        <select
          className="select"
          value={config.template}
          onChange={(e) => update({ template: e.target.value })}
        >
          {templates.map((t) => (
            <option key={t.id} value={t.id}>{t.name}</option>
          ))}
        </select>
      </div>

      <div className="form-group">
        <div className="label">内容赛道</div>
        <select
          className="select"
          value={config.track}
          onChange={(e) => update({ track: e.target.value })}
        >
          {tracks.map((t) => (
            <option key={t.id} value={t.id}>{t.name}</option>
          ))}
        </select>
      </div>

      <div className="toggle-row">
        <span className="toggle-label">使用 Emoji</span>
        <button
          className={`toggle${config.emojiEnabled ? " on" : ""}`}
          onClick={() => update({ emojiEnabled: !config.emojiEnabled })}
        />
      </div>

      <div className="toggle-row">
        <span className="toggle-label">爆款小标题</span>
        <button
          className={`toggle${config.subtitlesEnabled ? " on" : ""}`}
          onClick={() => update({ subtitlesEnabled: !config.subtitlesEnabled })}
        />
      </div>
    </div>
  );
}
