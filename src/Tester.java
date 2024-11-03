import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import org.json.JSONArray;
import org.json.JSONObject;

public class Tester {

    public static void consultarServidor(String comando) {
        String host = "127.0.0.1";
        int puerto = 7878;

        try (Socket socket = new Socket(host, puerto);
             OutputStream out = socket.getOutputStream();
             DataInputStream in = new DataInputStream(socket.getInputStream())) {

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
                System.out.println("Datos recibidos (parseados):");
                System.out.println(datos.toString());

                if (datos.isEmpty()) {
                    System.out.println("No se encontraron asientos disponibles.");
                    return;
                }

                System.out.println("Asientos disponibles:");
                for (int i = 0; i < datos.length(); i++) {
                    JSONArray combinacion = datos.getJSONArray(i);
                    System.out.println("Combinación " + i + ":");
                    for (int j = 0; j < combinacion.length(); j++) {
                        JSONObject asiento = combinacion.getJSONObject(j);
                        System.out.println("  Fila: " + asiento.getInt("row_index") +
                                ", Asiento: " + asiento.getInt("site_index"));
                    }
                }

                // Solicitar confirmación del usuario
                System.out.println("\nIntroduce un número para seleccionar la combinación deseada (0, 1, o 2), o -1 para cancelar la compra:");
                String confirmacion = new java.util.Scanner(System.in).nextLine();

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

        } catch (IOException e) {
            System.out.println("Se produjo un error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println("\nBúsqueda específica de asientos (ejemplo):");
        consultarServidor("2/A1");
    }
}
