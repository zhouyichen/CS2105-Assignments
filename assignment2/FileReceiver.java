// <Zhou Yichen> 
import java.io.*;
import java.net.*;

class FileReceiver {
    
    private DatagramSocket socket; 
    private DatagramPacket packet;
    
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

            byte[] buffer = new byte[FileSender.PACKET_LENGTH];

            // get file name
            packet = new DatagramPacket(buffer, PACKET_LENGTH);
            socket.receive(packet);
            String fileName = new String(packet.getData(), 0, packet.getLength());

            // preapre file output
            FileOutputStream fos = new FileOutputStream(fileName);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            while (true) {
                // receive every packet and write them into the file
                packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                bos.write(packet.getData(), 0, packet.getLength());

                // if the length of the packet is 0, this is the end of the file
                if (packet.getLength() == 0) {
                    break;
                }
            }
            bos.close();
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
