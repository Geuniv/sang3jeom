FROM python:3.10

WORKDIR /app

# image-service 디렉토리의 모든 파일 복사
COPY backend/image-service/ .

# scripts 디렉토리 복사
COPY scripts ./scripts

# dos2unix 설치 & 스크립트 변환 & 실행권한 부여
RUN apt-get update \
    && apt-get install -y --no-install-recommends dos2unix \
    && find ./scripts -type f -name "*.sh" -exec dos2unix {} \; \
    && chmod +x ./scripts/*.sh \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Python 패키지 설치
RUN pip install --no-cache-dir --upgrade pip \
    && pip install --no-cache-dir -r requirements.txt

# 컨테이너 실행 명령
CMD ["sh", "./scripts/docker-start.sh", "image-service", "8000"]
