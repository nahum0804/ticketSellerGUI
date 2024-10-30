import javax.swing.SwingUtilities;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClienteSocketGUI gui = null;
            try {
                gui = new ClienteSocketGUI();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            gui.setVisible(true);
        });
    }
}
