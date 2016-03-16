// <Zhou Yichen> 
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.InetAddress;
import java.util.zip.CRC32;

class FileSender {
    
    private DatagramSocket socket; 
    private DatagramPacket packet;
    private byte[] packetByte;
    private ByteBuffer packetBuffer;

    public CRC32 crc;
    public static final int PACKET_LENGTH = 1000;
    public static final int CHECKSUM_OFFSET = 0;
    public static final int CHECKSUM_LENGTH = 8;
    public static final int SEQ_NO_OFFSET = CHECKSUM_LENGTH;
    public static final int SEQ_NO_LENGTH = 4;
    public static final int DATA_OFFSET = SEQ_NO_LENGTH + CHECKSUM_LENGTH;
    public static final int DATA_LENGTH = PACKET_LENGTH - DATA_OFFSET;


    public static void main(String[] args) {
        
        // check if the number of command line argument is 4
        if (args.length != 4) {
            System.out.println("Usage: java FileSender <path/filename> "
                                   + "<unreliNetIP> <unreliNetPort> <rcvFileName>");
            System.exit(1);
        }
        
        new FileSender(args[0], args[1], args[2], args[3]);
    }
    
    public FileSender(String fileToOpen, String host, String port, String rcvFileName) {
        try {
            // get receiver address
            InetAddress receiverAddress = InetAddress.getByName(host);
            // get port number
            int portNumber = Integer.parseInt(port);

            // create sender socket
            socket = new DatagramSocket();

            // create packet and buffer
            packetByte = new byte[PACKET_LENGTH];
            packetBuffer = ByteBuffer.wrap(packetByte);
            byte[] data = new byte[DATA_LENGTH];
            packet = new DatagramPacket(packetByte, PACKET_LENGTH, receiverAddress, portNumber);
            crc = new CRC32();
            
            int sequenceNunmber = 0;
            // sent fileName Packet
            byte[] fileName = rcvFileName.getBytes();
            sendPacket(fileName, sequenceNunmber++);

            // prepare the file input buffer
            FileInputStream fis = new FileInputStream(fileToOpen);
            BufferedInputStream bis = new BufferedInputStream(fis);
            
            // send packets for file content
            int numBytes;
            while (true) {
                numBytes = bis.read(data);
                if (numBytes > 0) {
                    sendPacket(data, sequenceNunmber++);
                    Thread.sleep(1);
                }
                else {
                    break;
                }
            }
            // send an empty buffer to signal the end of file
            byte[] emptyBuffer = new byte[0];
            sendPacket(emptyBuffer, sequenceNunmber);

            bis.close();
            socket.close();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void buildPacket(byte[] data, int sequenceNunmber) {
        packetBuffer.clear();
        packetBuffer.putLong(0);
        packetBuffer.putInt(sequenceNunmber);
        packetBuffer.put(data);
        long checksum = calculateChecksum(packetBuffer);
        packetBuffer.putLong(0, checksum);
    }

    public long calculateChecksum(ByteBuffer buffer) {
        crc.reset();
        crc.update(buffer.array());
        return crc.getValue();
    }

    private void sendPacket(byte[] data, int sequenceNunmber) throws IOException{
        try {
            buildPacket(data, sequenceNunmber);
            socket.send(packet);
        } catch (SocketTimeoutException e){
        }
    }
}