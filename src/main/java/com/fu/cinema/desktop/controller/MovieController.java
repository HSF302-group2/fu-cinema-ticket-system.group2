package com.fu.cinema.desktop.controller;

import com.fu.cinema.desktop.facade.MovieFacade;
import com.fu.cinema.desktop.model.GenreItem;
import com.fu.cinema.desktop.model.MovieRow;
import com.fu.cinema.desktop.navigation.SceneManager;
import com.fu.cinema.desktop.navigation.View;
import com.fu.cinema.desktop.util.AlertUtil;
import com.fu.cinema.desktop.util.FxAsync;
import com.fu.cinema.dto.MovieForm;
import com.fu.cinema.enums.MovieStatus;
import javafx.animation.PauseTransition;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Window;
import javafx.util.Duration;
import javafx.util.StringConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class MovieController {

    private static final PseudoClass STOPPED = PseudoClass.getPseudoClass("stopped");

    private final MovieFacade facade;
    private final SceneManager sceneManager;
    private final FxAsync fxAsync;

    @FXML private TableView<MovieRow> table;
    @FXML private TableColumn<MovieRow, Number> colId;
    @FXML private TableColumn<MovieRow, String> colTitle;
    @FXML private TableColumn<MovieRow, String> colGenre;
    @FXML private TableColumn<MovieRow, Integer> colDuration;
    @FXML private TableColumn<MovieRow, LocalDate> colRelease;
    @FXML private TableColumn<MovieRow, String> colAge;
    @FXML private TableColumn<MovieRow, String> colStatus;

    @FXML private TextField tfSearch;
    @FXML private ComboBox<GenreItem> cbGenre;
    @FXML private ComboBox<MovieStatus> cbStatus;
    @FXML private ComboBox<String> cbSort;
    @FXML private Pagination pagination;
    @FXML private Label lblCount;
    @FXML private Button btnEdit, btnDelete;

    private final ObservableList<MovieRow> rows = FXCollections.observableArrayList();
    private final PauseTransition debounce = new PauseTransition(Duration.millis(300));
    private List<GenreItem> genreList = new ArrayList<>();

    @FXML
    private void initialize() {
        colId.setCellValueFactory(c -> new SimpleLongProperty(c.getValue().id()));
        colTitle.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().title()));
        colGenre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().genreName()));

        colDuration.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().duration()));
        colDuration.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : v + " phút");
            }
        });

        colRelease.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().releaseDate()));
        colRelease.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : v.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
        });

        colAge.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().ageRating() != null ? c.getValue().ageRating().name() : ""));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().status() != null ? c.getValue().status().name() : ""));

        table.setRowFactory(tv -> {
            TableRow<MovieRow> row = new TableRow<>() {
                @Override
                protected void updateItem(MovieRow m, boolean empty) {
                    super.updateItem(m, empty);
                    pseudoClassStateChanged(STOPPED, !empty && m != null && m.status() == MovieStatus.STOPPED);
                }
            };
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    openDialog(row.getItem().id());
                }
            });
            return row;
        });
        table.setItems(rows);

        btnEdit.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        btnDelete.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());

        cbStatus.setConverter(new StringConverter<>() {
            @Override
            public String toString(MovieStatus status) {
                return status == null ? "Tất cả trạng thái" : status.name();
            }

            @Override
            public MovieStatus fromString(String string) {
                return null;
            }
        });
        cbStatus.getItems().add(null);
        cbStatus.getItems().addAll(MovieStatus.values());
        cbStatus.setValue(null);

        cbGenre.setConverter(new StringConverter<>() {
            @Override
            public String toString(GenreItem item) {
                return item == null ? "Tất cả thể loại" : item.name();
            }

            @Override
            public GenreItem fromString(String string) {
                return null;
            }
        });

        cbSort.getItems().setAll("releaseDate", "title");
        cbSort.setValue("releaseDate");

        debounce.setOnFinished(e -> search());
        tfSearch.textProperty().addListener((o, a, b) -> debounce.playFromStart());
        cbGenre.valueProperty().addListener((o, a, b) -> search());
        cbStatus.valueProperty().addListener((o, a, b) -> search());
        cbSort.valueProperty().addListener((o, a, b) -> search());

        pagination.currentPageIndexProperty().addListener((o, a, b) -> load(b.intValue()));

        fxAsync.run(facade::genres, g -> {
            genreList = g;
            cbGenre.getItems().clear();
            cbGenre.getItems().add(null);
            cbGenre.getItems().addAll(g);
            cbGenre.setValue(null);
            load(0);
        });
    }

    private void search() {
        if (pagination.getCurrentPageIndex() != 0) {
            pagination.setCurrentPageIndex(0);
        } else {
            load(0);
        }
    }

    @FXML
    public void onRefresh() {
        load(pagination.getCurrentPageIndex());
    }

    private void load(int page) {
        String kw = tfSearch.getText() != null ? tfSearch.getText().trim() : "";
        Long genreId = cbGenre.getValue() != null ? cbGenre.getValue().id() : null;
        MovieStatus st = cbStatus.getValue();
        String sort = cbSort.getValue() != null ? cbSort.getValue() : "releaseDate";

        fxAsync.run(() -> facade.search(kw, genreId, st, sort, page), result -> {
            rows.setAll(result.items());
            pagination.setPageCount(result.totalPages());
            lblCount.setText(result.totalElements() + " phim");
        });
    }

    @FXML
    public void onAdd() {
        openDialog(null);
    }

    @FXML
    public void onEdit() {
        MovieRow selected = table.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openDialog(selected.id());
        }
    }

    private void openDialog(Long movieId) {
        Window owner = table.getScene().getWindow();
        fxAsync.run(
                () -> movieId == null ? new MovieForm() : facade.getForm(movieId),
                form -> {
                    SceneManager.Loaded<MovieDialogController> dlg = sceneManager.load(View.MOVIE_DIALOG);
                    dlg.controller().open(owner, form, genreList).ifPresent(f -> {
                        fxAsync.runVoid(
                                () -> facade.save(f),
                                () -> {
                                    AlertUtil.info("Đã lưu phim thành công!");
                                    load(pagination.getCurrentPageIndex());
                                },
                                table
                        );
                    });
                }
        );
    }

    @FXML
    public void onDelete() {
        MovieRow m = table.getSelectionModel().getSelectedItem();
        if (m == null || !AlertUtil.confirm("Bạn có chắc chắn muốn xoá phim \"" + m.title() + "\"?")) {
            return;
        }

        fxAsync.run(
                () -> facade.delete(m.id()),
                deleted -> {
                    if (deleted) {
                        AlertUtil.info("Đã xoá phim thành công!");
                    } else {
                        AlertUtil.info("Phim đã có suất chiếu -> Đã chuyển trạng thái sang STOPPED");
                    }
                    load(pagination.getCurrentPageIndex());
                },
                table
        );
    }
}
