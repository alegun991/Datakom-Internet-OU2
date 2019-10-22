import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.*;
import java.io.IOException;

class STUN_LOOKUP_PDU extends PDU {
    private int port;

    public STUN_LOOKUP_PDU(int port) {
        super(PDU.STUN_LOOKUP);
        this.port = port;
    }

    @Override
    public void send(ByteChannel channel) throws IOException{
        ByteBuffer buf = ByteBuffer.allocate(4);

        buf.put((byte)type);
        buf.put((byte)0);
        buf.putShort((short)port).order(ByteOrder.BIG_ENDIAN);
        buf.flip();
        channel.write(buf);
    }
}

