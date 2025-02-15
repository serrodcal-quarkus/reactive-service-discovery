#!/bin/bash

# Paso 1: Registrar la aplicación con un dominio y otros datos
echo "Paso 1: Registrando la aplicación..."
register_response=$(grpcurl -plaintext -d '{"domain": "my-app.mydomain.net","ip": "192.168.1.10","name": "MyApp","mode": "LIVE"}' localhost:9000 discovery.ServiceDiscovery.Register)
echo "Resultado Registro: $register_response"
uuid=$(echo $register_response | jq -r '.uuid')
echo "UUID recibido: $uuid"
echo ""

# Paso 2: Buscar la aplicación por dominio
echo "Paso 2: Buscando por dominio..."
search_response=$(grpcurl -plaintext -d "{\"domain\": \"my-app.mydomain.net\"}" localhost:9000 discovery.ServiceDiscovery.Search)
echo "Resultado de la búsqueda: $search_response"
echo ""

# Paso 3: Enviar un HeartBeat con el UUID recibido
echo "Paso 3: Enviando HeartBeat con UUID $uuid..."
heartBeat_response=$(grpcurl -plaintext -d "{\"uuid\": \"$uuid\"}" localhost:9000 discovery.ServiceDiscovery.HeartBeat)
echo "Resultado HeartBeat: $heartBeat_response"
echo ""

# Paso 4: Buscar la aplicación nuevamente para verificar la actualización de 'confidence'
echo "Paso 4: Buscando nuevamente por dominio..."
search_response_updated=$(grpcurl -plaintext -d "{\"domain\": \"my-app.mydomain.net\"}" localhost:9000 discovery.ServiceDiscovery.Search)
echo "Resultado de la búsqueda actualizada: $search_response_updated"
