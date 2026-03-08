package io.memora.storage.sqlite;

import io.memora.core.MemoryEntry;
import io.memora.core.MemoryEntryStore;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SQLiteMemoryEntryStore implements MemoryEntryStore {
    private static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS memory_entries ("
                    + "id TEXT PRIMARY KEY,"
                    + "type TEXT NOT NULL,"
                    + "scope_id TEXT,"
                    + "session_id TEXT,"
                    + "source TEXT,"
                    + "content TEXT NOT NULL,"
                    + "payload_json TEXT,"
                    + "created_at INTEGER NOT NULL"
                    + ")";

    private final String jdbcUrl;

    public SQLiteMemoryEntryStore(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public void initializeSchema() {
        try (Connection connection = openConnection(); Statement statement = connection.createStatement()) {
            statement.execute(CREATE_TABLE_SQL);
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to initialize SQLite schema", exception);
        }
    }

    public void saveAll(List<MemoryEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return;
        }

        final String sql =
                "INSERT INTO memory_entries (id, type, scope_id, session_id, source, content, payload_json, created_at) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            for (MemoryEntry entry : entries) {
                statement.setString(1, entry.getId());
                statement.setString(2, entry.getType());
                statement.setString(3, entry.getScopeId());
                statement.setString(4, entry.getSessionId());
                statement.setString(5, entry.getSource());
                statement.setString(6, entry.getContent());
                statement.setString(7, entry.getPayloadJson());
                statement.setLong(8, entry.getCreatedAt());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to persist memory entries", exception);
        }
    }

    public List<MemoryEntry> search(String query, String scopeId, String sessionId, int limit) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        final String sql =
                "SELECT id, type, scope_id, session_id, source, content, payload_json, created_at "
                        + "FROM memory_entries "
                        + "WHERE (? IS NULL OR scope_id = ?) "
                        + "AND content LIKE ? "
                        + "ORDER BY CASE WHEN ? IS NOT NULL AND session_id = ? THEN 1 ELSE 0 END DESC, created_at DESC "
                        + "LIMIT ?";

        List<MemoryEntry> result = new ArrayList<MemoryEntry>();
        try (Connection connection = openConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, scopeId);
            statement.setString(2, scopeId);
            statement.setString(3, "%" + query + "%");
            statement.setString(4, sessionId);
            statement.setString(5, sessionId);
            statement.setInt(6, limit);

            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    result.add(new MemoryEntry(
                            rows.getString("id"),
                            rows.getString("type"),
                            rows.getString("scope_id"),
                            rows.getString("session_id"),
                            rows.getString("source"),
                            rows.getString("content"),
                            rows.getString("payload_json"),
                            rows.getLong("created_at")));
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to search memory entries", exception);
        }
        return result;
    }

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl);
    }
}
