package org.nevertouchgrass.prolific.constants;

public enum Cursor {
    SW_RESIZE(javafx.scene.Cursor.SW_RESIZE),
    DEFAULT(javafx.scene.Cursor.DEFAULT),
    S_RESIZE(javafx.scene.Cursor.S_RESIZE),
    SE_RESIZE(javafx.scene.Cursor.SE_RESIZE),
    W_RESIZE(javafx.scene.Cursor.W_RESIZE),
    E_RESIZE(javafx.scene.Cursor.E_RESIZE),
    N_RESIZE(javafx.scene.Cursor.N_RESIZE),
    NE_RESIZE(javafx.scene.Cursor.NE_RESIZE),
    NW_RESIZE(javafx.scene.Cursor.NW_RESIZE);

    public final javafx.scene.Cursor cursor;
    Cursor(javafx.scene.Cursor cursor) {
        this.cursor = cursor;
    }
    public static Cursor getCursor(javafx.scene.Cursor cursor) {
        for (Cursor c : Cursor.values()) {
            if (c.cursor.equals(cursor)) {
                return c;
            }
        }
        return null;
    }
}
