package com.fu.cinema.desktop.controller;

import com.fu.cinema.desktop.model.GenreItem;
import com.fu.cinema.desktop.util.FxValidator;
import com.fu.cinema.dto.MovieForm;
import com.fu.cinema.enums.AgeRating;
import com.fu.cinema.enums.MovieStatus;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class MovieDialogController {

    private final FxValidator validator;

    @FXML private GridPane root;
    @FXML private TextField tfTitle, tfDirector, tfPoster;
    @FXML private Spinner<Integer> spDuration;
    @FXML private DatePicker dpRelease;
    @FXML private ComboBox<AgeRating> cbAgeRating;
    @FXML private ComboBox<GenreItem> cbGenre;
    @FXML private ComboBox<MovieStatus> cbStatus;
    @FXML private TextArea taDescription;

    private Long movieId;

    @FXML
    private void initialize() {
        spDuration.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 120));
        spDuration.setEditable(true);
        spDuration.focusedProperty().addListener((obs, was, is) -> {
            if (!is) commitSpinner();
        });
        cbAgeRating.getItems().setAll(AgeRating.values());
        cbStatus.getItems().setAll(MovieStatus.values());
    }

    public Optional<MovieForm> open(Window owner, MovieForm form, List<GenreItem> genres) {
        cbGenre.getItems().setAll(genres);
        fill(form);

        Dialog<MovieForm> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle(form.getMovieId() == null ? "Thêm phim mới" : "Chỉnh sửa phim");
        dialog.getDialogPane().setContent(root);
        ButtonType save = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);

        dialog.getDialogPane().lookupButton(save).addEventFilter(ActionEvent.ACTION, e -> {
            if (!validate()) {
                e.consume();
            }
        });

        dialog.setResultConverter(bt -> bt == save ? toForm() : null);
        return dialog.showAndWait();
    }

    private boolean validate() {
        commitSpinner();
        MovieForm form = toForm();
        return validator.mark(validator.validate(form), Map.of(
                "title", tfTitle,
                "director", tfDirector,
                "duration", spDuration,
                "releaseDate", dpRelease,
                "ageRating", cbAgeRating,
                "genreId", cbGenre,
                "status", cbStatus,
                "posterUrl", tfPoster,
                "description", taDescription
        ));
    }

    private MovieForm toForm() {
        MovieForm f = new MovieForm();
        f.setMovieId(movieId);
        f.setTitle(tfTitle.getText() != null ? tfTitle.getText().trim() : "");
        f.setDirector(tfDirector.getText() != null ? tfDirector.getText().trim() : "");
        f.setDuration(spDuration.getValue());
        f.setReleaseDate(dpRelease.getValue());
        f.setAgeRating(cbAgeRating.getValue());
        f.setGenreId(cbGenre.getValue() != null ? cbGenre.getValue().id() : null);
        f.setStatus(cbStatus.getValue());
        f.setPosterUrl(tfPoster.getText() != null ? tfPoster.getText().trim() : "");
        f.setDescription(taDescription.getText() != null ? taDescription.getText().trim() : "");
        return f;
    }

    private void fill(MovieForm f) {
        movieId = f.getMovieId();
        tfTitle.setText(f.getTitle() != null ? f.getTitle() : "");
        tfDirector.setText(f.getDirector() != null ? f.getDirector() : "");
        if (f.getDuration() != null) {
            spDuration.getValueFactory().setValue(f.getDuration());
        }
        dpRelease.setValue(f.getReleaseDate());
        cbAgeRating.setValue(f.getAgeRating() != null ? f.getAgeRating() : AgeRating.P);
        cbStatus.setValue(f.getStatus() != null ? f.getStatus() : MovieStatus.SHOWING);
        tfPoster.setText(f.getPosterUrl() != null ? f.getPosterUrl() : "");
        taDescription.setText(f.getDescription() != null ? f.getDescription() : "");

        if (f.getGenreId() != null) {
            cbGenre.getItems().stream()
                    .filter(g -> g.id().equals(f.getGenreId()))
                    .findFirst()
                    .ifPresent(cbGenre::setValue);
        }
    }

    private void commitSpinner() {
        try {
            spDuration.getValueFactory().setValue(Integer.parseInt(spDuration.getEditor().getText().trim()));
        } catch (Exception ignored) {
            spDuration.getEditor().setText(String.valueOf(spDuration.getValue()));
        }
    }
}
