package seoultech.se.core.model.board;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import seoultech.se.core.model.block.enumType.Color;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class Cell {
    private Color color = Color.NONE;
    private boolean isOccupied = false;
    

    // Factory Methods 1~4.
    public static Cell of(Color color) {
        return Cell.of(color, true);
    }

    public static Cell of(Color color, boolean isOccupied) {
        Cell cell = new Cell();
        cell.setColor(color);
        cell.setOccupied(isOccupied);
        return cell;
    }

    public static Cell empty() {
        return Cell.of(Color.NONE, false);
    }

    public void clear() {
        this.color = Color.NONE;
        this.isOccupied = false;
    }
}
