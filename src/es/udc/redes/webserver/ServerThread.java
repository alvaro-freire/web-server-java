package es.udc.redes.webserver;

import java.net.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;


public class ServerThread extends Thread {

    private Socket socket;

    public ServerThread(Socket s) {
        // Store the socket s
        this.socket = s;
    }

    public void headRequest(String request, PrintWriter writer){

    }

    public void getRequest(String request, PrintWriter writer) {
        String[] msgArray = request.split(" ");
        String path = msgArray[1];
        String httpVersion = msgArray[2];

        try {
            String contenido = new String(Files.readAllBytes(Paths.get
                    ("/mnt/z/GEI UDC/Q4/Redes/PRACTICAS/java-labs-alvaro-freire/p1-files/index.html")));
            writer.println(httpVersion + " 200 OK\n" +
                    "Date: Sat, 1 Jan 2000 12:00:15 GMT\n" +
                    "Server: Apache/1.3.0 (Unix)\n" +
                    "Last-Modified: Fri, 24 Dic 1999 13:03:32 GMT\n" +
                    "Content-Length: 6821\n" +
                    "Content-Type: text/html\n\n" +
                    contenido);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void processRequest(String request, PrintWriter writer) {
        String[] msgArray = request.split(" ");
        String command = msgArray[0];
        String path = msgArray[1];
        String httpVersion = msgArray[2];

        switch (command) {
            case "GET":
                getRequest(request, writer);
                break;
            case "HEAD":
                headRequest(request, writer);
                break;
            default:
                System.out.println("Nothing");
        }
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

            processRequest(message, writer);

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
