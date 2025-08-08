#!/usr/bin/env bash
set -euo pipefail

if [[ -z "${ECR:-}" || -z "${RDS_ENDPOINT:-}" || -z "${RDS_DB_NAME:-}" || -z "${RDS_USERNAME:-}" || -z "${RDS_PASSWORD:-}" ]]; then
  echo "[ERROR] Export variables first: ECR, RDS_ENDPOINT, RDS_DB_NAME, RDS_USERNAME, RDS_PASSWORD"
  exit 1
fi

# 1) Namespace
kubectl apply -f k8s/ns-dev.yaml

# 2) Config/Secret (envsubst)
export AWS_ACCESS_KEY="${AWS_ACCESS_KEY:-}"
export AWS_SECRET_KEY="${AWS_SECRET_KEY:-}"
export REPLICATE_API_TOKEN="${REPLICATE_API_TOKEN:-}"
export JWT_SECRET="${JWT_SECRET:-changeme}"
envsubst < k8s/config-dev.yaml | kubectl apply -f -

# 3) Services
env ECR=$ECR RDS_ENDPOINT=$RDS_ENDPOINT RDS_DB_NAME=$RDS_DB_NAME envsubst < k8s/user-service.yaml | kubectl apply -f -
env ECR=$ECR RDS_ENDPOINT=$RDS_ENDPOINT RDS_DB_NAME=$RDS_DB_NAME envsubst < k8s/community-service.yaml | kubectl apply -f -
env ECR=$ECR RDS_ENDPOINT=$RDS_ENDPOINT RDS_DB_NAME=$RDS_DB_NAME envsubst < k8s/order-service.yaml | kubectl apply -f -
env ECR=$ECR RDS_ENDPOINT=$RDS_ENDPOINT RDS_DB_NAME=$RDS_DB_NAME envsubst < k8s/review-service.yaml | kubectl apply -f -
env ECR=$ECR RDS_ENDPOINT=$RDS_ENDPOINT RDS_DB_NAME=$RDS_DB_NAME envsubst < k8s/image-service.yaml | kubectl apply -f -

# 4) Ingress
kubectl apply -f k8s/ingress-dev.yaml

# 5) Status
kubectl -n dev get pods,svc,ingress 