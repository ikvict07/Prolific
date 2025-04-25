package org.nevertouchgrass.prolific.javafxcontroller;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import lombok.Setter;
import org.nevertouchgrass.prolific.annotation.Initialize;
import org.nevertouchgrass.prolific.annotation.StageComponent;
import org.nevertouchgrass.prolific.service.FxmlProvider;
import org.nevertouchgrass.prolific.service.searching.comparators.ProjectComparatorBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

@StageComponent
public class SortingBarController {

    @FXML
    public Label sortByTitleLabel;
    @FXML
    public Label sortByTypeLabel;
    @FXML
    public Label sortByRunningLabel;
    @FXML
    public Label sortByStarredLabel;
    @FXML
    public HBox root;
    @FXML
    public HBox sortByStarredArrowContainer;
    @FXML
    public HBox sortByRunningArrowContainer;
    @FXML
    public HBox sortByTypeArrowContainer;
    @FXML
    public HBox sortByTitleArrowContainer;
    @Setter(onMethod_ = {@Autowired})
    private FxmlProvider fxmlProvider;
    @Setter(onMethod_ = {@Autowired})
    private ProjectsPanelController projectsPanelController;
    @Setter(onMethod_ = {@Autowired})
    private ProjectComparatorBuilder projectComparatorBuilder;

    private Map<HBox, State> arrowContainerToStateMap;
    private final Map<HBox, ProjectComparatorBuilder.ComparatorOption> arrowContainerToComparator = new HashMap<>();
    private final Map<HBox, String> containerToComparatorType = new HashMap<>();

    @Initialize
    public void init() {
        arrowContainerToStateMap = new HashMap<>(Map.of(
                sortByTitleArrowContainer, State.DEFAULT,
                sortByTypeArrowContainer, State.DEFAULT,
                sortByRunningArrowContainer, State.DEFAULT,
                sortByStarredArrowContainer, State.DEFAULT
        ));
        arrowContainerToComparator.put(sortByTitleArrowContainer, new ProjectComparatorBuilder.ComparatorOption("name", 0));
        arrowContainerToComparator.put(sortByTypeArrowContainer, new ProjectComparatorBuilder.ComparatorOption("type", 0));
        arrowContainerToComparator.put(sortByRunningArrowContainer, new ProjectComparatorBuilder.ComparatorOption("running", 0));
        arrowContainerToComparator.put(sortByStarredArrowContainer, new ProjectComparatorBuilder.ComparatorOption("starred", 0));
        containerToComparatorType.put(sortByTitleArrowContainer, "name");
        containerToComparatorType.put(sortByTypeArrowContainer, "type");
        containerToComparatorType.put(sortByRunningArrowContainer, "running");
        containerToComparatorType.put(sortByStarredArrowContainer, "starred");
    }

    @FXML
    private void sort(Event event) {
        HBox parent = (HBox) event.getSource();
        arrowContainerToStateMap.put(parent, arrowContainerToStateMap.get(parent).next());
        changeArrowDirection(parent, arrowContainerToStateMap.get(parent));
        changeSorting(parent);
    }

    private void changeSorting(HBox parent) {
        var state = arrowContainerToStateMap.get(parent);
        arrowContainerToComparator.put(parent, new ProjectComparatorBuilder.ComparatorOption(containerToComparatorType.get(parent), state.value));
        var comparator = projectComparatorBuilder.build(arrowContainerToComparator.values().toArray(new ProjectComparatorBuilder.ComparatorOption[0]));
        projectsPanelController.changeComparator(comparator);
    }

    private void changeArrowDirection(HBox arrowContainer, State state) {
        arrowContainer.getChildren().removeLast();
        switch (state) {
            case DESC -> arrowContainer.getChildren().add(fxmlProvider.getIcon("sortArrowDown"));
            case DEFAULT -> arrowContainer.getChildren().add(fxmlProvider.getIcon("defaultSorting"));
            case ASC -> arrowContainer.getChildren().add(fxmlProvider.getIcon("sortArrowUp"));
        }
    }


    private enum State {
        DESC(-1),
        DEFAULT(0),
        ASC(1);

        public State next() {
            return switch (this) {
                case DESC -> DEFAULT;
                case DEFAULT -> ASC;
                case ASC -> DESC;
            };
        }
        private final int value;
        State(int value) {
            this.value = value;
        }
    }
}
