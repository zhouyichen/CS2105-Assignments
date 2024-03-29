// Author: Yichen

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.PublicKey;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.KeyGenerator;
import javax.crypto.SealedObject;

/**********************************************************************
  * This skeleton program is prepared for weak and average students.  *
  *                                                                   *
  * If you are very strong in programming, DIY!                       *
  *                                                                   *
  * Feel free to modify this program.                                 *
  *********************************************************************/

// Alice knows Bob's public key
// Alice sends Bob session (AES) key
// Alice receives messages from Bob, decrypts and saves them to file

class Alice {  // Alice is a TCP client
    
    private Socket connectionSkt;
    private ObjectOutputStream toBob;   // to send session key to Bob
    private ObjectInputStream fromBob;  // to read encrypted messages from Bob
    private Crypto crypto;        // object for encryption and decryption
    public static final String MESSAGE_FILE = "msgs.txt"; // file to store messages
    
    public static void main(String[] args) {
        
        // Check if the number of command line argument is 2
        if (args.length != 2) {
            System.err.println("Usage: java Alice BobIP BobPort");
            System.exit(1);
        }
        
        new Alice(args[0], args[1]);
    }
    
    // Constructor
    public Alice(String ipStr, String portStr) {
        
        this.crypto = new Crypto();

        try {
            connectionSkt = new Socket(ipStr, Integer.parseInt(portStr));
        } catch (IOException ioe) {
            System.out.println("Error creating connection socket");
            System.exit(1);
        }

        try {
            toBob = new ObjectOutputStream(connectionSkt.getOutputStream());
            fromBob = new ObjectInputStream(connectionSkt.getInputStream());
        } catch (IOException e) {
            System.out.println("Error: cannot get input/output streams");
            System.exit(1);
        }

        // Send session key to Bob
        sendSessionKey();
        
        // Receive encrypted messages from Bob,
        // decrypt and save them to file
        receiveMessages();
    }
    
    // Send session key to Bob
    public void sendSessionKey() {
        
        try {
            toBob.writeObject(this.crypto.getSessionKey());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // Receive messages one by one from Bob, decrypt and write to file
    public void receiveMessages() {
        try {
            PrintWriter printWriter = new PrintWriter(MESSAGE_FILE);
            while (true) {
                try {
                    SealedObject encryptedMessage = (SealedObject) fromBob.readObject();
                    String msg = this.crypto.decryptMsg(encryptedMessage);
                    printWriter.println(msg);                    
                } catch (EOFException e) {
                    // No more new lines, stop receiving
                    break;
                }
            }
            printWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }
    
    /*****************/
    /** inner class **/
    /*****************/
    class Crypto {
        
        // Bob's public key, to be read from file
        private PublicKey pubKey;
        // Alice generates a new session key for each communication session
        private SecretKey sessionKey;
        // File that contains Bob' public key
        public static final String PUBLIC_KEY_FILE = "public.key";
        
        // Constructor
        public Crypto() {
            // Read Bob's public key from file
            readPublicKey();
            // Generate session key dynamically
            initSessionKey();
        }
        
        // Read Bob's public key from file
        public void readPublicKey() {
            // key is stored as an object and need to be read using ObjectInputStream.
            // See how Bob read his private key as an example.
            try {
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(PUBLIC_KEY_FILE));
                this.pubKey = (PublicKey)ois.readObject();
                ois.close();
            } catch (IOException oie) {
                System.out.println("Error reading public key from file");
                System.exit(1);
            } catch (ClassNotFoundException cnfe) {
                System.out.println("Error: cannot typecast to class PublicKey");
                System.exit(1);            
            }
            
            System.out.println("Public key read from file " + PUBLIC_KEY_FILE);
        }
        
        // Generate a session key
        public void initSessionKey() {
            // suggested AES key length is 128 bits
            try{
                KeyGenerator generator = KeyGenerator.getInstance("AES");
                generator.init(128);
                sessionKey = generator.generateKey();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        
        // Seal session key with RSA public key in a SealedObject and return
        public SealedObject getSessionKey() {
            SealedObject sealedSessionKey = null;

            // RSA imposes size restriction on the object being encrypted (117 bytes).
            // Instead of sealing a Key object which is way over the size restriction,
            // we shall encrypt AES key in its byte format (using getEncoded() method).
            try{
                // Alice must use the same RSA key/transformation as Bob specified
                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                cipher.init(Cipher.ENCRYPT_MODE, this.pubKey);
                sealedSessionKey = new SealedObject(sessionKey.getEncoded(), cipher);
                
            } catch (Exception e){
                e.printStackTrace();
            }
            return sealedSessionKey;
        }
        
        // Decrypt and extract a message from SealedObject
        public String decryptMsg(SealedObject encryptedMsgObj) {     
            String plainText = null;       
            try{
                // Alice and Bob use the same AES key/transformation
                Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
                cipher.init(Cipher.DECRYPT_MODE, sessionKey);
                plainText = (String)encryptedMsgObj.getObject(cipher);
                
            } catch(Exception e){
                e.printStackTrace();
            }
            return plainText;
        }
    }
}