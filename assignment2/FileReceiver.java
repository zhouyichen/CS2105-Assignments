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
    public CRC32 crc;
    
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
            int portNumber = Integer.parseInt(localPort);
            socket = new DatagramSocket(portNumber);

            byte[] packetByte = new byte[FileSender.PACKET_LENGTH];
            packet = new DatagramPacket(packetByte, FileSender.PACKET_LENGTH);

            packetBuffer = ByteBuffer.wrap(packetByte);
            crc = new CRC32();

            // get file name
            receivePacket();

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
                bos.write(dataByte);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int receivePacket() throws IOException{

        socket.receive(packet);
        packetByte = packet.getData();
        packetBuffer = ByteBuffer.wrap(packetByte);
        long checkSum = packetBuffer.getLong();

        // checksum
        crc.reset();
        crc.update(packetByte, FileSender.CHECKSUM_LENGTH, FileSender.PACKET_LENGTH - FileSender.CHECKSUM_LENGTH);

        int sequenceNunmber = packetBuffer.getInt();
        dataByte = new byte[FileSender.DATA_LENGTH];
        packetBuffer.get(dataByte);
        return sequenceNunmber;
    }

}
