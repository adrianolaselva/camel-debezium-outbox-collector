package com.collector.outbox.debezium.connect;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.connect.runtime.WorkerConfig;
import org.apache.kafka.connect.storage.MemoryOffsetBackingStore;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static java.lang.String.format;

@Slf4j
public class MySqlOffsetStore extends MemoryOffsetBackingStore {

    private JdbcTemplate jdbcTemplate;
    private String offsetStoreTableName;

    private final String DEFAULT_OFFSET_STORE_TABLE_NAME = "offset_store";
    private final String QUERY_LOAD_OFFSET_STORE = "select buffer_key, buffer_value from %s";
    private final String PERSIST_OFFSET_STORE = "replace into %s (uuid, buffer_key, buffer_value, created_at) values (?, ?, ?, ?)";

    @Override
    public void configure(WorkerConfig config) {
        var driverManagerDataSource = new DriverManagerDataSource();
        driverManagerDataSource.setDriverClassName(config.originals().get("database.history.driver.class").toString());
        driverManagerDataSource.setUrl(config.originals().get("database.history.url").toString());
        driverManagerDataSource.setUsername(config.originals().get("database.history.user").toString());
        driverManagerDataSource.setPassword(config.originals().get("database.history.password").toString());
        this.offsetStoreTableName = config.originals().getOrDefault("database.offset.table", DEFAULT_OFFSET_STORE_TABLE_NAME).toString();
        this.jdbcTemplate = new JdbcTemplate(driverManagerDataSource);
        super.configure(config);
    }

    public synchronized void start() {
        super.start();
        log.info("Starting JDBCOffsetBackingStore with file");
        this.load();
    }

    public synchronized void stop() {
        super.stop();
        log.info("Stopped JDBCOffsetBackingStore");
    }


    private void load() {
        this.data = new HashMap<>();
        this.jdbcTemplate.query(format(QUERY_LOAD_OFFSET_STORE, this.offsetStoreTableName), rs -> {
            var key = rs.getString(1).getBytes(StandardCharsets.UTF_8);
            var value = rs.getString(2).getBytes(StandardCharsets.UTF_8);
            this.data.put(ByteBuffer.wrap(key), ByteBuffer.wrap(value));
        });
    }

    protected void save() {
        try {
            Map<byte[], byte[]> raw = new HashMap<>();
            for (final Map.Entry<ByteBuffer, ByteBuffer> byteBufferByteBufferEntry : this.data.entrySet()) {
                Map.Entry<ByteBuffer, ByteBuffer> mapEntry = (Map.Entry) byteBufferByteBufferEntry;
                byte[] key = mapEntry.getKey() != null ? ((ByteBuffer) mapEntry.getKey()).array() : null;
                byte[] value = mapEntry.getValue() != null ? ((ByteBuffer) mapEntry.getValue()).array() : null;
                raw.put(key, value);
            }

            var rows = raw.entrySet().stream().toList();
            this.jdbcTemplate.batchUpdate(format(PERSIST_OFFSET_STORE, this.offsetStoreTableName), new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(final PreparedStatement ps, final int i) throws SQLException {
                        ps.setString(1, UUID.nameUUIDFromBytes(rows.get(i).getKey()).toString());
                        ps.setString(2, new String(rows.get(i).getKey(), StandardCharsets.UTF_8));
                        ps.setString(3, new String(rows.get(i).getValue(), StandardCharsets.UTF_8));
                        ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                    }

                    @Override
                    public int getBatchSize() {
                        return rows.size();
                    }
                });
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

}
