package com.tournamaths.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.elasticache.ElastiCacheClient;
import software.amazon.awssdk.services.elasticache.model.CacheCluster;
import software.amazon.awssdk.services.elasticache.model.CacheNode;
import software.amazon.awssdk.services.elasticache.model.DescribeCacheClustersRequest;
import software.amazon.awssdk.services.elasticache.model.DescribeCacheClustersResponse;
import software.amazon.awssdk.services.elasticache.model.ElastiCacheException;

@Configuration
@Profile("prod")  // only use this for production - locally rely on application-dev.properties
public class ProdRedisConfig {
    // Loaded from application-prod.properties
    @Value("${aws.region}")
    private String region;



    private CacheNode getCacheNode() {
        try (ElastiCacheClient elasticacheClient = ElastiCacheClient.builder()
                .region(Region.of(region))
                .build()) {

            DescribeCacheClustersRequest request = DescribeCacheClustersRequest.builder()
                    .cacheClusterId("tournamaths-redis-cluster")
                    .showCacheNodeInfo(true)
                    .build();

            DescribeCacheClustersResponse response = elasticacheClient.describeCacheClusters(request);
            List<CacheCluster> cacheClusters = response.cacheClusters();

            if (cacheClusters.size() != 1){
                System.err.println("Should have 1 Redis Cluster of identifier tournamaths-redis-cluster, but have "+cacheClusters.size());
                System.exit(1);
            }

            List<CacheNode> cacheNodes = cacheClusters.get(0).cacheNodes();

            if (cacheNodes.size() != 1){
                System.err.println("Should have 1 Redis Node in the Cluster with identifier tournamaths-redis-cluster, but have "+cacheNodes.size());
                System.exit(1);
            }

            return cacheNodes.get(0);

        } catch (ElastiCacheException e) {
            System.err.println(e.awsErrorDetails().errorMessage());
            System.exit(1);
            return null;  // Keep type system happy.
        }
    }
}
