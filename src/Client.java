import java.io.*;
import java.net.*;
import java.util.*;

public class Client implements RPCInterface {
    private DatagramPacket sendPacket, receivePacket;
    private DatagramSocket sendReceiveSocket;
    private InetAddress serverAddr;
    private int serverPort;
    private static final String hostAcknowledgment = "[Host] Acknowledgment";

    /**
     * Client constructor for the client application
     */
    public Client(String host, int port){
        try {
            sendReceiveSocket = new DatagramSocket(); // start up the socket
            serverAddr = InetAddress.getByName(host);
            serverPort = port;
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Prompts user to enter a name for their player
     * @return user's desired name
     */
    public String promptUsername(){
        Scanner s = new Scanner(System.in);
        System.out.println("Enter your player name: ");
        return s.nextLine();
    }

    /**
     * Check if the Host acknowledged the Client's request
     * @return if host acknowledged or not
     */
    private boolean isAcknowledged() {
        byte[] data = new byte[1024];
        receivePacket = new DatagramPacket(data, data.length);

        // try to get the acknowledgment message from the host
        try {
            sendReceiveSocket.receive(receivePacket);
            String acknowledgement = new String(receivePacket.getData(), 0, receivePacket.getLength());
            return acknowledgement.equals(hostAcknowledgment); // return boolean
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Prompt the player for a command
     * @param playerId the player's id
     * @return the player's command
     */
    private String playerCommand(int playerId){
        // listen for user's commands in terminal
        Scanner s = new Scanner(System.in);
        System.out.println("\nCommands: MOVE dx dy | PICKUP lootId | STATE | QUIT");
        System.out.println("\nEnter your command: ");
        String command = s.nextLine().toUpperCase(); // convert to upper case to be processed properly

        // parse the command for specific instructions
        String[] processCommand = command.split(" ");
        if (Objects.equals(processCommand[0], "MOVE")){
            command = String.format("%s:%d:%s:%s",  processCommand[0], playerId, processCommand[1], processCommand[2]);
        } else if (Objects.equals(processCommand[0], "PICKUP")) {
            command = String.format("%s:%d:%s", processCommand[0], playerId, processCommand[1]);
        } // 'state' and 'quit' do not require player id
        return command;
    }


    /**
     * Enroll the player into the game
     * @return the id of the newly enrolled player
     */
    public int enrollPlayer(){
        String playerName = "JOIN:" + promptUsername(); // user inputs their desired username
        // send the enrollment request to server
        String received = rpc_send(playerName);
        
        // parse the message for the player id assigned by server
        String[] m = received.split(":");
        int playerId = Integer.parseInt(m[1]);
        System.out.println("\nJoined game with playerId = " + playerId);
        return playerId; // store player id
    }

    /**
     * Begin client application and establish connection with intermediate host
     */
    public void startClient(){
        int playerId = enrollPlayer(); // enroll player into the game

        while (true){ // infinite loop until user enters 'quit'
            String command = playerCommand(playerId);
            // close socket and end process if user wanted to quit
            if (Objects.equals(command, "QUIT")) {
                System.out.println(command + "\n" + "Client closed.");
                sendReceiveSocket.close();
                System.exit(0);
            } // quit request will have been sent to host and server, to which handle their own shutdown

            // send the request to the server
            String request = rpc_send(command);

            // what the server responded with
            System.out.println("Server says: " + request);
        }

    }

    /**
     * Main method
     * @param args args
     */
    public static void main(String[] args) {
        System.out.println("Client started. Socket on random port.");
        Client c = new Client("localhost", 5000); // make a client instance and start it
        c.startClient();
    }

    /**
     * Reusable method to handle Remote Procedure Call (RPC) communication
     * in a single step instead of manually writing send() and receive()
     *
     * @param request the request from client or server
     * @return the response
     */
    @Override
    public String rpc_send(String request) {
        try {
            // prepare the send packet to send to the client
            byte[] outData = request.getBytes();
            sendPacket = new DatagramPacket(outData, outData.length,
                    serverAddr, serverPort);

            sendReceiveSocket.send(sendPacket);
            String clientSent = new String(sendPacket.getData(),0,sendPacket.getLength());

            System.out.println("[Client -> Host] Sent request: " + clientSent);

            // check if acknowledgment is received
            if (!isAcknowledged()) { // not received
                System.out.println("Error: Unable to receive acknowledgment from Host. Exiting.");
                System.exit(1);
            } // received acknowledgment, prepare to receive request
            System.out.println("[Client <- Host] Got reply: ACCEPT(" + clientSent + ")");

            // prepare the packet to receive the response from the server to prior request
            byte[] inData = new byte[100];
            receivePacket = new DatagramPacket(inData, inData.length);
            sendReceiveSocket.receive(receivePacket);

            // showcase what was received from host
            String clientReceived = new String(receivePacket.getData(),0,receivePacket.getLength());
            System.out.println("[Server -> Host -> Client] Got reply: " + clientReceived);

            return clientReceived;
        } catch (IOException e) {
            e.printStackTrace();
            return "ERROR: Client I/O Exception";
        }
    }
}
