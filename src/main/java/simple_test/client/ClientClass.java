package simple_test.client;

import simple_test.impl.MyRemoteInterface;

import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * @author Dan Graur 11/16/2017
 */
public class ClientClass {
    private final static String PATH = "rmi://localhost:5555/add_class";


    public static void main(String[] args) throws Exception {

        MyRemoteInterface remoteObj = (MyRemoteInterface) LocateRegistry.getRegistry(5555).lookup(PATH);

        System.out.println("The result of addition is: " + remoteObj.addNumbers(1, 2));



    }
}
