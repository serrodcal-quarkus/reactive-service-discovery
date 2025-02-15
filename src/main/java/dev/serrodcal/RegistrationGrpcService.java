package dev.serrodcal;

import com.google.protobuf.Empty;
import dev.serrodcal.discovery.*;
import dev.serrodcal.entities.Instance;
import dev.serrodcal.entities.Mode;
import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import io.quarkus.cache.CaffeineCache;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@GrpcService
public class RegistrationGrpcService implements ServiceDiscovery {

    private static final Logger log = Logger.getLogger(RegistrationGrpcService.class);

    @Inject
    @CacheName("service-discovery")
    Cache registrationCache;

    @Inject
    @CacheName("uudi-domain")
    Cache domainCache;

    @Override
    public Uni<Registered> register(Registration request) {
        UUID uuid = UUID.randomUUID();
        Instance instance = new Instance(
                uuid,
                request.getName(),
                request.getDomain(),
                request.getIp(),
                Mode.LIVE,
                -1
        );

        registrationCache.as(CaffeineCache.class).put(uuid, CompletableFuture.completedFuture(instance));
        domainCache.as(CaffeineCache.class).put(request.getDomain(), CompletableFuture.completedFuture(uuid));

        Registered registered = Registered.newBuilder()
                .setUuid(uuid.toString())
                .build();

        return Uni.createFrom().item(registered);
    }

    @Override
    public Uni<Empty> heartBeat(HeartBeatMessage request) {
        UUID uuid = UUID.fromString(request.getUuid()); // Suponiendo que el UUID viene como String en el mensaje

        return registrationCache.get(uuid, k -> null)
                .onItem().ifNotNull().invoke(i -> log.info(i.toString()))
                .onItem().ifNotNull().invoke(instanceItem -> {
                    Instance instance = (Instance) instanceItem;
                    registrationCache.as(CaffeineCache.class).put(
                            uuid,
                            CompletableFuture.completedFuture(
                                    new Instance(
                                            uuid,
                                            instance.name(),
                                            instance.domain(),
                                            instance.ip(),
                                            instance.mode(),
                                            100
                                    )
                                )
                            );
                })
                .onItem().ifNotNull().transform(i -> Empty.newBuilder().build());
    }

    @Override
    public Uni<Result> search(SearchRequest request) {
       return domainCache.get(request.getDomain(), k -> null)
               .onItem().ifNotNull().invoke(i -> log.info("UUID: " + i.toString()))
               .onItem().ifNotNull().transformToUni(i -> {
                   return registrationCache.get(i, k -> null)
                           .onItem().ifNotNull().invoke(j -> log.info(j.toString()))
                           .onItem().ifNotNull().transform(j -> {
                               Instance instance = (Instance) j;
                               return Result
                                       .newBuilder()
                                       .setDomain(instance.domain())
                                       .setIp(instance.ip())
                                       .setConfidence(instance.confidence() == 0 ? 0 : instance.confidence())
                                       .build();
                           });
               });
    }
}
