import java.io.*;
import java.net.*;

public class Server implements RPCInterface{
    private DatagramPacket sendPacket, receivePacket;
    private DatagramSocket sendReceiveSocket;
    private final GameState gameState = new GameState();

    /**
     * Server constructor for the server application
     */
    public Server(){
        try {
            sendReceiveSocket = new DatagramSocket(6000); // specific port for server
        } catch (SocketException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    /**
     * Begin server  application and establish connection with intermediate host
     */
    public void startServer(){
        byte[] data = new byte[1024]; // size of the message

        // prepare a datagram packet hold message that is to be received from host
        receivePacket = new DatagramPacket(data, data.length);

        while (true){ // infinite loop until receiving a 'quit' request
            try {
                // listen for datagram packet to be received from host
                sendReceiveSocket.receive(receivePacket);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // message received from host
            String serverReceived = new String(data,0,receivePacket.getLength());

            // showcase what was received from host
            System.out.println("\nServer: received:" +
                    "\nFrom host: " + receivePacket.getAddress() +
                    "\nFrom host port: " + receivePacket.getPort() +
                    "\nLength: " + receivePacket.getLength() +
                    "\nContaining: " + serverReceived);

            // process the request from client (sent via host)
            String response = processRequest(serverReceived);

            // close socket and terminal process if client requested to quit
            if (serverReceived.equals("QUIT")){
                sendReceiveSocket.close();
                System.exit(0);
            }

            // prepare datagram packet to be sent to host for client regarding processed request
            sendPacket = new DatagramPacket(response.getBytes(), response.getBytes().length, receivePacket.getAddress(), receivePacket.getPort());

            try { // send UDP datagram packet containing processed command to host
                sendReceiveSocket.send(sendPacket);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // showcase what was sent to host
            String serverSent = new String(sendPacket.getData(),0,sendPacket.getLength());

            System.out.println("\nServer: sent:" +
                    "\nTo host: " + sendPacket.getAddress() +
                    "\nTo host port: " + sendPacket.getPort() +
                    "\nLength: " + sendPacket.getLength() +
                    "\nContaining: " + serverSent);
        }
    }

    /**
     * Process request from client sent through the intermediate host
     * @param message the message containing the client's command
     * @return the processed command
     */
    public String processRequest(String message){
        String[] m = message.split(":"); // parse message for the specific command
        return switch (m[0]) {
            // for each case, return the result of its command being processed
            case "JOIN" -> {
                Player newPlayer = gameState.addNewPlayer(message.substring(5));
                yield "JOINED:" + newPlayer.getId();
            }
            case "MOVE" -> {
                gameState.movePlayer(Integer.parseInt(m[1]), Integer.parseInt(m[2]), Integer.parseInt(m[3]));
                yield "MOVE_OK";
            }
            case "PICKUP" -> {
                if (gameState.processPickup(Integer.parseInt(m[1]), Integer.parseInt(m[2]))) yield "PICKUP_OK";
                yield "PICKUP_FAIL";
            }
            case "STATE" -> gameState.serialize();
            // otherwise client inputted a command that doesn't exist
            default -> "NOT_A_COMMAND"; // INVALID_COMMAND also works here
        };
    }

    /**
     * Main method
     * @param args args
     */
    public static void main(String[] args) {
        System.out.println("Server starting...");
        Server server = new Server();
        server.startServer();
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
        return null;
    }
}
