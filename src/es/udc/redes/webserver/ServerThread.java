package es.udc.redes.webserver;

import java.net.*;
import java.io.*;


public class ServerThread extends Thread {

    private Socket socket;

    public ServerThread(Socket s) {
        // Store the socket s
        this.socket = s;
    }

    public void run() {
        try {
            // This code processes HTTP requests and generates
            // HTTP responses

            // Set the input channel
            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));

            // Set the output channel
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            // Receive the message from the client
            String message = reader.readLine();
            String[] msgArray = message.split(" ");
            String path = msgArray[1];



            // Close the streams
            input.close();
            output.close();
        } catch (SocketTimeoutException e) {
           System.err.println("Nothing received in 300 secs");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            // Close the client socket
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
