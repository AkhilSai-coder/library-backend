-- =============================================================
--  GRIET Central Library – PostgreSQL Index Migration
--  Run this ONCE on your Railway PostgreSQL database.
--  These indexes are CRITICAL for 130k book performance.
-- =============================================================

-- ── Enable pg_trgm extension for full-text search ────────────
-- Required for trigram-based LIKE queries on title/authors.
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- ── BOOKS table indexes ───────────────────────────────────────

-- GIN trigram index on title: makes LIKE '%java%' sub-100ms on 130k rows
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_books_title_trgm
    ON books USING gin (title gin_trgm_ops);

-- GIN trigram index on authors field
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_books_authors_trgm
    ON books USING gin (authors gin_trgm_ops);

-- Branch filter (equality)
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_books_branch
    ON books (branch);

-- Category filter (equality)
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_books_category
    ON books (category);

-- Composite: branch + category (most common combined filter)
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_books_branch_category
    ON books (branch, category);

-- Partial index: only available books (heavily filtered in catalogue)
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_books_available_true
    ON books (id) WHERE available = true;

-- Accession number lookups (unique, already has constraint index)
CREATE UNIQUE INDEX CONCURRENTLY IF NOT EXISTS idx_books_accession
    ON books (accession_number);

-- ISBN lookups
CREATE UNIQUE INDEX CONCURRENTLY IF NOT EXISTS idx_books_isbn
    ON books (isbn) WHERE isbn IS NOT NULL;

-- ── BOOK_REQUESTS table indexes ────────────────────────────────

-- Status filter: librarian pending queue
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_request_status
    ON book_requests (status);

-- User's own requests
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_request_user_id
    ON book_requests (user_id);

-- Composite: user + status (student "my pending requests" query)
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_request_user_status
    ON book_requests (user_id, status);

-- Timestamp ordering for librarian queue
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_request_requested_at
    ON book_requests (requested_at DESC);

-- ── BORROWS table indexes ──────────────────────────────────────

-- Active borrows filter (most common query)
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_borrow_returned
    ON borrows (returned);

-- Overdue detection: due_date + returned
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_borrow_due_returned
    ON borrows (due_date, returned) WHERE returned = false;

-- User's borrow history
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_borrow_user_id
    ON borrows (user_id);

-- Book availability check
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_borrow_book_returned
    ON borrows (book_id, returned);

-- ── USERS table indexes ────────────────────────────────────────

-- Role-based counts (dashboard)
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_users_role
    ON users (role);

-- College ID lookups (unique, likely already indexed)
CREATE UNIQUE INDEX CONCURRENTLY IF NOT EXISTS idx_users_college_id
    ON users (college_id);

-- =============================================================
--  ANALYZE tables after index creation (updates query planner)
-- =============================================================
ANALYZE books;
ANALYZE book_requests;
ANALYZE borrows;
ANALYZE users;

-- =============================================================
--  VERIFY indexes created
-- =============================================================
SELECT
    schemaname,
    tablename,
    indexname,
    indexdef
FROM pg_indexes
WHERE tablename IN ('books', 'book_requests', 'borrows', 'users')
ORDER BY tablename, indexname;
