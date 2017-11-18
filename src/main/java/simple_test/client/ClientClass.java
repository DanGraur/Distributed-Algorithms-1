package simple_test.client;

import simple_test.impl.MyRemoteInterface;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * @author Dan Graur 11/16/2017
 */
public class ClientClass {
    private final static String PATH = "add_class";

    private void setUpPolicy() {
        /*
         * If you start the program using the CL and the class files, you'll need to set this to "file:generic.policy";
         * else you'll need the full path to the ./src/....
         */
        System.setProperty("java.security.policy", "file:generic.policy");
        System.setSecurityManager(new SecurityManager());
    }

    public void doJob() throws RemoteException, NotBoundException {
        setUpPolicy();
        Registry registry = LocateRegistry.getRegistry("192.168.0.101", 1099);

        for (String s : registry.list()) {
            System.out.println("HERE: " + s);
        }

        MyRemoteInterface remoteObj = (MyRemoteInterface) registry.lookup(PATH);

        System.out.println("The result of addition is: " + remoteObj.addNumbers(1, 2));
    }


    public static void main(String[] args) throws Exception {

        new ClientClass().doJob();

    }
}
