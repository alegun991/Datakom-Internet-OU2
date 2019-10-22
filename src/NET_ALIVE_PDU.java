import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.io.IOException;

class NET_ALIVE_PDU extends PDU {
    private int port;

    public NET_ALIVE_PDU(int port) {
        super(PDU.NET_ALIVE);
        this.port = port;
    }

    @Override
    public void send(ByteChannel channel) throws IOException {
        ByteBuffer buf = ByteBuffer.allocate(4);

        buf.put((byte) type);
        buf.put((byte) 0);
        buf.putShort((short) port);
        buf.flip();
        channel.write(buf);
    }
}