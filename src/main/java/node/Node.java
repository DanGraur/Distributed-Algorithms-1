package node;

import node.message.AckMessage;
import node.message.GenericMessageSender;
import node.message.Message;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * @author Dan Graur 11/18/2017
 */
public class Node implements GenericMessageSender, Runnable {
    /**
     * Unique name assigned to this node
     */
    private String name;

    /**
     * This node's PID
     */
    private long pid;

    /**
     * The scalar clock held in this node
     */
    private Long sClock;

    /**
     * This queue will hold the incoming messages. It will be ordered based on the scalar clock and the pid of this process
     */
    private Queue<Message> inQueue;

    /**
     * Map of sent messages, which maps from UUID to the message
     */
    private Map<String, Message> sentMessages;

    /**
     * Will hold the communication channels to the other peers
     */
    private Map<String, GenericMessageSender> peers;

    public Node(long pid, Map<String, GenericMessageSender> peers) {
        this.pid = pid;
        this.sClock = 0L;
        this.inQueue = new PriorityBlockingQueue<>();
        this.peers = peers;
        this.sentMessages = new HashMap<>();
    }

    public Node(long pid) {
        this.pid = pid;
        this.sClock = 0L;
        this.inQueue = new PriorityBlockingQueue<>();
        this.sentMessages = new HashMap<>();
    }

    public void setPeers(Map<String, GenericMessageSender> peers) {
        this.peers = peers;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * This is actually the receive message, but it's name will be left as this
     *
     * @param message the message being received
     * @throws RemoteException
     */
    @Override
    public synchronized void sendMessage(Message message) throws RemoteException {
        /* Update the scalar clock */
        sClock = Math.max(sClock, message.getsClock()) + 1;

        /* Check if this is an ack message */
        if (message.isAck()) {
            AckMessage ackMessage = (AckMessage) message;

            /* Check if this is a message for this node */
            if (ackMessage.getSourcePid() == pid) {
                Message msgInQuestion = sentMessages.get(ackMessage.getContents());

                /* Ack the message */
                msgInQuestion.addAck(ackMessage.getProcName());

                /* Check if this message has been ack'd by all */
                if (msgInQuestion.containsAll(peers.keySet()))
                    msgInQuestion.setCanRelease(true);
            }

        } else {
            /* Not that it matters since this is synchronized, but the queue should be thread-safe */
            inQueue.add(message);

            /* Send response */
            sendMessageToEveryone(
                    new AckMessage(
                            pid,
                            name,
                            sClock,
                            message.getMessageId(),
                            message.getPid()
                    )
            );
        }
    }

    // TODO: use my initial idea: use a buffer before the process' request buffer, i.e. a priority queue, based on a sClock + pid sort. A method will handle its behavior which will be identical to what is currently done in the sendMessage. This will improve thread safety and all that
    //

    /**
     * Check the message located at the head of the queue
     */
    public void checkHeadMessage() {
        Message headMessage = inQueue.peek();

        /* Check if there is a message in the requrest queue, and if so, see if one can release it */
        if (headMessage != null && headMessage.isCanRelease()) {
            inQueue.poll();

            /* Send a release message to all the peers that they can also release this message */
        }

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
                            pid, name, sClock++, false, "This is a non-ack message"
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
                ", peers=" + peers +
                '}';
    }
}
