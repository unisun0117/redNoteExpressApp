"""
Gemini 图片分析工具。

通过自定义代理调用 Gemini 模型，对图片进行视觉分析并输出文字描述。
适用于产品设计图、页面截图等需要提取文字内容的场景。

用法:
    python gemini.py <image_path> [--prompt "自定义提示词"]
    python gemini.py screenshot.png
    python gemini.py design.png --prompt "描述图中所有按钮和字段"
"""

import argparse
import base64
import json
import sys
import urllib.request

API_KEY = "sk-ant-api03-7t0bgAhPleNlq0MzOSO5P8-eIEP4IjQVm_siRsgroGGkLXeQmugvAHIY3nmsSlC1JHG_PiUA8QqAO7Z5GnxWig"
BASE_URL = "https://api.aicodemirror.com/api/gemini/v1beta/models"
MODEL = "gemini-3.5-flash"

DEFAULT_PROMPT = """请详细描述图中所有内容，包括：
1. 页面标题和功能名称
2. 页面布局（搜索区、列表区、操作按钮等）
3. 列表表格中的每个列名和字段
4. 搜索区域的筛选项
5. 所有按钮及其位置
6. 弹窗/表单中的每个字段名称、类型
7. 数据字典信息
8. 任何交互逻辑说明

请用中文详细描述，不要遗漏任何细节。"""


def analyze_image(image_path: str, prompt: str = DEFAULT_PROMPT) -> str:
    """分析图片并返回模型文字输出。"""
    with open(image_path, "rb") as f:
        image_data = base64.b64encode(f.read()).decode("utf-8")

    payload = {
        "contents": [
            {
                "role": "user",
                "parts": [
                    {"text": prompt},
                    {"inlineData": {"mimeType": "image/png", "data": image_data}},
                ]
            }
        ]
    }

    url = f"{BASE_URL}/{MODEL}:generateContent"
    req = urllib.request.Request(
        url,
        data=json.dumps(payload).encode("utf-8"),
        headers={
            "Content-Type": "application/json",
            "x-goog-api-key": API_KEY,
        },
    )

    try:
        resp = urllib.request.urlopen(req, timeout=120)
        result = json.loads(resp.read().decode("utf-8"))
        parts = []
        if "candidates" in result:
            for cand in result["candidates"]:
                if "content" in cand:
                    for part in cand["content"]["parts"]:
                        if "text" in part:
                            parts.append(part["text"])
        return "\n".join(parts) if parts else json.dumps(result, indent=2, ensure_ascii=False)
    except urllib.error.HTTPError as e:
        body = e.read().decode("utf-8")
        raise RuntimeError(f"HTTP {e.code}: {e.reason}\n{body}")


def main():
    parser = argparse.ArgumentParser(description="Gemini 图片分析工具")
    parser.add_argument("image", help="图片文件路径")
    parser.add_argument("--prompt", "-p", default=DEFAULT_PROMPT, help="自定义分析提示词")
    args = parser.parse_args()

    try:
        result = analyze_image(args.image, args.prompt)
        print(result)
    except Exception as e:
        print(f"错误: {e}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
