package Server;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ClientThread implements Runnable {
    private static final String MESSAGES_FILE = "messages.json";
    private Socket socket;
    private List<ClientThread> clientThreads;
    private BufferedReader in;
    private PrintWriter out;
    private Lock lock = new ReentrantLock(); // Lock para manejar acceso concurrente al archivo JSON

    public ClientThread(Socket socket, List<ClientThread> clientThreads) {
        this.socket = socket;
        this.clientThreads = clientThreads;
        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            // Cargar mensajes previos cuando un cliente se conecta
        	String izena = "USUARIO";
            cargarMensajes();

            while (!socket.isClosed()) {
                String message = in.readLine();  // Leer mensaje en texto plano
                if (message != null && !message.trim().isEmpty()) {
                    System.out.println("SERVER > Received from "+izena+": " + message);

                    // Guardar el mensaje en el archivo JSON
                    guardarMensaje(message, izena);

                    // Enviar el mensaje a todos los clientes conectados
                    for (ClientThread client : clientThreads) {
                        if (client != this) {
                            client.sendMessage(message, izena);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    public void sendMessage(String message, String izena) {
        out.println(izena+ ">" + message);  // Enviar mensaje en texto plano
    }

    private void closeConnection() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para cargar los mensajes previos desde el archivo JSON
    private synchronized void cargarMensajes() {
        try {
            File file = new File(MESSAGES_FILE);

            if (!file.exists() || file.length() == 0) {
                out.println("No hay mensajes previos en el historial.");
                return;
            }

            // Leer el contenido del archivo de manera eficiente
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }

                // Procesar el JSON (eliminar corchetes y procesar los mensajes)
                String contentString = content.toString();
                if (contentString.length() > 2) {
                    // Quitar los corchetes y dividir los mensajes
                    String[] messages = contentString.substring(1, contentString.length() - 1).split("(?<=\\}),");

                    for (String msg : messages) {
                        // Parsear manualmente el cliente y el mensaje
                        String client = msg.split("\"client\":\"")[1].split("\",")[0];
                        String message = msg.split("\"message\":\"")[1].split("\"")[0];

                        // Enviar el mensaje al cliente
                        out.println(client + " > " + message);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para guardar el mensaje en el archivo JSON
    private synchronized void guardarMensaje(String message, String izena) {
        lock.lock();  // Asegurar acceso exclusivo al archivo JSON
        try {
            File file = new File(MESSAGES_FILE);
            StringBuilder jsonContent = new StringBuilder();

            if (file.exists() && file.length() > 0) {
                // Leer el contenido existente del archivo de manera eficiente
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        jsonContent.append(line);
                    }
                }

                // Eliminar el corchete final y preparar para agregar el nuevo mensaje
                jsonContent.deleteCharAt(jsonContent.length() - 1); // Eliminar el "]"
                jsonContent.append(",");
            } else {
                // Si el archivo no existe, comenzar con un corchete de apertura
                jsonContent.append("[");
            }

            // Agregar el nuevo mensaje en formato JSON
            jsonContent.append(String.format("{\"client\":\"%s\",\"message\":\"%s\"}", izena, message));
            jsonContent.append("]");

            // Escribir el contenido actualizado en el archivo
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(jsonContent.toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();  // Liberar el lock después de acceder al archivo
        }
    }
}
