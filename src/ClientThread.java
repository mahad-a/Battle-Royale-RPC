public class ClientThread implements Runnable {
    private Host host;

    /**
     * Constructor for the client thread
     * @param host the intermediate host that owns the thread
     */
    public ClientThread(Host host){
        this.host = host;
    }

    /**
     * Runs this operation.
     */
    @Override
    public void run() {
        while (true){
            // get the message from the client
            String clientMessage = host.receiveFromClient();
            if (!clientMessage.isEmpty() && !clientMessage.equals("REQUEST_DATA")) {
                // ensure the message is not empty and is not the server request for data
                if (!clientMessage.equals(Host.clientAcknowledgment)) { // ensure message is not the acknowledgment
                    // send the message to the server
                    host.sendToServer(clientMessage);
                    System.out.println("[Host -> Server] Forwarded request to server: " + clientMessage);
                }
            }
        }
    }
}
