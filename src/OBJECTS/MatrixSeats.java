package OBJECTS;

import java.util.ArrayList;
import java.util.Arrays;

public class MatrixSeats {

    private ArrayList<Block> matrix = new ArrayList<>();

    public MatrixSeats() {
        int count = 1;
        Block blockC = new Block("C");
        while (count <= 3) {
            ArrayList<Site> newRow = new ArrayList<>();
            for (int seat = 1; seat < 11; seat++) {
                newRow.add(new Site(count, seat, 'F'));
            }
            blockC.addRow(newRow);
            count++;
        }
        matrix.add(blockC);

        count = 1;
        Block blockB = new Block("B");
        while (count <= 3) {
            ArrayList<Site> newRow = new ArrayList<>();
            for (int seat = 1; seat < 11; seat++) {
                newRow.add(new Site(count, seat, 'F'));
            }
            blockB.addRow(newRow);
            count++;
        }
        matrix.add(blockB);

        count = 1;
        Block blockA2 = new Block("A2");
        while (count <= 3) {
            ArrayList<Site> newRow = new ArrayList<>();
            for (int seat = 1; seat < 11; seat++) {
                newRow.add(new Site(count, seat, 'F'));
            }
            blockA2.addRow(newRow);
            count++;
        }
        matrix.add(blockA2);

        count = 1;
        Block blockA1 = new Block("A1");
        while (count <= 3) {
            ArrayList<Site> newRow = new ArrayList<>();
            for (int seat = 1; seat < 11; seat++) {
                newRow.add(new Site(count, seat, 'F'));
            }
            blockA1.addRow(newRow);
            count++;
        }
        matrix.add(blockA1);

        count = 1;
        Block blockVIP = new Block("VIP");
        while (count <= 5) {
            ArrayList<Site> newRow = new ArrayList<>();
            for (int seat = 1; seat < 11; seat++) {
                newRow.add(new Site(count, seat, 'F'));
            }
            blockVIP.addRow(newRow);
            count++;
        }
        matrix.add(blockVIP);
    }

    public Site getSite(String block, int row, int seat) {
        for (Block b : matrix) {
            if (b.getBlock().equals(block)) {
                return b.getRow(row - 1).get(seat - 1); // Restamos 1 porque las filas/asientos inician desde 1
            }
        }
        return null; // Si no se encuentra
    }

    public void changeState(String block, int row, int seat, char newState) {
        Site site = getSite(block, row, seat);
        if (site != null) {
            site.setState(newState);
        }
    }

    private char convertState(String stateServer){
        return switch (stateServer) {
            case "Sold" -> 'O';
            case "Reserved" -> 'R';
            default -> 'F';
        };
    }

    public void updateMatrixServer(String[][] info) {
        for(String[] site: info) {
            //System.out.println(Arrays.toString(site));
            changeState(site[0], Integer.parseInt(site[2]), Integer.parseInt(site[3]), convertState(site[1]));
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Block block : matrix) {
            sb.append("OBJECTS.Block ").append(block.getBlock()).append(":\n");
            for (int i = 0; i < block.getNumberOfRows(); i++) { // Cambiado para usar el tamaño real de filas
                sb.append("Row ").append(i + 1).append(": ");
                for (Site site : block.getRow(i)) {
                    sb.append("[Seat ").append(site.getColumn()).append(", State: ").append(site.getState()).append("] ");
                }
                sb.append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
