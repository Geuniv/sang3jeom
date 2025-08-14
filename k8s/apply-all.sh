#!/usr/bin/env bash
set -euo pipefail

# Required envs
if [[ -z "${ECR:-}" || -z "${RDS_ENDPOINT:-}" ]]; then
  echo "[ERROR] Export variables first: ECR, RDS_ENDPOINT"
  exit 1
fi

# Optional secrets with sensible defaults (replace with strong passwords in CI/Secrets)
USER_DB_USERNAME=${USER_DB_USERNAME:-user_service}
USER_DB_PASSWORD=${USER_DB_PASSWORD:-user_service}
ORDER_DB_USERNAME=${ORDER_DB_USERNAME:-order_service}
ORDER_DB_PASSWORD=${ORDER_DB_PASSWORD:-order_service}
COMMUNITY_DB_USERNAME=${COMMUNITY_DB_USERNAME:-community_service}
COMMUNITY_DB_PASSWORD=${COMMUNITY_DB_PASSWORD:-community_service}
REVIEW_DB_USERNAME=${REVIEW_DB_USERNAME:-review_service}
REVIEW_DB_PASSWORD=${REVIEW_DB_PASSWORD:-review_service}

# 1) Namespace
kubectl apply -f k8s/ns-dev.yaml

# 2) Config/Secret update (no files needed)
#    - ConfigMap: RDS endpoint + per-service DB names/URLs
#    - Secret   : per-service DB credentials
kubectl -n dev patch configmap sang3jeom-config --type merge \
  -p "{\"data\":{
    \"RDS_ENDPOINT\":\"$RDS_ENDPOINT\",
    \"USER_DB_NAME\":\"user_service\",
    \"REVIEW_DB_NAME\":\"review_service\",
    \"ORDER_DB_URL\":\"jdbc:mysql://$RDS_ENDPOINT:3306/order_service?useSSL=false&characterEncoding=utf8&serverTimezone=UTC\",
    \"COMMUNITY_DB_URL\":\"jdbc:mysql://$RDS_ENDPOINT:3306/community_service?useSSL=false&characterEncoding=utf8&serverTimezone=UTC\"
  }}" || true

kubectl -n dev patch secret sang3jeom-secrets --type merge \
  -p "{\"stringData\":{
    \"USER_DB_USERNAME\":\"$USER_DB_USERNAME\",\"USER_DB_PASSWORD\":\"$USER_DB_PASSWORD\",
    \"ORDER_DB_USERNAME\":\"$ORDER_DB_USERNAME\",\"ORDER_DB_PASSWORD\":\"$ORDER_DB_PASSWORD\",
    \"COMMUNITY_DB_USERNAME\":\"$COMMUNITY_DB_USERNAME\",\"COMMUNITY_DB_PASSWORD\":\"$COMMUNITY_DB_PASSWORD\",
    \"REVIEW_DB_USERNAME\":\"$REVIEW_DB_USERNAME\",\"REVIEW_DB_PASSWORD\":\"$REVIEW_DB_PASSWORD\"
  }}" || true

# 3) Services (substitute only ECR, RDS_ENDPOINT)
env ECR=$ECR RDS_ENDPOINT=$RDS_ENDPOINT envsubst < k8s/user-service.yaml | kubectl apply -f -
env ECR=$ECR RDS_ENDPOINT=$RDS_ENDPOINT envsubst < k8s/community-service.yaml | kubectl apply -f -
env ECR=$ECR RDS_ENDPOINT=$RDS_ENDPOINT envsubst < k8s/order-service.yaml | kubectl apply -f -
env ECR=$ECR RDS_ENDPOINT=$RDS_ENDPOINT envsubst < k8s/review-service.yaml | kubectl apply -f -
env ECR=$ECR RDS_ENDPOINT=$RDS_ENDPOINT envsubst < k8s/image-service.yaml | kubectl apply -f -

# 4) Ingress
kubectl apply -f k8s/ingress-dev.yaml || true

# 5) Status
kubectl -n dev get pods,svc,ingress 