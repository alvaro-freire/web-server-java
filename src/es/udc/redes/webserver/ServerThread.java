package es.udc.redes.webserver;

import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ServerThread extends Thread {

    private Socket socket;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z");

    public ServerThread(Socket s) {
        // Store the socket s
        this.socket = s;
    }

    public String getServerTime() {
        return "Date: " + ZonedDateTime.now(ZoneId.of("CET")).format(formatter) + "\n";
    }

    public String getLastModified(File file) {
        ZonedDateTime date = ZonedDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.systemDefault());
        return "Last-Modified: " + date.format(formatter.withZone(ZoneId.of("CET"))) + "\n";
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

    public void error304(OutputStream writer, String httpVersion) throws IOException {
        String headers = httpVersion + " 304 Not Modified\n" + getServerTime() + "Server: Web_Server268\n\n";
        writer.write(headers.getBytes());
    }

    public void error(OutputStream writer, boolean get, int statusCode) throws IOException {
        String file = "error" + statusCode + ".html";
        String statusLine;

        if (statusCode == 400) {
            statusLine = "HTTP/1.0 400 Bad Request\n";
        } else {
            statusLine = "HTTP/1.0 404 Not Found\n";
        }

        String path = "p1-files" + File.separator + file;

        String header = statusLine + getServerTime() + "Server: Web_Server268\n"
                + "Content-Type: text/html\n" + getContentLength(path) + "Connection: close\n\n";

        String html = "";

        if (get) {
            html = new String(Files.readAllBytes(Paths.get(path)));
        }

        writer.write((header + html).getBytes());
    }

    public void processRequest(String request, OutputStream writer) throws IOException {
        String[] requestArray = request.split("\n");
        String[] requestLine = requestArray[0].split(" ");
        String directory = "p1-files" + File.separator;
        boolean modifiedSince = false;

        if (requestLine.length != 3) {
            error(writer, true, 400);
            return;
        }

        String ifModifiedSince;
        ZonedDateTime iMSDate = null;

        for (String line : requestArray) {
            if (Objects.equals(line.split(" ", 2)[0], "If-Modified-Since:")) {
                modifiedSince = true;
                ifModifiedSince = line.split(" ", 2)[1];
                iMSDate = ZonedDateTime.parse(ifModifiedSince, formatter);
                System.out.println(iMSDate.format(formatter));
                break;
            }
        }


        String method = requestLine[0];      // GET or HEAD
        String path = requestLine[1];
        if (Objects.equals(path, "/")) {
            path = "index.html";
        }
        String httpVersion = requestLine[2];
        String file = findResource(directory, path);

        if (!Objects.equals(httpVersion, "HTTP/1.0") && !Objects.equals(httpVersion, "HTTP/1.1")) {
            error(writer, method.equals("GET"), 400);
        }

        switch (method) {
            case "GET", "HEAD" -> {
                if (file == null) {
                    error(writer, method.equals("GET"), 404);
                    return;
                }
                if (modifiedSince) {
                    ZonedDateTime lastModifiedDate = ZonedDateTime.ofInstant(Instant.ofEpochMilli
                            (new File(directory + file).lastModified()), ZoneId.systemDefault());

                    if (iMSDate.isAfter(lastModifiedDate)) {
                        error304(writer, httpVersion);
                        return;
                    }
                }
                writer.write((buildHeaders(directory, path, file, httpVersion)).getBytes());
                if (method.equals("GET")) {
                    writer.write(Files.readAllBytes(Paths.get(directory + file)));
                    writer.write("\n".getBytes());
                }
            }
            default -> error(writer, true, 400);
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
