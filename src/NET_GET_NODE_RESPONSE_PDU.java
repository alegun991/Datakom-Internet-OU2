import java.nio.ByteBuffer;
import java.nio.channels.*;

class NET_GET_NODE_RESPONSE_PDU extends PDU {
    private String address;
    private int port;

    public NET_GET_NODE_RESPONSE_PDU(ByteBuffer buf) {
        super(PDU.NET_GET_NODE_RESPONSE);
        byte[] a = new byte[16];
        buf.get(a);
        address = new String(a);
        buf.get();
        port = Short.toUnsignedInt(buf.getShort());
    }

    @Override
    public void send(ByteChannel channel) {

    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }
}
