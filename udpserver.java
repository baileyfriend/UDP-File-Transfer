/*Madison Brooks and Bailey Freund
October 2, 2017
CIS 457-20 */
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.io.FileOutputStream;
import java.io.File;

class udpserver{
    
    public udpserver(){} //empty constructor

    /*Method to make a packtet number into a 4byte array*/
    public static byte[] getPNumBytes(int number){
			//will return the number of which packet it is in 4 bytes
			byte[] pnum = new byte[4];
			pnum = number.toByteArray;
			return pnum;
	}
    
    
    public static void main(String args[]){
        
        try{
            udpserver server = new udpserver();
            System.out.println("Below is a list of available files: ");
            File curDir = new File(".");
            File[] filesList = curDir.listFiles();
                for(File f : filesList){
                        if(f.isFile()){
                                System.out.println("\t"+f.getName());
                        }
                }


            Console cons = System.console();
            Boolean portNotValid = false;
            String portStr = cons.readLine("Enter port number to listen on: ");
            int portInt = 9876; //default port number - will be changed to whatever user inputs
            if(portStr.matches("^[0-9]*$")){
                    portInt = Integer.parseInt(portStr);
                    if(portInt<1024 || 49151<portInt){
                portNotValid = true;
                }
                }else{
                    portNotValid = true;
                }
            while(portNotValid){
                System.out.println("INVALID PORT NUMBER!");
                portStr = cons.readLine("Enter port number to listen on: ");
                if(portStr.matches("^[0-9]*$")){
                            portInt = Integer.parseInt(portStr);
                            if(1024<portInt && portInt<49151){
                            portNotValid = false;
                    }
                        }else{
                            portNotValid = true;
                        }
            }
            System.out.println("Now using port number "+portInt);


            DatagramChannel c = DatagramChannel.open();
            c.bind(new InetSocketAddress(portInt));
            SocketAddress clientaddr = c.receive(buffer);
            // @TODO - change this so that it doesn't just send back the message that the client sent, but instead transfers the file
            String fileStr = new String(buffer.array());
            fileStr = fileStr.trim(); // have to trim leading and trailing spaces due to making string from oversized buffer
            File f = new File(fileStr);
            
            String filename = f.getName();
            boolean fileExists = f.exists();
            if(fileExists){
                System.out.println("File exists");
                
                long filesize = f.length();
                //sc.getOutputStream
                FileInputStream instream = new FileInputStream(f);
                FileChannel fc = instream.getChannel();
                ByteBuffer buf = ByteBuffer.allocate(100000);
                int bytesread = fc.read(buf);
    
                
                
                while(bytesread != -1){
                    buf.flip();
                    c.send(buf, clientaddr);
                    buf.compact();
                    bytesread = fc.read(buf);
                }
            }
            // while(true){
            //     ByteBuffer buffer = ByteBuffer.allocate(4096);
            //     //SocketAddress clientaddr = c.receive(buffer); //reading the socket
            //     String message = new String(buffer.array());  
            //     //returns a socket address of whoever sent the packet
            //     System.out.println(message);
            //     buffer.flip();
            //     c.send(buffer,clientaddr); //will this work
            // }
        }catch(IOException e){
            System.out.println("Got an IO Exception");
        }

    }
}
