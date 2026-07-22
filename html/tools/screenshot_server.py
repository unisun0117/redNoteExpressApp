# -*- coding: utf-8 -*-
"""
可视化截图服务 — 启动后在浏览器里拖拽 HTML 文件，自动截图下载。

用法:
    python screenshot_server.py
    → 浏览器自动打开 http://localhost:8765
    → 拖入 HTML 文件 → 选择模式 → 点「截图」
    → Playwright 真渲染 → 自动下载 PNG
"""

import sys
import os
import re
import json
import tempfile
import threading
import webbrowser
import zipfile
import shutil
import datetime
from pathlib import Path
from urllib.parse import unquote
from http.server import HTTPServer, SimpleHTTPRequestHandler
from socketserver import ThreadingMixIn

class ThreadingHTTPServer(ThreadingMixIn, HTTPServer):
    """多线程 HTTP 服务器，每个请求独立线程。"""
    daemon_threads = True

if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")

PORT = 8765
THIS_DIR = Path(__file__).parent

# ── 前端页面 ────────────────────────────────────────────

FRONTEND_HTML = r"""<!DOCTYPE html>
<html lang="zh-CN">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>HTML 截图工具</title>
<style>
*{margin:0;padding:0;box-sizing:border-box}
:root{
  --bg:#f8f9fb;--card:#fff;--border:#e2e6eb;--text:#1e293b;
  --muted:#64748b;--accent:#6366F1;--accent2:#8B5CF6;--green:#10B981;
  --radius:16px;--radius-sm:10px;
}
body{
  font-family:-apple-system,BlinkMacSystemFont,"Segoe UI","PingFang SC","Microsoft YaHei",sans-serif;
  background:var(--bg);color:var(--text);min-height:100vh;padding:32px 16px;
}
.app{max-width:1000px;margin:0 auto}
.header{text-align:center;margin-bottom:28px}
.header h1{font-size:28px;font-weight:800}
.header h1 b{background:linear-gradient(135deg,var(--accent),var(--accent2));-webkit-background-clip:text;-webkit-text-fill-color:transparent}
.header p{font-size:13px;color:var(--muted);margin-top:4px}

.workspace{display:flex;gap:16px;min-height:550px}
.panel{flex:0 0 360px;display:flex;flex-direction:column;gap:14px}
.preview{flex:1;background:var(--card);border-radius:var(--radius);box-shadow:0 1px 3px rgba(0,0,0,.04);overflow:hidden;display:flex;flex-direction:column}

/* Drop zone */
.dropzone{
  background:var(--card);border:2px dashed var(--border);border-radius:var(--radius);
  padding:40px 24px;text-align:center;cursor:pointer;transition:all .25s;
  box-shadow:0 1px 3px rgba(0,0,0,.04);position:relative;
}
.dropzone:hover,.dropzone.drag-over{border-color:var(--accent);background:#F5F3FF;transform:translateY(-1px)}
.dropzone.drag-over{border-style:solid;background:#EDE9FE}
.dropzone .dz-icon{font-size:40px;margin-bottom:8px;display:block}
.dropzone .dz-title{font-size:16px;font-weight:700;margin-bottom:2px}
.dropzone .dz-hint{font-size:12px;color:var(--muted)}
.dropzone input[type=file]{position:absolute;inset:0;opacity:0;cursor:pointer}

/* File info */
.file-chip{
  background:var(--card);border-radius:var(--radius-sm);padding:12px 16px;
  box-shadow:0 1px 3px rgba(0,0,0,.04);display:flex;align-items:center;gap:10px;font-size:13px;
}
.file-chip .chip-name{flex:1;font-weight:600;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}
.file-chip button{width:28px;height:28px;border-radius:50%;border:1px solid var(--border);background:var(--card);cursor:pointer;font-size:12px;color:var(--muted)}

/* Options */
.opts{background:var(--card);border-radius:var(--radius);padding:16px;box-shadow:0 1px 3px rgba(0,0,0,.04)}
.opts h3{font-size:13px;font-weight:700;margin-bottom:10px}
.opt-row{display:flex;align-items:center;gap:10px;padding:10px 12px;border-radius:var(--radius-sm);cursor:pointer;border:2px solid transparent;font-size:14px;font-weight:600;margin-bottom:4px}
.opt-row:hover{background:#f8fafc}
.opt-row.selected{border-color:var(--accent);background:#F5F3FF}
.opt-row .radio{width:18px;height:18px;border-radius:50%;border:2px solid var(--border);flex-shrink:0;display:flex;align-items:center;justify-content:center}
.opt-row.selected .radio{border-color:var(--accent);background:var(--accent)}
.opt-row.selected .radio::after{content:'';width:6px;height:6px;border-radius:50%;background:#fff}
.opt-row .opt-label{font-size:11px;color:var(--muted);font-weight:400;margin-top:1px}

/* Settings */
.settings{display:flex;gap:10px;margin-top:4px}
.setting{flex:1}
.setting label{font-size:11px;color:var(--muted);display:block;margin-bottom:3px;font-weight:600}
.setting select,.setting input{width:100%;padding:6px 8px;border-radius:6px;border:1px solid var(--border);font-size:12px;font-family:inherit;background:var(--card)}

/* Buttons */
.btn{
  width:100%;padding:14px;border-radius:var(--radius-sm);border:none;
  font-size:17px;font-weight:700;cursor:pointer;transition:all .2s;
}
.btn-go{background:linear-gradient(135deg,var(--accent),var(--accent2));color:#fff;box-shadow:0 4px 14px rgba(99,102,241,.3)}
.btn-go:hover:not(:disabled){transform:translateY(-1px);box-shadow:0 6px 20px rgba(99,102,241,.4)}
.btn-go:disabled{opacity:.35;cursor:not-allowed}
.btn-go.loading{pointer-events:none}

/* Preview */
.pv-topbar{padding:10px 16px;border-bottom:1px solid var(--border);font-size:12px;color:var(--muted);display:flex;justify-content:space-between}
.pv-topbar .pv-title{font-weight:700;color:var(--text)}
.pv-wrap{flex:1;overflow:hidden;background:#e2e8f0;position:relative}
.pv-wrap iframe{width:100%;height:100%;border:none;background:#fff}
.pv-empty{display:flex;align-items:center;justify-content:center;height:100%;color:var(--muted);font-size:14px}

/* Status */
.status{
  text-align:center;padding:8px;border-radius:var(--radius-sm);font-size:12px;font-weight:600;
  display:none;
}
.status.processing{display:block;background:#FFF7ED;color:#C2410C;border:1px solid #FED7AA}
.status.done{display:block;background:#ECFDF5;color:#065F46;border:1px solid #A7F3D0}

@media(max-width:768px){
  .workspace{flex-direction:column}
  .panel{flex:auto}
  .preview{min-height:400px}
}
</style>
</head>
<body>

<div class="app">
  <div class="header">
    <h1>📸 HTML <b>截图工具</b></h1>
    <p>拖入 HTML 文件 → Playwright 真渲染 → 完美截图下载</p>
  </div>

  <div class="workspace">
    <div class="panel">
      <div class="dropzone" id="dropzone">
        <span class="dz-icon">📂</span>
        <div class="dz-title">拖拽 HTML 文件到这里</div>
        <div class="dz-hint">或点击选择文件</div>
        <input type="file" id="fileInput" accept=".html,.htm">
      </div>

      <div class="file-chip" id="fileChip" style="display:none">
        <span>📄</span>
        <span class="chip-name" id="chipName"></span>
        <button id="chipDel">✕</button>
      </div>

      <div class="opts" id="opts" style="display:none">
        <h3>截图模式</h3>
        <div class="opt-row selected" data-mode="full">
          <div class="radio"></div>
          <div>整页长图<div class="opt-label">完整页面高度，一张 PNG</div></div>
        </div>
        <div class="opt-row" data-mode="sections">
          <div class="radio"></div>
          <div>分段截图<div class="opt-label">每个 Section 单独一张 PNG</div></div>
        </div>
        <div class="settings">
          <div class="setting">
            <label>宽度</label>
            <select id="setWidth">
              <option value="1280">1280px</option>
              <option value="1440">1440px</option>
              <option value="1920">1920px</option>
              <option value="2560">2560px (2K)</option>
            </select>
          </div>
          <div class="setting">
            <label>缩放</label>
            <select id="setScale">
              <option value="1">1x</option>
              <option value="2" selected>2x (高清)</option>
              <option value="3">3x (超清)</option>
            </select>
          </div>
        </div>
      </div>

      <div class="status" id="status"></div>

      <button class="btn btn-go" id="btnGo" disabled>📸 开始截图</button>
    </div>

    <div class="preview">
      <div class="pv-topbar">
        <span class="pv-title">实时预览</span>
        <span id="pvInfo"></span>
      </div>
      <div class="pv-wrap">
        <div class="pv-empty" id="pvEmpty">拖入 HTML 文件开始预览</div>
        <iframe id="pvIframe" style="display:none"></iframe>
      </div>
    </div>
  </div>
</div>

<script>
const $ = id => document.getElementById(id);

let blobUrl = null, rawHtml = null, fileName = '', mode = 'full';
let sectionCount = 0;

// Drag & drop
['dragover','dragenter'].forEach(ev => $('dropzone').addEventListener(ev, e => { e.preventDefault(); $('dropzone').classList.add('drag-over') }));
['dragleave','drop'].forEach(ev => $('dropzone').addEventListener(ev, { handleEvent(e) { e.preventDefault(); $('dropzone').classList.remove('drag-over'); } }));
$('dropzone').addEventListener('drop', e => { const f = e.dataTransfer.files[0]; if (f) loadFile(f); });
$('dropzone').addEventListener('click', (e) => { if (e.target !== $('fileInput')) $('fileInput').click(); });
$('fileInput').addEventListener('change', () => { const f = $('fileInput').files[0]; if (f) loadFile(f); });
$('fileInput').addEventListener('click', (e) => e.stopPropagation());
$('chipDel').addEventListener('click', reset);

// Mode selector
$('opts').addEventListener('click', e => {
  const row = e.target.closest('.opt-row');
  if (!row) return;
  $('opts').querySelectorAll('.opt-row').forEach(r => r.classList.remove('selected'));
  row.classList.add('selected');
  mode = row.dataset.mode;
});

// Load file
function loadFile(file) {
  if (!/\.html?$/i.test(file.name)) { alert('请选择 .html 文件'); return; }
  if (blobUrl) URL.revokeObjectURL(blobUrl);

  const reader = new FileReader();
  reader.onload = function(e) {
    rawHtml = e.target.result;
    blobUrl = URL.createObjectURL(new Blob([rawHtml], { type:'text/html' }));
    fileName = file.name.replace(/\.html?$/i, '');

    // Count sections
    const doc = new DOMParser().parseFromString(rawHtml, 'text/html');
    sectionCount = doc.querySelectorAll('section').length;

    // Update UI
    $('chipName').textContent = file.name;
    $('fileChip').style.display = 'flex';
    $('opts').style.display = 'flex';
    $('btnGo').disabled = false;
    $('pvIframe').src = blobUrl;
    $('pvIframe').style.display = 'block';
    $('pvEmpty').style.display = 'none';
    $('pvInfo').textContent = sectionCount ? sectionCount + ' 个段落' : '';
  };
  reader.readAsText(file, 'UTF-8');
}

function reset() {
  if (blobUrl) URL.revokeObjectURL(blobUrl);
  blobUrl = null; rawHtml = null; fileName = '';
  $('fileChip').style.display = 'none';
  $('opts').style.display = 'none';
  $('btnGo').disabled = true;
  $('pvIframe').style.display = 'none';
  $('pvEmpty').style.display = 'flex';
  $('pvIframe').src = '';
  $('fileInput').value = '';
  $('status').style.display = 'none';
}

function setStatus(msg, type) {
  const s = $('status');
  s.textContent = msg;
  s.className = 'status ' + type;
  s.style.display = 'block';
  if (type === 'done') setTimeout(() => { s.style.display = 'none'; }, 5000);
}

// Screenshot button
$('btnGo').addEventListener('click', async function() {
  if (!rawHtml) return;

  $('btnGo').disabled = true;
  $('btnGo').classList.add('loading');
  $('btnGo').textContent = '⏳ 正在截图...';
  setStatus('正在用 Playwright 渲染 HTML 并截图...', 'processing');

  try {
    const resp = await fetch('/api/screenshot', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        html: rawHtml,
        mode: mode,
        width: parseInt($('setWidth').value),
        scale: parseInt($('setScale').value),
        filename: fileName,
      }),
    });

    if (!resp.ok) {
      const err = await resp.json();
      throw new Error(err.error || 'Unknown error');
    }

    const data = await resp.json();

    if (data.zip) {
      // Single ZIP download
      const a = document.createElement('a');
      a.href = '/api/download/' + data.zip;
      a.download = data.zip;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      const count = data.count || 1;
      setStatus('✅ 完成！' + count + ' 张截图已打包下载 (ZIP)', 'done');
    } else if (data.error) {
      throw new Error(data.error);
    } else {
      setStatus('✅ 截图已下载', 'done');
    }
  } catch(err) {
    setStatus('❌ 失败: ' + err.message, 'done');
    console.error(err);
  }

  $('btnGo').disabled = false;
  $('btnGo').classList.remove('loading');
  $('btnGo').textContent = '📸 开始截图';
});
</script>
</body>
</html>"""


# ── HTTP Server ────────────────────────────────────────────

class ScreenshotHandler(SimpleHTTPRequestHandler):
    """自定义 handler：首页返回 UI，/api/screenshot 用 Playwright 截图。"""

    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=str(THIS_DIR), **kwargs)

    def do_GET(self):
        if self.path == "/" or self.path == "/index.html":
            self._serve_html(FRONTEND_HTML)
        elif self.path.startswith("/api/download/"):
            self._serve_download()
        else:
            super().do_GET()

    def do_POST(self):
        if self.path == "/api/screenshot":
            self._handle_screenshot()
        else:
            self.send_error(404)

    def _serve_html(self, html):
        self.send_response(200)
        self.send_header("Content-Type", "text/html; charset=utf-8")
        self.send_header("Cache-Control", "no-cache")
        self.end_headers()
        self.wfile.write(html.encode("utf-8"))

    def _serve_download(self):
        filename = unquote(self.path.replace("/api/download/", ""))
        filepath = THIS_DIR / "screenshot_output" / filename
        if not filepath.exists():
            self.send_error(404)
            return

        ext = filepath.suffix.lower()
        ctype = "application/zip" if ext == ".zip" else "image/png"
        # Encode filename for HTTP header (RFC 5987 for non-ASCII)
        raw_name = filepath.name
        try:
            raw_name.encode('latin-1')
            dlname = raw_name
            header = f'attachment; filename="{dlname}"'
        except UnicodeEncodeError:
            from urllib.parse import quote
            dlname = raw_name
            header = f"attachment; filename*=UTF-8''{quote(dlname)}"

        self.send_response(200)
        self.send_header("Content-Type", ctype)
        self.send_header("Content-Disposition", header)
        self.send_header("Content-Length", str(filepath.stat().st_size))
        self.end_headers()
        with open(filepath, "rb") as f:
            self.wfile.write(f.read())

    def _handle_screenshot(self):
        try:
            length = int(self.headers.get("Content-Length", 0))
            body = self.rfile.read(length)
            data = json.loads(body)

            html = data.get("html", "")
            mode = data.get("mode", "full")
            width = data.get("width", 1280)
            scale = data.get("scale", 2)
            safe_name = data.get("filename", "screenshot")

            if not html:
                self._send_json({"error": "No HTML content"}, status=400)
                return

            # Write HTML to temp file
            tmp_fd, tmp_path = tempfile.mkstemp(suffix=".html")
            with os.fdopen(tmp_fd, "w", encoding="utf-8") as f:
                f.write(html)
            tmp_path = Path(tmp_path)

            try:
                from playwright.sync_api import sync_playwright

                out_dir = THIS_DIR / "screenshot_output"
                # Clean old screenshots
                if out_dir.exists():
                    shutil.rmtree(out_dir)
                out_dir.mkdir(parents=True, exist_ok=True)

                png_files = []

                with sync_playwright() as p:
                    browser_path = _find_browser()
                    launch_args = {}
                    if browser_path:
                        launch_args["executable_path"] = browser_path
                    browser = p.chromium.launch(**launch_args)
                    page = browser.new_page(viewport={"width": width, "height": 900}, device_scale_factor=scale)

                    page.goto(tmp_path.resolve().as_uri(), wait_until="networkidle", timeout=30000)

                    # Wait for fonts & animations to settle
                    page.evaluate("""() => document.fonts.ready""")
                    page.wait_for_timeout(2000)

                    # Freeze all animations + make everything visible
                    page.evaluate("""() => {
                        // Kill all CSS animations & transitions
                        const sheet = new CSSStyleSheet();
                        sheet.replaceSync('*, *::before, *::after { animation: none !important; transition: none !important; animation-delay: 0s !important; animation-duration: 0s !important; transition-duration: 0s !important; opacity: 1 !important; visibility: visible !important; }');
                        document.adoptedStyleSheets = [...document.adoptedStyleSheets, sheet];

                        // Force all elements to be visible (some animations hide elements initially)
                        document.querySelectorAll('*').forEach(el => {
                            const s = getComputedStyle(el);
                            if (s.opacity === '0') el.style.opacity = '1';
                            if (s.visibility === 'hidden') el.style.visibility = 'visible';
                            if (s.transform && s.transform.includes('scale(0)')) el.style.transform = 'none';
                        });
                    }""")
                    page.wait_for_timeout(500)

                    # Full-page mode: fix position:fixed elements so they span full page height
                    if mode == "full":
                        page.evaluate("""() => {
                            document.querySelectorAll('*').forEach(el => {
                                if (getComputedStyle(el).position === 'fixed') {
                                    el.style.position = 'absolute';
                                }
                            });
                        }""")
                        page.wait_for_timeout(300)

                    if mode == "sections":
                        sections = page.query_selector_all("section, div.section")
                        if sections:
                            for i, sec in enumerate(sections):
                                try:
                                    sec.scroll_into_view_if_needed()
                                    page.wait_for_timeout(200)
                                    fname = f"{safe_name}_{i+1:02d}.png"
                                    sec.screenshot(path=str(out_dir / fname))
                                    png_files.append(fname)
                                except Exception:
                                    pass
                        if not png_files:
                            fname = f"{safe_name}.png"
                            page.screenshot(path=str(out_dir / fname), full_page=True)
                            png_files.append(fname)
                    else:
                        fname = f"{safe_name}.png"
                        page.screenshot(path=str(out_dir / fname), full_page=True)
                        png_files.append(fname)

                    browser.close()

                # ZIP name: date_first_mode_identifier for easy sorting
                # Sanitize filename: keep Chinese + ASCII, only strip truly problematic chars
                safe = re.sub(r'[^\w一-鿿\-.]', '_', safe_name).strip('_')
                # If too long or empty after sanitize, use timestamp
                if len(safe) > 40 or not safe or safe == '_':
                    safe = datetime.datetime.now().strftime('%H%M%S')
                today = datetime.date.today().strftime('%Y%m%d')
                suffix = "all" if mode == "full" else f"x{len(png_files):02d}"
                zip_name = f"{today}_{safe}_{suffix}.zip"
                zip_path = out_dir / zip_name
                with zipfile.ZipFile(zip_path, "w", zipfile.ZIP_DEFLATED) as zf:
                    for fname in png_files:
                        zf.write(out_dir / fname, fname)

                self._send_json({"zip": zip_name, "count": len(png_files)})

            except ImportError:
                self._send_json({"error": "请先安装: pip install playwright"}, status=500)
            except Exception as e:
                self._send_json({"error": str(e)}, status=500)
            finally:
                try:
                    os.unlink(tmp_path)
                except Exception:
                    pass

        except Exception as e:
            self._send_json({"error": str(e)}, status=500)

    def _send_json(self, data, status=200):
        body = json.dumps(data, ensure_ascii=False).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def log_message(self, format, *args):
        # Suppress default log; print clean version
        if "/api/" in str(args[0]):
            print(f"  [{self.command}] {args[0]}")


def _find_browser():
    """Find an installed browser executable."""
    candidates = [
        os.path.expandvars(r"%PROGRAMFILES(x86)%\Microsoft\Edge\Application\msedge.exe"),
        os.path.expandvars(r"%PROGRAMFILES%\Microsoft\Edge\Application\msedge.exe"),
        os.path.expandvars(r"%PROGRAMFILES%\Google\Chrome\Application\chrome.exe"),
        os.path.expandvars(r"%LOCALAPPDATA%\Google\Chrome\Application\chrome.exe"),
    ]
    for p in candidates:
        if Path(p).exists():
            return p
    return None


# ── Main ────────────────────────────────────────────────────

def main():
    print("=" * 50)
    print("  📸 HTML 截图服务")
    print("=" * 50)
    print()

    browser = _find_browser()
    if browser:
        print(f"  ✅ 浏览器: {Path(browser).name}")
    else:
        print("  ⚠️  未找到系统浏览器，将使用 Playwright Chromium")
        print("     如果报错，请运行: playwright install chromium")
    print()

    server = ThreadingHTTPServer(("0.0.0.0", PORT), ScreenshotHandler)
    url = f"http://localhost:{PORT}"

    print(f"  🌐 服务已启动: {url}")
    print(f"  📂 按 Ctrl+C 停止服务")
    print()

    # Auto-open browser
    threading.Thread(target=lambda: webbrowser.open(url), daemon=True).start()

    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\n  已停止")
        server.shutdown()


if __name__ == "__main__":
    main()
