#!/bin/bash
set -e

echo "=========================================="
echo "  Quasar Fire Platform - K8s Deployment"
echo "=========================================="

echo ""
echo "[1/7] Creando namespace..."
kubectl apply -f namespace/namespace.yaml

echo ""
echo "[2/7] Desplegando secrets..."
kubectl apply -f infinispan/secret.yaml
kubectl apply -f postgresql/secret.yaml

echo ""
echo "[3/7] Desplegando Infinispan..."
kubectl apply -f infinispan/configmap.yaml
kubectl apply -f infinispan/statefulset.yaml
echo "  Esperando a que Infinispan este listo..."
kubectl -n quasar-fire wait --for=condition=ready pod/infinispan-0 --timeout=120s

echo ""
echo "[4/7] Desplegando PostgreSQL..."
kubectl apply -f postgresql/statefulset.yaml
echo "  Esperando a que PostgreSQL este listo..."
kubectl -n quasar-fire wait --for=condition=ready pod/postgresql-0 --timeout=120s

echo ""
echo "[5/7] Desplegando Kafka (KRaft mode)..."
kubectl apply -f kafka/statefulset.yaml
echo "  Esperando a que Kafka este listo..."
kubectl -n quasar-fire wait --for=condition=ready pod/kafka-0 --timeout=120s

echo ""
echo "[6/7] Desplegando microservicios..."
kubectl apply -f consumer/deployment.yaml
kubectl apply -f tracker/deployment.yaml
echo "  Esperando a que los pods esten listos..."
kubectl -n quasar-fire wait --for=condition=ready pod -l app=position-consumer --timeout=120s || echo "  WARN: Consumer aun no tiene imagen. Construir y pushear primero."

echo ""
echo "[7/7] Desplegando observabilidad e ingress..."
kubectl apply -f monitoring/prometheus.yaml
kubectl apply -f monitoring/grafana.yaml
kubectl apply -f ingress/ingress.yaml

echo ""
echo "=========================================="
echo "  Despliegue completado!"
echo "=========================================="
echo ""
echo "  Agregar a C:\Windows\System32\drivers\etc\hosts:"
echo "    127.0.0.1  quasar.local"
echo ""
echo "  URLs de acceso:"
echo "    Consumer API:  http://quasar.local/quasar/fire/Api/v1/topsecret"
echo "    Tracker API:   http://quasar.local/api/tracker/v1/satellites"
echo "    Grafana:       http://quasar.local/grafana  (admin/admin)"
echo "    Prometheus:    http://quasar.local/prometheus"
echo ""
echo "  Comandos utiles:"
echo "    kubectl -n quasar-fire get pods"
echo "    kubectl -n quasar-fire logs -f deploy/position-consumer"
echo "    kubectl -n quasar-fire logs -f deploy/satellite-tracker"
echo "    kubectl -n quasar-fire port-forward svc/grafana 3000:3000"
echo ""
