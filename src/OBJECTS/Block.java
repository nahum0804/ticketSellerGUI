package OBJECTS;

import java.util.ArrayList;

public class Block {
    private String block;
    private ArrayList<ArrayList<Site>> sites = new ArrayList<>(); // Asegúrate de inicializarlo

    public Block(String block) {
        this.block = block;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    public void addRow(ArrayList<Site> row) {
        sites.add(row);
    }

    public ArrayList<Site> getRow(int row) {
        return sites.get(row);
    }

    public int getNumberOfRows() {
        return sites.size(); // Devuelve el número de filas en este bloque
    }
}
