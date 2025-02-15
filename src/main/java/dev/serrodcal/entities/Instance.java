package dev.serrodcal.entities;

import java.util.UUID;

public record Instance(
        UUID uuid,
        String name,
        String domain,
        String ip,
        Mode mode,
        Integer confidence
) { }
