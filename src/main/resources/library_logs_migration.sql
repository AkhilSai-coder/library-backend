-- ============================================================
--  GRIET Central Library — library_logs migration
--  Run once against your PostgreSQL database.
--  If you use spring.jpa.hibernate.ddl-auto=update this table
--  will be created automatically by Hibernate; the manual
--  indexes below are still recommended for query performance.
-- ============================================================

-- 1. Create the table (idempotent)
CREATE TABLE IF NOT EXISTS library_logs (
    id          BIGSERIAL    PRIMARY KEY,
    college_id  VARCHAR(50)  NOT NULL,
    entry_time  TIMESTAMP    NOT NULL,
    exit_time   TIMESTAMP                     -- NULL while student is inside
);

-- 2. Performance indexes
--    a) Speed up "find active entry for student X" — most-frequent query
CREATE INDEX IF NOT EXISTS idx_library_logs_active_entry
    ON library_logs (college_id, exit_time);

--    b) Speed up "today's logs" date-range scan
CREATE INDEX IF NOT EXISTS idx_library_logs_entry_time
    ON library_logs (entry_time DESC);

--    c) Speed up "student history" lookups
CREATE INDEX IF NOT EXISTS idx_library_logs_college_id
    ON library_logs (college_id);

-- 3. Partial index — ultra-fast "is student currently inside?" check
--    Only indexes rows where exit_time IS NULL (currently inside)
CREATE INDEX IF NOT EXISTS idx_library_logs_currently_inside
    ON library_logs (college_id)
    WHERE exit_time IS NULL;
