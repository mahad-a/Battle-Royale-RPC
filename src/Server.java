import java.io.*;
import java.net.*;

public class Server implements RPCInterface {
    private DatagramPacket sendPacket, receivePacket;
    private DatagramSocket sendReceiveSocket;
    private final GameState gameState = new GameState();
    private static final String requestHost = "REQUEST_DATA";

    /**
     * Server constructor for the server application
     */
    public Server(int port){
        try {
            sendReceiveSocket = new DatagramSocket(port); // specific port for server
        } catch (SocketException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    /**
     * Begin server application and establish connection with intermediate host
     */
    public void startServer(){

        while (true){ // infinite loop until receiving a 'quit' request
            String response = rpc_send(requestHost); // get the request from the host

            // close socket and terminal process if client requested to quit
            if (response.equals("QUIT")){
                sendReceiveSocket.close();
                System.exit(0);
            }

            rpc_send(response); // send the response back to the host
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
            case requestHost -> requestHost; // request for data, don't do anything
            default -> "NOT_A_COMMAND"; // INVALID_COMMAND also works here
        };
    }

    /**
     * Main method
     * @param args args
     */
    public static void main(String[] args) {
        System.out.println("Battle Royale Server started on port 6000");
        Server server = new Server(6000);
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
        try{
            // prepare receive packet and receive the message from the client
            byte[] inData = new byte[100];
            receivePacket = new DatagramPacket(inData, inData.length);

            sendReceiveSocket.receive(receivePacket);

            // save the values of the client address and port
            InetAddress clientAddr = receivePacket.getAddress();
            int clientPort = receivePacket.getPort();

            // get the message of the request from the host
            String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.println("\n[Server] Received: " + message + " from Host(" + clientAddr + ")");

            // process the request
            String response = processRequest(message);

            // send the response using the saved values for client address and client port
            byte[] outData = response.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(outData, outData.length,
                    clientAddr, clientPort);

            sendReceiveSocket.send(sendPacket);
            System.out.println("[Server] Sent response back to Host: " + response);
            return response;
        } catch (IOException e){
            e.printStackTrace();
            return "ERROR: Client I/O Exception";
        }
    }
}
