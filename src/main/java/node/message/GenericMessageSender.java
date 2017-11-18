package node.message;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Dan Graur 11/18/2017
 */
public interface GenericMessageSender extends Remote {

    void sendMessage(Message message) throws RemoteException;

}
