/*Madison Brooks and Bailey Freund
October 2, 2017
CIS 457-20 */
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.file.*;
import java.nio.channels.*;
import java.io.FileOutputStream;
import java.io.File;
import java.util.*;
import java.nio.charset.Charset;

class udpserver{
    
    public udpserver(){} //empty constructor
    
/*
			Returns the packet number of a given buffer/packet
			@return Int packet number
*/
		public static int getPNum(ByteBuffer bb){
			bb.position(0); //this is 4 only because we know all aknowlagements will be size 4 ;')

			byte a = bb.get();
			byte b = bb.get();
			byte c = bb.get();
			byte d = bb.get();

			int num = ((0xFF & a) << 24) | ((0xFF & b) << 16) | ((0xFF & c) << 8) | (0xFF & d);
			return num;
		
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

            
            while(true){
            	ByteBuffer buffer = ByteBuffer.allocate(1024); //our buffer can only be 1024
            	SocketAddress clientaddr = c.receive(buffer); //reading in the file name b/c its the first thing being sent
            	System.out.println("A client is requesting information");
				
				Path cwd = Paths.get("");
				
				String fileStr = new String(buffer.array());
				fileStr = fileStr.trim(); // have to trim leading and trailing spaces due to making string from oversized buffer
				
				File f = new File(fileStr);
				
				String filename = f.getName();
				boolean fileExists = f.exists();

				Path filepath = f.toPath();
				int size = (int)Files.size(filepath); // the size in bytes 
            	
            	if(fileExists){
                	System.out.println("File exists");
					
					server.sendPackets(f, c, clientaddr);
                }
             }
        }catch(IOException e){
            System.out.println("Got an IO Exception: " + e);
        }

	}
	

	public void sendPackets(File f, DatagramChannel c, SocketAddress clientaddr){
		try{
		Path filepath = f.toPath();
		int size = (int)Files.size(filepath); // the size in bytes 
		
		int totalP = size/1020; //(hopfully can use this to find out how many packets it is going to take)
		
		FileInputStream instream = new FileInputStream(f);
		FileChannel fc = instream.getChannel();
		
		Map<Integer, ByteBuffer> mapOfBuffers = new HashMap<Integer, ByteBuffer>();
		
		ByteBuffer bufNum = ByteBuffer.allocate(4);
		ByteBuffer tempBuf = ByteBuffer.allocate(1020);
		
		
		int n = 1;
		
		
			while(fc.read(tempBuf) > 0){ //loop through file
				tempBuf.flip();
				//put packet number and tempBuf into mapOfBuffers
				mapOfBuffers.put(n, tempBuf);
				
				//increment n and the file location
				n++;
				
			}

		
		// Now mapOfBuffers contains the packet number and the flipped ByteBuffers for the whole file
		// NEXT: Send each packet (max 5 at a time) and wait for acks to move forward

		int MAXNUM = 5; //Max number of open packets (size of sliding window)
		int last = 0; // This is the last packet number we got an ack from
		int numAcksAwaiting = 0;
		ByteBuffer currentBuf = ByteBuffer.allocate(1024);
		ByteBuffer acknumBuf = ByteBuffer.allocate(4);
		
		for (Map.Entry<Integer, ByteBuffer> entry: mapOfBuffers.entrySet()){ //loop through mapOfBuffers
			currentBuf.putInt(entry.getKey());
			currentBuf.put(entry.getValue()); // entry.getValue will return the ByteBuffer containing the file contents
			currentBuf.flip();
			c.send(currentBuf, clientaddr);
			currentBuf.clear();
			numAcksAwaiting++; //Every time we send another packet we are awaiting another ack
			
			

			if(numAcksAwaiting >= MAXNUM){ // if at max of sliding window
				//System.out.println("At max");
				SocketAddress addr = c.receive(acknumBuf); //wait and recieve the ack back from the client
				acknumBuf.flip();
				int acknum = acknumBuf.getInt();
				if(acknum < 0){
					return; //Then client confirmed that they got the endmarker
				}

				last = getPNum(acknumBuf);
				acknumBuf.clear();
			} else {
				SocketAddress addr = c.receive(acknumBuf); //wait and recieve the ack back from the client
				last = getPNum(acknumBuf);
				acknumBuf.clear();
			}

		}
		int endMarkerNum = -10;
		currentBuf.putInt(endMarkerNum);
		c.send(currentBuf, clientaddr);
		return;
	} catch(IOException e){
		System.out.println("Caught exception e: " + e);
		return;
	}
	}


}

