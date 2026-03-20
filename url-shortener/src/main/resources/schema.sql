-- Table: URLs
CREATE TABLE urls (
                      id BIGSERIAL PRIMARY KEY,
                      short_code VARCHAR(6) UNIQUE NOT NULL,
                      original_url TEXT NOT NULL,
                      description TEXT,
                      user_id BIGINT,

    -- Timestamps
                      created_at BIGINT NOT NULL,
                      expires_at BIGINT,
                      updated_at BIGINT,

    -- Statistics
                      click_count BIGINT NOT NULL DEFAULT 0,
                      unique_clicks BIGINT NOT NULL DEFAULT 0,
                      last_clicked_at BIGINT,

    -- Status & Settings
                      is_active BOOLEAN NOT NULL DEFAULT TRUE,
                      is_public BOOLEAN NOT NULL DEFAULT TRUE,

    -- Security
                      password TEXT,
                      max_clicks INTEGER,

    -- Custom
                      title VARCHAR(200),
                      tags VARCHAR(500),
                      metadata TEXT,

    -- Constraints
                      CONSTRAINT short_code_length CHECK (char_length(short_code) = 6)
);

-- Indexes
CREATE INDEX idx_urls_short_code ON urls(short_code);
CREATE INDEX idx_urls_user_id ON urls(user_id);
CREATE INDEX idx_urls_created_at ON urls(created_at);
CREATE INDEX idx_urls_original_url ON urls(original_url);



-- Table: Click Analytics
CREATE TABLE click_analytics (
                                 id BIGSERIAL PRIMARY KEY,
                                 short_code VARCHAR(6) NOT NULL,
                                 timestamp BIGINT NOT NULL,

    -- IP & Location
                                 ip_address VARCHAR(45),
                                 country VARCHAR(2),
                                 city VARCHAR(100),
                                 region VARCHAR(100),
                                 latitude DECIMAL(10, 8),
                                 longitude DECIMAL(11, 8),
                                 timezone VARCHAR(50),

    -- Device Info
                                 browser VARCHAR(50),
                                 operating_system VARCHAR(50),
                                 device_type VARCHAR(20),

    -- Technical Info
                                 user_agent TEXT,
                                 referer TEXT,
                                 language VARCHAR(100),

    -- Additional tracking
                                 session_id VARCHAR(100),

                                 FOREIGN KEY (short_code) REFERENCES urls(short_code) ON DELETE CASCADE
);

-- Indexes for analytics queries
CREATE INDEX idx_analytics_short_code ON click_analytics(short_code);
CREATE INDEX idx_analytics_timestamp ON click_analytics(timestamp);
CREATE INDEX idx_analytics_country ON click_analytics(country);
CREATE INDEX idx_analytics_device_type ON click_analytics(device_type);
CREATE INDEX idx_analytics_browser ON click_analytics(browser);

-- Composite index cho time-series queries
CREATE INDEX idx_analytics_code_time ON click_analytics(short_code, timestamp DESC);

-- Partitioning by month (optional - cho scale lớn)
-- CREATE TABLE click_analytics_2026_01 PARTITION OF click_analytics
--     FOR VALUES FROM (1704067200000) TO (1706745600000);

-- Table: Users (optional)
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       api_key VARCHAR(64) UNIQUE,
                       created_at BIGINT NOT NULL,
                       plan_type VARCHAR(20) DEFAULT 'free',
                       is_active BOOLEAN DEFAULT TRUE
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_api_key ON users(api_key);

-- Function: Update click count
CREATE OR REPLACE FUNCTION update_click_count()
RETURNS TRIGGER AS $$
BEGIN
UPDATE urls
SET click_count = click_count + 1
WHERE short_code = NEW.short_code;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger: Auto update click count
CREATE TRIGGER trigger_update_click_count
    AFTER INSERT ON click_analytics
    FOR EACH ROW
    EXECUTE FUNCTION update_click_count();

-- Materialized View: Daily stats (tối ưu query)
CREATE MATERIALIZED VIEW daily_stats AS
SELECT
    short_code,
    (to_timestamp(timestamp / 1000))::date as date,
    COUNT(*) as clicks,
    COUNT(DISTINCT ip_address) as unique_visitors,
    COUNT(DISTINCT country) as countries,
    mode() WITHIN GROUP (ORDER BY country) as top_country,
    mode() WITHIN GROUP (ORDER BY browser) as top_browser,
    mode() WITHIN GROUP (ORDER BY device_type) as top_device
FROM click_analytics
GROUP BY short_code, (to_timestamp(timestamp / 1000))::date;

CREATE UNIQUE INDEX idx_daily_stats ON daily_stats(short_code, date);

-- Refresh materialized view (chạy mỗi ngày)
-- REFRESH MATERIALIZED VIEW CONCURRENTLY daily_stats;