package com.fu.cinema.desktop.util;

import com.fu.cinema.desktop.model.ReportBookingRow;
import com.fu.cinema.desktop.model.ReportData;
import com.fu.cinema.desktop.model.RevenueRow;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CsvExporter {

    private CsvExporter() {
    }

    public static void write(Path path, ReportData d) {
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            try (BufferedWriter w = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                w.write('\uFEFF'); // BOM → Excel đọc đúng tiếng Việt
                w.write("Báo cáo doanh thu," + d.start() + "," + d.end());
                w.newLine();
                w.write("Phim,Số vé,Doanh thu");
                w.newLine();
                for (RevenueRow r : d.revenue()) {
                    String revStr = r.revenue() != null ? r.revenue().toPlainString() : "0";
                    w.write(String.join(",", csv(r.title()), String.valueOf(r.tickets()), revStr));
                    w.newLine();
                }
                w.newLine();
                w.write("Mã đơn,Ngày đặt,Khách hàng,Phim,Tổng tiền");
                w.newLine();
                for (ReportBookingRow b : d.bookings()) {
                    String dateStr = b.bookingDate() != null ? b.bookingDate().toString() : "";
                    String totalStr = b.total() != null ? b.total().toPlainString() : "0";
                    w.write(String.join(",", String.valueOf(b.id()), dateStr, csv(b.customerName()),
                            csv(b.movieTitle()), totalStr));
                    w.newLine();
                }
                String totRevStr = d.totalRevenue() != null ? d.totalRevenue().toPlainString() : "0";
                w.write("TỔNG,%d đơn,%d vé,,%s".formatted(d.totalBookings(), d.totalTickets(), totRevStr));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String csv(String v) {
        if (v == null) {
            return "";
        }
        return v.contains(",") || v.contains("\"") || v.contains("\n")
                ? "\"" + v.replace("\"", "\"\"") + "\""
                : v;
    }
}
