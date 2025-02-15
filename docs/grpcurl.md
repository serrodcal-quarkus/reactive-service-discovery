# gRPCurl

## Example

```shell
brew install grpcurl 
```

```shell
grpcurl -plaintext localhost:9000 list
```

```shell
grpcurl -plaintext localhost:9000 list hello.HelloGrpc
```

```shell
grpcurl -plaintext -d '{"name": "Quarkus"}' localhost:9000 hello.HelloGrpc/SayHello
```

## Service Discovery

* List:

```shell
grpcurl -plaintext localhost:9000 list discovery.ServiceDiscovery
```

* Registration:

```shell
grpcurl -plaintext -d '{"domain": "my-app.mydomain.net","ip": "192.168.1.10","name": "MyApp","mode": "LIVE"}' localhost:9000 discovery.ServiceDiscovery.Register
```

* Search by domain:

```shell
grpcurl -plaintext -d '{"domain": "my-app.mydomain.net"}' localhost:9000 discovery.ServiceDiscovery.Search
```

* Send heart beat:

```java
grpcurl -plaintext -d '{"uuid": "cf74fcd4-5c3e-44e9-b7a7-1a1690cc775e"}' localhost:9000 discovery.ServiceDiscovery.HeartBeat
```
