# 백엔드 운영 이미지. 빌드는 JDK, 실행은 JRE 이미지에서 한다.
# 베이스 이미지는 태그와 digest로 고정한다 (AGENTS.md 의존성 규칙: 공개 7일 지난 버전)

FROM eclipse-temurin:21.0.13_9-jdk-alpine@sha256:82801f9dcdcbad5a05082aba8c86d1da5b72ccb498e71318b1f91b5a477887b3 AS build
# Java4Shell(CVE-2026-114938) 완화: Temurin 수정판(21.0.13.1)이 아직 없어서 JDK Base64 디코더의 AVX-512 경로를 끈다
ENV JAVA_TOOL_OPTIONS="-XX:-UseBASE64Intrinsics"
WORKDIR /app
# 의존성만 먼저 받아 두면 소스만 바뀐 빌드에서 이 층을 재사용한다
COPY gradlew settings.gradle build.gradle gradle.lockfile ./
COPY gradle gradle
RUN ./gradlew --no-daemon -q dependencies > /dev/null
COPY src src
# 테스트는 CI에서 돌린다. 툴체인 자동 감지를 꺼서 이 이미지의 JDK만 쓴다
RUN ./gradlew --no-daemon -Dorg.gradle.java.installations.auto-detect=false bootJar -x test \
    && cp "$(ls build/libs/*.jar | grep -v -- '-plain.jar$')" /app/app.jar

FROM eclipse-temurin:21.0.13_9-jre-alpine@sha256:ac05c3c42b7c2f1a52c9bd52f01041fa1958231b15ebeae7420429cbc5baf474
ENV JAVA_TOOL_OPTIONS="-XX:-UseBASE64Intrinsics"
RUN addgroup -S app && adduser -S -G app -H app
WORKDIR /app
COPY --from=build /app/app.jar app.jar
USER app
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
