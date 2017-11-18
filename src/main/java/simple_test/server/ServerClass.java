package simple_test.server;

import simple_test.impl.MyImplementingClass;
import simple_test.impl.MyRemoteInterface;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * @author Dan Graur 11/16/2017
 */
public class ServerClass {

    private final static String PATH = "add_class";

    public static void main(String[] args) throws Exception {
//        System.setProperty("java.rmi.server.hostname", "80.112.133.96");
        System.setProperty("java.security.policy","file:src/main/resources/generic.policy");
        System.setSecurityManager(new SecurityManager());

        Registry registry = LocateRegistry.createRegistry(1099);

        /*
        MyRemoteInterface baseObject = new MyImplementingClass();
        MyRemoteInterface baseStub = (MyRemoteInterface) UnicastRemoteObject.exportObject(baseObject, 5555);

        registry.rebind(PATH, baseStub);

        for (String s : registry.list()) {
            System.out.println(s);
        }
        */

    }
}
