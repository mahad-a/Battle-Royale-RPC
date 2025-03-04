public class ClientThread implements Runnable {
    private Host host;

    public ClientThread(Host host){
        this.host = host;
    }

    /**
     * Runs this operation.
     */
    @Override
    public void run() {
        while (true){
            String clientMessage = host.receiveFromClient();
            if (!clientMessage.isEmpty() && !clientMessage.equals("REQUEST_DATA")) {

                if (!clientMessage.equals(Host.clientAcknowledgment)) {
                    host.sendToServer(clientMessage);
                    System.out.println("[Host -> Server] Forwarded request to server: " + clientMessage);
                }
            }
        }
    }
}
