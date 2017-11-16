package simple_test.impl;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Dan Graur 11/16/2017
 */
public interface MyRemoteInterface extends Remote {

    /**
     * Add the two numbers
     *
     * @param a
     * @param b
     * @return a + b
     */
    public int addNumbers(int a, int b) throws RemoteException;

}
