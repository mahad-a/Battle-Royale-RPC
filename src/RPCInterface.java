/**
 * Interface for remote procedure calls
 */
public interface RPCInterface {
    /**
     * Reusable method to handle Remote Procedure Call (RPC) communication
     * in a single step instead of manually writing send() and receive()
     * @param request the request from client or server
     * @return the response
     */
    String rpc_send(String request);
}
