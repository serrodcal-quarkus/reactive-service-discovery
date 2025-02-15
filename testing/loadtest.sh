#!/bin/bash

# URL del servicio gRPC
grpc_url="localhost:9000"

# Definir el archivo proto del servicio gRPC para que ghz pueda entenderlo
proto_file="./src/main/proto/discovery.proto"

# Paso 1: Registrar la aplicación con un dominio y otros datos
echo "Paso 1: Registrando la aplicación..."
register_response=$(grpcurl -plaintext -d '{"domain": "my-app.mydomain.net","ip": "192.168.1.10","name": "MyApp","mode": "LIVE"}' $grpc_url discovery.ServiceDiscovery.Register)
uuid=$(echo $register_response | jq -r '.uuid')

echo "Resultado Registro: $register_response"
echo "UUID recibido: $uuid"

# Paso 2: Crear un archivo de carga utilizando ghz
echo "Paso 2: Generando prueba de carga..."
ghz -c 100 -n 1000 \
  --proto $proto_file \
  --call discovery.ServiceDiscovery.Register \
  -d '{"domain": "my-app.mydomain.net", "ip": "192.168.1.10", "name": "MyApp", "mode": "LIVE"}' \
  --insecure \
  $grpc_url

# Paso 3: Buscar la aplicación por dominio
echo "Paso 3: Buscando por dominio..."
ghz -c 100 -n 1000 \
  --proto $proto_file \
  --call discovery.ServiceDiscovery.Search \
  -d "{\"domain\": \"my-app.mydomain.net\"}" \
  --insecure \
  $grpc_url

# Paso 4: Enviar un HeartBeat con el UUID recibido
echo "Paso 4: Enviando HeartBeat con UUID $uuid..."
ghz -c 100 -n 1000 \
  --proto $proto_file \
  --call discovery.ServiceDiscovery.HeartBeat \
  -d "{\"uuid\": \"$uuid\"}" \
  --insecure \
  $grpc_url

# Paso 5: Buscar nuevamente para verificar la actualización de 'confidence'
echo "Paso 5: Buscando nuevamente por dominio..."
ghz -c 100 -n 1000 \
  --proto $proto_file \
  --call discovery.ServiceDiscovery.Search \
  -d "{\"domain\": \"my-app.mydomain.net\"}" \
  --insecure \
  $grpc_url
