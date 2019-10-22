import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.*;
import java.io.IOException;

class NET_GET_NODE_PDU extends PDU {
    private int port;

    public NET_GET_NODE_PDU(int port) {
        super(PDU.NET_GET_NODE);
        this.port = port;
    }

    @Override
    public void send(ByteChannel channel) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(4);

        buf.put((byte) type);
        buf.put((byte) 0);
        buf.putShort((short) port).order(ByteOrder.BIG_ENDIAN);
        buf.flip();
        channel.write(buf);
    }
}
