public class ServerThread implements Runnable{
    private Host host;

    /**
     * Constructor for server thread
     * @param host the intermediate host
     */
    public ServerThread(Host host){
        this.host = host;
    }

    /**
     * Runs this operation.
     */
    @Override
    public void run() {
        while (true) {
            // get message from server
            String serverMessage = host.receiveFromServer();
            if (serverMessage.equals("REQUEST_DATA")){ // ensure server asked for data first
                String clientMessage = host.receiveFromClient(); // get message from client
                if (!clientMessage.equals("REQUEST_DATA")) {
                    // don't send back the request data message to server
                    host.sendToServer(clientMessage);
                    System.out.println("\n[Host -> Server] Forwarded request to server: " + clientMessage);

                    // get the server's response then send to the client
                    String serverResponse = host.receiveFromServer();
                    host.sendToClient(serverResponse);
                }
            } else { // a regular message otherwise not a server request
                host.sendToClient(serverMessage);
            }
        }
    }
}
