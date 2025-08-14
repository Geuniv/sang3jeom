#!/bin/bash

echo "🚀 Setting up ArgoCD..."

# 1. ArgoCD 설치
echo "📦 Installing ArgoCD..."
kubectl apply -f ../k8s/argocd-install.yaml

# 2. ArgoCD 파드가 준비될 때까지 대기
echo "⏳ Waiting for ArgoCD pods to be ready..."
kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=argocd-server -n argocd --timeout=300s
kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=argocd-application-controller -n argocd --timeout=300s
kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=argocd-repo-server -n argocd --timeout=300s

# 3. ArgoCD 애플리케이션 생성
echo "📋 Creating ArgoCD applications..."
kubectl apply -f ../k8s/argocd-applications.yaml

# 4. ArgoCD 서버 포트포워딩 정보 출력
echo "🌐 ArgoCD setup complete!"
echo ""
echo "To access ArgoCD UI, run:"
echo "kubectl port-forward svc/argocd-server 8080:80 -n argocd"
echo ""
echo "Then open http://localhost:8080 in your browser"
echo ""
echo "Default credentials:"
echo "Username: admin"
echo "Password: $(kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d)"
echo ""
echo "Current ArgoCD applications:"
kubectl get applications -n argocd
