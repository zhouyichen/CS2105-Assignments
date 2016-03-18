// <Zhou Yichen> 
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileOutputStream;
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

class FileReceiver {
    
    private DatagramSocket socket; 
    private DatagramPacket packet;
    private byte[] packetByte;
    private ByteBuffer packetBuffer;
    private byte[] dataByte;
    
    private CRC32 crc;

    private DatagramPacket ackPacket;
    private byte[] ackByte;
    private ByteBuffer ackPacketBuffer;
    
    public static final int ACK_LENGTH = 16;
    public static final int ACK_FLAG = Integer.MAX_VALUE;
    public static final int NAK_FLAG = 0;
    
    public static void main(String[] args) {
        
        // check if the number of command line argument is 1
        if (args.length != 1) {
            System.out.println("Usage: java FileReceiver port");
            System.exit(1);
        }
        
        new FileReceiver(args[0]);
    }
    
    public FileReceiver(String localPort) {
        try {
            // prepare socket and packet for receiving data
            int portNumber = Integer.parseInt(localPort);
            socket = new DatagramSocket(portNumber);
            byte[] packetByte = new byte[FileSender.PACKET_LENGTH];
            packet = new DatagramPacket(packetByte, FileSender.PACKET_LENGTH);
            packetBuffer = ByteBuffer.wrap(packetByte);
            crc = new CRC32();

            // prepare packet for sending ack/nak
            byte[] ackByte = new byte[ACK_LENGTH];
            ackPacket = new DatagramPacket(ackByte, ACK_LENGTH);
            ackPacketBuffer = ByteBuffer.wrap(ackByte);
            crc = new CRC32();

            // get file name
            String fileName = receiveFileNamePacket();

            // preapre file output
            FileOutputStream fos = new FileOutputStream(fileName.trim());
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            while (true) {
                // receive every packet and write them into the file
                int sequenceNunmber = receivePacket();

                // check sequence number for end of the file
                if (sequenceNunmber < 0) {
                    bos.write(dataByte, 0, -sequenceNunmber);
                    bos.close();
                }
                bos.write(dataByte);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String receiveFileNamePacket() throws IOException{
        checkPacket();
        ackPacket.setSocketAddress(packet.getSocketAddress());
        int sequenceNunmber = packetBuffer.getInt();
        dataByte = new byte[FileSender.DATA_LENGTH];
        packetBuffer.get(dataByte);
        return new String(dataByte);
    }

    public int receivePacket() throws IOException{
        checkPacket();

        int sequenceNunmber = packetBuffer.getInt();
        dataByte = new byte[FileSender.DATA_LENGTH];
        packetBuffer.get(dataByte);
        return sequenceNunmber;
    }

    public void checkPacket() throws IOException{
        socket.receive(packet);
        packetByte = packet.getData();
        packetBuffer = ByteBuffer.wrap(packetByte);
        long checkSum = packetBuffer.getLong();

        // checksum
        crc.reset();
        crc.update(packetByte, FileSender.CHECKSUM_LENGTH, FileSender.PACKET_LENGTH - FileSender.CHECKSUM_LENGTH);
    }

}
