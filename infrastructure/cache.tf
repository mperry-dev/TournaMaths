# Using non-serverless cache without replicas as cheaper when choose most basic instance. In real website I'd recommend serverless as simpler.
# NOTE can't turn on transit_encryption_enabled for Redis
resource "aws_elasticache_cluster" "tournamaths_elasticache_redis" {
  cluster_id           = "tournamaths-redis-cluster"
  engine               = "redis"
  node_type            = "cache.t4g.micro"
  num_cache_nodes      = 1                # Keeping low so cheap
  parameter_group_name = "default.redis7" # Cluster mode is disabled https://docs.aws.amazon.com/AmazonElastiCache/latest/red-ug/ParameterGroups.Redis.html#ParameterGroups.Redis.7
  engine_version       = "7.1"
  port                 = 6379
  subnet_group_name    = aws_elasticache_subnet_group.tournamaths_private_elasticache_subnet_group.name
  security_group_ids   = [aws_security_group.tournamaths_elasticache_sg.id]

  tags = {
    Name = "TournaMathsElastiCacheRedis"
  }
}
