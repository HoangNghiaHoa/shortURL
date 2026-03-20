# Kiến trúc Hệ thống URL Shortener

## 🎯 Tổng quan

Hệ thống URL Shortener được thiết kế để đáp ứng:
- **High Performance**: Xử lý 10,000+ requests/second
- **High Availability**: 99.99% uptime
- **Scalability**: Horizontal scaling dễ dàng
- **Security**: Chống DDoS, rate limiting, input validation
- **Analytics**: Thu thập đầy đủ thông tin người dùng

## 🏗️ Kiến trúc Hệ thống

```
                         ┌─────────────┐
                         │  CloudFlare │
                         │  (DDoS, CDN)│
                         └──────┬──────┘
                                │
                         ┌──────▼──────┐
                         │    Nginx    │
                         │ (LB + Cache)│
                         └──────┬──────┘
                                │
                    ┌───────────┼───────────┐
                    ▼           ▼           ▼
            ┌───────────┐ ┌───────────┐ ┌───────────┐
            │  App-1    │ │  App-2    │ │  App-3    │
            │(Spring Boot)│(Spring Boot)│(Spring Boot)
            └─────┬─────┘ └─────┬─────┘ └─────┬─────┘
                  │             │             │
          ┌───────┼─────────────┼─────────────┼───────┐
          │       │             │             │       │
     ┌────▼───┐ ┌─▼──────┐  ┌──▼─────┐  ┌────▼────┐ │
     │ Redis  │ │ Kafka  │  │Postgres│  │ GeoIP   │ │
     │(Cache) │ │(Queue) │  │  (DB)  │  │MaxMind  │ │
     └────────┘ └────────┘  └────────┘  └─────────┘ │
          │                                          │
          └──────────────────────────────────────────┘
```

## 🔄 Flow Xử lý

### 1. Tạo Short URL
```
User Request
    ↓
Nginx (Rate Limit Check)
    ↓
Application (Validate URL)
    ↓
Generate 6-char code
    ↓
Save to PostgreSQL
    ↓
Cache to Redis (24h TTL)
    ↓
Return short URL
```

### 2. Redirect Flow
```
User clicks short URL
    ↓
Nginx (Rate Limit + Cache Check)
    ↓
If cached → Direct Redirect (301/302)
    ↓
If not cached → Query Redis
    ↓
If in Redis → Redirect + Update Cache
    ↓
If not in Redis → Query PostgreSQL
    ↓
Send analytics to Kafka (async)
    ↓
Redirect user
    ↓
Kafka Consumer processes analytics
    ↓
Save to PostgreSQL + Update Redis stats
```

## 🛡️ Bảo mật & Chống DDoS

### Layer 1: CloudFlare
- DDoS protection (L3/L4/L7)
- WAF (Web Application Firewall)
- Bot detection
- IP reputation filtering
- Geographic blocking

### Layer 2: Nginx
- Rate limiting per IP
    - API: 100 requests/hour
    - Redirect: 1000 requests/minute
- Connection limits: 10 concurrent per IP
- Request size limits (1KB)
- Invalid request filtering

### Layer 3: Application
- Bucket4j rate limiting (distributed via Redis)
- Input validation
- SQL injection prevention (JPA)
- XSS protection

### Layer 4: Database
- Prepared statements
- Connection pooling
- Read replicas for analytics

## 📊 Analytics Pipeline

```
Click Event
    ↓
Extract data (IP, UA, Referer, etc.)
    ↓
Enrich with GeoIP data
    ↓
Publish to Kafka topic
    ↓
Update Redis real-time stats
    ↓ (async)
Kafka Consumer
    ↓
Batch insert to PostgreSQL
    ↓
Aggregate to materialized views
```

### Metrics Collected
- **Request**: IP, timestamp, referer
- **Location**: Country, city, region, lat/lon, timezone
- **Device**: Browser, OS, device type
- **Network**: ISP, VPN detection
- **Performance**: Response time, cache hit rate

## 🚀 Performance Optimizations

### Caching Strategy
1. **L1 Cache**: Redis (in-memory)
    - Short URL → Original URL mapping
    - Real-time statistics
    - TTL: 24 hours

2. **L2 Cache**: Nginx
    - Redirect responses
    - TTL: 1 minute

3. **L3 Cache**: Database query cache
    - Materialized views for analytics

### Database Optimization
- **Indexes**: Short code, timestamp, country, device
- **Partitioning**: Click analytics by month
- **Connection pooling**: HikariCP (20 connections)
- **Batch inserts**: 20 records per batch
- **Read replicas**: Separate analytics queries

### Application Optimization
- **Async processing**: Kafka for analytics
- **Non-blocking I/O**: Spring WebFlux ready
- **Object pooling**: Database connections
- **Compression**: Gzip for responses

## 📈 Scalability

### Horizontal Scaling
```bash
# Add more app instances
docker-compose up --scale url-shortener=5
```

### Database Scaling
- **Write scaling**: Sharding by short code prefix
- **Read scaling**: Read replicas for analytics
- **Partitioning**: Time-based partitions for analytics

### Redis Scaling
- **Redis Cluster**: 3+ nodes
- **Redis Sentinel**: High availability

## 🔧 Configuration

### Environment Variables
```bash
DB_PASSWORD=your_secure_password
REDIS_PASSWORD=your_redis_password
KAFKA_BOOTSTRAP_SERVERS=kafka:9092
```

### System Resources (Per Instance)
- **CPU**: 2 cores minimum
- **RAM**: 2GB minimum
- **Storage**: 20GB (logs + database)
- **Network**: 1Gbps

## 📦 Deployment

### Development
```bash
docker-compose up -d
```

### Production
```bash
# Build
mvn clean package -DskipTests

# Run with production profile
java -jar -Dspring.profiles.active=prod target/url-shortener.jar
```

### Kubernetes (Optional)
- Deployment with 3+ replicas
- HPA (Horizontal Pod Autoscaler)
- Ingress with rate limiting
- StatefulSet for Kafka/Redis

## 🔍 Monitoring

### Metrics (Prometheus + Grafana)
- Request rate
- Error rate
- Response time (p50, p95, p99)
- Cache hit rate
- Database connection pool
- JVM metrics

### Alerts
- High error rate (>1%)
- Slow response time (>500ms)
- High CPU/Memory usage
- Database connection issues
- Kafka lag

## 💾 Backup & Recovery

### Database Backup
```bash
# Daily backup
pg_dump urlshortener > backup_$(date +%Y%m%d).sql

# Restore
psql urlshortener < backup_20260109.sql
```

### Redis Backup
- AOF enabled (append-only file)
- Snapshot every 60 seconds if 1000+ changes

## 🔐 Security Checklist

- ✅ HTTPS only (TLS 1.2+)
- ✅ Rate limiting on all endpoints
- ✅ Input validation and sanitization
- ✅ SQL injection prevention
- ✅ XSS protection headers
- ✅ CSRF tokens (if web UI)
- ✅ API key authentication (optional)
- ✅ Malicious URL detection
- ✅ Regular security updates
- ✅ Encrypted database backups

## 📊 Capacity Planning

### Single Instance Capacity
- **Requests/second**: ~1,000 RPS
- **Short URLs**: 1 million+ (6 chars = 56B possibilities)
- **Analytics events**: 10,000 events/second

### Scale Requirements
| Users/day | RPS   | Instances | DB Size/month |
|-----------|-------|-----------|---------------|
| 100K      | 100   | 2         | 10GB          |
| 1M        | 1,000 | 5         | 100GB         |
| 10M       | 10,000| 20        | 1TB           |

## 🎓 Best Practices

1. **Always validate input** - URL format, length limits
2. **Use connection pooling** - Database and Redis
3. **Implement circuit breakers** - For external services
4. **Monitor everything** - Metrics, logs, traces
5. **Test under load** - Use JMeter/Gatling
6. **Plan for failure** - Graceful degradation
7. **Keep it simple** - Don't over-engineer

## 📚 References

- [Spring Boot Best Practices](https://spring.io/guides)
- [Redis Performance](https://redis.io/docs/management/optimization/)
- [PostgreSQL Tuning](https://wiki.postgresql.org/wiki/Performance_Optimization)
- [Nginx Performance](https://www.nginx.com/blog/tuning-nginx/)