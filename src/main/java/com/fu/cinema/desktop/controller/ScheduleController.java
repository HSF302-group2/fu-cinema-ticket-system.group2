package com.fu.cinema.desktop.controller;

import com.fu.cinema.desktop.facade.RoomFacade;
import com.fu.cinema.desktop.facade.ShowtimeFacade;
import com.fu.cinema.desktop.model.*;
import com.fu.cinema.desktop.util.AlertUtil;
import com.fu.cinema.desktop.util.FxAsync;
import com.fu.cinema.desktop.util.FxValidator;
import com.fu.cinema.desktop.util.MoneyUtil;
import com.fu.cinema.dto.RoomForm;
import com.fu.cinema.dto.ShowtimeForm;
import com.fu.cinema.enums.RoomStatus;
import com.fu.cinema.enums.RoomType;
import com.fu.cinema.enums.ShowtimeStatus;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class ScheduleController {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    private final RoomFacade roomFacade;
    private final ShowtimeFacade showtimeFacade;
    private final FxAsync fxAsync;
    private final FxValidator validator;

    // --- TAB PHÒNG ---
    @FXML private TableView<RoomRow> tableRooms;
    @FXML private TableColumn<RoomRow, String> colRoomName, colRoomType, colRoomCapacity, colRoomStatus;
    @FXML private VBox formRoomBox;
    @FXML private TextField tfRoomName;
    @FXML private ComboBox<RoomType> cbRoomType;
    @FXML private Spinner<Integer> spRows, spSeatsPerRow;
    @FXML private ComboBox<RoomStatus> cbRoomStatus;
    @FXML private Button btnSaveRoom, btnDeleteRoom;

    private final ObservableList<RoomRow> roomRows = FXCollections.observableArrayList();
    private Long editingRoomId;

    // --- TAB SUẤT CHIẾU ---
    @FXML private ComboBox<MovieItem> cbFilterMovie;
    @FXML private ComboBox<RoomItem> cbFilterRoom;
    @FXML private DatePicker dpFilterDate;
    @FXML private TableView<ShowtimeRow> tableShowtimes;
    @FXML private TableColumn<ShowtimeRow, String> colShowMovie, colShowRoom, colShowStart, colShowEnd, colShowPrice, colShowStatus;
    @FXML private Pagination pagination;
    @FXML private VBox formShowtimeBox;
    @FXML private ComboBox<MovieItem> cbMovie;
    @FXML private ComboBox<RoomItem> cbRoom;
    @FXML private DatePicker dpDate;
    @FXML private Spinner<Integer> spHour, spMinute;
    @FXML private TextField tfPrice;
    @FXML private Label lblEnd;
    @FXML private Button btnSaveShowtime, btnDeleteShowtime;

    private final ObservableList<ShowtimeRow> showtimeRows = FXCollections.observableArrayList();
    private Long editingShowtimeId;

    @FXML
    private void initialize() {
        initRoomTab();
        initShowtimeTab();
    }

    private void initRoomTab() {
        colRoomName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().name()));
        colRoomType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().type().name()));
        colRoomCapacity.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().capacity())));
        colRoomStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().status().name()));

        tableRooms.setItems(roomRows);
        tableRooms.getSelectionModel().selectedItemProperty().addListener((o, oldV, r) -> fillRoom(r));

        cbRoomType.getItems().setAll(RoomType.values());
        cbRoomStatus.getItems().setAll(RoomStatus.values());

        spRows.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 26, 8));
        spSeatsPerRow.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 30, 12));

        btnDeleteRoom.disableProperty().bind(tableRooms.getSelectionModel().selectedItemProperty().isNull());
        btnSaveRoom.disableProperty().bind(tfRoomName.textProperty().isEmpty());

        reloadRooms();
    }

    private void initShowtimeTab() {
        colShowMovie.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().movieTitle()));
        colShowRoom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().roomName()));
        colShowStart.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().start().format(TIME_FMT)));
        colShowEnd.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().end().format(TIME_FMT)));
        colShowPrice.setCellValueFactory(c -> new SimpleStringProperty(MoneyUtil.format(c.getValue().price())));
        colShowStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().status().name()));

        tableShowtimes.setItems(showtimeRows);
        tableShowtimes.getSelectionModel().selectedItemProperty().addListener((o, oldV, s) -> fillShowtime(s));

        spHour.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 19));
        spMinute.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 55, 0, 5));
        tfPrice.setTextFormatter(new TextFormatter<>(c -> c.getControlNewText().matches("\\d{0,9}") ? c : null));

        dpDate.setDayCellFactory(p -> new DateCell() {
            @Override
            public void updateItem(LocalDate d, boolean empty) {
                super.updateItem(d, empty);
                setDisable(empty || d.isBefore(LocalDate.now()));
            }
        });

        lblEnd.textProperty().bind(Bindings.createStringBinding(() -> {
            LocalDateTime end = suggestedEnd();
            return end == null ? "—" : "Kết thúc (gợi ý): " + end.format(TIME_FMT);
        }, cbMovie.valueProperty(), dpDate.valueProperty(), spHour.valueProperty(), spMinute.valueProperty()));

        btnDeleteShowtime.disableProperty().bind(tableShowtimes.getSelectionModel().selectedItemProperty().isNull());

        cbFilterMovie.valueProperty().addListener((o, a, b) -> loadShowtimes(0));
        cbFilterRoom.valueProperty().addListener((o, a, b) -> loadShowtimes(0));
        dpFilterDate.valueProperty().addListener((o, a, b) -> loadShowtimes(0));
        pagination.currentPageIndexProperty().addListener((o, a, b) -> loadShowtimes(b.intValue()));

        loadFilterData();
        loadShowtimes(0);
    }

    private LocalDateTime startTime() {
        return dpDate.getValue() == null ? null : LocalDateTime.of(dpDate.getValue(), LocalTime.of(spHour.getValue(), spMinute.getValue()));
    }

    private LocalDateTime suggestedEnd() {
        LocalDateTime start = startTime();
        return start == null || cbMovie.getValue() == null ? null : start.plusMinutes(cbMovie.getValue().duration() + 15L);
    }

    private void reloadRooms() {
        fxAsync.run(roomFacade::findAll, roomRows::setAll);
    }

    private void loadFilterData() {
        fxAsync.run(showtimeFacade::schedulableMovies, movies -> {
            cbMovie.getItems().setAll(movies);
            cbFilterMovie.getItems().setAll(movies);
        });
        fxAsync.run(showtimeFacade::activeRooms, rooms -> {
            cbRoom.getItems().setAll(rooms);
            cbFilterRoom.getItems().setAll(rooms);
        });
    }

    private void loadShowtimes(int page) {
        Long movieId = cbFilterMovie.getValue() == null ? null : cbFilterMovie.getValue().id();
        Long roomId = cbFilterRoom.getValue() == null ? null : cbFilterRoom.getValue().id();
        LocalDate date = dpFilterDate.getValue();
        fxAsync.run(() -> showtimeFacade.search(movieId, roomId, date, page), result -> {
            showtimeRows.setAll(result.items());
            pagination.setPageCount(result.totalPages());
        });
    }

    // --- Actions Tab Phòng ---
    @FXML
    private void onNewRoom() {
        editingRoomId = null;
        tfRoomName.clear();
        cbRoomType.setValue(RoomType.TWO_D);
        cbRoomStatus.setValue(RoomStatus.ACTIVE);
        spRows.getValueFactory().setValue(8);
        spSeatsPerRow.getValueFactory().setValue(12);
        tableRooms.getSelectionModel().clearSelection();
    }

    private void fillRoom(RoomRow r) {
        if (r == null) return;
        editingRoomId = r.id();
        tfRoomName.setText(r.name());
        cbRoomType.setValue(r.type());
        cbRoomStatus.setValue(r.status());
        spRows.getValueFactory().setValue(r.rows());
        spSeatsPerRow.getValueFactory().setValue(r.seatsPerRow());
    }

    @FXML
    private void onSaveRoom() {
        RoomForm f = new RoomForm();
        f.setRoomId(editingRoomId);
        f.setRoomName(tfRoomName.getText().trim());
        f.setRoomType(cbRoomType.getValue());
        f.setStatus(cbRoomStatus.getValue());
        f.setTotalRows(spRows.getValue());
        f.setSeatsPerRow(spSeatsPerRow.getValue());

        if (!validator.mark(validator.validate(f), Map.of(
                "roomName", tfRoomName, "roomType", cbRoomType,
                "status", cbRoomStatus, "totalRows", spRows, "seatsPerRow", spSeatsPerRow))) {
            return;
        }

        fxAsync.runVoid(() -> roomFacade.save(f), () -> {
            AlertUtil.info("Đã lưu phòng chiếu thành công");
            onNewRoom();
            reloadRooms();
            loadFilterData();
        }, formRoomBox);
    }

    @FXML
    private void onDeleteRoom() {
        RoomRow r = tableRooms.getSelectionModel().getSelectedItem();
        if (r == null || !AlertUtil.confirm("Xoá phòng \"" + r.name() + "\"?")) return;
        fxAsync.run(() -> roomFacade.delete(r.id()), deleted -> {
            AlertUtil.info(deleted ? "Đã xoá phòng thành công" : "Phòng đã có suất chiếu → đã chuyển sang MAINTENANCE");
            onNewRoom();
            reloadRooms();
            loadFilterData();
        });
    }

    // --- Actions Tab Suất chiếu ---
    @FXML
    private void onNewShowtime() {
        editingShowtimeId = null;
        cbMovie.setValue(null);
        cbRoom.setValue(null);
        dpDate.setValue(LocalDate.now());
        spHour.getValueFactory().setValue(19);
        spMinute.getValueFactory().setValue(0);
        tfPrice.clear();
        tableShowtimes.getSelectionModel().clearSelection();
    }

    private void fillShowtime(ShowtimeRow s) {
        if (s == null) return;
        editingShowtimeId = s.id();
        dpDate.setValue(s.start().toLocalDate());
        spHour.getValueFactory().setValue(s.start().getHour());
        spMinute.getValueFactory().setValue(s.start().getMinute());
        tfPrice.setText(s.price().toPlainString());
        cbMovie.getItems().stream().filter(m -> m.title().equals(s.movieTitle())).findFirst().ifPresent(cbMovie::setValue);
        cbRoom.getItems().stream().filter(r -> r.name().equals(s.roomName())).findFirst().ifPresent(cbRoom::setValue);
    }

    @FXML
    private void onSaveShowtime() {
        ShowtimeForm f = new ShowtimeForm();
        f.setShowtimeId(editingShowtimeId);
        f.setMovieId(cbMovie.getValue() == null ? null : cbMovie.getValue().id());
        f.setRoomId(cbRoom.getValue() == null ? null : cbRoom.getValue().id());
        f.setStartTime(startTime());
        f.setEndTime(suggestedEnd());
        f.setTicketPrice(tfPrice.getText().isBlank() ? null : new BigDecimal(tfPrice.getText().trim()));
        f.setStatus(ShowtimeStatus.OPEN);

        if (!validator.mark(validator.validate(f), Map.of(
                "movieId", cbMovie, "roomId", cbRoom,
                "startTime", dpDate, "ticketPrice", tfPrice))) {
            return;
        }

        fxAsync.runVoid(() -> showtimeFacade.save(f), () -> {
            AlertUtil.info("Đã lưu suất chiếu thành công");
            onNewShowtime();
            loadShowtimes(pagination.getCurrentPageIndex());
        }, formShowtimeBox);
    }

    @FXML
    private void onDeleteShowtime() {
        ShowtimeRow s = tableShowtimes.getSelectionModel().getSelectedItem();
        if (s == null || !AlertUtil.confirm("Xoá suất chiếu \"" + s.movieTitle() + "\" lúc " + s.start().format(TIME_FMT) + "?")) return;
        fxAsync.run(() -> showtimeFacade.delete(s.id()), deleted -> {
            AlertUtil.info(deleted ? "Đã xoá suất chiếu thành công" : "Đã huỷ suất chiếu và các đơn liên quan (BR-09)");
            onNewShowtime();
            loadShowtimes(pagination.getCurrentPageIndex());
        });
    }

    @FXML
    private void onClearFilters() {
        cbFilterMovie.setValue(null);
        cbFilterRoom.setValue(null);
        dpFilterDate.setValue(null);
        loadShowtimes(0);
    }
}
