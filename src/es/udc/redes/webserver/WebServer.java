package es.udc.redes.webserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class WebServer {
    
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Format: es.udc.redes.webserver.WebServer <port>");
            System.exit(-1);
        }

        ServerSocket serverSocket = null;

        try {
            // Create a server socket
            serverSocket = new ServerSocket(Integer.parseInt(args[0]));

            // Set a timeout of 300 secs
            serverSocket.setSoTimeout(300000);

            while (true) {
                // Wait for connections
                Socket socket = serverSocket.accept();

                // Create a ServerThread object, with the new connection as parameter
                ServerThread serverThread = new ServerThread(socket);

                // Initiate thread using the start() method
                serverThread.start();
            }
            // Uncomment next catch clause after implementing the logic
        } catch (SocketTimeoutException e) {
            System.err.println("Nothing received in 300 secs");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            //Close the socket
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
}
