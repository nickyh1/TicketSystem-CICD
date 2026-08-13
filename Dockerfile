# 第一阶段：编译和打包
FROM eclipse-temurin:21-jdk AS build

WORKDIR /workspace

# 先复制 Maven Wrapper 和 pom.xml
# 只要 pom.xml 不变，这一层下载的依赖就可以复用缓存
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw \
    && ./mvnw -B -ntp dependency:go-offline

# 再复制源代码
COPY src/ src/

# GitHub Actions 会在构建镜像前执行完整测试
# 这里不重复执行测试，只负责生成 JAR
RUN ./mvnw -B -ntp clean package -DskipTests


# 第二阶段：运行程序
FROM eclipse-temurin:21-jre

WORKDIR /app

# Compose 的健康检查会使用 curl
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system appgroup \
    && useradd --system --gid appgroup appuser

COPY --from=build --chown=appuser:appgroup /workspace/target/app.jar /app/app.jar

# 生产容器不使用 root 运行
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]