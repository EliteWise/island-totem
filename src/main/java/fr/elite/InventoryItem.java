package fr.elite;

import java.util.List;

public class InventoryItem {
    private String material;
    private List<String> lores;
    private String message;
    private String color;
    private Position position;

    public InventoryItem() {
    }

    public InventoryItem(String material, List<String> lores, String message, String color, Position position) {
        this.material = material;
        this.lores = lores;
        this.message = message;
        this.color = color;
        this.position = position;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public List<String> getLores() {
        return lores;
    }

    public void setLores(List<String> lores) {
        this.lores = lores;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public static class Position {
        private int x;
        private int y;

        public Position() {
        }

        public Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

    }
}
