#!/bin/bash

# URL de tu servidor gRPC
GRPC_SERVER="localhost:9000"

# Archivo proto que usas
PROTO_FILE="./src/main/proto/discovery.proto"

# Número de registros a insertar
NUM_REGISTERS=1000

# Iterar 1000 veces para insertar los registros
for i in $(seq 1 $NUM_REGISTERS); do
  # Generar un UUID aleatorio
  UUID=$(uuidgen)

  # Generar un dominio, IP y nombre aleatorios
  DOMAIN="my-app-${UUID}.mydomain.net"
  IP="192.168.1.$((RANDOM % 255 + 1))"
  NAME="MyApp-${UUID}"
  MODE="LIVE"

  # Crear el JSON con los datos
  JSON_DATA=$(cat <<EOF
{
  "domain": "$DOMAIN",
  "ip": "$IP",
  "name": "$NAME",
  "mode": "$MODE"
}
EOF
)

  # Registrar el servicio usando grpcurl
  echo "Registrando servicio $i con UUID $UUID..."
  grpcurl -plaintext -d "$JSON_DATA" $GRPC_SERVER discovery.ServiceDiscovery.Register
done

echo "Registro de $NUM_REGISTERS servicios completado."
