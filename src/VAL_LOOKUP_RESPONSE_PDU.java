import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

public class VAL_LOOKUP_RESPONSE_PDU extends PDU {

    private String ssn;
    private String name;
    private String email;

    public VAL_LOOKUP_RESPONSE_PDU(String ssn, String name, String email){
        super(PDU.VAL_LOOKUP_RESPONSE);
        this.ssn = ssn;
        this.name = name;
        this.email = email;
    }


    public VAL_LOOKUP_RESPONSE_PDU(ByteBuffer buf){

        super(PDU.VAL_LOOKUP_RESPONSE);
        byte[] a = new byte[13];
        buf.get(a);
        ssn = new String(a);

        var nameLength = buf.get();
        buf.get();
        byte[] b = new byte[nameLength];
        buf.get(b);
        name = new String(b);

        var emailLength = buf.get();
        byte[] c = new byte[7];
        buf.get(c);
        byte[] d = new byte[emailLength];
        buf.get(d);
        email = new String(d);
    }


    @Override
    public void send(ByteChannel channel) throws IOException {

        var buffer = ByteBuffer.allocate(600);

        buffer.put((byte) type);
        buffer.put(ssn.getBytes());
        buffer.put(((byte)name.getBytes().length));
        buffer.put((byte) 0);
        buffer.put(name.getBytes());
        buffer.put((byte)email.getBytes().length);

        for(int i = 0; i < 7; i++){
            buffer.put((byte) 0);
        }

        buffer.put(email.getBytes());
        buffer.flip();

        channel.write(buffer);

    }
    public void entryNotFound(ByteChannel channel) throws IOException {

        var buffer = ByteBuffer.allocate(24);

        buffer.put((byte) type);
        byte[] b = new byte[23];
        buffer.put(b);
        buffer.flip();

        channel.write(buffer);
    }


    public String getSsn() {
        return ssn;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

}

