package cliente;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class clients {
    private DataInputStream in;
    private DataOutputStream out;
    private Socket socket;

    private String serverHost = "localhost";
    private int portNumber = 5555;
    private String clientName;

    public clients(String clientName, String serverHost, int portNumber) {
        this.clientName = clientName;
        this.serverHost = serverHost;
        this.portNumber = portNumber;
    }

    public void createClient() {
        while (true) {
            try {
                connectToServer();
                break; // Salir del bucle si la conexión es exitosa
            } catch (IOException e) {
                System.out.println("CLIENT > Unable to connect to server. Retrying in 5 seconds...");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void connectToServer() throws IOException {
        // Conectar al servidor
        socket = new Socket(serverHost, portNumber);
        System.out.println("CLIENT > Connected to server on port " + portNumber);

        // Configurar streams de entrada y salida
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());

        // Enviar el nombre del cliente al servidor
        sendMessage("CLIENT_NAME:" + clientName);

        // Hilo para escuchar mensajes del servidor
        new Thread(this::listenToServer).start();

        // Hilo para enviar mensajes al servidor
        new Thread(this::sendMessages).start();
    }

    private void listenToServer() {
        try {
            while (!socket.isClosed()) {
                String input = in.readUTF();
                System.out.println("SERVER > " + input);
            }
        } catch (IOException e) {
            System.out.println("CLIENT > Connection lost");
            closeConnection();
        }
    }

    private void sendMessages() {
        try (Scanner scanner = new Scanner(System.in)) {
            while (!socket.isClosed()) {
                String message = scanner.nextLine();
                sendMessage(message);
            }
        }
    }

    public void sendMessage(String message) {
        try {
            if (out != null) {
                out.writeUTF(message);
                out.flush();
                System.out.println("CLIENT > Sent: " + message);
            }
        } catch (IOException e) {
            System.out.println("CLIENT > Error sending message");
        }
    }

    private void closeConnection() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
            System.out.println("CLIENT > Connection closed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
