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
				//System.out.println((int)Files.size(fileStr));
				File f = new File(fileStr);
				
				String filename = f.getName();
				boolean fileExists = f.exists();
				System.out.println("Filename to be transferred: " + filename);

				Path filepath = f.toPath();
				int size = (int)Files.size(filepath); // the size in bytes 
				System.out.println("size of file 1 : " + size);
				//int totalP = size/1020;
            	
            	if(fileExists){
                	System.out.println("File exists");
                	// Path filepath = f.toPath();
                	// int size = (int)Files.size(filepath); // the size in bytes 
                	// int totalP = size/1020; //(hopfully can use this to find out how many packets it is going to take)
                	
                	// FileInputStream instream = new FileInputStream(f);
					// FileChannel fc = instream.getChannel();
					
					server.sendPackets(f, c, clientaddr);
                	//ByteBuffer buf = ByteBuffer.allocate(1024);
                	//int bytesread = fc.read(buf); //might not use this???
            //     	int lastA = -1;
            //     	int akNum = -1;
            //     	int packetNum = 0;
            //     	int lastsent = -1;
            //     	ArrayList<ByteBuffer> buffersthatwehave = new ArrayList<ByteBuffer>(); //an array list of all the aknowlaged packet numbers
            //     	ArrayList<Integer> nums = new ArrayList<Integer>(); //an array of acknowlaged packets
            //     	ByteBuffer bufff = ByteBuffer.allocate(1024); //buffer for packets
            //     	ByteBuffer akBuff = ByteBuffer.allocate(4); //buffer for acknowlagements
                	
		    //         	//for(int x=lastA; x < x+5; x++){ // this is a never ending loop SEE client side for detail because it is the same 
			// //loop in both
		    //         	//	System.out.println("loop is here"); //error checking
		    //         		bufff.putInt(packetNum); //putting the packet number into the first 4 bytes of the buffer
		    //         		fc.read(bufff); //filling the rest of the buffer with information from the file
		    //         		bufff.flip(); //flipping the buffer
		    //         		System.out.println(Arrays.toString(bufff.array())); // printing the buffer for error checking
		    //         		c.send(bufff,clientaddr); //sending the buffer
		    //         		lastsent = packetNum; // setting the last send to what we last sent
		    //         		packetNum = packetNum+1; // incrementing packet number
			// 				bufff.compact();
			// 				//c.receive(akBuff); //receiving an aknowlagement
			// 				//akNum = getPNum(akBuff); //getting packet number of the acknowlagement
			// 				//nums.add(akNum); //adding it to the list
			// 				//if (akNum == 0){ //this was the last packet we had to send so we want to break the loop.
			// 				//	break; //done sending this file
			// 				//}
			// 				//if(lastA != lastsent -4){ //if we need to resent a packet
			// 				//	break;
		    //         		//	}
			// 			//}
						



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
		System.out.println("size of file: " + size);
		int totalP = size/1020; //(hopfully can use this to find out how many packets it is going to take)
		
		FileInputStream instream = new FileInputStream(f);
		FileChannel fc = instream.getChannel();
		System.out.println("Entered sendPackets method");
		Map<ByteBuffer, ByteBuffer> mapOfBuffers = new HashMap<ByteBuffer, ByteBuffer>();
		System.out.println("sendPackets method 1");
		ByteBuffer bufNum = ByteBuffer.allocate(4);
		System.out.println("Entered sendPackets method 2");
		ByteBuffer tempBuf = ByteBuffer.allocate(1020);
		System.out.println("Entered sendPackets method 3");
		int n = 1;
		System.out.println("Entered sendPackets method 4");
		
		
			System.out.println("entered try catch");
			int bytesread = fc.read(tempBuf);
			System.out.println("entered try catch 2: " + bytesread);
			while(bytesread > 0){ //loop through file
				System.out.println("Reading File loop 1");
				System.out.println("reading file");
				
				//putting packet number into bufNum
				bufNum.asIntBuffer().put(n);
				//put part of contents of file into tempBuf
				bytesread = fc.read(tempBuf);
				tempBuf.flip();
				//put packet number and tempBuf into mapOfBuffers
				mapOfBuffers.put(bufNum, tempBuf);
	
				//increment n and the file location
				n++;
				//bytesread = fc.read(tempBuf);
			}

		
		// Now mapOfBuffers contains the packet number and the flipped ByteBuffers for the whole file
		// NEXT: Send each packet (max 5 at a time) and wait for acks to move forward

		int MAXNUM = 5; //Max number of open packets (size of sliding window)
		int last = 0; // This is the last packet number we got an ack from
		int numAcksAwaiting = 0;
		ByteBuffer currentBuf = ByteBuffer.allocate(1024);
		

		for (Map.Entry<ByteBuffer, ByteBuffer> entry: mapOfBuffers.entrySet()){ //loop through mapOfBuffers
			currentBuf.put(entry.getKey());
			currentBuf.put(entry.getValue()); // entry.getValue will return the ByteBuffer containing the file contents

			//DatagramPacket packet = new DatagramPacket(currentBuf, currentBuf.length, clientaddr);
			c.send(currentBuf, clientaddr);
			currentBuf.clear();
			numAcksAwaiting++; //Every time we send another packet we are awaiting another ack

			if(numAcksAwaiting == MAXNUM){
				System.out.println("WE MADE IT INTO THE LOOP OMG OMG OMG OMG");
			}

		}
	} catch(Exception e){
		System.out.println("Caught exception e: " + e);
	}
	}
}
	
	/*Code below is for sending a file when using TCP for reference
							buf.flip();
				  		    c.send(buf, clientaddr);
				  		    buf.compact();
							bytesread = fc.read(buf);
							currWindow++;


Below is an idea for a class but we could always do these things without it
	class slidingWindow(){ 
	
		public static byte[] getPNumBytes(int number){
			//will return the number of which packet it is in 4 bytes
			byte[] pnum = new byte[4];
			pnum = number.toByteArray;
			return pnum;
		}
		public static boolean isAknowlaged(){
			//will return whether or not the package was aknowlaged by the client.
		}
	
	
	
	@TODO: for the sliding window:
			-send packet number with packet
			-for simplicity just number packets 1 to 4billion (no need to wrap around and reuse numbers)
			-only send 5 packets at a time
			-recive aknowlagements for all packets and keep the last one that is IN ORDER 
				for example if we get aknowlagements 1 2 4 5 then the variable last aknowlaged would be 2
			-HAVE variable last packet recived (as decribed above)
			-keep all packets we sent in an array (the contents of the buffers)
			-resend missing packets
				also off of the example above (aknowlagements for 1 2 4 5) MOVE the window by sending packets 6 and 7
	*/
	
	
	
	
	
	
	
