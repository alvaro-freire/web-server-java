package es.udc.redes.webserver;

import java.net.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;


public class ServerThread extends Thread {

    private Socket socket;

    public String error400 = "../p1-files/error400.html";
    public String error404 = "../p1-files/error404.html";

    public ServerThread(Socket s) {
        // Store the socket s
        this.socket = s;
    }

    public String getServerTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.UK);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(calendar.getTime()) + "\n";
    }

    public String getLastModified(File file) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.UK);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(file.lastModified()) + "\n";
    }

    public String getContentLength(String path) throws IOException {
        return "Content-Length: " + Files.size(Paths.get(path)) + "\n";
    }

    public String getContentType(String path) throws IOException {
        return "Content-Type: " + Files.probeContentType(Paths.get(path)) + "\n\n";
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

    public String buildHeaders(String dir, String resource) throws IOException {
        String path = dir + resource;

        if (findResource(dir, resource) == null) {
            return "HTTP/1.0 404 Not Found\n" + "Date: " + getServerTime() + "Server: Web_Server268\n" +
                    getLastModified(new File(error404)) + getContentLength(error404) +
                    getContentType(error404);
        }

        return "HTTP/1.0 200 OK\n" + "Date: " + getServerTime() + "Server: Web_Server268\n" +
                getLastModified(new File(path)) + getContentLength(path) +
                getContentType(path);
    }

    public void getAndHead(String[] request, PrintWriter writer) throws IOException {
        String[] requestLine = request[0].split(" ");
        String directory = "../p1-files/";
        String method = requestLine[0]; // GET or HEAD
        String resource = requestLine[1];

        String file = findResource(directory, resource);

        writer.println(buildHeaders(directory, resource));

        if (file == null) {
            writer.println(new String(Files.readAllBytes(Paths.get(error404))));
            return;
        }
        if (Objects.equals(method, "GET")) {
            writer.println(new String(Files.readAllBytes(Paths.get(directory + file))));
        }
    }

    public void processRequest(String request, PrintWriter writer) throws IOException {
        String[] requestArray = request.split("\n");
        String[] requestLine = requestArray[0].split(" ");
        String method = requestLine[0];      // GET or HEAD

        switch (method) {
            case "GET", "HEAD" -> getAndHead(requestArray, writer);
            default -> writer.println(new String(Files.readAllBytes(Paths.get(error400))));
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
            StringBuilder request = new StringBuilder();
            String s;
            while ((s = reader.readLine()) != null && !s.equals("")) {
                request.append(s).append("\n");
            }

            processRequest(request.toString(), writer);

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
