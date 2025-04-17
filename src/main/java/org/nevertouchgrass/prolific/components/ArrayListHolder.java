package org.nevertouchgrass.prolific.components;

import javafx.beans.DefaultProperty;
import javafx.scene.layout.Pane;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@DefaultProperty("items")
@Getter
public class ArrayListHolder<T> extends Pane {
    private final List<T> items = new ArrayList<>();
}
