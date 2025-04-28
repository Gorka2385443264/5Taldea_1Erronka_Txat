package Server;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ClientThread implements Runnable {
    private static final String MESSAGES_FILE = "messages.json";
    private Lock lock = new ReentrantLock(); // Lock para manejar acceso concurrente al archivo JSON
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
            // Cargar mensajes previos cuando un cliente se conecta
            cargarMensajes();

            while (!socket.isClosed()) {
                String message = in.readLine();  // Leer mensaje en texto plano
                if (message != null && !message.trim().isEmpty()) {
                    System.out.println(message);

                    // Guardar el mensaje en el archivo JSON
                    guardarMensaje(message);

                    // Enviar el mensaje a todos los clientes conectados
                    synchronized(clientThreads) {
                        for (ClientThread client : clientThreads) {
                            if (client != this) {
                                client.sendMessage(message);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
            synchronized(clientThreads) {
                clientThreads.remove(this);
            }
        }
    }

    private void closeConnection() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
            System.out.println("Conexion cerrada...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        out.println(message);  // Enviar mensaje en texto plano
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

                    while (true) {
                        String client = line.split("\"client\":\"")[1].split("\",")[0];
                        String message = "";

                        if (message.contains(">")) {
                            // Es un archivo
                            String[] fileParts = message.split(">", 2);
                            if (fileParts.length == 2) {
                                String fileName = fileParts[0];
                                String fileContent = fileParts[1];
                                out.println(client + " > " + fileName + " > " + fileContent);
                            } else {
                                System.err.println("Formato de archivo incorrecto en el historial.");
                            }
                        } else {
                            // Es un mensaje normal
                            out.println(client + " > " + message);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Método para guardar el mensaje en el archivo JSON
    private synchronized void guardarMensaje(String message) {
        String[] parts = message.split(">", 2);
        String izena = parts[0];
        String mensajeRestante = parts[1];

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
                jsonContent.deleteCharAt(jsonContent.length() - 1); // Quita el último ']'
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(jsonContent.toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

}
