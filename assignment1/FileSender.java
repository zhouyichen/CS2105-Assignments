// <Zhou Yichen> 
import java.io.*;
import java.net.*;

class FileSender {
    
    public DatagramSocket socket; 
    public DatagramPacket pkt;
    
    public static void main(String[] args) {
        
        // check if the number of command line argument is 4
        if (args.length != 4) {
            System.out.println("Usage: java FileSender <path/filename> "
                                   + "<rcvHostName> <rcvPort> <rcvFileName>");
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
            this.socket = new DatagramSocket();

            // send file name packet
            byte[] fileName = rcvFileName.getBytes();
            this.pkt = new DatagramPacket(fileName, fileName.length, receiverAddress, portNumber);
            this.socket.send(this.pkt);
            Thread.sleep(1);

            // prepare the buffer
            byte[] buffer = new byte[1000];
            FileInputStream fis = new FileInputStream(fileToOpen);
            BufferedInputStream bis = new BufferedInputStream(fis);

            int numBytes = bis.read(buffer);

            // send packets for file content
            while (numBytes > 0) {
                this.pkt = new DatagramPacket(buffer, numBytes, receiverAddress, portNumber);
                this.socket.send(this.pkt);
                numBytes = bis.read(buffer);
                Thread.sleep(1);
            }

            // send an empty buffer to signal the end of file
            byte[] emptyBuffer = new byte[0];
            this.pkt = new DatagramPacket(emptyBuffer, 0, receiverAddress, portNumber);
            this.socket.send(this.pkt);

            bis.close();
            this.socket.close();

        } catch(Exception e) {
            e.printStackTrace();
        }

    }
}