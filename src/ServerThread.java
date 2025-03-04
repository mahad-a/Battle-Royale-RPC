public class ServerThread implements Runnable{
    private Host host;

    public ServerThread(Host host){
        this.host = host;
    }

    /**
     * Runs this operation.
     */
    @Override
    public void run() {
        while (true) {
            String serverMessage = host.receiveFromServer();
            if (serverMessage.equals("REQUEST_DATA")){
                String clientMessage = host.receiveFromClient();
                System.out.println();
                if (!clientMessage.equals("REQUEST_DATA")) {
                    host.sendToServer(clientMessage);
                    System.out.println("[Host -> Server] Forwarded request to server: " + clientMessage);

                    System.out.println();
                    String serverResponse = host.receiveFromServer();


                    host.sendToClient(serverResponse);

                }
            } else {
                host.sendToClient(serverMessage);
            }
        }
    }
}
