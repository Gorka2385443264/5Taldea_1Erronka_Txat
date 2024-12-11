package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


public class clients {
	private DataInputStream in;
    private DataOutputStream out;
    private Socket socket;

	public int portNumber = 8080;

	public void createClient() {
        try {
            // Conectarse al servidor
            socket = new Socket("localhost", portNumber);
            System.out.println("CLIENT > Connected to server on port " + portNumber);

            // Configurar streams de entrada y salida
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            // Hilo para escuchar mensajes del servidor
            new Thread(() -> {
                while (!socket.isClosed()) {
                    try {
                        if (in.available() > 0) {
                            String input = in.readUTF();
                            System.out.println("SERVER > " + input);
                        }
                    } catch (IOException e) {
                        System.out.println("CLIENT > Connection lost");
                        closeConnection();
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        try {
            if (out != null) {
                out.writeUTF(message);
                out.flush();
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
