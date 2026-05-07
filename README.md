# 🚀 URL Shortener - Enterprise Grade

Hệ thống rút gọn link chuyên nghiệp với khả năng chống DDoS, analytics đầy đủ và hiệu năng cao.

## ✨ Tính năng chính

- ✅ **Mã link 6 ký tự** (a-zA-Z0-9) - 56+ tỷ URLs khả dụng
- ✅ **Bảo mật cao** - Multi-layer rate limiting, DDoS protection
- ✅ **Hiệu năng cao** - 10,000+ RPS, < 10ms latency (cached)
- ✅ **Analytics đầy đủ** - IP, location, browser, device, OS,...
- ✅ **Scalable** - Horizontal scaling dễ dàng
- ✅ **Real-time stats** - Redis + Kafka streaming

## 📋 Yêu cầu hệ thống

- Java 17+
- Docker & Docker Compose
- Maven 3.6+
- 4GB RAM (minimum)
- 20GB disk space

## 🛠️ Cấu trúc Project

```
url-shortener/
├── src/
│   ├── main/
│   │   ├── java/com/urlshortener/
│   │   │   ├── controller/
│   │   │   │   └── URLController.java
│   │   │   ├── service/
│   │   │   │   ├── URLShortenerService.java
│   │   │   │   ├── AnalyticsService.java
│   │   │   │   └── GeoIPService.java
│   │   │   ├── entity/
│   │   │   │   ├── URLEntity.java
│   │   │   │   └── ClickAnalytics.java
│   │   │   ├── repository/
│   │   │   │   ├── URLRepository.java
│   │   │   │   └── ClickAnalyticsRepository.java
│   │   │   ├── dto/
│   │   │   │   ├── AnalyticsData.java
│   │   │   │   └── CreateURLRequest.java
│   │   │   ├── security/
│   │   │   │   └── RateLimiter.java
│   │   │   └── Application.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── schema.sql
├── data/
│   └── GeoLite2-City.mmdb (download riêng)
├── docker-compose.yml
├── nginx.conf
├── pom.xml
└── README.md
```

## 🚀 Quick Start

### 1. Clone và Setup

```bash
git clone <repository-url>
cd url-shortener
```

### 2. Download GeoIP Database

```bash
# Tạo folder data
mkdir -p data

# Download GeoLite2-City database (free)
# Đăng ký tại: https://dev.maxmind.com/geoip/geoip2/geolite2/
# Hoặc dùng wget (cần license key)
wget "https://download.maxmind.com/app/geoip_download?...&suffix=tar.gz" -O GeoLite2-City.tar.gz
tar -xzf GeoLite2-City.tar.gz
mv GeoLite2-City_*/GeoLite2-City.mmdb data/
```

### 3. Configure Environment

```bash
# Tạo file .env
cat > .env << EOF
DB_PASSWORD=secure_postgres_password
REDIS_PASSWORD=secure_redis_password
EOF
```

### 4. Start với Docker Compose

```bash
# Build và start tất cả services
docker-compose up -d

# Xem logs
docker-compose logs -f url-shortener

# Stop
docker-compose down
```

### 5. Initialize Database

```bash
# Connect to PostgreSQL
docker exec -it url-shortener-db psql -U postgres -d urlshortener

# Import schema (nếu chưa auto-import)
\i /docker-entrypoint-initdb.d/schema.sql

# Verify tables
\dt
```

## 📝 API Documentation

### 1. Create Short URL

```bash
POST /api/shorten
Content-Type: application/json

{
  "url": "https://example.com/very-long-url",
  "userId": 1
}

# Response
{
  "shortCode": "Xs2dF3",
  "shortURL": "https://yourdomain.com/Xs2dF3",
  "originalURL": "https://example.com/very-long-url"
}
```

### 2. Redirect (Short URL)

```bash
GET /{shortCode}

# Example
GET /Xs2dF3
# → 302 redirect to original URL
```

### 3. Get Statistics

```bash
GET /api/stats/{shortCode}

# Example
GET /api/stats/Xs2dF3

# Response
{
  "totalClicks": 1543,
  "uniqueVisitors": 892,
  "byCountry": [
    {"name": "VN", "count": 450},
    {"name": "US", "count": 320}
  ],
  "byBrowser": [
    {"name": "Chrome", "count": 980},
    {"name": "Firefox", "count": 340}
  ],
  "byDevice": [
    {"name": "Mobile", "count": 820},
    {"name": "Desktop", "count": 723}
  ],
  "hourlyTrend": {...},
  "recentClicks": [...]
}
```

## 🔧 Configuration

### Application Properties

Chỉnh sửa `src/main/resources/application.yml`:

```yaml
app:
  domain: "https://yourdomain.com"
  rate-limit:
    create-url-per-hour: 100    # API rate limit
    redirect-per-minute: 1000   # Redirect rate limit
  cache:
    ttl-hours: 24                # Redis TTL
```

### Nginx Configuration

Chỉnh sửa `nginx.conf` để tùy chỉnh:
- Rate limits
- SSL certificates
- Upstream servers (load balancing)
- Cache settings

### Database Tuning

```sql
-- Tăng performance cho production
ALTER SYSTEM SET shared_buffers = '512MB';
ALTER SYSTEM SET effective_cache_size = '2GB';
ALTER SYSTEM SET maintenance_work_mem = '128MB';
ALTER SYSTEM SET max_connections = 300;
```

## 📊 Monitoring

### Prometheus Metrics

```bash
# Access Prometheus
http://localhost:9090

# Available metrics:
- http_requests_total
- http_request_duration_seconds
- redis_cache_hits_total
- kafka_messages_processed_total
```

### Grafana Dashboard

```bash
# Access Grafana
http://localhost:3000
# Login: admin / admin

# Import dashboard cho:
- Request rate & latency
- Cache hit ratio
- Database performance
- JVM metrics
```

### Health Check

```bash
curl http://localhost:8080/health
```

## 🧪 Testing

### Unit Tests

```bash
mvn test
```

### Load Testing với Apache Bench

```bash
# Test create URL endpoint
ab -n 1000 -c 10 -p request.json -T application/json \
   http://localhost/api/shorten

# Test redirect endpoint
ab -n 10000 -c 100 http://localhost/Xs2dF3
```

### Load Testing với JMeter

```bash
# Download JMeter test plan
wget https://example.com/url-shortener-test.jmx

# Run test
jmeter -n -t url-shortener-test.jmx -l results.jtl
```

## 🔒 Security Best Practices

1. **HTTPS Only**
    - Deploy với SSL certificate (Let's Encrypt)
    - Redirect tất cả HTTP → HTTPS

2. **Rate Limiting**
    - API: 100 requests/hour per IP
    - Redirect: 1000 requests/minute per IP

3. **Input Validation**
    - URL format validation
    - Max URL length: 2048 chars
    - Short code format: 6 alphanumeric chars

4. **DDoS Protection**
    - CloudFlare (recommended)
    - Nginx rate limiting
    - Connection limits

5. **Database Security**
    - Strong passwords
    - Encrypted connections
    - Regular backups

## 📈 Scaling Guide

### Horizontal Scaling

```bash
# Scale app instances
docker-compose up -d --scale url-shortener=5

# Configure Nginx load balancing
upstream app_backend {
    server url-shortener-1:8080;
    server url-shortener-2:8080;
    server url-shortener-3:8080;
    server url-shortener-4:8080;
    server url-shortener-5:8080;
}
```

### Database Scaling

```yaml
# Read replicas cho analytics queries
postgres-replica:
  image: postgres:16-alpine
  environment:
    POSTGRES_MASTER_SERVICE_HOST: postgres
```

### Redis Cluster

```yaml
# Redis cluster cho high availability
redis-cluster:
  image: redis:7-alpine
  command: redis-cli --cluster create ...
```

## 🐛 Troubleshooting

### Lỗi kết nối Database

```bash
# Check PostgreSQL logs
docker-compose logs postgres

# Test connection
docker exec -it url-shortener-db psql -U postgres -d urlshortener
```

### Redis connection issues

```bash
# Check Redis
docker-compose logs redis

# Test Redis
docker exec -it url-shortener-redis redis-cli ping
```

### Kafka issues

```bash
# Check Kafka logs
docker-compose logs kafka

# List topics
docker exec -it url-shortener-kafka kafka-topics --list \
  --bootstrap-server localhost:9092
```

### High latency

1. Check Redis cache hit rate
2. Monitor database slow queries
3. Review Nginx cache configuration
4. Check network bandwidth

## 🔄 Backup & Restore

### Database Backup

```bash
# Backup
docker exec url-shortener-db pg_dump -U postgres urlshortener > backup.sql

# Restore
docker exec -i url-shortener-db psql -U postgres urlshortener < backup.sql
```

### Redis Backup

```bash
# Backup (RDB snapshot)
docker exec url-shortener-redis redis-cli SAVE

# Copy snapshot
docker cp url-shortener-redis:/data/dump.rdb ./redis-backup.rdb
```

## 📚 References

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Redis Best Practices](https://redis.io/docs/manual/patterns/)
- [PostgreSQL Performance Tuning](https://wiki.postgresql.org/wiki/Performance_Optimization)
- [Nginx Optimization](https://www.nginx.com/blog/tuning-nginx/)
- [Kafka Documentation](https://kafka.apache.org/documentation/)

## 📄 License

MIT License - xem file LICENSE để biết thêm chi tiết
 
