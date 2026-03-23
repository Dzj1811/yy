package com.yourname.clinic.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DbBackupUtil {
    private DbBackupUtil() {}

    public static Path backupNow(Path dbFile, Path backupDir) throws IOException {
        if (!Files.exists(dbFile)) {
            throw new FileNotFoundException("数据库文件不存在: " + dbFile);
        }
        Files.createDirectories(backupDir);
        String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        Path target = backupDir.resolve("clinic_" + ts + ".db");
        Files.copy(dbFile, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
        return target;
    }

    public static void restore(Path backupFile, Path dbFile) throws IOException {
        if (!Files.exists(backupFile)) {
            throw new FileNotFoundException("备份文件不存在: " + backupFile);
        }
        Files.createDirectories(dbFile.getParent());
        Files.copy(backupFile, dbFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
    }
}
