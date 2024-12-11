package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientThread implements Runnable {

    private Socket socket;
    private Server server; // Si renombraste la clase, cámbialo a "Server"
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

    public ClientThread(Server server, Socket socket) { // Ajusta si renombraste a "Server"
        this.server = server;
        this.socket = socket;
    }

    @Override
    public void run() {
        try (DataInputStream in = new DataInputStream(socket.getInputStream());
             DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

            out.writeUTF("HI FROM SERVER");
            System.out.println("SERVER > Sent greeting to client");

            while (!socket.isClosed()) {
                try {
                    if (in.available() > 0) {
                        String input = in.readUTF();
                        System.out.println("SERVER > Received: " + input);

                        // Broadcast message to all clients
                        for (ClientThread thatClient : server.getClients()) {
                            if (thatClient != this) { // Opcional: no enviar al cliente que lo envió
                                DataOutputStream clientOut = new DataOutputStream(thatClient.getSocket().getOutputStream());
                                clientOut.writeUTF(input);
                            }
                        }
                    }
                } catch (IOException e) {
                    System.out.println("SERVER > Error in client communication");
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("SERVER > Client disconnected: " + socket.getRemoteSocketAddress());
        } finally {
            try {
                server.getClients().remove(this);
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
                System.out.println("SERVER > Connection closed for client: " + socket.getRemoteSocketAddress());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
