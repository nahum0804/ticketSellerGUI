import GUI.SeatingLayout;
import OBJECTS.MatrixSeats;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class ClienteSocketGUI extends JFrame {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 7878;
    private MatrixSeats mapaPrincipal = new MatrixSeats();

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

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
    private SeatingLayout seatingLayout;

    public ClienteSocketGUI() throws IOException {
        // Configuración de la interfaz
        setTitle("Solicitud de Asientos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Configuración de tamaño de ventana
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Panel de solicitud
        JPanel solicitudPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        solicitudPanel.setBorder(BorderFactory.createTitledBorder("Solicitud de Asientos"));

        solicitudPanel.add(new JLabel("Cantidad de asientos:"));
        cantidadTextField = new JTextField();
        solicitudPanel.add(cantidadTextField);

        solicitudPanel.add(new JLabel("Categoría:"));
        categoriaComboBox = new JComboBox<>(new String[]{"VIP", "A1", "A2", "B", "C"});
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

        resultadosTable.getSelectionModel().addListSelectionListener(e -> {
            // Solo continuamos si no hay ajustes en curso para evitar reentradas
            if (!e.getValueIsAdjusting()) {
                int selectedRow = resultadosTable.getSelectedRow();

                if (selectedRow != -1) {
                    // Hay una fila seleccionada
                    aceptarButton.setEnabled(true);
                    System.out.println("Índice de fila seleccionada: " + selectedRow);
                } else {
                    // No hay ninguna fila seleccionada
                    aceptarButton.setEnabled(false);
                }
            }
        });

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
        panelDerecho.add(solicitudPanel, BorderLayout.NORTH);
        panelDerecho.add(resultadoPanel, BorderLayout.CENTER);
        panelDerecho.add(botonesPanel, BorderLayout.SOUTH);

        seatingLayout = new SeatingLayout(mapaPrincipal);
        actualizarMapaPrincipal();
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, seatingLayout, panelDerecho);
        splitPane.setDividerLocation(600);

        setLayout(new BorderLayout());
        add(splitPane, BorderLayout.CENTER);
        add(tiempoLabel, BorderLayout.NORTH);

        buscarButton.addActionListener(e -> enviarSolicitud());
        aceptarButton.addActionListener(e -> enviarConfirmacion());
        cancelarButton.addActionListener(e -> cancelarEspacios());

    }

    private void actualizarMapaPrincipal() {
        try {
            Socket socket1 = new Socket(SERVER_HOST, SERVER_PORT);
            PrintWriter out1 = new PrintWriter(socket1.getOutputStream(), true);
            BufferedReader in1 = new BufferedReader(new InputStreamReader(socket1.getInputStream()));

            String mensaje = "mapping";
            out1.println(mensaje);
            String respuesta = in1.readLine();
            String[][] formateado = Arrays.stream(respuesta.split("\\|"))
                    .map(dato -> dato.split(","))
                    .toArray(String[][]::new);

            mapaPrincipal.updateMatrixServer(formateado);

            // Actualiza SeatingLayout después de actualizar el mapa
            seatingLayout.updateLayout(mapaPrincipal); // Método que deberás implementar en SeatingLayout
            seatingLayout.revalidate();
            seatingLayout.repaint();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al conectar con el servidor.");
        }
    }

    private void enviarSolicitud() {
        String host = "127.0.0.1";
        int puerto = 7878;

        try (Socket socket = new Socket(host, puerto);
             OutputStream out = socket.getOutputStream();
             DataInputStream in = new DataInputStream(socket.getInputStream())) {

            //Obtener comando de los textbox
            String comando = cantidadTextField.getText() + "/" + categoriaComboBox.getSelectedItem() ;
            // Enviar el comando al servidor usando writeBytes en lugar de writeUTF
            out.write(comando.getBytes("UTF-8"));
            out.flush();

            // Leer la respuesta del servidor
            StringBuilder respuesta = new StringBuilder();
            int length;
            byte[] buffer = new byte[1024];
            while ((length = in.read(buffer)) != -1) {
                respuesta.append(new String(buffer, 0, length));
                if (in.available() == 0) break; // salir si no hay más datos
            }

            System.out.println("Respuesta del servidor en bruto (JSON):");
            System.out.println(respuesta.toString());

            // Intentar parsear la respuesta como JSON
            try {
                JSONArray datos = new JSONArray(respuesta.toString());
                ArrayList<String> listaCombinaciones = new ArrayList<>();

                System.out.println("Datos recibidos (parseados):");
                System.out.println(datos.toString());

                if (datos.isEmpty()) {
                    System.out.println("No se encontraron asientos disponibles.");
                    return;
                }

                for (int i = 0; i < datos.length(); i++) {
                    JSONArray combinacion = datos.getJSONArray(i);
                    StringBuilder combinacionString = new StringBuilder("Combinación " + (i + 1) + ":\n");

                    for (int j = 0; j < combinacion.length(); j++) {
                        JSONObject asiento = combinacion.getJSONObject(j);
                        combinacionString.append("  Fila: ").append(asiento.getInt("row_index"))
                                .append(", Asiento: ").append(asiento.getInt("site_index"))
                                .append("\n");
                    }

                    listaCombinaciones.add(combinacionString.toString()); // Añadir la combinación como texto a la lista
                }

                // Actualizar la tabla con las combinaciones
                actualizarTabla(listaCombinaciones);

                iniciarContador();

                // Solicitar confirmación del usuario
                //System.out.println("\nIntroduce un número para seleccionar la combinación deseada (0, 1, o 2), o -1 para cancelar la compra:");
                //String confirmacion = new java.util.Scanner(System.in).nextLine();

                String confirmacion = "0";  // Confirmar la primera combinación por defecto
                // Enviar la confirmación (índice de combinación o -1)
                out.write(confirmacion.getBytes("UTF-8"));
                out.flush();

                // Leer la respuesta de confirmación del servidor
                respuesta.setLength(0);  // Limpiar el buffer de respuesta
                while ((length = in.read(buffer)) != -1) {
                    respuesta.append(new String(buffer, 0, length));
                    if (in.available() == 0) break;
                }

                System.out.println("Respuesta de confirmación del servidor:");
                System.out.println(respuesta.toString());

            } catch (Exception e) {
                System.out.println("Error: La respuesta del servidor no es un JSON válido.");
                System.out.println("Respuesta recibida: " + respuesta);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al conectar con el servidor.");
        }
    }

    private void enviarConfirmacion() {
        try {
            int selectedRow = resultadosTable.getSelectedRow();
            if (selectedRow == -1) {
                out.println(selectedRow);
                System.out.println("Confirmación enviada al servidor (puntero): " + selectedRow);

                // Deshabilitar el botón "Aceptar" para evitar reenvíos accidentales
                aceptarButton.setEnabled(false);
                // Detener el contador de tiempo
                if (contadorTiempo != null) {
                    contadorTiempo.stop();
                }
                tiempoLabel.setVisible(false);
                limpiarResultados();
                actualizarMapaPrincipal();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al enviar la confirmación.");
            actualizarMapaPrincipal();
        }
    }


    private void cancelarEspacios() {
        out.println(-1);
        limpiarResultados();
        tiempoLabel.setVisible(false);

        if (contadorTiempo != null) {
            contadorTiempo.stop();
        }

        actualizarMapaPrincipal();
    }

    private void iniciarContador() {
        tiempoRestante = 120;
        tiempoLabel.setText("Tiempo restante: " + tiempoRestante + " segundos");
        tiempoLabel.setVisible(true);

        contadorTiempo = new Timer(1000, e -> {
            tiempoRestante--;
            tiempoLabel.setText("Tiempo restante: " + tiempoRestante + " segundos");

            if (tiempoRestante <= 0) {
                ((Timer) e.getSource()).stop();
                tiempoLabel.setText("Tiempo de confirmación agotado.");
                cancelarEspacios();
            }
        });
        contadorTiempo.start();
    }

    private void limpiarResultados() {
        DefaultTableModel model = (DefaultTableModel) resultadosTable.getModel();
        while (model.getRowCount() > 0) {
            model.removeRow(0);
        }
    }

    private void actualizarTabla(ArrayList<String> datos) {
        DefaultTableModel model = (DefaultTableModel) resultadosTable.getModel();
        model.setRowCount(0);
        for (String dato : datos) {
            model.addRow(new Object[]{dato}); // Añadir cada combinación como una fila en la tabla
        }
    }
}