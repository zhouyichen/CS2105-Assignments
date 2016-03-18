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
    private int currentSequenceNumber;

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
            crc = new CRC32();

            // prepare packet for sending ack/nak
            ackByte = new byte[ACK_LENGTH];

            ackPacketBuffer = ByteBuffer.wrap(ackByte);
            ackPacket = new DatagramPacket(ackByte, ACK_LENGTH);
            
            crc = new CRC32();
            currentSequenceNumber = -1;

            // get file name
            boolean isFileNameReceived = receiveFileNamePacket();
            String fileName = new String(dataByte);

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
                // data is correctly received when sequence number is larger than 0
                if (sequenceNunmber > 0) {
                    bos.write(dataByte);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean receiveFileNamePacket() throws IOException{
        boolean isValidFile = checkPacket();
        int sequenceNunmber = packetBuffer.getInt();

        if (isValidFile) {
            ackPacket.setSocketAddress(packet.getSocketAddress());

            sendAckAndWrite(sequenceNunmber);
        } else {
            sendFeedback(false, sequenceNunmber);
        }
        return isValidFile;
    }

    public int receivePacket() throws IOException{
        boolean isValidFile = checkPacket();
        int sequenceNunmber = packetBuffer.getInt();

        if (isValidFile) {
            sequenceNunmber = sendAckAndWrite(sequenceNunmber);
        } else {
            sendFeedback(false, sequenceNunmber);
            sequenceNunmber = 0;
        }
        return sequenceNunmber;
    }

    public boolean checkPacket() throws IOException{
        socket.receive(packet);
        packetByte = packet.getData();
        packetBuffer = ByteBuffer.wrap(packetByte);
        long checkSum = packetBuffer.getLong();

        // checksum
        crc.reset();
        crc.update(packetByte, FileSender.CHECKSUM_LENGTH, FileSender.PACKET_LENGTH - FileSender.CHECKSUM_LENGTH);
        return (checkSum == crc.getValue());
    }

    public int sendAckAndWrite(int sequenceNunmber){
        sendFeedback(true, sequenceNunmber);

        if (currentSequenceNumber != sequenceNunmber) {
            dataByte = new byte[FileSender.DATA_LENGTH];
            packetBuffer.get(dataByte);
            currentSequenceNumber = sequenceNunmber;
        } else {
            sequenceNunmber = 0;
        }
        return sequenceNunmber;
    }

    public void sendFeedback(boolean isAck, int sequenceNunmber) {
        try {
            buildFeedbackPacket(isAck, sequenceNunmber);
            socket.send(ackPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void buildFeedbackPacket(boolean isAck, int sequenceNunmber){
        ackPacketBuffer.clear();
        ackPacketBuffer.putLong(0);
        ackPacketBuffer.putInt(sequenceNunmber);
        if (isAck) {
            ackPacketBuffer.putInt(ACK_FLAG);
        } else {
            ackPacketBuffer.putInt(NAK_FLAG);
        }
        long checkSum = calculateChecksum();
        ackPacketBuffer.putLong(0, checkSum);
    }

    public long calculateChecksum() {
        crc.reset();
        crc.update(ackByte, FileSender.CHECKSUM_LENGTH, ACK_LENGTH - FileSender.CHECKSUM_LENGTH);
        return crc.getValue();
    }
}
