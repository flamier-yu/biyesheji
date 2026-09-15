# ============================================================
# 后端应用镜像
# 构建前置条件：先在宿主机执行 mvn clean package -DskipTests
# 产物需存在于 target/pms-server.jar
# ============================================================
FROM eclipse-temurin:8-jre-jammy

LABEL maintainer="云协同研发中心" \
      description="云协同 PMS 后端服务"

ENV TZ=Asia/Shanghai \
    JAVA_OPTS="-Xms256m -Xmx512m -Dfile.encoding=UTF-8" \
    LOG_HOME=/app/logs

WORKDIR /app

# 上传目录与日志目录
RUN mkdir -p /app/uploads /app/logs

COPY target/pms-server.jar /app/app.jar

EXPOSE 8080

# 显式指定端口：避免外部环境注入 server.port 导致端口不确定
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -DLOG_HOME=$LOG_HOME -jar /app/app.jar --server.port=8080"]
