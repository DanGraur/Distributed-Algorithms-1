package node;

import node.message.GenericMessageSender;
import node.message.Message;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * @author Dan Graur 11/18/2017
 */
public class Node implements GenericMessageSender, Runnable {
    /**
     * This node's PID
     */
    private long pid;

    /**
     * The scalar clock held in this node
     */
    private long sClock;

    /**
     * This queue will hold the incoming messages. It will be ordered based on the scalar clock and the pid of this process
     */
    private Queue<Message> inQueue;

    /**
     * The queue which will hosld the 'received messages' which have not been delivered yet to the other nodes
     */
    private Queue<Message> sentQueue;

    /**
     * Will hold the communication channels to the other peers
     */
    private Map<String, GenericMessageSender> peers;

    public Node(long pid, Map<String, GenericMessageSender> peers) {
        this.pid = pid;
        this.sClock = 0;
        this.inQueue = new PriorityBlockingQueue<>();
        this.sentQueue = new PriorityQueue<>();
        this.peers = peers;
    }

    public Node(long pid) {
        this.pid = pid;
        this.sClock = 0;
        this.inQueue = new PriorityBlockingQueue<>();
        this.sentQueue = new PriorityQueue<>();
    }

    public void setPeers(Map<String, GenericMessageSender> peers) {
        this.peers = peers;
    }

    /**
     * This is actually the receive message, but it's name will be left as this
     *
     * @param message the message being received
     * @throws RemoteException
     */
    @Override
    public synchronized void sendMessage(Message message) throws RemoteException {
        sClock = Math.max(sClock, message.getsClock());

        /* Not that it matters since this is synchronized, but the queue should be thread-safe */
        inQueue.add(message);
    }

    /**
     * Send a message to all other peers (including itself)
     *
     * @param message the message being sent
     * @throws RemoteException
     */
    public void sendMessageToEveryone(Message message) throws RemoteException {

        for (GenericMessageSender peer : peers.values()) {
            
            peer.sendMessage(message);
            
        }
        
    }


    @Override
    public void run() {

        System.out.println("I'm in the run method");

        while(true) {

            if (pid == 1)
                try {
//                    System.out.println(peers.containsKey("0"));
//                    System.out.println(peers.get("0").toString());

                    /*peers.get("0")
                            .sendMessage(
                            new Message(
                                pid, sClock++, false, "This is a non-ack message"
                            )
                    );*/

                    Message msg = new Message(
                            pid, sClock++, false, "This is a non-ack message"
                    );

                    sendMessageToEveryone(msg);

                    System.out.println("Have sent message");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            else {
                Message message = inQueue.poll();
                System.out.println("Have received a message");

                if (message != null)
                    System.out.println(message.toString());
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    public String toString() {
        return "Node{" +
                "pid=" + pid +
                ", sClock=" + sClock +
                ", inQueue=" + inQueue +
                ", sentQueue=" + sentQueue +
                ", peers=" + peers +
                '}';
    }
}
