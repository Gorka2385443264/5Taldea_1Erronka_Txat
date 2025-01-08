package Server;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;

public class ClientThread implements Runnable {
	String MESSAGES_FILE = "messages.json";
    private Socket socket;
    private List<ClientThread> clientThreads;
    private BufferedReader in;
    private PrintWriter out;

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
            // Comunicación del cliente
            while (!socket.isClosed()) {
                String message = in.readLine();  // Leer mensaje en texto plano
                //guardarMensaje("Usuario",message);
                if (message != null) {
                    System.out.println("SERVER > Received: " + message);

                    // Enviar el mensaje a todos los clientes conectados
                    for (ClientThread client : clientThreads) {
                        if (client != this) {
                            client.sendMessage(message);
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

    public void sendMessage(String message) {
        out.println(message);  // Enviar mensaje en texto plano
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

    public Socket getSocket() {
        return this.socket;
    }

    private synchronized void cargarMensajes(DataOutputStream out) {
        try {
            File file = new File(MESSAGES_FILE);

            if (!file.exists() || file.length() == 0) {
                out.writeUTF("No hay mensajes previos en el historial.");
                return;
            }

            // Leer el contenido del archivo
            String content = new String(Files.readAllBytes(file.toPath()));

            // Eliminar los corchetes y dividir los mensajes
            String[] messages = content.substring(1, content.length() - 1).split("(?<=\\}),");

            for (String msg : messages) {
                // Parsear manualmente el cliente y el mensaje
                String client = msg.split("\"client\":\"")[1].split("\",")[0];
                String message = msg.split("\"message\":\"")[1].split("\"")[0];

                // Enviar el mensaje al cliente
                out.writeUTF(client + " > " + message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void guardarMensaje(String clientName, String message) {
        try {
            File file = new File(MESSAGES_FILE);
            StringBuilder jsonContent = new StringBuilder();

            if (file.exists() && file.length() > 0) {
                // Leer el contenido existente del archivo
                String content = new String(Files.readAllBytes(file.toPath()));

                // Eliminar el corchete final
                jsonContent.append(content.substring(0, content.length() - 1));
                if (jsonContent.length() > 1) {
                    jsonContent.append(",");
                }
            } else {
                // Si el archivo no existe, comenzar con un corchete de apertura
                jsonContent.append("[");
            }

            // Agregar el nuevo mensaje en formato JSON
            jsonContent.append(String.format("{\"client\":\"%s\",\"message\":\"%s\"}", clientName, message));
            jsonContent.append("]");

            // Escribir el contenido actualizado en el archivo
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(jsonContent.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
