// config/RouterConfig.java
package com.rentalmanagement.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouterConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Customer Service Routes
                .route("customer-service", r -> r.path("/api/customers/**")
                        .uri("lb://customer-service"))

                // Room Service Routes
                .route("room-types", r -> r.path("/api/room-types/**")
                        .uri("lb://room-service"))
                .route("rooms", r -> r.path("/api/rooms/**")
                        .uri("lb://room-service"))

                // Booking Service Routes
                .route("bookings", r -> r.path("/api/bookings/**")
                        .uri("lb://booking-service"))
                .route("check-ins", r -> r.path("/api/check-ins/**")
                        .uri("lb://booking-service"))

                // Payment Service Routes
                .route("invoices", r -> r.path("/api/invoices/**")
                        .uri("lb://payment-service"))
                .route("payments", r -> r.path("/api/payments/**")
                        .uri("lb://payment-service"))
                .route("reports", r -> r.path("/api/reports/**")
                        .uri("lb://payment-service"))
                .build();
    }
}