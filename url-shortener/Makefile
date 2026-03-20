.PHONY: help build run stop clean logs test

help:
	@echo "URL Shortener - Available Commands:"
	@echo "  make build      - Build Docker images"
	@echo "  make run        - Start all services"
	@echo "  make stop       - Stop all services"
	@echo "  make restart    - Restart all services"
	@echo "  make logs       - View application logs"
	@echo "  make clean      - Remove all containers and volumes"
	@echo "  make test       - Run tests"
	@echo "  make db-shell   - Connect to PostgreSQL"
	@echo "  make redis-cli  - Connect to Redis"
	@echo "  make scale      - Scale application instances"

build:
	@echo "Building Docker images..."
	docker-compose build

run:
	@echo "Starting all services..."
	docker-compose up -d
	@echo "Services started! Access at:"
	@echo "  - Application: http://localhost:8080"
	@echo "  - Grafana: http://localhost:3000"
	@echo "  - Prometheus: http://localhost:9090"

stop:
	@echo "Stopping all services..."
	docker-compose stop

restart:
	@echo "Restarting services..."
	docker-compose restart

logs:
	docker-compose logs -f url-shortener

clean:
	@echo "Removing all containers and volumes..."
	docker-compose down -v
	@echo "Cleaning up..."
	rm -rf target/

test:
	@echo "Running tests..."
	mvn test

db-shell:
	docker exec -it url-shortener-db psql -U postgres -d urlshortener

redis-cli:
	docker exec -it url-shortener-redis redis-cli

scale:
	@echo "Scaling to 3 instances..."
	docker-compose up -d --scale url-shortener=3

backup-db:
	@echo "Backing up database..."
	docker exec url-shortener-db pg_dump -U postgres urlshortener > backup_$(shell date +%Y%m%d_%H%M%S).sql
	@echo "Backup completed!"

restore-db:
	@echo "Usage: make restore-db FILE=backup_20260109_120000.sql"
	docker exec -i url-shortener-db psql -U postgres urlshortener < $(FILE)

setup-geoip:
	@echo "Setting up GeoIP database..."
	@echo "Please download GeoLite2-City.mmdb from:"
	@echo "https://dev.maxmind.com/geoip/geoip2/geolite2/"
	@echo "And place it in: data/GeoLite2-City.mmdb"

health-check:
	@echo "Checking service health..."
	@curl -s http://localhost:8080/health | jq .
	@echo ""
	@docker-compose ps

stats:
	@echo "Container Statistics:"
	@docker stats --no-stream url-shortener-app url-shortener-db url-shortener-redis