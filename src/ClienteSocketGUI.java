import GUI.SeatingLayout;
import OBJECTS.MatrixSeats;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClienteSocketGUI extends JFrame {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 7878;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private ArrayList<MatrixSeats> mapeo = new ArrayList<>();

    // Componentes de la interfaz
    private JTextField cantidadTextField;
    private JComboBox<String> categoriaComboBox;
    private JButton buscarButton;
    private JButton aceptarButton;
    private JButton cancelarButton;
    private JTable resultadosTable;
    private JLabel tiempoLabel;

    private Timer contadorTiempo;
    private int tiempoRestante = 15;

    public ClienteSocketGUI() throws IOException {
        // Configuración de la interfaz
        setTitle("Solicitud de Asientos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        //setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Panel para la solicitud
        JPanel solicitudPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        solicitudPanel.setBorder(BorderFactory.createTitledBorder("Solicitud de Asientos"));

        solicitudPanel.add(new JLabel("Cantidad de asientos:"));
        cantidadTextField = new JTextField();
        solicitudPanel.add(cantidadTextField);

        solicitudPanel.add(new JLabel("Categoría:"));
        categoriaComboBox = new JComboBox<>(new String[]{"A1", "A2", "VIP"});
        solicitudPanel.add(categoriaComboBox);

        buscarButton = new JButton("Buscar");
        solicitudPanel.add(buscarButton);

        tiempoLabel = new JLabel("Tiempo restante: 15 segundos");
        tiempoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        tiempoLabel.setVisible(false);
        add(tiempoLabel, BorderLayout.NORTH);

        // Panel para los resultados
        JPanel resultadoPanel = new JPanel(new BorderLayout());
        resultadoPanel.setBorder(BorderFactory.createTitledBorder("Resultado"));

        resultadosTable = new JTable(new DefaultTableModel(new Object[]{"Respuesta del Servidor"}, 0));
        resultadoPanel.add(new JScrollPane(resultadosTable), BorderLayout.CENTER);

        aceptarButton = new JButton("Aceptar");
        aceptarButton.setBackground(Color.GREEN);
        aceptarButton.setEnabled(false);

        cancelarButton = new JButton("Cancelar");
        cancelarButton.setBackground(Color.RED);

        JPanel botonesPanel = new JPanel(new FlowLayout());
        botonesPanel.add(aceptarButton);
        botonesPanel.add(cancelarButton);

        JPanel panelDerecho = new JPanel(new BorderLayout());
        panelDerecho.add(solicitudPanel,BorderLayout.NORTH);
        panelDerecho.add(resultadoPanel,BorderLayout.CENTER);
        panelDerecho.add(botonesPanel,BorderLayout.SOUTH);

        SeatingLayout seatingLayout = new SeatingLayout(new MatrixSeats());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, seatingLayout, panelDerecho);
        splitPane.setDividerLocation(600);

        setLayout(new BorderLayout());
        add(splitPane, BorderLayout.CENTER);
        add(tiempoLabel,BorderLayout.NORTH);

        buscarButton.addActionListener(e -> enviarSolicitud());
        aceptarButton.addActionListener(e -> enviarConfirmacion());
        cancelarButton.addActionListener(e -> cancelarOperacion());
    }

    private void enviarSolicitud() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            int cantidad;
            try {
                cantidad = Integer.parseInt(cantidadTextField.getText());
                if (cantidad <= 0 || cantidad > 10) {
                    JOptionPane.showMessageDialog(this, "La cantidad de asientos debe ser entre 1 y 10.");
                    return;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Introduce un número válido para la cantidad de asientos.");
                return;
            }

            String categoria = (String) categoriaComboBox.getSelectedItem();
            String mensaje = cantidad + "/" + categoria;

            // Envair al server ls asientos data
            long startTime = System.currentTimeMillis();
            out.println(mensaje);
            System.out.println("Mensaje enviado al servidor: " + mensaje);

            // Leer y almacenar respuestas del servidor para cada asiento solicitado
            ArrayList<String> resultados = new ArrayList<>();
            for (int i = 0; i < cantidad; i++) {
                String respuesta = in.readLine();
                if (respuesta != null) {
                    System.out.println("Respuesta del servidor: " + respuesta);
                    resultados.add(respuesta);
                }
            }

            long endTime = System.currentTimeMillis();
            double tiempoRespuesta = (endTime - startTime) / 1000.0;
            System.out.println("Tiempo de respuesta total: " + tiempoRespuesta + " segundos");

            tiempoLabel.setText("Tiempo de respuesta: " + tiempoRespuesta + " segundos");
            tiempoLabel.setVisible(true);

            actualizarTabla(resultados);


            aceptarButton.setEnabled(true);

            iniciarContador();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al conectar con el servidor.");
        }
    }

    private void actualizarMapa() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String mensaje = "mapping";
            out.println(mensaje);
            String respuesta = in.toString();
            System.out.println("Respuesta: "+respuesta);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al conectar con el servidor.");
        }
    }

    private void enviarConfirmacion() {
        try {
            out.println("yes");
            System.out.println("Confirmación enviada al servidor: yes");

            // Leer respuesta fina
            String respuestaFinal = in.readLine();
            if (respuestaFinal != null) {
                System.out.println("Respuesta final del servidor: " + respuestaFinal);
                ArrayList<String> resultadosFinal = new ArrayList<>();
                resultadosFinal.add(respuestaFinal);
                actualizarTabla(resultadosFinal);
            }

            // Deshabilitar el botón "Aceptar" para evitar reenvíos accidentales
            aceptarButton.setEnabled(false);

            // Cerrar conexión
            cerrarConexion();

            // Detener el contador de tiempo
            if (contadorTiempo != null) {
                contadorTiempo.stop();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al enviar la confirmación.");
        }
    }

    private void cancelarOperacion() {
        cerrarConexion();
        limpiarResultados();
        tiempoLabel.setVisible(false);
        if (contadorTiempo != null) {
            contadorTiempo.stop();
        }
    }

    private void iniciarContador() {
        tiempoRestante = 15;
        tiempoLabel.setText("Tiempo restante: " + tiempoRestante + " segundos");
        tiempoLabel.setVisible(true);

        contadorTiempo = new Timer(1000, e -> {
            tiempoRestante--;
            tiempoLabel.setText("Tiempo restante: " + tiempoRestante + " segundos");

            if (tiempoRestante <= 0) {
                ((Timer) e.getSource()).stop();
                tiempoLabel.setText("Tiempo de confirmación agotado.");
                cerrarConexion();
            }
        });
        contadorTiempo.start();
    }

    private void cerrarConexion() {
        try {
            out.println("no");
            if (in != null) in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void limpiarResultados() {
        DefaultTableModel model = (DefaultTableModel) resultadosTable.getModel();
        model.setRowCount(0);
    }

    private void actualizarTabla(ArrayList<String> datos) {
        DefaultTableModel model = (DefaultTableModel) resultadosTable.getModel();
        model.setRowCount(0);
        for (String dato : datos) {
            model.addRow(new Object[]{dato});
        }
    }
}
