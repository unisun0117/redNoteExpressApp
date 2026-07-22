FROM bellsoft/liberica-openjdk-debian:25 AS builder
WORKDIR /builder

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} application.jar

# 按 Spring Boot 分层布局解压，最大化 Docker 层缓存复用
RUN java -Djarmode=tools -jar application.jar extract --layers --destination extracted

# ============================================================
# 运行阶段：精简 JRE + CDS 优化
# ============================================================
FROM bellsoft/liberica-openjre-debian:25-cds

# 安装运行时系统依赖
#   fontconfig  - 中文字体渲染（K8s hostPath 挂载 /home/qdama/fonts）
#   tzdata      - 时区数据（容器默认 UTC，项目需要 Asia/Shanghai）
#   ca-certificates - 微信支付证书 HTTPS 验证
RUN apt-get update && apt-get install -y --no-install-recommends \
    fontconfig \
    tzdata \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 创建非 root 用户 qdama
RUN groupadd -g 1001 qdama && \
    useradd -u 1001 -g qdama -m -s /bin/bash qdama

WORKDIR /home/qdama

# 创建运行时挂载目录并授权给 qdama 用户
RUN mkdir -p /home/qdama/logs \
             /home/qdama/certs && \
    chown -R qdama:qdama /home/qdama

# 分层复制 jar 内容（最稳定的依赖层先复制，代码层最后）
COPY --from=builder /builder/extracted/dependencies/ ./
COPY --from=builder /builder/extracted/spring-boot-loader/ ./
COPY --from=builder /builder/extracted/snapshot-dependencies/ ./
COPY --from=builder /builder/extracted/application/ ./

# 微信支付证书（Jenkins 构建时从 Credentials 注入，本地构建跳过）
# .ci/certs/ 已加入 .gitignore，不会提交到仓库
ARG CERT_DIR=.ci/certs
COPY ${CERT_DIR}/ /home/qdama/certs/
RUN chmod -R 600 /home/qdama/certs/ && chown -R qdama:qdama /home/qdama/certs/

# 切换到非 root 用户
USER qdama

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar application.jar"]
