package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    int port = 5555;
    private List<ClientThread> clients;

    public List<ClientThread> getClients(){
        return clients;
    }

    public void startServer(){
        clients = new ArrayList<ClientThread>();
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("SERVER ON\nPORT:"+port);
            System.out.println("SERVER > Waiting for connections...");

            while (true) {
                try {
                	int num = 0;
                    Socket socket = serverSocket.accept();
                    System.out.println("SERVER > New connection: " + socket.getRemoteSocketAddress());
                    ClientThread client = new ClientThread(this, socket,String.valueOf(num));
                    num++;
                    Thread thread = new Thread(client);
                    thread.start();
                    clients.add(client);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("SERVER > Accept failed");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
