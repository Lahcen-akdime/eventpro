package com.eventPro.api.Config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "eventpro.reservation")
public record ReservationProperties(
        @NotNull Duration holdDuration
) {}