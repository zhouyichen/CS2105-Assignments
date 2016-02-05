// <Fill in your name> 

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
        
        // Refer to Assignment 0 Ex #4 on how to open a file with BufferedInputStream
        
        // UDP transmission is unreliable. Sender may overrun
        // receiver if sending too fast, giving packet lost as a result.
        // It is suggested that sender pause for a while after sending every packet:
        // E.g., Thread.sleep(1); // pause for 1 millisecond
        // On the other hand, don't pause more than 10ms after sending
        // a packet, or your program will take a long time to send a small file.
    }
}