package com.griet.library.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Caffeine Cache Configuration
 *
 * Cache strategy:
 *  - "books-page"   : paginated book results (TTL 10 min, max 200 pages cached)
 *  - "books-search" : search result caches   (TTL 5 min,  max 100 entries)
 *  - "dashboard"    : dashboard stats        (TTL 5 min,  max 10 entries)
 *  - "book-detail"  : single book lookup     (TTL 30 min, max 2000 entries)
 *
 * For 130k books:  pagination cache dramatically reduces DB hits.
 * For 100 users:   dashboard cache means N users → 1 DB query per 5 minutes.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {

        CaffeineCache booksPage = buildCache("books-page",
                200, 10, TimeUnit.MINUTES);

        CaffeineCache booksSearch = buildCache("books-search",
                100, 5, TimeUnit.MINUTES);

        CaffeineCache dashboard = buildCache("dashboard",
                10, 5, TimeUnit.MINUTES);

        CaffeineCache bookDetail = buildCache("book-detail",
                2000, 30, TimeUnit.MINUTES);

        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(booksPage, booksSearch, dashboard, bookDetail));
        return manager;
    }

    private CaffeineCache buildCache(String name, int maxSize,
                                     long ttl, TimeUnit unit) {
        return new CaffeineCache(name,
                Caffeine.newBuilder()
                        .maximumSize(maxSize)
                        .expireAfterWrite(ttl, unit)
                        .recordStats()   // enables hit-rate logging
                        .build());
    }
}
