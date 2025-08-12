# Frontend 환경변수 설정 가이드 (Vite)

## ⚠️ 중요: Vite 환경변수 방식

Vite에서는 **`VITE_` 접두사**를 사용하고 **`import.meta.env.VITE_변수명`**으로 접근합니다.

## 환경변수 파일 생성

### 1. 로컬 개발용 (.env.development)
프론트엔드 루트에 `frontend/.env.development` 파일을 생성하고 다음 내용을 추가하세요:

```bash
# Local Development Environment Variables
# 각 마이크로서비스별 로컬 포트
VITE_USER_SERVICE_URL=http://localhost:8080
VITE_COMMUNITY_SERVICE_URL=http://localhost:8083
VITE_ORDER_SERVICE_URL=http://localhost:8082
VITE_REVIEW_SERVICE_URL=http://localhost:8084
VITE_IMAGE_SERVICE_URL=http://localhost:8000

# API 기본 URL (주로 사용하는 서비스)
VITE_API_BASE_URL=http://localhost:8080

# OAuth2 (User Service에서 처리)
VITE_OAUTH2_BASE_URL=http://localhost:8080

# 프론트엔드
VITE_FRONTEND_URL=http://localhost:3000

# Feature Flags
VITE_ENABLE_OAUTH=true
VITE_ENABLE_SOCIAL_LOGIN=true
```

### 2. 프로덕션 배포용 (.env.production)
프론트엔드 루트에 `frontend/.env.production` 파일을 생성하고 다음 내용을 추가하세요:

```bash
# Production Environment Variables (EKS Deployment)
# 모든 서비스가 하나의 로드밸런서 뒤에 있음
VITE_USER_SERVICE_URL=http://your-alb-dns.ap-northeast-2.elb.amazonaws.com
VITE_COMMUNITY_SERVICE_URL=http://your-alb-dns.ap-northeast-2.elb.amazonaws.com
VITE_ORDER_SERVICE_URL=http://your-alb-dns.ap-northeast-2.elb.amazonaws.com
VITE_REVIEW_SERVICE_URL=http://your-alb-dns.ap-northeast-2.elb.amazonaws.com
VITE_IMAGE_SERVICE_URL=http://your-alb-dns.ap-northeast-2.elb.amazonaws.com

# API 기본 URL
VITE_API_BASE_URL=http://your-alb-dns.ap-northeast-2.elb.amazonaws.com

# OAuth2
VITE_OAUTH2_BASE_URL=http://your-alb-dns.ap-northeast-2.elb.amazonaws.com

# 프론트엔드
VITE_FRONTEND_URL=https://yourdomain.com

# Feature Flags
VITE_ENABLE_OAUTH=true
VITE_ENABLE_SOCIAL_LOGIN=true
```

### 3. 기본 환경 (.env)
프론트엔드 루트에 `frontend/.env` 파일을 생성하고 공통 설정만 추가하세요:

```bash
# 공통 설정
VITE_ENABLE_OAUTH=true
VITE_ENABLE_SOCIAL_LOGIN=true
```

## 환경별 실행

### 로컬 개발
```bash
cd frontend
npm run dev  # .env.development 자동 로드
```

### 프로덕션 빌드
```bash
cd frontend
npm run build  # .env.production 자동 로드
```

## 코드에서 사용법

```javascript
// Vite 방식으로 환경변수 접근
const apiUrl = import.meta.env.VITE_API_BASE_URL;
const userServiceUrl = import.meta.env.VITE_USER_SERVICE_URL;
const oauthUrl = import.meta.env.VITE_OAUTH2_BASE_URL;

// 기본값과 함께 사용
const apiUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
```

## 현재 적용된 변경사항

✅ **코드 수정 완료**:
- `Login.jsx`: `process.env.REACT_APP_` → `import.meta.env.VITE_`
- `axiosInstance.js`: `process.env.REACT_APP_` → `import.meta.env.VITE_`

## 다음 단계

1. **환경변수 파일 생성**: 위의 내용으로 `.env.development`, `.env.production`, `.env` 파일 생성
2. **로컬 테스트**: `npm run dev`로 환경변수 적용 확인
3. **EKS 연동**: 실제 ALB DNS로 `.env.production` 수정 후 배포 테스트

## 주의사항

1. **VITE_ 접두사 필수**: React의 `REACT_APP_`이 아님
2. **빌드 시점 포함**: 환경변수는 빌드 시점에 포함되므로 런타임 변경 불가
3. **Git**: `.env*` 파일들은 `.gitignore`에 추가하여 커밋하지 마세요
4. **ALB DNS**: EKS 배포 시 실제 로드밸런서 DNS로 `.env.production` 수정 필요
