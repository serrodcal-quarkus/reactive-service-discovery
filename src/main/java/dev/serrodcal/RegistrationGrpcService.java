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
        Instance instance = newInstance(uuid, request);

        registerNewInstance(uuid, instance);
        registerByDomain(request.getDomain(), uuid);

        return Uni.createFrom().item(
                Registered.newBuilder()
                .setUuid(uuid.toString())
                .build()
        );
    }

    @Override
    public Uni<Empty> heartBeat(HeartBeatMessage request) {
        UUID uuid = UUID.fromString(request.getUuid());

        return registrationCache.get(uuid, k -> null)
                .onItem().ifNotNull().invoke(this::logHeartBeat)
                .onItem().ifNotNull().invoke(instance -> updateInstanceConfidence(uuid, (Instance) instance))
                .onItem().ifNotNull().transform(i -> Empty.newBuilder().build());
    }

    @Override
    public Uni<Result> search(SearchRequest request) {
        return domainCache.get(request.getDomain(), k -> null)
                .onItem().ifNotNull().invoke(i -> log.debug("UUID: " + i.toString()))
                .onItem().ifNotNull().transformToUni(i -> getInstanceByUUID(i));
    }

    private Instance newInstance(UUID uuid, Registration request) {
        return new Instance(
                uuid,
                request.getName(),
                request.getDomain(),
                request.getIp(),
                Mode.LIVE,
                -1
        );
    }

    private void registerNewInstance(UUID uuid, Instance instance) {
        log.debug("New " + instance.toString());
        registrationCache.as(CaffeineCache.class).put(uuid, CompletableFuture.completedFuture(instance));
    }

    private void registerByDomain(String domain, UUID uuid) {
        domainCache.as(CaffeineCache.class).put(domain, CompletableFuture.completedFuture(uuid));
    }

    private void logHeartBeat(Object instance) {
        log.debug(instance.toString());
    }

    private void updateInstanceConfidence(UUID uuid, Instance instance) {
        registrationCache.as(CaffeineCache.class).put(
                uuid,
                CompletableFuture.completedFuture(
                        Instance.builder()
                                .uuid(uuid)
                                .name(instance.name())
                                .domain(instance.domain())
                                .ip(instance.ip())
                                .mode(instance.mode())
                                .confidence(100)
                                .build()
                )
        );
    }

    private Uni<Result> getInstanceByUUID(Object i) {
        return registrationCache.get(i, k -> null)
                .onItem().ifNotNull().invoke(j -> log.debug("Found " + j.toString()))
                .onItem().ifNotNull().transform(j -> createResultFromInstance((Instance) j));
    }

    private Result createResultFromInstance(Instance instance) {
        return Result
                .newBuilder()
                .setDomain(instance.domain())
                .setIp(instance.ip())
                .setConfidence(instance.confidence() == 0 ? 0 : instance.confidence())
                .build();
    }
}
