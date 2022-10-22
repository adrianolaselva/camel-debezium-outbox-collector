package com.collector.outbox.camel.debezium.history;

import io.debezium.config.Configuration;
import io.debezium.document.DocumentReader;
import io.debezium.relational.history.AbstractDatabaseHistory;
import io.debezium.relational.history.DatabaseHistoryException;
import io.debezium.relational.history.DatabaseHistoryListener;
import io.debezium.relational.history.HistoryRecord;
import io.debezium.relational.history.HistoryRecordComparator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Consumer;

import static java.lang.String.format;

@Slf4j
public class MySqlDatabaseHistory extends AbstractDatabaseHistory {

    private JdbcTemplate jdbcTemplate;
    private final DocumentReader reader = DocumentReader.defaultReader();
    private String historyTableName;

    private final String DEFAULT_HISTORY_TABLE_NAME = "database_history";
    private final String QUERY_RECOVER_RECORDS = "select content from %s";
    private final String QUERY_COUNT_RECORDS = "select count(1) from %s";
    private final String QUERY_VERIFY_SCHEMA_EXISTS = "select count(1) from information_schema.tables where table_name = ?";
    private final String PERSIST_DATABASE_HISTORY = "insert into %s (uuid, content, created_at) values (?,?,?)";

    @Override
    public void configure(final Configuration config, final HistoryRecordComparator comparator, final DatabaseHistoryListener listener,
        final boolean useCatalogBeforeSchema) {
        var driverManagerDataSource = new DriverManagerDataSource();
        driverManagerDataSource.setDriverClassName(config.getString("database.history.driver.class"));
        driverManagerDataSource.setUrl(config.getString("database.history.url"));
        driverManagerDataSource.setUsername(config.getString("database.history.user"));
        driverManagerDataSource.setPassword(config.getString("database.history.password"));
        this.jdbcTemplate = new JdbcTemplate(driverManagerDataSource);
        this.historyTableName = config.getString("database.history.table", DEFAULT_HISTORY_TABLE_NAME);
        super.configure(config, comparator, listener, useCatalogBeforeSchema);
    }

    @Override
    public synchronized void start() {
        super.start();
    }

    @Override
    public synchronized void stop() {
        super.stop();
    }

    @Override
    protected void storeRecord(final HistoryRecord record) throws DatabaseHistoryException {
        this.jdbcTemplate.update(format(PERSIST_DATABASE_HISTORY, this.historyTableName), ps -> {
            ps.setString(1, UUID.randomUUID().toString());
            ps.setString(2, record.toString());
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
        });
    }

    @Override
    protected synchronized void recoverRecords(final Consumer<HistoryRecord> records) {
        this.jdbcTemplate.query(format(QUERY_RECOVER_RECORDS, this.historyTableName), rs -> {
            var content = rs.getString(1);

            try {
                var recordObj = new HistoryRecord(reader.read(content));
                records.accept(recordObj);
                log.trace("Recovered database history: {}", recordObj);
            } catch (IOException e) {
                log.error("Error while deserializing history record '{}'", content, e);
            }
        });
    }

    @Override
    public boolean exists() {
        var rows = this.jdbcTemplate.queryForObject(format(QUERY_COUNT_RECORDS, this.historyTableName), Long.class);

        return rows != null && rows > 0;
    }

    @Override
    public boolean storageExists() {
        var rows = this.jdbcTemplate.queryForObject(QUERY_VERIFY_SCHEMA_EXISTS, new Object[] {
            this.historyTableName
        }, Long.class);

        return rows != null && rows > 0;
    }

}
