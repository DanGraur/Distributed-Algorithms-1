package simple_test.server;

import simple_test.impl.MyImplementingClass;
import simple_test.impl.MyRemoteInterface;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * @author Dan Graur 11/16/2017
 */
public class ServerClass {

    private final static String PATH = "rmi://localhost:5555/add_class";

    public static void main(String[] args) throws Exception {
        if (System.getSecurityManager() == null) {
            System.setProperty("java.security.policy","src/main/resources/generic.policy");

            System.setSecurityManager(new SecurityManager());
        }

        Registry registry = LocateRegistry.createRegistry(5555);

        MyRemoteInterface implementingClass = new MyImplementingClass();

        registry.bind(PATH, implementingClass);
    }
}
