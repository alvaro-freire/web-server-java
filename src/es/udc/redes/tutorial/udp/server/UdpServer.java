package es.udc.redes.tutorial.udp.server;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Implements a UDP echo server.
 */
public class UdpServer {

    public static void main(String argv[]) {
        if (argv.length != 1) {
            System.err.println("Format: es.udc.redes.tutorial.udp.server.UdpServer <port_number>");
            System.exit(-1);
        }

        DatagramSocket socket = null;
        byte[] buf = new byte[256];


        try {
            // Create a server socket
            socket = new DatagramSocket(Integer.parseInt(argv[0]));

            // Set maximum timeout to 300 secs
            socket.setSoTimeout(300000);

            while (true) {
                // Prepare datagram for reception
                DatagramPacket request = new DatagramPacket(buf, buf.length);

                // Receive the message
                socket.receive(request);

                System.out.println("SERVER: Received "
                        + new String(request.getData(), 0, request.getLength())
                        + " from " + request.getAddress().toString() + ":"
                        + request.getPort());

                // Prepare datagram to send response
                InetAddress clientAddress = request.getAddress();
                int clientPort = request.getPort();
                String data = new String(request.getData());
                buf = data.getBytes();

                // Send response
                DatagramPacket response = new DatagramPacket(buf, buf.length, clientAddress, clientPort);
                socket.send(response);

                System.out.println("SERVER: Sending "
                        + new String(response.getData(), 0, response.getLength())
                        + " from " + clientAddress.toString() + ":"
                        + clientPort);
            }

            // Uncomment next catch clause after implementing the logic
        } catch (SocketTimeoutException e) {
            System.err.println("No requests received in 300 secs ");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close the socket
            socket.close();
        }
    }
}
