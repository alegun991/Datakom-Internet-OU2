import org.w3c.dom.ls.LSOutput;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class Node {

    private DatagramChannel aliveChannel, udpChannel;
    private ServerSocketChannel acceptChannel;
    private SocketChannel predecessor, successor;
    private Selector selector;
    private InetSocketAddress tracker;
    private int minHash, maxHash;
    private HashMap<SSN, Entry> map;
    private String selfAddress;

    public static void main(String[] args) throws IOException {
        new Node(args[0], Integer.parseInt(args[1]));
    }

    public Node(String trackerIP, int trackerPort) throws IOException {
        tracker = new InetSocketAddress(trackerIP, trackerPort);
        selector = Selector.open();

        aliveChannel = DatagramChannel.open();
        aliveChannel.bind(new InetSocketAddress("0.0.0.0", 0));
        aliveChannel.connect(tracker);

        udpChannel = DatagramChannel.open();
        udpChannel.configureBlocking(false);
        udpChannel.register(selector, SelectionKey.OP_READ);
        udpChannel.bind(new InetSocketAddress("0.0.0.0", 0));

        acceptChannel = ServerSocketChannel.open();
        acceptChannel.configureBlocking(false);

        initializeNode();
        runNode();
    }

    private void initializeNode() throws IOException {
        PDU p = new STUN_LOOKUP_PDU(udpChannel.socket().getLocalPort());
        p.send(aliveChannel);

        boolean running = true;

        while (running) {

            if (selector.select(1000) == 0) {
                p = new NET_ALIVE_PDU(udpChannel.socket().getLocalPort());
                p.send(aliveChannel);
            }

            for (SelectionKey key : selector.selectedKeys()) {
                if (key.isReadable()) {
                    ByteBuffer buffer = ByteBuffer.allocate(30);
                    ((DatagramChannel) key.channel()).receive(buffer);
                    buffer.flip();

                    p = PDU.create(buffer, (ByteChannel) key.channel());

                    if (p.type == PDU.STUN_RESPONSE) {
                        handleStunResponse((STUN_RESPONSE_PDU) p);
                        p = new NET_GET_NODE_PDU(udpChannel.socket().getLocalPort());
                        p.send(aliveChannel);
                    } else if (p.type == PDU.NET_GET_NODE_RESPONSE) {
                        handleGetNodeResponse((NET_GET_NODE_RESPONSE_PDU) p);
                        running = false;
                    } else {
                        System.err.println("Got invalid PDU!");
                    }
                }
            }
            selector.selectedKeys().clear();
        }
    }

    private void handleStunResponse(STUN_RESPONSE_PDU pdu) {
        selfAddress = pdu.getAddress();
        System.out.printf("Got STUN_RESPONSE, my address is %s\n", selfAddress.substring(0,
                selfAddress.indexOf('\0')));

    }

    private void handleGetNodeResponse(NET_GET_NODE_RESPONSE_PDU pdu) {
        System.out.println("Got NODE_RESPONSE:");
        PDU p = new NET_ALIVE_PDU(udpChannel.socket().getLocalPort());

        if (pdu.getPort() == 0) {
            System.out.println("Response is empty, I am all alone :(");
            map = new HashMap<>();
            minHash = 0;
            maxHash = 255;
            System.out.println("My range is: " + minHash + "-" + maxHash);

            try {
                p.send(aliveChannel);
            } catch (IOException e) {
                System.err.println("Could not send alive message " + e.getMessage());
            }

        } else {
            System.out.println("Response is not empty, sending NET_JOIN!");
            map = new HashMap<>();
            try {
                short port = (short) acceptChannel.socket().getLocalPort();
                int acceptPort = Short.toUnsignedInt(port);
                acceptChannel.register(selector, SelectionKey.OP_ACCEPT);

                PDU join = new NET_JOIN_PDU(selfAddress, acceptPort);
                DatagramChannel chnl = DatagramChannel.open();
                chnl.connect(new InetSocketAddress(pdu.getAddress(), pdu.getPort()));
                join.send(chnl);
                chnl.close();

                acceptChannel.bind(new InetSocketAddress(selfAddress, acceptPort));

            } catch (IOException e) {

                System.err.println("Error sending NET_JOIN" + e.getMessage());
            }
        }
    }

    private void runNode() {
        boolean running = true;
        PDU p;

        while (running) {

            try {
                if (selector.select(1000) == 0) {
                    p = new NET_ALIVE_PDU(udpChannel.socket().getLocalPort());
                    p.send(aliveChannel);
                }

                for (SelectionKey key : selector.selectedKeys()) {

                    Channel c = key.channel();

                    if (key.isAcceptable() && c == acceptChannel) {
                        predecessor = ((ServerSocketChannel) key.channel()).accept();
                        predecessor.configureBlocking(false);
                        predecessor.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(60));
                        System.out.println("Connected to predecessor!");

                    } else if (key.isReadable() && c == predecessor) {

                        var buffer = (ByteBuffer) key.attachment();

                        ((SocketChannel) key.channel()).read(buffer);
                        buffer.flip();
                        p = PDU.create(buffer, (ByteChannel) key.channel());

                        if (p.type == PDU.NET_JOIN_RESPONSE) {
                            handleNetJoinResponse((NET_JOIN_RESPONSE_PDU) p);
                            buffer.clear();

                        } else if (p.type == PDU.VAL_INSERT) {
                            handleRangeTcp((VAL_INSERT_PDU) p);

                        } else if (p.type == PDU.VAL_LOOKUP) {
                            handleIncomingValueLookup((VAL_LOOKUP_PDU) p);

                        } else if (p.type == PDU.VAL_REMOVE) {
                            handleIncomingValueRemove((VAL_REMOVE_PDU) p);
                        } else {
                            System.out.println("Got invalid PDU!");
                        }

                    } else if (key.isReadable() && c == udpChannel) {
                        var buffer = ByteBuffer.allocate(300);
                        ((DatagramChannel) key.channel()).receive(buffer);
                        buffer.flip();
                        p = PDU.create(buffer, null);
                        if (p.type == PDU.NET_JOIN) {
                            System.out.println("Received a NET_JOIN over Udp channel!");
                            handleIncomingJoin((NET_JOIN_PDU) p);
                        }
                        else if (p.type == PDU.VAL_INSERT) {
                            handleIncomingValueInsert((VAL_INSERT_PDU) p);
                            buffer.clear();

                        } else if (p.type == PDU.VAL_LOOKUP) {
                            handleIncomingValueLookup((VAL_LOOKUP_PDU) p);
                            buffer.clear();

                        } else if (p.type == PDU.VAL_REMOVE) {
                            handleIncomingValueRemove((VAL_REMOVE_PDU) p);
                            buffer.clear();

                        } else {
                            System.out.println("Got invalid PDU!");
                        }


                    }
                }
                selector.selectedKeys().clear();

            } catch (IOException e) {
                System.out.println("Something went wrong: " + e.toString());
            }
        }
    }

    private void handleIncomingValueInsert(VAL_INSERT_PDU pdu) {

        String ssnReceived = pdu.getSsn();
        String nameReceived = pdu.getName();
        String emailReceived = pdu.getEmail();

        SSN ssn = new SSN(ssnReceived);
        int hashCode = ssn.hashCode();
        //System.out.println("Received VAL_INSERT with: " + ssnReceived + " " + nameReceived + " " + emailReceived);
        System.out.println("Detta är hashvärdet: " + hashCode + "ssn: " + ssn);
        //TODO: check that current hash range is smaller than size of map
        if (hashCode >= minHash && hashCode <= maxHash) {
            Entry entry = new Entry(ssnReceived, nameReceived, emailReceived);
            map.put(ssn, entry);
            System.out.println("Inserted in position: " + hashCode);
        } else {

            try {
                pdu.send(successor);
                System.out.println("Position " + hashCode + " is not in my table, forwarded to successor!");
            } catch (IOException e) {
                System.err.println("Could not send VAL_INSERT to successor " + e.toString());
            }
        }

    }

    private void handleIncomingValueLookup(VAL_LOOKUP_PDU pdu) {
        String ssnReceived = pdu.getSsn();
        SSN ssn = new SSN(ssnReceived);
        int hashCode = ssn.hashCode();

        System.out.println("Received VAL_LOOKUP for: " + ssnReceived);

        if (hashCode >= minHash && hashCode <= maxHash) {

            if (map.size() == 0) {
                System.out.println("No values in table");
                VAL_LOOKUP_RESPONSE_PDU lookup_response_pdu = new VAL_LOOKUP_RESPONSE_PDU(ssnReceived, "", "");
                lookupResponse(lookup_response_pdu, pdu.getSendAddress(), pdu.getSendPort());
            }

            for (Map.Entry<SSN, Entry> entry : map.entrySet()) {

                String key = entry.getKey().getSSN();

                if (key.equals(ssnReceived)) {
                    String ssnResponse = entry.getValue().getSSN().getSSN();
                    String nameResponse = entry.getValue().getName();
                    String emailResponse = entry.getValue().getEmail();

                    VAL_LOOKUP_RESPONSE_PDU lookup_response_pdu = new VAL_LOOKUP_RESPONSE_PDU(ssnResponse, nameResponse, emailResponse);

                    lookupResponse(lookup_response_pdu, pdu.getSendAddress(), pdu.getSendPort());

                } else {
                    System.out.println("Could not find ssn: " + ssnReceived + " in table");
                    VAL_LOOKUP_RESPONSE_PDU lookup_response_pdu = new VAL_LOOKUP_RESPONSE_PDU(ssnReceived, "", "");
                    lookupResponse(lookup_response_pdu, pdu.getSendAddress(), pdu.getSendPort());
                }
            }
        } else {

            try {
                pdu.send(successor);
                System.out.println(ssnReceived + " is not in my table, forwarded to successor!");
            } catch (IOException e) {
                System.err.println("Could not send LOOKUP_RESPONSE to successor " + e.getMessage());
            }
        }
    }

    private void lookupResponse(VAL_LOOKUP_RESPONSE_PDU pdu, String address, int port) {
        try {
            DatagramChannel channel = DatagramChannel.open();
            channel.connect(new InetSocketAddress(address, port));
            pdu.send(channel);
            System.out.println("LOOKUP_RESPONSE sent!");
            channel.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleIncomingValueRemove(VAL_REMOVE_PDU pdu) {

        String ssnReceived = pdu.getSsn();
        SSN ssn = new SSN(ssnReceived);
        int hashCode = ssn.hashCode();

        System.out.println("Received VAL_REMOVE for: " + ssnReceived);

        if (hashCode >= minHash && hashCode <= maxHash) {

            if (map.size() != 0) {
                for (Iterator<Map.Entry<SSN, Entry>> it = map.entrySet().iterator(); it.hasNext(); ) {

                    Map.Entry<SSN, Entry> entry = it.next();
                    String key = entry.getKey().getSSN();

                    if (key.equals(ssnReceived)) {
                        it.remove();
                        System.out.println("Removed entry from position: " + hashCode);
                    }
                }
            } else {
                System.out.println("No entry in position: " + hashCode);
            }

        } else {
            try {
                pdu.send(successor);
                System.out.println(ssnReceived + " is not in my table, forwarded to successor!");
            } catch (IOException e) {
                System.err.println("Could not send VAL_REMOVE to successor " + e.getMessage());
            }
        }
    }

    private void handleIncomingJoin(NET_JOIN_PDU pdu) {
        String src_address = pdu.getSrc_address();
        int src_port = pdu.getSrc_port();

        if (successor == null) {

            System.out.println("I am the only node in network, sending JOIN_RESPONSE");
            try {
                successor = SocketChannel.open();
                successor.connect(new InetSocketAddress(src_address, src_port));
                successor.configureBlocking(false);
                successor.register(selector, SelectionKey.OP_CONNECT);
                successor.register(selector, SelectionKey.OP_WRITE);
                System.out.println("Successor connected!");

                UpdateRange span = new UpdateRange(minHash, maxHash);
                minHash = span.getMinRangeP();
                maxHash = span.getMaxRangeP();
                System.out.println("Updated range is: " + minHash + "-" + maxHash);

                acceptChannel.register(selector, SelectionKey.OP_ACCEPT);
                short port = (short) acceptChannel.socket().getLocalPort();
                int acceptPort = Short.toUnsignedInt(port);
                acceptChannel.bind(new InetSocketAddress(selfAddress, acceptPort));

                NET_JOIN_RESPONSE_PDU response_pdu = new NET_JOIN_RESPONSE_PDU(
                        selfAddress, acceptPort, span.getMinRangeS(), span.getMaxRangeS());

                response_pdu.send(successor);

                SSN ssn;
                int hashCode;
                if (!map.isEmpty()){

                    for (Map.Entry<SSN, Entry> entry : map.entrySet()) {

                        String key = entry.getKey().getSSN();
                        String entrySsn = entry.getValue().getSSN().getSSN();
                        String name = entry.getValue().getName();
                        String email = entry.getValue().getEmail();
                        ssn = new SSN(key);
                        hashCode = ssn.hashCode();

                        if (hashCode > maxHash) {
                            System.out.println("sending Value insert to successor for hash value: " + hashCode);
                            VAL_INSERT_PDU valInsertPdu = new VAL_INSERT_PDU(entrySsn, name, email);
                            valInsertPdu.send(successor);
                        }

                    }

                }

            } catch (IOException e) {
                System.err.println("Could not connect to successor " + e.toString());
            }
        }
    }

    private void handleNetJoinResponse(NET_JOIN_RESPONSE_PDU pdu) {
        System.out.println("Got JOIN_RESPONSE, connecting to " + pdu.getAddress() + ":" + pdu.getPort());

        minHash = pdu.getRangeStart();
        maxHash = pdu.getRangeEnd();

        try {
            successor = SocketChannel.open();
            successor.connect(new InetSocketAddress(pdu.getAddress(), pdu.getPort()));
            System.out.println("Successor connected!");
            System.out.println("My range is: " + minHash + "-" + maxHash);
        } catch (IOException e) {

            System.err.println("Could not connect to successor " + e.getMessage());
        }
    }

    private void handleRangeTcp(VAL_INSERT_PDU pdu) throws IOException {

        String ssnReceived = pdu.getSsn();
        String nameReceived = pdu.getName();
        String emailReceived = pdu.getEmail();

        SSN ssn = new SSN(ssnReceived);
        int hashCode = ssn.hashCode();

        if (hashCode >= minHash && hashCode <= maxHash) {
            Entry entry = new Entry(ssn, nameReceived, emailReceived);
            map.put(ssn, entry);
        } else {
            pdu.send(successor);
        }
    }
}