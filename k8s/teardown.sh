#!/bin/bash
echo "Eliminando namespace quasar-fire y todos los recursos..."
kubectl delete namespace quasar-fire --grace-period=30
kubectl delete clusterrole prometheus 2>/dev/null || true
kubectl delete clusterrolebinding prometheus 2>/dev/null || true
echo "Limpieza completada."
