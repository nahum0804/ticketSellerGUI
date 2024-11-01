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
        initLayout();
    }

    private void initLayout() throws IOException {
        removeAll();  // Limpia el layout actual antes de actualizarlo
        add(createBlockPanel("C"));
        add(createBlockPanel("B"));
        add(createBlockPanel("A1"));
        add(createBlockPanel("A2"));
        add(createBlockPanel("VIP"));
        revalidate();  // Refresca el panel para mostrar los cambios
        repaint();
    }

    private JPanel createBlockPanel(String blockName) throws IOException {
        JPanel blockPanel = new JPanel();
        blockPanel.setBorder(BorderFactory.createTitledBorder(blockName));

        int rows = blockName.equals("VIP") ? 5 : 3;
        blockPanel.setLayout(new GridLayout(rows, 10)); // 10 asientos por fila

        for (int row = 1; row <= rows; row++) {
            for (int seat = 1; seat <= 10; seat++) {
                Site site = matrixSeats.getSite(blockName, row, seat);
                BufferedImage img = ImageIO.read(SeatingLayout.class.getResource(site.getImage()));
                Image scaledImg = img.getScaledInstance(30, 35, Image.SCALE_SMOOTH);
                JButton seatButton = new JButton(new ImageIcon(scaledImg));
                blockPanel.add(seatButton);
            }
        }
        return blockPanel;
    }

    public void updateLayout(MatrixSeats newMatrixSeats) throws IOException {
        this.matrixSeats = newMatrixSeats;
        initLayout();
    }
}