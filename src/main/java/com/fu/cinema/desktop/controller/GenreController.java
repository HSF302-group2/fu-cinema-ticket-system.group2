package com.fu.cinema.desktop.controller;

import com.fu.cinema.desktop.facade.GenreFacade;
import com.fu.cinema.desktop.model.GenreRow;
import com.fu.cinema.desktop.util.AlertUtil;
import com.fu.cinema.desktop.util.FxAsync;
import com.fu.cinema.desktop.util.FxValidator;
import com.fu.cinema.dto.GenreForm;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class GenreController {

    private final GenreFacade facade;
    private final FxAsync fxAsync;
    private final FxValidator validator;

    @FXML private TableView<GenreRow> table;
    @FXML private TableColumn<GenreRow, Number> colId;
    @FXML private TableColumn<GenreRow, String> colName;
    @FXML private TableColumn<GenreRow, String> colDesc;

    @FXML private VBox formBox;
    @FXML private Label lblFormTitle;
    @FXML private TextField tfName;
    @FXML private TextArea taDesc;
    @FXML private Button btnSave, btnDelete, btnNew;

    private final ObservableList<GenreRow> rows = FXCollections.observableArrayList();
    private Long editingId;

    @FXML
    private void initialize() {
        colId.setCellValueFactory(c -> new SimpleLongProperty(c.getValue().id()));
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().name()));
        colDesc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().description()));
        table.setItems(rows);

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldV, row) -> fill(row));

        btnDelete.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());
        btnSave.disableProperty().bind(tfName.textProperty().isEmpty());

        onReload();
    }

    @FXML
    public void onReload() {
        fxAsync.run(facade::findAll, data -> {
            rows.setAll(data);
            onNew();
        });
    }

    private void fill(GenreRow row) {
        if (row == null) {
            onNew();
            return;
        }
        editingId = row.id();
        lblFormTitle.setText("Chỉnh sửa thể loại #" + row.id());
        tfName.setText(row.name());
        taDesc.setText(row.description());
        validator.mark(Map.of(), Map.of("genreName", tfName, "description", taDesc));
    }

    @FXML
    public void onNew() {
        editingId = null;
        table.getSelectionModel().clearSelection();
        lblFormTitle.setText("Thêm thể loại mới");
        tfName.clear();
        taDesc.clear();
        validator.mark(Map.of(), Map.of("genreName", tfName, "description", taDesc));
    }

    @FXML
    public void onSave() {
        GenreForm form = new GenreForm();
        form.setGenreId(editingId);
        form.setGenreName(tfName.getText().trim());
        form.setDescription(taDesc.getText().trim());

        if (!validator.mark(validator.validate(form), Map.of("genreName", tfName, "description", taDesc))) {
            return;
        }

        fxAsync.runVoid(
                () -> facade.save(form),
                () -> {
                    AlertUtil.info("Đã lưu thể loại thành công!");
                    onReload();
                },
                formBox
        );
    }

    @FXML
    public void onDelete() {
        GenreRow row = table.getSelectionModel().getSelectedItem();
        if (row == null) return;

        if (AlertUtil.confirm("Bạn có chắc chắn muốn xoá thể loại \"" + row.name() + "\"?")) {
            fxAsync.runVoid(
                    () -> facade.delete(row.id()),
                    () -> {
                        AlertUtil.info("Đã xoá thể loại thành công!");
                        onReload();
                    },
                    formBox
            );
        }
    }
}
