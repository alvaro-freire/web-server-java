package es.udc.redes.webserver;

import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

public class ServerThread extends Thread {

    private Socket socket;

    public ServerThread(Socket s) {
        // Store the socket s
        this.socket = s;
    }

    public String getServerTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.UK);
        dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));
        return "Date: " + dateFormat.format(calendar.getTime()) + "\n";
    }

    public String getLastModified(File file) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.UK);
        dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Madrid"));
        return "Date: " + dateFormat.format(file.lastModified()) + "\n";
    }

    public String getContentLength(String path) throws IOException {
        return "Content-Length: " + Files.size(Paths.get(path)) + "\n";
    }

    public String getContentType(String path) throws IOException {
        return "Content-Type: " + Files.probeContentType(Paths.get(path)) + "\n";
    }

    public String findResource(String dir, String resource) {
        File path = new File(dir);

        File[] list = path.listFiles();

        if (list != null) {
            for (File file : list) {
                if (resource.equalsIgnoreCase(File.separator + file.getName())) {
                    return file.getName();
                }
                if (resource.equalsIgnoreCase(file.getName())) {
                    return file.getName();
                }
            }
        }

        return null;
    }

    public String buildHeaders(String dir, String resource, String file, String httpVersion) throws IOException {
        String path;
        String status;
        boolean error;
        if (file != null) {
            path = dir + resource;
            status = "200 OK\n";
            error = false;
        } else {
            if (Objects.equals(httpVersion, "HTTP/1.0") || Objects.equals(httpVersion, "HTTP/1.1")) {
                path = dir + "error404.html";
                status = "404 Not Found\n";
            } else {
                path = dir + "error400.html";
                status = "400 Bad Request\n";
            }
            error = true;
        }

        String header = httpVersion + " " + status + getServerTime() + "Server: Web_Server268\n"
                + getContentLength(path) + getContentType(path);

        if (error) {
            return header + "Connection: close\n\n";
        } else {
            return header + getLastModified(new File(path)) + "\n";
        }
    }

    public void badRequest(OutputStream writer) throws IOException {
        String file = "p1-files" + File.separator + "error400.html";

        String header = "HTTP/1.0 400 Bad Request\n" + getServerTime() + "Server: Web_Server268\n"
                + "Content-Type: text/html\n" + getContentLength(file) + "Connection: close\n\n";
        String html = new String(Files.readAllBytes(Paths.get(file)));

        writer.write((header + html).getBytes());
    }

    public void processRequest(String request, OutputStream writer) throws IOException {
        String[] requestArray = request.split("\n");
        String[] requestLine = requestArray[0].split(" ");
        String directory = "p1-files" + File.separator;

        if (requestLine.length != 3) {
            badRequest(writer);
            return;
        }

        String method = requestLine[0];      // GET or HEAD
        String path = requestLine[1];
        if (Objects.equals(path, "/")) {
            path = "index.html";
        }
        String httpVersion = requestLine[2];
        String file = findResource(directory, path);

        switch (method) {
            case "GET" -> {
                writer.write((buildHeaders(directory, path, file, httpVersion)).getBytes());
                if (file == null) {
                    if (Objects.equals(httpVersion, "HTTP/1.0")) {
                        writer.write(Files.readAllBytes(Paths.get(directory + "error404.html")));
                    } else {
                        writer.write(Files.readAllBytes(Paths.get(directory + "error400.html")));
                    }
                } else {
                    writer.write(Files.readAllBytes(Paths.get(directory + file)));
                }
            }
            case "HEAD" -> writer.write((buildHeaders(directory, path, file, httpVersion)).getBytes());
            default -> badRequest(writer); // Status Code 501 - Not Implemented
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

            // Receive the message from the client
            StringBuilder request = new StringBuilder();
            String s;
            while ((s = reader.readLine()) != null && !s.equals("")) {
                request.append(s).append("\n");
            }

            processRequest(request.toString(), output);

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
