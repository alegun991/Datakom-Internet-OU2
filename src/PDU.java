import java.nio.channels.*;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.util.Objects;

abstract class PDU {
    int type;

    public PDU(int type) {
        this.type = type;
    }

    public abstract void send(ByteChannel channel) throws IOException;

    public ByteBuffer readAllBytes(ByteBuffer buf, ByteChannel src) throws IOException {

        while (buf.hasRemaining()) {

            src.read(buf);
            if (buf.limit() != buf.position()) {
                buf.get();
            }
        }
        buf.rewind();
        return buf;
    }

    public static PDU create(ByteBuffer buffer, ByteChannel src) throws IOException {
        int t = Byte.toUnsignedInt(buffer.get());
        PDU p = null;
        switch (t) {
            case STUN_RESPONSE:
                p = new STUN_RESPONSE_PDU(buffer, src);
                break;
            case NET_GET_NODE_RESPONSE:
                p = new NET_GET_NODE_RESPONSE_PDU(buffer, src);
                break;
            case NET_JOIN:
                if(src == null){
                    p = new NET_JOIN_PDU(buffer);
                }
                else{
                    p = new NET_JOIN_PDU(buffer);
                }
                break;
            case NET_JOIN_RESPONSE:
                p = new NET_JOIN_RESPONSE_PDU(buffer, src);
                break;
            case VAL_INSERT:
                if (src == null) {
                    p = new VAL_INSERT_PDU(buffer);
                } else {
                    p = new VAL_INSERT_PDU(buffer, src);
                }
                break;
            case VAL_LOOKUP:
                if (src == null) {
                    p = new VAL_LOOKUP_PDU(buffer);
                } else {
                    p = new VAL_LOOKUP_PDU(buffer, src);
                }
                break;
            case VAL_LOOKUP_RESPONSE:
                p = new VAL_LOOKUP_RESPONSE_PDU(buffer);
                break;
            case VAL_REMOVE:
                if (src == null) {
                    p = new VAL_REMOVE_PDU(buffer);
                } else {
                    p = new VAL_REMOVE_PDU(buffer, src);
                }
                break;
        }
        return p;
    }

    public static final int NET_ALIVE = 0;
    public static final int NET_GET_NODE = 1;
    public static final int NET_GET_NODE_RESPONSE = 2;
    public static final int NET_JOIN = 3;
    public static final int NET_JOIN_RESPONSE = 4;
    public static final int NET_CLOSE_CONNECTION = 5;
    public static final int NET_NEW_RANGE = 6;
    public static final int NET_LEAVING = 7;
    public static final int VAL_INSERT = 100;
    public static final int VAL_REMOVE = 101;
    public static final int VAL_LOOKUP = 102;
    public static final int VAL_LOOKUP_RESPONSE = 103;
    public static final int STUN_LOOKUP = 200;
    public static final int STUN_RESPONSE = 201;
}
