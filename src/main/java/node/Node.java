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

    @Override
    public synchronized void sendMessage(Message message) throws RemoteException {
        sClock = Math.max(sClock, message.getsClock());

        /* Not that it matters since this is synchronized, but the queue should be thread-safe */
        inQueue.add(message);
    }

    @Override
    public void run() {

    }
}
