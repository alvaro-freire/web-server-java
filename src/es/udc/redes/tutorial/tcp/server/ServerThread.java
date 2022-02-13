package es.udc.redes.tutorial.tcp.server;

import java.net.*;
import java.io.*;

/**
 * Thread that processes an echo server connection.
 */

public class ServerThread extends Thread {

    private Socket socket;

    public ServerThread(Socket s) {
        // Store the socket s
        this.socket = s;
    }

    public void run() {
        try {
            // Set the input channel
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            // Set the output channel
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            // Receive the message from the client
            String message = reader.readLine();
            System.out.println("SERVER: Received " + message
                    + " from " + socket.getInetAddress().toString()
                    + ":" + socket.getPort());

            // Sent the echo message to the client
            writer.println(message);
            System.out.println("SERVER: Sending " + message +
                    " to " + socket.getInetAddress().toString() +
                    ":" + socket.getPort());

            // Close the streams
            input.close();
            output.close();

            // Uncomment next catch clause after implementing the logic
        } catch (SocketTimeoutException e) {
            System.err.println("Nothing received in 300 secs");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            // Close the socket
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
