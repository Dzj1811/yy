package com.yourname.clinic.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public final class CsvUtil {
    private CsvUtil() {}

    public static void writeCsv(Path path, List<String> headers, List<List<String>> rows) throws IOException {
        Files.createDirectories(path.getParent());
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write("\uFEFF");
            writer.write(String.join(",", headers));
            writer.newLine();
            for (List<String> row : rows) {
                writer.write(row.stream().map(CsvUtil::escape).collect(Collectors.joining(",")));
                writer.newLine();
            }
        }
    }

    private static String escape(String val) {
        if (val == null) return "";
        String escaped = val.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return '"' + escaped + '"';
        }
        return escaped;
    }
}
