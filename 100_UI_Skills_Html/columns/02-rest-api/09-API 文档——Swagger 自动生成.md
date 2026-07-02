# REST API 设计从0到1 第9篇：API 文档：Swagger 自动生成
@[TOC](目录)
## 摘要
> 手动写 API 文档是程序员最讨厌的工作之一，而且永远跟不上代码的变化。Swagger（OpenAPI）让你"写代码即写文档"，接口改了什么文档自动同步。本文带你用 Flask 快速搭建 Swagger 文档，并提供可交互的在线测试页面。
## 引言
你有没有遇到过这种情况：后端改了接口的返回格式，忘了通知前端，结果前端线上报错。排查了半天才发现，原来是 `userName` 改成了 `name`，后端觉得是小改动不用通知，前端却炸了。

API 文档是前后端协作的"契约"。契约写清楚了对双方的预期，谁违反契约谁负责。但手动维护一份 Word 或 Markdown 文档的问题是：人总会忘记更新。代码改了，文档却停留在三个月前的版本。

Swagger（现在正式名称是 OpenAPI）解决了这个问题。它的核心理念是"文档即代码"——你在代码里用装饰器或注解描述接口，Swagger 工具链自动生成美观、可交互的 API 文档页面。接口改了，文档自动同步，永远不落伍。
## 基础知识储备
- **Flask 基本使用**：了解路由装饰器、请求处理、JSON 响应。
- **装饰器概念**：理解 Python 中装饰器如何为函数添加元数据。
- **YAML/JSON 格式**：OpenAPI 规范可以用 YAML 或 JSON 编写，了解其中一种即可。
- **REST API 基本概念**：已经掌握了前面 8 篇文章中的内容（URL 设计、HTTP 方法、状态码、响应格式等）。
- **文档协作意识**：理解为什么"契约先行"能减少前后端沟通成本。
## 正文
### 1. Swagger/OpenAPI 是什么

Swagger 最早是一个开源的 API 文档工具，后来它的规范部分被捐赠给了 Linux 基金会，更名为 OpenAPI Specification（OAS）。现在当我们说"Swagger"时，通常指的是整个工具生态：

- **OpenAPI 规范**：一个标准的 API 描述格式（YAML 或 JSON），定义了接口的所有细节。
- **Swagger UI**：根据 OpenAPI 规范自动生成的交互式文档页面，可以直接在页面上测试接口。
- **Swagger Codegen**：根据 OpenAPI 规范自动生成客户端 SDK 和服务端代码框架。

对于后端开发者来说，最常见的用法是：在代码中添加描述信息，然后 Swagger 自动生成 OpenAPI 规范的 JSON 文件，Swagger UI 读取这个 JSON 生成可交互的文档页面。

### 2. 实战：Flask + Flasgger 搭建 Swagger 文档

Flasgger 是 Flask 的 Swagger 扩展，它可以从代码中的 docstring（文档字符串）自动提取 API 描述，生成 Swagger UI。

首先安装依赖：
```bash
pip install flask flasgger
```

然后创建应用。Flasgger 支持在路由函数的 docstring 中用 YAML 格式描述接口规范：

```python
from flask import Flask, request, jsonify
from flasgger import Swagger

app = Flask(__name__)

# Swagger 配置
swagger_config = {
    "headers": [],
    "specs": [{"endpoint": 'apispec', "route": '/apispec.json'}],
    "static_url_path": "/flasgger_static",
    "swagger_ui": True,
    "specs_route": "/docs"  # Swagger UI 页面地址
}

swagger_template = {
    "info": {
        "title": "在线图书馆 API",
        "description": "一个简单的在线图书馆 REST API，支持图书的增删改查",
        "version": "1.0.0",
        "contact": {
            "name": "API 支持",
            "email": "api@library.com"
        }
    },
    "basePath": "/api/v1",  # API 统一前缀
}

swagger = Swagger(app, config=swagger_config, template=swagger_template)

# 模拟数据库
books = [
    {"id": 1, "title": "REST API 设计指南", "author": "张三", "year": 2024},
    {"id": 2, "title": "Python Web 开发", "author": "李四", "year": 2023},
]

@app.route('/api/v1/books', methods=['GET'])
def list_books():
    """
    获取图书列表
    ---
    tags:
      - 图书管理
    summary: 获取所有图书
    description: 返回图书列表，支持分页、关键字搜索和年份筛选
    parameters:
      - name: page
        in: query
        type: integer
        required: false
        default: 1
        description: 页码（从1开始）
      - name: size
        in: query
        type: integer
        required: false
        default: 10
        description: 每页数量（1-100）
      - name: keyword
        in: query
        type: string
        required: false
        description: 搜索关键字（匹配书名和作者）
      - name: year
        in: query
        type: integer
        required: false
        description: 按出版年份筛选
    responses:
      200:
        description: 成功返回图书列表
        schema:
          type: object
          properties:
            code:
              type: integer
              example: 0
            message:
              type: string
              example: success
            data:
              type: object
              properties:
                items:
                  type: array
                  items:
                    $ref: '#/definitions/Book'
    """
    page = request.args.get('page', 1, type=int)
    size = request.args.get('size', 10, type=int)
    keyword = request.args.get('keyword', '').strip()
    year = request.args.get('year', type=int)

    filtered = books
    if keyword:
        filtered = [b for b in filtered
                    if keyword in b['title'] or keyword in b['author']]
    if year:
        filtered = [b for b in filtered if b['year'] == year]

    total = len(filtered)
    start = (page - 1) * size
    end = start + size

    return jsonify({
        "code": 0,
        "message": "success",
        "data": {
            "items": filtered[start:end],
            "total": total,
            "page": page,
            "size": size
        }
    })

@app.route('/api/v1/books/<int:book_id>', methods=['GET'])
def get_book(book_id):
    """
    获取单本图书
    ---
    tags:
      - 图书管理
    summary: 根据 ID 获取图书详情
    parameters:
      - name: book_id
        in: path
        type: integer
        required: true
        description: 图书 ID
    responses:
      200:
        description: 成功
      404:
        description: 图书不存在
    """
    book = next((b for b in books if b['id'] == book_id), None)
    if book:
        return jsonify({"code": 0, "message": "success", "data": book})
    return jsonify({"code": 20001, "message": "图书不存在", "data": None}), 404

@app.route('/api/v1/books', methods=['POST'])
def create_book():
    """
    创建新图书
    ---
    tags:
      - 图书管理
    summary: 添加一本新书
    parameters:
      - name: body
        in: body
        required: true
        schema:
          type: object
          required:
            - title
            - author
          properties:
            title:
              type: string
              description: 书名
              example: 深入理解计算机系统
            author:
              type: string
              description: 作者
              example: Randal E. Bryant
            year:
              type: integer
              description: 出版年份
              example: 2016
    responses:
      201:
        description: 创建成功
      400:
        description: 参数错误
    """
    data = request.json
    if not data or 'title' not in data:
        return jsonify({"code": 10001, "message": "书名不能为空", "data": None}), 400

    new_book = {
        "id": len(books) + 1,
        "title": data['title'],
        "author": data.get('author', '未知'),
        "year": data.get('year', 2024)
    }
    books.append(new_book)
    return jsonify({"code": 0, "message": "success", "data": new_book}), 201

# 数据模型定义（供 Swagger 引用）
@app.route('/apispec.json', methods=['GET'])
def apispec():
    """Swagger spec 端点，由 Flasgger 自动处理"""
    pass

if __name__ == '__main__':
    app.run(debug=True)
```

启动应用后，访问 `http://localhost:5000/docs`，你会看到一个完整的交互式 API 文档页面。

### 3. Swagger UI 的三种使用姿势

**第一，开发调试**：后端开发完一个接口，直接在 Swagger UI 上点"Try it out"，填参数、发请求、看响应。不需要单独打开 Postman，效率提升明显。

**第二，前后端联调**：前端开发者打开 Swagger 页面，每个接口的入参、返回值、状态码都一目了然。还可以直接复制请求的 curl 命令到终端执行。

**第三，对接文档**：把 Swagger 生成的 `apispec.json` 分享给第三方，对方可以用 Swagger Codegen 自动生成客户端 SDK。这意味着对方不需要从零开始写 HTTP 调用代码，直接 `import` 你生成的库就行。
## 总结
- Swagger（OpenAPI）的核心价值是"代码即文档"，接口改了，文档自动更新，告别手动维护。
- Flasgger 让 Flask 项目零成本接入 Swagger，在 docstring 中用 YAML 描述接口即可自动生成文档页面。
- Swagger UI 不仅是个文档，还是个在线调试工具，可以直接在页面上测试接口。
- OpenAPI 规范文件（JSON/YAML）可以被其他工具消费，实现自动生成客户端 SDK。
- 接口描述中应该包含：参数名、类型、是否必填、示例值、响应格式和可能的状态码。
## 注意事项
- Swagger docstring 中不要放敏感信息（如真实 API Key、生产环境域名）。
- 生产环境中建议对 Swagger UI 页面做访问控制（加密码或 IP 白名单），避免公开暴露接口细节。
- 接口文档的描述要及时更新——虽然 Swagger 自动生成结构，但描述文字还是需要开发者手动维护。
- 不要为了文档好看而虚构参数或响应格式。文档和实际行为必须一致，否则文档就是"谎言"。
## 坑点
| 坑 | 现象 | 原因 | 解决方案 |
|----|------|------|----------|
| 忘记更新描述 | 文档上还是旧的参数名，前端按照文档调接口失败 | 改了代码但没改 docstring | 养成"改接口必改 docstring"的习惯，Code Review 时检查 |
| 生产暴露 Swagger | 任何人都能看到所有接口细节 | Swagger UI 路由没有做访问控制 | 生产环境对 /docs 加 Basic Auth 或直接关闭 |
| YAML 缩进错误 | Swagger 页面加载失败，报 500 | docstring 中 YAML 缩进不对（多一个或少一个空格） | 用 IDE 的 YAML 校验功能，或者先写成 JSON 再转 YAML |
| 忽略示例值 | Swagger UI 的 Try it out 功能无法使用 | 参数没设置 example 字段 | 为每个参数提供真实的示例值，方便自己和他人测试 |
