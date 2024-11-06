import GUI.SeatingLayout;
import GUI.PaymentPlugin;
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
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClienteSocketGUI extends JFrame {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 7878;
    static final String PLUGIN_FOLDER="src/Plugins";
    private MatrixSeats mapaPrincipal = new MatrixSeats();
    private Map<String,Class> clasesCargadas;
    private Map<String,Class> clasesInstanciables;

    private Socket socket;
    private OutputStream out;
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
    private ArrayList<ArrayList<String[]>> options = new ArrayList<>();
    private int opcionAnterior = -1;

    private static Map cargarPlugins(){
        Map<String, Class> classList = new HashMap<>();
        //LinkedList<> classList = new LinkedList();
        File pluginFolder=new File(PLUGIN_FOLDER);
        if(!pluginFolder.exists())
        {
            if(pluginFolder.mkdirs())
            {
                System.out.println("Created plugin folder");
            }
        }
        File[] files=pluginFolder.listFiles((dir, name) -> name.endsWith(".jar"));
        ArrayList<URL> urls=new ArrayList<>();
        ArrayList<String> classes=new ArrayList<>();
        if(files!=null) {
            Arrays.stream(files).forEach(file -> {
                try {
                    JarFile jarFile=new JarFile(file);
                    urls.add(new URL("jar:file:"+PLUGIN_FOLDER+"/"+file.getName()+"!/"));
                    jarFile.stream().forEach(jarEntry -> {
                        if(jarEntry.getName().endsWith(".class"))
                        {
                            classes.add(jarEntry.getName());
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            URLClassLoader pluginLoader=new URLClassLoader(urls.toArray(new URL[urls.size()]));

            classes.forEach(s -> {
                try {
                    StringBuilder s1 = new StringBuilder();
                    s1.append(s);
                    s1.reverse();
                    String name = s1.substring(0,s1.indexOf("/"));
                    s1 = new StringBuilder();
                    name = s1.append(name).substring(s1.indexOf(".")+1,s1.length());
                    s1 = new StringBuilder();
                    name = s1.append(name).reverse().toString();
                    Class classs=pluginLoader.loadClass(s.replaceAll("/",".").replace(".class",""));
                    classList.put(name,classs);
                }   catch (ClassNotFoundException ex) {
                    Logger.getLogger(ClienteSocketGUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        }
        return classList;
    }

    public static Map cargarClasesInstanciables(Map listaClases){
        Map<String, Class> clasesInstanciables = new HashMap<String,Class>();
        //LinkedList clasesInstanciables = new LinkedList();
        listaClases.forEach((c,v) -> {
            Class[] interfaces=((Class)v).getInterfaces();
            for (Class anInterface : interfaces) {
                if(anInterface==PaymentPlugin.class){
                    clasesInstanciables.put(((String)c),((Class)v));
                }
            }

        });
        return clasesInstanciables;
    }

    public ClienteSocketGUI() throws IOException {
        this.clasesCargadas = cargarPlugins();
        this.clasesInstanciables = cargarClasesInstanciables(clasesCargadas);
        System.out.println(clasesCargadas);
        System.out.println(clasesInstanciables);
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
                    if(options != null){
                        ArrayList<String[]> selectedList = options.get(selectedRow);
                        String[][] selectedOption = selectedList.toArray(new String[selectedList.size()][]);
                        changueOption(selectedOption);
                        opcionAnterior = selectedRow;
                    }
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

        buscarButton.addActionListener(e -> {
            try {
                enviarSolicitud();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        aceptarButton.addActionListener(e -> enviarConfirmacion());
        cancelarButton.addActionListener(e -> {
            try {
                cancelarEspacios();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

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

    private void actualizarMapaPrincipal(MatrixSeats data) {
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
            mapaPrincipal = data;
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

    private void changueOption(String[][] option) {
        if(opcionAnterior!=-1){
            ArrayList<String[]> selectedList = options.get(opcionAnterior);
            String[][] selectedOption = selectedList.toArray(new String[selectedList.size()][]);
            mapaPrincipal.deleteOption(selectedOption);
        }
        try {
            mapaPrincipal.updateMatrixServer(option);

            // Actualiza SeatingLayout después de actualizar el mapa
            seatingLayout.updateLayout(mapaPrincipal); // Método que deberás implementar en SeatingLayout
            seatingLayout.revalidate();
            seatingLayout.repaint();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al conectar con el servidor.");
        }
    }

    private void enviarSolicitud() throws IOException {
        String host = "127.0.0.1";
        int puerto = 7878;

             socket = new Socket(host, puerto);
             out = socket.getOutputStream();
             DataInputStream in = new DataInputStream(socket.getInputStream());

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
            actualizarMapaPrincipal();


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
                options.clear();
                for (int i = 0; i < datos.length(); i++) {
                    JSONArray combinacion = datos.getJSONArray(i);
                    StringBuilder combinacionString = new StringBuilder("Combinación " + (i + 1) + ":\n");
                    ArrayList<String[]> option = new ArrayList<>();

                    for (int j = 0; j < combinacion.length(); j++) {
                        JSONObject asiento = combinacion.getJSONObject(j);
                        String[] site = new String[4];
                        site[0] = categoriaComboBox.getSelectedItem().toString();
                        site[1] = "Selected";
                        site[2] = String.valueOf(asiento.getInt("row_index")+1);
                        site[3] = String.valueOf(asiento.getInt("site_index"));
                        option.add(site);
                        combinacionString.append("  Fila: ").append(asiento.getInt("row_index"))
                                .append(", Asiento: ").append(asiento.getInt("site_index"))
                                .append("\n");
                    }
                    options.add(option);
                    listaCombinaciones.add(combinacionString.toString()); // Añadir la combinación como texto a la lista
                }



                // Actualizar la tabla con las combinaciones
                actualizarTabla(listaCombinaciones);

                iniciarContador();

            } catch (Exception e) {
                System.out.println("Error: La respuesta del servidor no es un JSON válido.");
                System.out.println("Respuesta recibida: " + respuesta);
            }

    }

    private void enviarConfirmacion() {
        try {
            int selectedRow = resultadosTable.getSelectedRow();
            if (selectedRow != -1) {
                String data = "" + selectedRow;
                out.write(data.getBytes("UTF-8"));
                out.flush();
                System.out.println("Confirmación enviada al servidor (puntero): " + selectedRow);

                // Deshabilitar el botón "Aceptar" para evitar reenvíos accidentales
                aceptarButton.setEnabled(false);
                // Detener el contador de tiempo
                if (contadorTiempo != null) {
                    contadorTiempo.stop();
                }
                tiempoLabel.setVisible(false);
                limpiarResultados();
                actualizarMapaPrincipal(new MatrixSeats());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al enviar la confirmación.");
            actualizarMapaPrincipal();
        }
    }

    private void cancelarEspacios() throws IOException {
        out.write("-1".getBytes("UTF-8"));
        out.flush();
        limpiarResultados();
        tiempoLabel.setVisible(false);

        if (contadorTiempo != null) {
            contadorTiempo.stop();
        }

        actualizarMapaPrincipal(new MatrixSeats());
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
                try {
                    cancelarEspacios();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                actualizarMapaPrincipal(new MatrixSeats());
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

    public static String[][] combinarMatrices(String[][] matriz1, String[][] matriz2) {
        int filasMatriz1 = matriz1.length;
        int filasMatriz2 = matriz2.length;

        // Crear un nuevo arreglo con la suma de las filas de ambos arreglos
        String[][] combinado = new String[filasMatriz1 + filasMatriz2][];

        // Copiar los elementos de matriz1 al nuevo arreglo
        for (int i = 0; i < filasMatriz1; i++) {
            combinado[i] = matriz1[i];
        }

        // Copiar los elementos de matriz2 al nuevo arreglo, empezando desde donde terminó matriz1
        for (int i = 0; i < filasMatriz2; i++) {
            combinado[filasMatriz1 + i] = matriz2[i];
        }

        return combinado;
    }
}