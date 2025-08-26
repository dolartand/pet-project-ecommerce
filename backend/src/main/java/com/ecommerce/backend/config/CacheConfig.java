package com.ecommerce.backend.config;

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CACHE_PRODUCTS = "products";
    public static final String CACHE_PRODUCT_DETAILS = "productDetails";
    public static final String CACHE_CATEGORIES = "categories";
    public static final String CACHE_USER_PROFILE = "userProfile";
    public static final String CACHE_REVIEWS = "reviews";
    public static final String CACHE_ORDERS = "orders";

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return  RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(60))
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer())
                );
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        return (builder) -> builder
                .withCacheConfiguration(CACHE_PRODUCTS,
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(30)))
                .withCacheConfiguration(CACHE_PRODUCT_DETAILS,
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(10)))
                .withCacheConfiguration(CACHE_CATEGORIES,
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofHours(2)))
                .withCacheConfiguration(CACHE_USER_PROFILE,
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofHours(1)))
                .withCacheConfiguration(CACHE_REVIEWS,
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(30)))
                .withCacheConfiguration(CACHE_ORDERS,
                        RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(5)));
    }
}
