import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

public class VAL_REMOVE_PDU extends PDU{

    private String ssn;

    public VAL_REMOVE_PDU(ByteBuffer buf){
        super(PDU.VAL_REMOVE);
        byte[] a = new byte[13];
        buf.get(a);
        ssn = new String(a);

    }

    public VAL_REMOVE_PDU(ByteBuffer buf, ByteChannel src) throws IOException {
        super(PDU.VAL_REMOVE);
        var buffer = ByteBuffer.allocate(13);
        src.read(buffer);
        buffer.flip();
        byte[] a = new byte[13];
        buffer.get(a);
        ssn = new String(a);

    }

    @Override
    public void send(ByteChannel channel) throws IOException {

        var buffer = ByteBuffer.allocate(15);
        buffer.put((byte) type);
        buffer.put(ssn.getBytes());
        buffer.flip();

        channel.write(buffer);


    }

    public String getSsn() {
        return ssn;
    }
}
