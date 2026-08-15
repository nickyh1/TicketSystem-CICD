FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

# 测试已经由 GitHub Actions 的 CI 阶段执行，
# 镜像阶段只负责生成可运行的 JAR，避免重复跑测试。
RUN mvn -B -ntp clean package -DskipTests


FROM eclipse-temurin:21-jre

WORKDIR /app

# curl 用于 Compose 健康检查。
# 同时创建无登录权限的普通系统用户。
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && groupadd --system ticketsystem \
    && useradd --system \
        --gid ticketsystem \
        --home-dir /app \
        --shell /usr/sbin/nologin \
        ticketsystem \
    && rm -rf /var/lib/apt/lists/*

# JAR 文件直接归普通用户所有。
COPY --from=build \
    --chown=ticketsystem:ticketsystem \
    /app/target/*.jar \
    /app/app.jar

# 从这里开始，后面的程序不再以 root 身份运行。
USER ticketsystem:ticketsystem

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]