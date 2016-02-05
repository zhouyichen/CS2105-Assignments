// <Zhou Yichen> 
import java.io.*;
import java.net.*;

class FileReceiver {
    
    public DatagramSocket socket; 
    public DatagramPacket pkt;
    
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
            this.socket = new DatagramSocket(portNumber);

            byte[] buffer = new byte[1000];

            // get file name
            this.pkt = new DatagramPacket(buffer, buffer.length);
            this.socket.receive(this.pkt);
            String fileName = new String(this.pkt.getData(), 0, this.pkt.getLength());

            // preapre file output
            FileOutputStream fos = new FileOutputStream(fileName);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            while (true) {
                // receive every packet and write them into the file
                this.pkt = new DatagramPacket(buffer, buffer.length);
                this.socket.receive(this.pkt);
                bos.write(this.pkt.getData(), 0, this.pkt.getLength());

                // if the length of the packet is 0, this is the end of the file
                if (this.pkt.getLength() == 0) {
                    break;
                }
            }
            bos.close();
            this.socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
