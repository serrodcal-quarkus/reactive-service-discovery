package dev.serrodcal.tasks;

import dev.serrodcal.entities.Instance;
import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import io.quarkus.cache.CaffeineCache;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@ApplicationScoped
public class CacheUpdaterTask {

    private static final Logger log = Logger.getLogger(CacheUpdaterTask.class);

    @Inject
    @CacheName("service-discovery")
    Cache registrationCache;

    @Scheduled(every = "{cache.update.interval}")
    void updateCacheEntries() {
        log.info("Iniciando actualización de la caché...");

        registrationCache.as(CaffeineCache.class).keySet().forEach(i -> {
            UUID uuid = (UUID) i;
            registrationCache.as(CaffeineCache.class).get(uuid, k -> null)
                    .subscribe().with(j -> {
                        Instance instance = (Instance) j;
                        log.debug("Updated " + instance.toString());
                        if (instance.confidence() > 10)
                            registrationCache.as(CaffeineCache.class).put(
                                    uuid,
                                    CompletableFuture.completedFuture(
                                            Instance.builder()
                                                    .uuid(uuid)
                                                    .name(instance.name())
                                                    .domain(instance.domain())
                                                    .ip(instance.ip())
                                                    .mode(instance.mode())
                                                    .confidence(instance.confidence() - 10)
                                                    .build()
                                    )
                            );
                        else
                            registrationCache.as(CaffeineCache.class).invalidate(uuid);
                    });
        });

        log.info("Actualización de la caché completada.");
    }
}

