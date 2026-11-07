package com.fu.cinema.desktop.util;

import com.fu.cinema.desktop.model.ReportBookingRow;
import com.fu.cinema.desktop.model.ReportData;
import com.fu.cinema.desktop.model.RevenueRow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvExporterTest {

    @Test
    @DisplayName("CsvExporter writes valid UTF-8 BOM CSV with proper Vietnamese characters and escaping")
    void testCsvExport(@TempDir Path tempDir) throws IOException {
        Path csvFile = tempDir.resolve("test_report.csv");

        ReportData data = new ReportData(
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 5),
                List.of(new RevenueRow("Phim Hành Động, 3D", 10, new BigDecimal("1000000"))),
                List.of(new ReportBookingRow(1L, LocalDateTime.of(2026, 11, 2, 10, 0), "Nguyễn Văn B", "Phim Hành Động, 3D", new BigDecimal("1000000"))),
                1,
                10,
                new BigDecimal("1000000")
        );

        CsvExporter.write(csvFile, data);

        assertTrue(Files.exists(csvFile));
        String content = Files.readString(csvFile, StandardCharsets.UTF_8);

        // Kiểm tra UTF-8 BOM
        assertTrue(content.startsWith("\uFEFF"));
        // Kiểm tra nội dung tiếng Việt và escaping dấu phẩy
        assertTrue(content.contains("\"Phim Hành Động, 3D\""));
        assertTrue(content.contains("Nguyễn Văn B"));
        assertTrue(content.contains("TỔNG,1 đơn,10 vé,,1000000"));
    }
}
