package dev.serrodcal.entities;

import java.util.UUID;

public record Instance(
        UUID uuid,
        String name,
        String domain,
        String ip,
        Mode mode,
        Integer confidence
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID uuid;
        private String name;
        private String domain;
        private String ip;
        private Mode mode;
        private Integer confidence;

        public Builder uuid(UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder domain(String domain) {
            this.domain = domain;
            return this;
        }

        public Builder ip(String ip) {
            this.ip = ip;
            return this;
        }

        public Builder mode(Mode mode) {
            this.mode = mode;
            return this;
        }

        public Builder confidence(Integer confidence) {
            this.confidence = confidence;
            return this;
        }

        public Instance build() {
            return new Instance(uuid, name, domain, ip, mode, confidence);
        }
    }
}
