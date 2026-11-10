package com.fu.cinema.desktop.controller;

import com.fu.cinema.desktop.facade.BookingFacade;
import com.fu.cinema.desktop.model.BookingDetail;
import com.fu.cinema.desktop.model.BookingRow;
import com.fu.cinema.desktop.util.AlertUtil;
import com.fu.cinema.desktop.util.FxAsync;
import com.fu.cinema.desktop.util.MoneyUtil;
import com.fu.cinema.enums.BookingStatus;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Scope("prototype")
public class BookingController {

    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    private final BookingFacade facade;
    private final FxAsync fxAsync;

    public BookingController(BookingFacade facade, FxAsync fxAsync) {
        this.facade = facade;
        this.fxAsync = fxAsync;
    }

    @FXML
    private ComboBox<StatusOption> cbStatus;

    @FXML
    private DatePicker dpFrom;

    @FXML
    private DatePicker dpTo;

    @FXML
    private TextField tfKeyword;

    @FXML
    private TableView<BookingRow> table;

    @FXML
    private TableColumn<BookingRow, Long> colId;

    @FXML
    private TableColumn<BookingRow, String> colBookingDate;

    @FXML
    private TableColumn<BookingRow, String> colCustomer;

    @FXML
    private TableColumn<BookingRow, String> colMovie;

    @FXML
    private TableColumn<BookingRow, String> colRoom;

    @FXML
    private TableColumn<BookingRow, String> colStartTime;

    @FXML
    private TableColumn<BookingRow, Integer> colSeats;

    @FXML
    private TableColumn<BookingRow, String> colTotal;

    @FXML
    private TableColumn<BookingRow, BookingStatus> colStatus;

    @FXML
    private Pagination pagination;

    @FXML
    private Button btnConfirm;

    @FXML
    private Button btnCancel;

    public record StatusOption(BookingStatus status, String label) {
        @Override
        public String toString() {
            return label;
        }
    }

    @FXML
    private void initialize() {
        // Cấu hình bộ lọc
        cbStatus.getItems().setAll(
                new StatusOption(null, "Tất cả trạng thái"),
                new StatusOption(BookingStatus.PENDING, "Chờ xác nhận (PENDING)"),
                new StatusOption(BookingStatus.CONFIRMED, "Đã xác nhận (CONFIRMED)"),
                new StatusOption(BookingStatus.CANCELLED, "Đã huỷ (CANCELLED)"));
        cbStatus.getSelectionModel().selectFirst();

        // Cấu hình các cột TableView
        colId.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().id()));
        colBookingDate.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().bookingDate() != null ? d.getValue().bookingDate().format(DT_FORMAT) : ""));
        colCustomer.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().customerName()));
        colMovie.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().movieTitle()));
        colRoom.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().roomName()));
        colStartTime.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().startTime() != null ? d.getValue().startTime().format(DT_FORMAT) : ""));
        colSeats.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().seatCount()));
        colTotal.setCellValueFactory(d -> new SimpleStringProperty(MoneyUtil.format(d.getValue().total())));
        colStatus.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().status()));

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(BookingStatus st, boolean empty) {
                super.updateItem(st, empty);
                getStyleClass().removeAll("st-pending", "st-confirmed", "st-cancelled");
                setText(empty || st == null ? null : st.name());
                if (!empty && st != null) {
                    getStyleClass().add("st-" + st.name().toLowerCase());
                }
            }
        });

        // Bật/tắt nút theo trạng thái dòng được chọn
        ReadOnlyObjectProperty<BookingRow> sel = table.getSelectionModel().selectedItemProperty();
        btnConfirm.disableProperty().bind(Bindings.createBooleanBinding(
                () -> sel.get() == null || sel.get().status() != BookingStatus.PENDING,
                sel));
        btnCancel.disableProperty().bind(Bindings.createBooleanBinding(
                () -> sel.get() == null || sel.get().status() == BookingStatus.CANCELLED,
                sel));

        // Double-click xem chi tiết đơn
        table.setRowFactory(tv -> {
            TableRow<BookingRow> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    BookingRow r = row.getItem();
                    fxAsync.run(() -> facade.detail(r.id()), this::showDetail);
                }
            });
            return row;
        });

        // Phân trang
        pagination.setPageFactory(pageIndex -> {
            load(pageIndex);
            return new VBox(); // Container rỗng vì TableView nằm ngoài pagination control
        });
    }

    @FXML
    private void onSearch() {
        pagination.setCurrentPageIndex(0);
        load(0);
    }

    @FXML
    private void onReset() {
        cbStatus.getSelectionModel().selectFirst();
        dpFrom.setValue(null);
        dpTo.setValue(null);
        tfKeyword.clear();
        onSearch();
    }

    @FXML
    private void onConfirm() {
        BookingRow r = table.getSelectionModel().getSelectedItem();
        if (r == null)
            return;
        if (AlertUtil.confirm("Xác nhận đơn #" + r.id() + " của khách " + r.customerName() + "?")) {
            fxAsync.runVoid(() -> facade.confirm(r.id()), () -> load(pagination.getCurrentPageIndex()), btnConfirm);
        }
    }

    @FXML
    private void onCancel() {
        BookingRow r = table.getSelectionModel().getSelectedItem();
        if (r == null)
            return;
        if (AlertUtil.confirm("Huỷ đơn #" + r.id() + "? Các vé sẽ được giải phóng lại ghế trống.")) {
            fxAsync.runVoid(() -> facade.cancel(r.id()), () -> load(pagination.getCurrentPageIndex()), btnCancel);
        }
    }

    private void load(int pageIndex) {
        StatusOption opt = cbStatus.getValue();
        BookingStatus status = opt != null ? opt.status() : null;
        LocalDate from = dpFrom.getValue();
        LocalDate to = dpTo.getValue();
        String keyword = tfKeyword.getText() != null ? tfKeyword.getText().trim() : "";

        fxAsync.run(
                () -> facade.search(status, from, to, keyword, pageIndex),
                result -> {
                    table.getItems().setAll(result.items());
                    pagination.setPageCount(result.totalPages());
                });
    }

    private void showDetail(BookingDetail detail) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Chi tiết đơn hàng #" + detail.id());
        dialog.setHeaderText("Thông tin chi tiết đơn #" + detail.id());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));

        int r = 0;
        grid.add(new Label("Khách hàng:"), 0, r);
        grid.add(new Label(detail.customerName() + " (" + detail.mobile() + ")"), 1, r++);

        grid.add(new Label("Phim:"), 0, r);
        grid.add(new Label(detail.movieTitle()), 1, r++);

        grid.add(new Label("Phòng chiếu:"), 0, r);
        grid.add(new Label(detail.roomName()), 1, r++);

        grid.add(new Label("Giờ chiếu:"), 0, r);
        grid.add(new Label(detail.startTime() != null ? detail.startTime().format(DT_FORMAT) : ""), 1, r++);

        grid.add(new Label("Danh sách ghế:"), 0, r);
        grid.add(new Label(String.join(", ", detail.seats())), 1, r++);

        grid.add(new Label("Tổng tiền:"), 0, r);
        Label lblTotal = new Label(MoneyUtil.format(detail.total()));
        lblTotal.setStyle("-fx-font-weight: bold; -fx-text-fill: #16a34a;");
        grid.add(lblTotal, 1, r++);

        grid.add(new Label("Trạng thái:"), 0, r);
        Label lblStatus = new Label(detail.status() != null ? detail.status().name() : "");
        lblStatus.setStyle("-fx-font-weight: bold;");
        grid.add(lblStatus, 1, r++);

        grid.add(new Label("Thời gian đặt:"), 0, r);
        grid.add(new Label(detail.bookingDate() != null ? detail.bookingDate().format(DT_FORMAT) : ""), 1, r++);

        dialog.getDialogPane().setContent(grid);
        dialog.showAndWait();
    }
}
