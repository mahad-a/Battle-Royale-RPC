import java.io.*;
import java.net.*;
import java.util.*;

public class Client implements RPCInterface {
    private DatagramPacket sendPacket, receivePacket;
    private DatagramSocket sendReceiveSocket;
    private static final String hostAcknowledgment = "Host: Acknowledged request from Client. Replying";

    /**
     * Client constructor for the client application
     */
    public Client(){
        try {
            sendReceiveSocket = new DatagramSocket(); // start up the socket
        } catch (SocketException e) {
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

        try {
            sendReceiveSocket.receive(receivePacket);
            String acknowledgement = new String(receivePacket.getData(), 0, receivePacket.getLength());
            return acknowledgement.equals(hostAcknowledgment);
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
        System.out.println("Commands: MOVE dx dy | PICKUP lootId | STATE | QUIT");
        System.out.println("Enter your command: ");
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
        // switching to rpc_send
        String received = rpc_send(playerName);
        
        // parse the message for the player id assigned by server
        String[] m = received.split(":");
        int playerId = Integer.parseInt(m[1]);
        System.out.println("Joined game with playerId = " + playerId);
        return playerId; // store player id
    }

    /**
     * Begin client application and establish connection with intermediate host
     */
    public void startClient(){
        byte[] data = new byte[1024]; // size of the message
        int playerId = enrollPlayer(); // enroll player into the game

        while (true){ // infinite loop until user enters 'quit'
            String command = playerCommand(playerId);
            // switching to rpc
            String request = rpc_send(command);

            // close socket and end process if user wanted to quit
            if (Objects.equals(command, "QUIT")) {
                sendReceiveSocket.close();
                System.exit(0);
            } // quit request will have been sent to host and server, to which handle their own shutdown
        }

    }

    /**
     * Main method
     * @param args args
     */
    public static void main(String[] args) {
        System.out.println("Client started...");
        Client c = new Client(); // make a client instance and start it
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
            byte[] outData = request.getBytes();
            sendPacket = new DatagramPacket(outData, outData.length,
                    InetAddress.getLocalHost(), 5000);

            sendReceiveSocket.send(sendPacket);
            String clientSent = new String(sendPacket.getData(),0,sendPacket.getLength());

            System.out.println("\nClient: sent:" +
                    "\nTo host: " + sendPacket.getAddress() +
                    "\nTo host port: " + sendPacket.getPort() +
                    "\nLength: " + sendPacket.getLength() +
                    "\nContaining: " + clientSent);

            // check if acknowledgment is received
            if (!isAcknowledged()) { // not received
                System.out.println("Error: Unable to receive acknowledgment from Host. Exiting.");
                System.exit(1);
            } // received acknowledgment, prepare to receive request


            byte[] inData = new byte[1024];
            receivePacket = new DatagramPacket(inData, inData.length);
            sendReceiveSocket.receive(receivePacket);

            // showcase what was received from host
            String clientReceived = new String(receivePacket.getData(),0,receivePacket.getLength());
            System.out.println("\nClient: received:" +
                    "\nFrom host: " + receivePacket.getAddress() +
                    "\nFrom host port: " + receivePacket.getPort() +
                    "\nLength: " + receivePacket.getLength() +
                    "\nContaining: " + clientReceived);

            return clientReceived;
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return null;
    }
}
