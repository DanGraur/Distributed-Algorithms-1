import javafx.util.Pair;
import node.Node;
import node.message.GenericMessageSender;

import java.io.File;
import java.io.FileNotFoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

/**
 * @author Dan Graur 11/18/2017
 */
public class Initiator {

    /**
     * args[0] - Registry IP
     * args[1] - Registry Port
     * args[2] - PID (since Java 1.8 does not allow this directly);
     * args[3] - path to file containing the peer information
     * args[4] - line index in the peer file (starts at 0)
     * args[5] - line index in the peer file (starts at 0)
     *
     * @param args contains the args of the node
     */
    public static void main(String[] args) throws FileNotFoundException, RemoteException, InterruptedException {
        if (args.length != 6) {
            System.err.println("Usage: java Iniitator <Reg-IP> <Reg-Port> <Base-Name> <My-PID> <My-Name> <path-to-peer-config>");

            return;
        }

        /* Configure the security manager */
        System.setProperty("java.security.policy", "file:generic.policy");
        System.setSecurityManager(new SecurityManager());

        /* Config the node */
        Node theNode = new Node(Integer.parseInt(args[2]));

        Pair<String, Map<String, GenericMessageSender>> result = configPeers(
                            args[0],
                            Integer.parseInt(args[1]),
                            theNode,
                            args[4],
                            Integer.parseInt(args[5])
        );

        /* Get the name */
        theNode.setName(result.getKey());

        /* Get the peers */
        theNode.setPeers(result.getValue());

        System.out.println("Have managed to successfully create the node");

        /* Start the node */
        Thread nodeThread = new  Thread(theNode);
        nodeThread.start();

        /* Wait until the node is finished, otherwise the GC will cause problems */
        nodeThread.join();
    }

    /**
     * Publish the node's comm channel to the registry, and collect the other channels as they become available
     *
     * @param registryIP the IP of the registry
     * @param registryPort the port of the registry
     * @param node the node which is currently being instantiated
     * @param path the path to the peer configuration file
     * @param lineIndex the index in the configuration file corresponding to the node being created
     * @return a pair containing the name of the new node and a map with all of the created nodes
     * @throws FileNotFoundException is thrown when the config file could not be found
     * @throws RemoteException is thrown when the registry could not be found
     */
    private static Pair<String, Map<String, GenericMessageSender>> configPeers(String registryIP,
                                                                               int registryPort,
                                                                               Node node,
                                                                               String path,
                                                                               int lineIndex) throws FileNotFoundException, RemoteException {
        /* Some initializations */
        Scanner fileReader = new Scanner(new File(path));
        Map<String, GenericMessageSender> peers = new HashMap<>();
        List<String> tempNameList = new ArrayList<>();

        String nameOfNode = "";

        /* Get the registry */
        Registry registry = LocateRegistry.getRegistry(registryIP, registryPort);

        /* Read while there is a next line */
        for (int i = 0; fileReader.hasNextLine(); ++i) {
            /* Get the tokens, separated by ';'. Awaited structure: <name>;port */
            String[] tokens = fileReader.nextLine().split(";");
            String nameInRegistry =  tokens[0].trim();

            tempNameList.add(nameInRegistry);

            /* Add the current node (as a stub) to the registry */
            if (i == lineIndex) {
                int port = Integer.parseInt(tokens[1].trim());

                GenericMessageSender stub = (GenericMessageSender) UnicastRemoteObject.exportObject(node, port);
                registry.rebind(nameInRegistry, stub);

                nameOfNode = nameInRegistry;
            }
        }

        for (String s : tempNameList) {
            System.out.println("Name: " + s);
        }

        /* Build up the peer list */
        for (String regName : tempNameList) {
            boolean collected = false;

            while (!collected) {
                try {
                    GenericMessageSender extractedStub = (GenericMessageSender) registry.lookup(regName);

                    peers.put(regName, extractedStub);

                    collected = true;

                } catch (NotBoundException e) {
                    //System.err.println("Could not collect the stub under the name: " + regName + ". Will go to sleep for a bit.");

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }


        }

        /* Return the collected peers */
        return new Pair<>(nameOfNode, peers);
    }
}
