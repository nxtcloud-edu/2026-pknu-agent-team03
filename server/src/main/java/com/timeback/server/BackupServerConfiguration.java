package com.timeback.server;

import com.timeback.backup.server.BackupServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class BackupServerConfiguration {
    @Bean
    public JdbcBackupStorage jdbcBackupStorage(
            JdbcTemplate jdbcTemplate,
            PlatformTransactionManager transactionManager
    ) {
        return new JdbcBackupStorage(jdbcTemplate, new TransactionTemplate(transactionManager));
    }

    @Bean
    public BackupServer backupServer(JdbcBackupStorage storage) {
        return new BackupServer(storage);
    }
}
