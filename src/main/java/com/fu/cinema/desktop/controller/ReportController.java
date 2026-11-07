package com.fu.cinema.desktop.controller;

import com.fu.cinema.desktop.facade.ReportFacade;
import com.fu.cinema.desktop.model.ReportBookingRow;
import com.fu.cinema.desktop.model.ReportData;
import com.fu.cinema.desktop.model.RevenueRow;
import com.fu.cinema.desktop.util.AlertUtil;
import com.fu.cinema.desktop.util.CsvExporter;
import com.fu.cinema.desktop.util.FxAsync;
import com.fu.cinema.desktop.util.MoneyUtil;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class ReportController {

    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    private final ReportFacade facade;
    private final FxAsync fxAsync;

    @FXML
    private DatePicker dpStart;

    @FXML
    private DatePicker dpEnd;

    @FXML
    private Button btnGenerate;

    @FXML
    private Button btnExport;

    @FXML
    private Label lblEmpty;

    @FXML
    private Label lblTotals;

    @FXML
    private BarChart<String, Number> chartRevenue;

    @FXML
    private PieChart pieTickets;

    @FXML
    private TableView<ReportBookingRow> tableBookings;

    @FXML
    private TableColumn<ReportBookingRow, Long> colId;

    @FXML
    private TableColumn<ReportBookingRow, String> colDate;

    @FXML
    private TableColumn<ReportBookingRow, String> colCustomer;

    @FXML
    private TableColumn<ReportBookingRow, String> colMovie;

    @FXML
    private TableColumn<ReportBookingRow, String> colTotal;

    private final ObjectProperty<ReportData> data = new SimpleObjectProperty<>();

    @FXML
    private void initialize() {
        dpStart.setValue(LocalDate.now().withDayOfMonth(1));
        dpEnd.setValue(LocalDate.now());

        // Chặn End < Start
        dpEnd.setDayCellFactory(p -> new DateCell() {
            @Override
            public void updateItem(LocalDate d, boolean empty) {
                super.updateItem(d, empty);
                setDisable(empty || (dpStart.getValue() != null && d.isBefore(dpStart.getValue())));
            }
        });

        dpStart.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && dpEnd.getValue() != null && dpEnd.getValue().isBefore(newVal)) {
                dpEnd.setValue(newVal);
            }
        });

        chartRevenue.setAnimated(false);
        pieTickets.setAnimated(false);

        btnExport.disableProperty().bind(Bindings.createBooleanBinding(
                () -> data.get() == null || data.get().isEmpty(),
                data
        ));

        // Cấu hình TableView
        colId.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().id()));
        colDate.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().bookingDate() != null ? d.getValue().bookingDate().format(DT_FORMAT) : ""
        ));
        colCustomer.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().customerName()));
        colMovie.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().movieTitle()));
        colTotal.setCellValueFactory(d -> new SimpleStringProperty(MoneyUtil.format(d.getValue().total())));

        // Tự động tải báo cáo cho kỳ hiện tại
        onGenerate();
    }

    @FXML
    private void onGenerate() {
        LocalDate start = dpStart.getValue();
        LocalDate end = dpEnd.getValue();
        if (start == null || end == null) {
            AlertUtil.warn("Vui lòng chọn đầy đủ ngày bắt đầu và kết thúc.");
            return;
        }
        if (end.isBefore(start)) {
            AlertUtil.warn("Ngày kết thúc không được trước ngày bắt đầu (BR-15).");
            return;
        }

        fxAsync.run(() -> facade.generate(start, end), this::render, btnGenerate);
    }

    private void render(ReportData d) {
        data.set(d);
        lblEmpty.setVisible(d.isEmpty());
        tableBookings.getItems().setAll(d.bookings());
        lblTotals.setText("Tổng: %d đơn · %d vé · %s".formatted(
                d.totalBookings(),
                d.totalTickets(),
                MoneyUtil.format(d.totalRevenue())
        ));

        // Vẽ biểu đồ cột Doanh thu
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doanh thu");
        d.revenue().forEach(r -> series.getData().add(new XYChart.Data<>(r.title(), r.revenue())));
        chartRevenue.getData().setAll(List.of(series));

        // Cài đặt Tooltip sau khi node của từng bar được gắn vào scene
        for (int i = 0; i < series.getData().size(); i++) {
            RevenueRow r = d.revenue().get(i);
            if (series.getData().get(i).getNode() != null) {
                Tooltip.install(series.getData().get(i).getNode(),
                        new Tooltip("%s: %d vé – %s".formatted(r.title(), r.tickets(), MoneyUtil.format(r.revenue()))));
            }
        }

        // Vẽ biểu đồ tròn Tỷ lệ vé
        pieTickets.getData().setAll(
                d.revenue().stream()
                        .map(r -> new PieChart.Data(r.title() + " (" + r.tickets() + ")", r.tickets()))
                        .toList()
        );
    }

    @FXML
    private void onExport() {
        ReportData snapshot = data.get();
        if (snapshot == null || snapshot.isEmpty()) {
            AlertUtil.warn("Không có dữ liệu để xuất file CSV.");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Xuất báo cáo doanh thu CSV");
        File downloadsDir = new File(System.getProperty("user.home"), "Downloads");
        if (downloadsDir.exists() && downloadsDir.isDirectory()) {
            fc.setInitialDirectory(downloadsDir);
        }
        fc.setInitialFileName("doanh-thu_%s_%s.csv".formatted(snapshot.start(), snapshot.end()));
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV (*.csv)", "*.csv"));

        File file = fc.showSaveDialog(btnExport.getScene().getWindow());
        if (file == null) {
            return; // Người dùng ấn Huỷ
        }

        fxAsync.runVoid(
                () -> CsvExporter.write(file.toPath(), snapshot),
                () -> AlertUtil.info("Đã xuất báo cáo thành công vào: " + file.getName()),
                btnExport
        );
    }
}
