package OBJECTS;
import GUI.SeatingLayout;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {

        MatrixSeats matrixSeats = new MatrixSeats();
        //System.out.println(matrixSeats);
        new SeatingLayout(matrixSeats);
    }
}