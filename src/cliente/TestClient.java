package cliente;

import java.util.Scanner;

public class TestClient {
    public static void main(String[] args) {
    	Scanner sc = new Scanner(System.in);
        clients client1 = new clients();

        // Crear los clientes
        client1.createClient();

        // Enviar mensajes entre clientes
        while(true) {
        	//String mensaje = sc.next();
        	client1.sendMessage("kaixo");
        	break;
        }
    }
}

