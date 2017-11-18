package node.message;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Dan Graur 11/18/2017
 */
public class Message implements Serializable, Comparable {

    private long pid;
    private long sClock;
    private boolean ack;
    private String contents;
    private Date timestamp;

    public Message(long pid, long sClock, boolean ack, String contents) {
        this.pid = pid;
        this.sClock = sClock;
        this.ack = ack;
        this.contents = contents;
        this.timestamp = new Date();
    }

    public void setAck(boolean ack) {
        this.ack = ack;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public long getPid() {
        return pid;
    }

    public long getsClock() {
        return sClock;
    }

    public boolean isAck() {
        return ack;
    }

    public String getContents() {
        return contents;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public int compareTo(Object o) {

        if (!(o instanceof Message))
            return -1;

        Message that = (Message) o;

        int diff = (int) (this.sClock - that.getsClock());

        /* If equal timestamps --> use pid's to break contention */
        if (diff == 0)
            return (int) (this.pid - that.getPid());

        /* Else use only the sClocks */
        return diff;
    }
}

