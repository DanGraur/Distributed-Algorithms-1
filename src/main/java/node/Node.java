package node;

import node.message.AckMessage;
import node.message.GenericMessageSender;
import node.message.Message;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
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
     * This queue will stand before the inQueue. It will store the unporcessed incoming messages.
     */
    private Queue<Message> intermediateQueue;

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
        this.intermediateQueue = new PriorityQueue<>();
    }

    public Node(long pid) {
        this.pid = pid;
        this.sClock = 0L;
        this.inQueue = new PriorityBlockingQueue<>();
        this.sentMessages = new HashMap<>();
        this.intermediateQueue = new PriorityQueue<>();
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

        /* Add the message to the temporary queue queue */
        intermediateQueue.add(message);
    }

    /**
     * Process all the messages currently located in the temporary queue
     */
    public void processTempMessages() {

        while (!intermediateQueue.isEmpty()) {
            Message message = intermediateQueue.poll();

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
                try {
                    sendMessageToEveryone(
                            new AckMessage(
                                    pid,
                                    name,
                                    sClock,
                                    message.getMessageId(),
                                    message.getPid()
                            )
                    );
                } catch (RemoteException e) {
                    System.err.println("There was an error when sending an ACK message to everyone");

                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * Check the message located at the head of the queue
     */
    public void checkHeadMessage() {
        Message headMessage = inQueue.peek();

        /* Check if there is a message in the request queue, and if so, see if one can release it */
        if (headMessage != null && headMessage.isCanRelease()) {
            /* Remove the message */
            inQueue.poll();

            /* Remove the message from the internal map */
            sentMessages.remove(headMessage.getMessageId());

            /* Send a release message to all the peers that they can also release this message */

        }
    }

    /**
     * Send a message to all other peers (including itself)
     *
     * @param message the message being sent
     * @throws RemoteException
     */
    private void sendMessageToEveryone(Message message) throws RemoteException {

        for (GenericMessageSender peer : peers.values()) {
            
            peer.sendMessage(message);
            
        }
        
    }

    /**
     * Send a message to all other peers (including itself). This is the initial message sent by a
     * process, and will be added to the process' internal map of sent messages. These sort of messages
     * should trigger an entire communication cycle.
     *
     * @param message the message being sent
     * @throws RemoteException
     */
    private void sendTrueMessageToEveryone(Message message) throws RemoteException {
        sentMessages.put(message.getMessageId(), message);

        sendMessageToEveryone(message);
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
                    processTempMessages();

                    Message msg = new Message(
                            pid, name, sClock++, false, "This is a non-ack message"
                    );

                    sendTrueMessageToEveryone(msg);

                    System.out.println("Have sent message");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            else {
                processTempMessages();
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
