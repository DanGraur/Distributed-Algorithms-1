package node.message;

/**
 * @author Dan Graur 11/24/2017
 *
 * It is expected that the content of this message refers to the id of the Message being acknowledged
 */
public class AckMessage extends Message {

    /**
     * The PID of the process who initially sent this message
     */
    private long sourcePid;

    public AckMessage(long pid, String procName, long sClock, String contents, long sourcePid) {
        super(pid, procName, sClock, MessageType.ACK, contents);

        this.sourcePid = sourcePid;
    }

    public long getSourcePid() {
        return sourcePid;
    }

    @Override
    public String toString() {
        return "AckMessage{" +
                "sourcePid=" + sourcePid +
                ", procName='" + procName + '\'' +
                ", pid=" + pid +
                ", sClock=" + sClock +
                ", contents='" + contents + '\'' +
                '}';
    }
}
