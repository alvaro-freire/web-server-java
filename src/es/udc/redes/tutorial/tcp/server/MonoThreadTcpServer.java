package es.udc.redes.tutorial.tcp.server;

import java.net.*;
import java.io.*;

/**
 * MonoThread TCP echo server.
 */
public class MonoThreadTcpServer {

    public static void main(String argv[]) {
        if (argv.length != 1) {
            System.err.println("Format: es.udc.redes.tutorial.tcp.server.MonoThreadTcpServer <port>");
            System.exit(-1);
        }

        ServerSocket serverSocket = null;

        try {
            // Create a server socket
            serverSocket = new ServerSocket(Integer.parseInt(argv[0]));

            // Set a timeout of 300 secs
            serverSocket.setSoTimeout(300000);

            while (true) {
                // Wait for connections
                Socket socket = serverSocket.accept();

                // Set the input channel
                InputStream input = socket.getInputStream();

                // Set the output channel
                OutputStream output = socket.getOutputStream();

                // Receive the client message
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                String message = reader.readLine();
                System.out.println("SERVER: Received " + message
                        + " from " + socket.getInetAddress().toString()
                        + ":" + socket.getPort());

                // Send response to the client
                PrintWriter writer = new PrintWriter(output, true);
                writer.println(message);
                System.out.println("SERVER: Sending " + message +
                        " to " + socket.getInetAddress().toString() +
                        ":" + socket.getPort());

                // Close the streams
                input.close();
                output.close();
            }
            // Uncomment next catch clause after implementing the logic
        } catch (SocketTimeoutException e) {
            System.err.println("Nothing received in 300 secs ");
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
