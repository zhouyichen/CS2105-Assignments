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
    private DatagramPacket pkt;
    private int numBytes;

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
            this.socket = new DatagramSocket();

            // send file name packet
            this.pkt = buildFileNamePacket(rcvFileName, receiverAddress, portNumber);
            sendPacket();
            
            // prepare the buffer
            FileInputStream fis = new FileInputStream(fileToOpen);
            BufferedInputStream bis = new BufferedInputStream(fis);

            byte[] buffer = new byte[1000];
            
            // send packets for file content
            while (true) {
                numBytes = bis.read(buffer);
                if (numBytes > 0) {
                    this.pkt = new DatagramPacket(buffer, numBytes, receiverAddress, portNumber);
                    this.socket.send(this.pkt);
                    Thread.sleep(1);
                }
                else {
                    break;
                }
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

    private DatagramPacket buildFileNamePacket(String rcvFileName, InetAddress receiverAddress, int portNumber) {
        byte[] fileName = rcvFileName.getBytes();
        return new DatagramPacket(fileName, fileName.length, receiverAddress, portNumber);
    }

    private DatagramPacket buildPacket(byte[] buffer, BufferedInputStream bis, InetAddress receiverAddress, int portNumber) {
        return new DatagramPacket(buffer, numBytes, receiverAddress, portNumber);
    }

    private void sendPacket() throws IOException{
        try {
            this.socket.send(this.pkt);         
        } catch (SocketTimeoutException e){
        }
    }
}