import java.io.*;
import java.net.*;
import java.util.Arrays;

public class Host {
    private DatagramPacket clientSendPacket, clientReceivePacket, serverSendPacket, serverReceivePacket;
    private DatagramSocket clientSocket, serverSocket;
    private InetAddress clientAddress, serverAddress;
    private int clientPort, serverPort;
    public static final String clientAcknowledgment = "Host: Acknowledged request from Client. Replying";

    /**
     * Host constructor to act as an intermediate host between client and server
     */
    public Host() {
        try{ // create datagram sockets to communicate with client and server using UDP
            clientSocket = new DatagramSocket(5000); // specific port for client
            serverSocket = new DatagramSocket(); // specify port when sending to server
        } catch (SocketException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Sends an acknowledgment to the client
     */
    public void acknowledgeClient() {
        try { // send acknowledgment to client
            byte[] ackBytes = clientAcknowledgment.getBytes();
            DatagramPacket ackPacket = new DatagramPacket(ackBytes, ackBytes.length,
                    clientAddress, clientPort);
            clientSocket.send(ackPacket);

            System.out.println("[Host -> Client] Sent immediate ACCEPT to " + clientAddress);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Sends a datagram packet to the Client using UDP
     * @param hostReceivedServer the message to send to Client
     */
    public void sendToClient(String hostReceivedServer) {
        try { // send the processed command to the client on a new datagram packet using UDP
            // use the origin port of the packet received from client earlier to directly contact the client
            clientSendPacket = new DatagramPacket(hostReceivedServer.getBytes(), hostReceivedServer.getBytes().length,
                    clientAddress, clientPort);
            clientSocket.send(clientSendPacket);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // showcase what was sent to client
        String hostForwardClient = new String(clientSendPacket.getData(),0,clientSendPacket.getLength());
        System.out.println("[Host -> Client] Forwarded server response to " + clientAddress + ": " + hostForwardClient);
    }

    /**
     * Receive a datagram packet from the Client using UDP and extract the message
     * @return the message from the client
     */
    public String receiveFromClient() {
        byte[] data = new byte[1024];  // size of the message
        clientReceivePacket = new DatagramPacket(data, data.length);

        try { // receive initial command from client
            while (true) {
                clientSocket.receive(clientReceivePacket);

                // save the client address and port
                clientAddress = clientReceivePacket.getAddress();
                clientPort = clientReceivePacket.getPort();

                // ensure the packet is valid and not corrupted
                if (clientAddress == null || clientPort == 0) {
                    System.out.println("ERROR: Received a packet but address/port is missing!");
                    return null;  // stop the process of receiving
                }

                // showcase what was received from client
                String hostReceivedClient = new String(data,0,clientReceivePacket.getLength());
                System.out.println("\n[Host] Got from client: " + hostReceivedClient + " (from " + clientAddress + ")");

                acknowledgeClient(); // send acknowledgment to the client that message is received

                return hostReceivedClient;
            }
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

       return null;
    }

    /**
     * Sends a datagram packet to the Server using UDP
     * @param hostReceivedClient the message to send to the Server
     */
    public void sendToServer(String hostReceivedClient) {
        try { // send client's command to server on a new datagram packet using UDP
            serverSendPacket = new DatagramPacket(hostReceivedClient.getBytes(), hostReceivedClient.getBytes().length,
                    InetAddress.getLocalHost(), 6000); // port 6000 is server's specific port
            serverSocket.send(serverSendPacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // showcase what was sent/forwarded to server
        String hostForwardServer = new String(serverSendPacket.getData(),0,serverSendPacket.getLength());

    }

    /**
     * Receive a datagram packet from the Server using UDP and extract the message
     * @return the message from the server
     */
    public String receiveFromServer() {
        byte[] data = new byte[1024];  // size of the message
        serverReceivePacket = new DatagramPacket(data, data.length);

        try { // receive the processed command from server
            serverSocket.receive(serverReceivePacket);
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // save the server address and port
        serverAddress = serverReceivePacket.getAddress();
        serverPort = serverReceivePacket.getPort();

        String hostReceiveServer = new String(data,0, serverReceivePacket.getLength());

        // showcase what was received from server
        System.out.println("\n[Host] Got from server: " + hostReceiveServer + " (from " + serverAddress + ")");

        return hostReceiveServer;
    }

    /**
     * Starts the host and creates two threads to simulate two way communication
     */
    public void startHost() {
        // thread from client to server
        Thread clientThread = new Thread(new ClientThread(this));
        // thread from server to client
        Thread serverThread = new Thread(new ServerThread(this));

        // start threads
        clientThread.start();
        serverThread.start();
    }

    /**
     * Main method
     * @param args args
     */
    public static void main(String[] args) {
        System.out.println("Battle Royale Host started on port 5000");
        Host host = new Host();
        host.startHost();
    }
}
