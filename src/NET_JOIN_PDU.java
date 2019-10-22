import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.*;
import java.io.IOException;

class NET_JOIN_PDU extends PDU {

    private String src_address;
    private int src_port;
    private int max_span;
    private String max_address;
    private int max_port;

    public NET_JOIN_PDU(String address, int port) {
        super(PDU.NET_JOIN);
        this.src_address = address;
        this.src_port = port;
    }

    public NET_JOIN_PDU(ByteBuffer buf) {
        super(PDU.NET_JOIN);
        byte[] a = new byte[16];
        buf.get(a);
        src_address = new String(a);
        buf.get();
        src_port = Short.toUnsignedInt(buf.getShort());
        max_span = buf.get();
        byte[] b = new byte[16];
        buf.get(b);
        max_address = new String(b);
        buf.get();
        max_port = Short.toUnsignedInt(buf.getShort());
    }

    @Override
    public void send(ByteChannel channel) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(40);
        buf.put((byte) type);
        buf.put(src_address.getBytes());
        buf.put((byte) 0);
        buf.putShort((short) src_port).order(ByteOrder.BIG_ENDIAN);
        byte[] pad = new byte[20];
        buf.put(pad);
        buf.flip();
        channel.write(buf);
    }


    public String getSrc_address() {
        return src_address;
    }

    public int getSrc_port() {
        return src_port;
    }

}