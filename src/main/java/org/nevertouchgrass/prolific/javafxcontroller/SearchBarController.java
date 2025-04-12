package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import lombok.Setter;
import org.nevertouchgrass.prolific.annotation.Initialize;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.nevertouchgrass.prolific.constants.Filters;
import org.nevertouchgrass.prolific.model.Project;
import org.nevertouchgrass.prolific.service.localization.LocalizationHolder;
import org.nevertouchgrass.prolific.service.localization.LocalizationProvider;
import org.nevertouchgrass.prolific.service.searching.filters.ProjectFilterService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Predicate;

import static org.nevertouchgrass.prolific.constants.Filters.BY_NAME;

@StageComponent
public class SearchBarController {
    @FXML
    public HBox root;
    @FXML
    public TextField textField;
    @FXML
    public StackPane cancel;
    @FXML
    public StackPane regex;
    @FXML
    public StackPane filter;
    @FXML
    public HBox filterSection;

    private boolean isRegex = false;

    @Setter(onMethod_ = @Autowired)
    private LocalizationProvider localizationProvider;
    @Setter(onMethod_ = @Autowired)
    private ProjectFilterService filterService;
    @Setter(onMethod_ = @Autowired)
    private ProjectsPanelController projectsPanelController;
    @Setter(onMethod_ = {@Autowired})
    private Parent filtersDropdownParent;
    @Setter(onMethod_ = {@Autowired})
    private LocalizationHolder localizationHolder;
    private Filters filterType = BY_NAME;
    private ObservableList<Filters> appliedFilters = FXCollections.observableArrayList();

    private ContextMenu contextMenu = new ContextMenu();

    @FXML
    public void initialize() {
        cancel.setOnMouseClicked(_ -> {
            textField.clear();
            handleAction(new ActionEvent());
        });
        regex.setOnMouseClicked(_ -> {
            isRegex = !isRegex;
            if (isRegex) {
                regex.getStyleClass().add("selected");
            } else {
                regex.getStyleClass().remove("selected");
            }
            filterType = isRegex ? Filters.BY_NAME_REGEX : BY_NAME;
            handleAction(new ActionEvent());
        });
        filter.setOnMouseClicked(event -> {
            contextMenu = getContextMenu();
            contextMenu.show(filter, event.getScreenX() + 8, event.getScreenY() + 8);
        });

        appliedFilters.addListener((ListChangeListener<? super Filters>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    change.getAddedSubList().forEach(added -> {
                        var label = new Label();
                        label.textProperty().bind(localizationHolder.getLocalization(added.name()));
                        label.setUserData("filter:" + added.name());
                        label.getStyleClass().add("icon-button");
                        label.setOnMouseClicked(event -> {
                            appliedFilters.remove(added);
                            filterSection.getChildren().remove(label);
                            handleAction(new ActionEvent());
                        });
                        filterSection.getChildren().add(label);
                    });
                }
            }
        });
    }

    @Initialize
    public void init() {
        textField.promptTextProperty().bind(localizationProvider.search());
        root.requestFocus();
    }

    private void updateFiltering(Predicate<Project> filterFunction) {
        projectsPanelController.filterProjects(filterFunction);
    }

    public void handleAction(ActionEvent actionEvent) {
        if (actionEvent.getEventType() == ActionEvent.ACTION) {
            String searchText = textField.getText();
            var f = new ArrayList<ProjectFilterService.FilterOption>();
            f.add(new ProjectFilterService.FilterOption(filterType, searchText));
            appliedFilters.forEach(filter -> f.add(new ProjectFilterService.FilterOption(filter, true)));
            updateFiltering(filterService.getFilter(f.toArray(ProjectFilterService.FilterOption[]::new)));
        }
    }

    public ContextMenu getContextMenu() {
        contextMenu.getItems().clear();
        filtersDropdownParent.getChildrenUnmodifiable().forEach(node -> {
            Label label = (Label) node;
            var filter = extractUserData(label);
            if (appliedFilters.contains(filter)) {
                return;
            }
            MenuItem menuItem = new MenuItem();
            menuItem.textProperty().bind(label.textProperty());
            menuItem.setGraphic(label.getGraphic());
            menuItem.setOnAction(_ -> {
                appliedFilters.add(filter);
                contextMenu.getItems().remove(menuItem);
                handleAction(new ActionEvent());
            });
            contextMenu.getItems().addAll(menuItem);
        });
        return contextMenu;
    }

    private Filters extractUserData(Label label) {
        var userData = (String) label.getUserData();
        var data = Arrays.stream(userData.split(",")).filter(s -> s.contains("filter")).map(s -> s.split(":")[1]).findFirst().get();
        var filter = Filters.valueOf(data);
        return filter;
    }
}
