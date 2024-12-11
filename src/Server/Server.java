package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
	int port = 8080;
	private List<ClientThread> clients = new CopyOnWriteArrayList<>();

	public List<ClientThread> getClients(){
	    return clients;
	}
    public void startServer(){
        clients = new ArrayList<ClientThread>();
        ServerSocket serverSocket = null;
        try {
        	serverSocket = new ServerSocket(port);
            System.out.println("SERVER ON");
            System.out.println("SERVER > Waiting for connections...");


//		            ACCEPT ALL CONNECTIONS
            while (true){
                try {
                    Socket socket = serverSocket.accept();
                    System.out.println("SERVER > New connection: " + socket.getRemoteSocketAddress());
                    ClientThread client = new ClientThread(this, socket);
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
        }finally {
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }
}
