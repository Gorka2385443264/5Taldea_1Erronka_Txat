package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.net.Socket;
import java.nio.file.Files;

public class ClientThread implements Runnable {

    private Socket socket;
    private Server server;
    private String clientName;

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public ClientThread(Server server, Socket socket, String name) {
        this.server = server;
        this.socket = socket;
        this.clientName = name;
    }

    @Override
    public void run() {
        try {
            DataInputStream in = new DataInputStream(socket.getInputStream());
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            out.writeUTF("HI FROM SERVER");
            //cargarMensajes(out);

            while (!socket.isClosed()) {
                try {
                    if (in.available() > 0) {
                        String input = in.readUTF();
                        System.out.println(input);
                        String message = (this.getClientName()+ " > " + input);
                        System.out.println(message);
                        // Guardar el mensaje en un archivo JSON
                        //guardarMensaje(message);

                        // Enviar el mensaje a todos los clientes conectados
                        for (ClientThread thatClient : server.getClients()) {
                            DataOutputStream outputParticularClient = new DataOutputStream(thatClient.getSocket().getOutputStream());
                            outputParticularClient.writeUTF(message);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void cargarMensajes(DataOutputStream out) {
        try {
            File file = new File("messages.json");

            // Verificar si el archivo existe y tiene contenido
            if (file.exists()) {
                String content = new String(Files.readAllBytes(file.toPath()));

                // Eliminar los corchetes y dividir en objetos JSON individuales
                String[] messages = content.substring(1, content.length() - 1).split("(?<=\\}),");

                for (String msg : messages) {
                    // Parsear el mensaje manualmente
                    String client = msg.split("\"client\":\"")[1].split("\",")[0];
                    String message = msg.split("\"message\":\"")[1].split("\"")[0];

                    // Enviar el mensaje al cliente
                    out.writeUTF(client + " > " + message);
                }
            } else {
                out.writeUTF("No hay mensajes previos en el historial.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void guardarMensaje(String message) {
        try {
            // Archivo donde se guardarán los mensajes
            File file = new File("messages.json");

            StringBuilder jsonContent = new StringBuilder("[");
            if (file.exists()) {
                // Leer el contenido existente si el archivo ya existe
                String content = new String(Files.readAllBytes(file.toPath()));
                jsonContent = new StringBuilder(content.substring(0, content.length() - 1)); // Quitar el último ']'
            }

            // Crear el nuevo mensaje en formato JSON
            String newMessage = String.format(
                "{\"client\":\"%s\",\"message\":\"%s\"}",
                this.getClientName(), message
            );

            // Agregar el nuevo mensaje
            jsonContent.append(jsonContent.length() > 1 ? "," : "").append(newMessage).append("]");

            // Escribir el contenido actualizado en el archivo
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(jsonContent.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
