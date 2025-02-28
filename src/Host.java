import java.io.*;
import java.net.*;

public class Host {
    private DatagramPacket clientSendPacket, clientReceivePacket, serverSendPacket, serverReceivePacket;
    private DatagramSocket clientSocket, serverSocket;

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

    public void startHost(){
        byte[] data = new byte[1024];  // size of the message

        // UDP datagram packets to be created to receive from both client and server
        clientReceivePacket = new DatagramPacket(data, data.length);
        serverReceivePacket = new DatagramPacket(data, data.length);

        while (true){
            try { // receive initial command from client
                clientSocket.receive(clientReceivePacket);
            } catch(IOException e) {
                e.printStackTrace();
                System.exit(1);
            }

            // showcase what was received from client
            String hostReceivedClient = new String(data,0,clientReceivePacket.getLength());
            System.out.println("\nHost: received:" +
                    "\nFrom client: " + clientReceivePacket.getAddress() +
                    "\nFrom client port: " + clientReceivePacket.getPort() +
                    "\nLength: " + clientReceivePacket.getLength() +
                    "\nContaining: " + hostReceivedClient);

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
            System.out.println("\nHost: forwarded:" +
                    "\nTo server: " + serverSendPacket.getAddress() +
                    "\nTo server port: " + serverSendPacket.getPort() +
                    "\nLength: " + serverSendPacket.getLength() +
                    "\nContaining: " + hostForwardServer);

            // close datagram sockets and terminal process if client requested to quit
            if (hostReceivedClient.equals("QUIT") && hostForwardServer.equals("QUIT")){
                clientSocket.close();
                serverSocket.close();
                System.exit(0);
            }


            try { // receive the processed command from server
                serverSocket.receive(serverReceivePacket);
            } catch(IOException e) {
                e.printStackTrace();
                System.exit(1);
            }

            // showcase what was received from server
            String hostReceivedServer = new String(data,0,serverReceivePacket.getLength());

            System.out.println("\nHost: received:" +
                    "\nFrom server: " + serverReceivePacket.getAddress() +
                    "\nFrom server port: " + serverReceivePacket.getPort() +
                    "\nLength: " + serverReceivePacket.getLength() +
                    "\nContaining: " + hostReceivedServer);


            try { // send the processed command to the client on a new datagram packet using UDP
                // use the origin port of the packet received from client earlier to directly contact the client
                clientSendPacket = new DatagramPacket(hostReceivedServer.getBytes(), hostReceivedServer.getBytes().length,
                        clientReceivePacket.getAddress(), clientReceivePacket.getPort());
                clientSocket.send(clientSendPacket);
            } catch (UnknownHostException e) {
                e.printStackTrace();
                System.exit(1);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // showcase what was sent to client
            String hostForwardClient = new String(clientSendPacket.getData(),0,clientSendPacket.getLength());

            System.out.println("\nHost: forwarded:" +
                    "\nTo client: " + clientSendPacket.getAddress() +
                    "\nTo client port: " + clientSendPacket.getPort() +
                    "\nLength: " + clientSendPacket.getLength() +
                    "\nContaining: " + hostForwardClient);

        }
    }

    /**
     * Main method
     * @param args args
     */
    public static void main(String[] args) {
        Host host = new Host();
        host.startHost();
    }
}
