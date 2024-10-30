package GUI;

import OBJECTS.MatrixSeats;
import OBJECTS.Site;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class SeatingLayout extends JPanel {
    private MatrixSeats matrixSeats;

    public SeatingLayout(MatrixSeats matrixSeats) throws IOException {
        this.matrixSeats = matrixSeats;
        setLayout(new GridLayout(5, 1)); // 5 bloques en vertical

        // Crear paneles por bloque
        add(createBlockPanel("C"));
        add(createBlockPanel("B"));
        add(createBlockPanel("A1"));
        add(createBlockPanel("A2"));
        add(createBlockPanel("VIP"));
    }

    private JPanel createBlockPanel(String blockName) throws IOException {
        JPanel blockPanel = new JPanel();
        blockPanel.setBorder(BorderFactory.createTitledBorder(blockName));

        // Ajustar el número de filas según el bloque
        int rows = blockName.equals("VIP") ? 5 : 3; // 5 filas para VIP, 3 para los demás
        blockPanel.setLayout(new GridLayout(rows, 10)); // 10 asientos por fila

        for (int row = 1; row <= rows; row++) {
            for (int seat = 1; seat <= 10; seat++) {
                // Obtener el asiento desde la matriz
                Site site = matrixSeats.getSite(blockName, row, seat);
                BufferedImage img = ImageIO.read(SeatingLayout.class.getResource(site.getImage()));

                // Redimensionar la imagen al tamaño del botón
                Image scaledImg = img.getScaledInstance(30, 35, Image.SCALE_SMOOTH); // Ajusta 50x50 al tamaño deseado
                JButton seatButton = new JButton(new ImageIcon(scaledImg));

                blockPanel.add(seatButton);
            }
        }

        return blockPanel;
    }
}
