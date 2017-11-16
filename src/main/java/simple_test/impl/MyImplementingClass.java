package simple_test.impl;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;

/**
 * @author Dan Graur 11/16/2017
 */
public class MyImplementingClass extends UnicastRemoteObject implements MyRemoteInterface {

    public MyImplementingClass() throws RemoteException {
    }

    public MyImplementingClass(int port) throws RemoteException {
        super(port);
    }

    public MyImplementingClass(int port, RMIClientSocketFactory csf, RMIServerSocketFactory ssf) throws RemoteException {
        super(port, csf, ssf);
    }

    @Override
    public int addNumbers(int a, int b) throws RemoteException {
        return a + b;
    }
}
