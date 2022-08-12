package net.guru.microservice.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiGatewayConfiguration {

    @Bean
    public RouteLocator gatewayRouter(RouteLocatorBuilder builder) {
        return builder.routes().route(p -> p.path("/get") // http://localhost:8765/get
                .filters(f -> f.addRequestHeader("MyHeader", "MyURI").addRequestParameter("Param", "MyValue"))
                .uri("http://httpbin.org:80"))
                .route(p -> p.path("/currency-exchange-service/**").uri("lb://currency-exchange-service"))
                .route(p -> p.path("/currency-conversion-service/**").uri("lb://currency-conversion-service"))
                .route(p -> p.path("/currency-conversion-feign/**").uri("lb://currency-conversion-service"))
                .route(p -> p.path("/currency-conversion-new/**")
                                .filters(f -> f.rewritePath("/currency-conversion-new/(?<segment>.*)",
                                        "/currency-conversion-feign/${segment}"))
                                .uri("lb://currency-conversion-service"))
                .build();
    }
}