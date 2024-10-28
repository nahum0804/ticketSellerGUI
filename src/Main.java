import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClienteSocketGUI gui = new ClienteSocketGUI();
            gui.setVisible(true);
        });
    }
}
