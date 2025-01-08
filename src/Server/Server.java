package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    int portNumber = 5555;
    private Socket socket;
    private List<ClientThread> clientThreads = new ArrayList<>();

    public List<ClientThread> getClients() {
        return clientThreads;
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("SERVER > Listening on port " + portNumber);

            while (true) {
                // Acepta la conexión de un cliente
                socket = serverSocket.accept();
                System.out.println("SERVER > New client connected: " + socket.getInetAddress());

                // Crear un hilo para manejar la comunicación con este cliente
                ClientThread clientThread = new ClientThread(socket, clientThreads);
                clientThreads.add(clientThread);
                new Thread(clientThread).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        server.startServer();
    }
}
