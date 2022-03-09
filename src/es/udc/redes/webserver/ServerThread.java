package es.udc.redes.webserver;

import java.net.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;


public class ServerThread extends Thread {

    private Socket socket;

    public ServerThread(Socket s) {
        // Store the socket s
        this.socket = s;
    }

    String getServerTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.UK);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(calendar.getTime()) + "\n";
    }

    String getLastModified(File file) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.UK);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(file.lastModified()) + "\n";
    }

    public String statusCode404() {
        return "HTTP/1.0 404 Not Found\n";
    }

    public String findResource(String dir, String resource) {
        File path = new File(dir);

        File[] list = path.listFiles();

        if (list != null) {
            for (File file : list) {
                if (resource.equalsIgnoreCase("/" + file.getName())) {
                    return file.getName();
                }
                if (resource.equalsIgnoreCase(file.getName())) {
                    return file.getName();
                }
            }
        }

        return null;
    }

    public void headRequest(String request, PrintWriter writer) {

    }

    public void getRequest(String request, PrintWriter writer) {
        String[] msgArray = request.split(" ");
        String directory = "/mnt/z/GEI UDC/Q4/Redes/PRACTICAS/java-labs-alvaro-freire/p1-files/";
        String resource = msgArray[1];
        String date = getServerTime();

        String file = findResource(directory, resource);

        if (file == null) {
            writer.println(statusCode404());
            return;
        }

        String statusLine = "HTTP/1.0 200 OK\n";
        String path = directory + file;
        String lastModified = getLastModified(new File(path));
        // String contentLength = ;
        // String contentType = ;

        try {
            String contenido = new String(Files.readAllBytes(Paths.get
                    (path)));
            writer.println(statusLine + date +
                    "Server: Hamburguer/1.0 (Unix)\n" +
                    lastModified +
                    "Content-Length: 6821\n" +
                    "Content-Type: text/html\n\n" +
                    contenido);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void processRequest(String request, PrintWriter writer) {
        String[] msgArray = request.split(" ");
        String method = msgArray[0];
        String path = msgArray[1];
        String httpVersion = msgArray[2];

        switch (method) {
            case "GET" -> getRequest(request, writer);
            case "HEAD" -> headRequest(request, writer);
            default -> writer.println("501 Not Implemented\n");
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
