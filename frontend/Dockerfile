# 프론트 운영 이미지. next.config.ts의 output: "standalone" 결과물만 실행 이미지에 담는다.
# 베이스 이미지는 태그와 digest로 고정한다 (AGENTS.md 의존성 규칙: 공개 7일 지난 버전)

FROM node:22.23.5-alpine@sha256:1314fb30ec37592d5845e5a160d522c0e680972aa534549940b8d73bde88481a AS build
WORKDIR /app
ENV NEXT_TELEMETRY_DISABLED=1
COPY package.json package-lock.json .npmrc ./
# 설치 스크립트는 실행하지 않는다 (공급망 공격 대비)
RUN npm ci --ignore-scripts
COPY . .
RUN npm run build

FROM node:22.23.5-alpine@sha256:1314fb30ec37592d5845e5a160d522c0e680972aa534549940b8d73bde88481a
WORKDIR /app
ENV NODE_ENV=production \
    NEXT_TELEMETRY_DISABLED=1 \
    PORT=3000 \
    HOSTNAME=0.0.0.0
COPY --from=build --chown=node:node /app/.next/standalone ./
COPY --from=build --chown=node:node /app/.next/static ./.next/static
COPY --from=build --chown=node:node /app/public ./public
USER node
EXPOSE 3000
CMD ["node", "server.js"]
