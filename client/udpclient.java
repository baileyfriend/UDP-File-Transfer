/*Madison Brooks and Bailey Freund
  CIS 457-20 */
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

class udpclient{

	public udpclient(){}
		/*
			Returns the packet number of a given buffer/packet
			@return Int packet number
		  */
		public static int getPNum(ByteBuffer bb){
			bb.position(0); //I was trying to do it with bb.position() - 1024

			byte a = bb.get(); //gets the first byte in the buffer
			byte b = bb.get(); //gets the second byte in the buffer
			byte c = bb.get(); //gets the third byte in the buffer
			byte d = bb.get(); //gets the fourth byte in the buffer

			int num = ((0xFF & a) << 24) | ((0xFF & b) << 16) | ((0xFF & c) << 8) | (0xFF & d);
			//turns the bytes into an integer
			
			return num; //returns that integer that is the packet number
		
		}
		  /*
			Checks if valid ip
			@return True if string is a valid ip, else false
		  */
		  public static boolean isValidIP(String ip){
			  try{
				  if(ip == null || ip.isEmpty()){
				  return false;
				  }
	
				  String[] ipArr = ip.split("\\.");
				  if( ipArr.length != 4 ){
				  return false;
				  }
	
				  for(String numStr : ipArr){
				  int num = Integer.parseInt(numStr);
				  if(num < 0 || num > 255){
					  return false;
				  }
				  }
	
				  if(ip.endsWith(".")){
				  return false;
				  }
	
				  return true;
	
			  } catch(NumberFormatException e){
				  return false; //means it wasn't a number
			  }
		  }

	public static void main(String args[]){
		try{
			DatagramChannel dc = DatagramChannel.open(); 
//changing the transport layer protocal
			Console cons = System.console();

			udpclient uc = new udpclient();
      	    String ipStr = "127.0.0.1"; // Default ip address
      	    boolean valid = false;
      	    while(valid == false){
          		ipStr = cons.readLine("Enter target IP address: ");
              	valid = uc.isValidIP(ipStr.trim());
          		if(!valid){
          		    System.out.println("IP address " + ipStr + " is not valid.");
          		    continue;
          		} else{
          		    valid = true;
          		}
      	    }


      	    String portStr = cons.readLine("Enter target port number: ");
	/*Validating the port number*/
                  Boolean portNotValid = false; //checking to see if the port is valid
		      int portInt=9876; //declaring portInt so the code works
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
			System.out.println("Now using port number " + portInt); //end of checking port number

			int portnum = Integer.parseInt(portStr);

			Boolean again = true;

			
			while(again){
				String m = cons.readLine("Enter a filename to transfer: ");
				if(m.equals("exit")){
					again = false;				
				}
				ByteBuffer buf = ByteBuffer.wrap(m.getBytes());
				dc.send(buf,new InetSocketAddress(ipStr,portnum));  //sending the file name

				uc.recievePackets(dc, ipStr, portnum, m);
				
				

				// int lastR = 0; //the last packet number recived IN ORDER from the server
				
				// int packetNum = -1; // a temperary variable to hold the values of the current packet number
				
				// File newfile = new File(m); //make an empty file with that file name
				// FileOutputStream outstream = new FileOutputStream(newfile); //set up an outputstream with that file
          		// 	FileChannel fc = outstream.getChannel(); //make the channel for the stream
				// ArrayList<ByteBuffer> buffersthatwehave = new ArrayList<ByteBuffer>(); //this is an arraylist that is supposed to hold the buffers 
				// //that are not in order but are going to be used later
				
				// //while loop until the end of the packet or having a flag for the last packet sent for the file
				// 	//Packet1
				// 	/*This for loop below is trying to impliemnt the sliding window. X is the packet number of the last packet received in order
				// 	and the loop will go 5 packets passed that (thats how the sliding window is supposed to work) the loop is a never ending loop
				// 	but I have it set up for a flagged packet, so the very last packet will have a packet number of zero and the loop will 
				// 	know that it is done looping and being a window.*/
				
				// 	//for(int x=lastR; x < x+5; x++){ //this is the loop described above
				// 	//	System.out.println("loop is here"); for testing purposes
				// 		ByteBuffer bufff = ByteBuffer.allocate(1024); //the bufffer for the loop
				// 		//int bytesRead = dc.read(bufff); //a failed attempt at getting the packet number reading working
				// 		SocketAddress serverAddr = dc.receive(bufff); //receving the packet
						
				// 		packetNum = getPNum(bufff); //getting the packet number
						
				// 		//if(packetNum == 0){ // need and if statement with a break inorder to stop this loop (as described above)
				// 		//	break;
				// 		//}
							
				// 		ByteBuffer akn = ByteBuffer.allocate(4); //buffer to hold the acknowlagement for packets
				// 		akn.putInt(packetNum); //putting the packet number into the buffer
				// 		//dc.send(akn,serverAddr);//sending the aknowlagement- a packet of 4 bytes with just the packet number
						
				// 		//if(packetNum == lastR+1){ //making sure this is the next packet we expect to get
						
				// 			lastR = packetNum; //incrementing the last recived so we know what in order packets we have
				// 			//bufff.position(4); FAILED attempt at moving the buffer but might work if implemented differnt?
				// 			//System.out.println(Arrays.toString(bufff.array())); //printing out the buffer
				// 			//bufff.flip(); //putting the buffer into reading mode BUT this broke the code once upon a time
				// 			//
				// 			//byte[] array = bufff.array(); // the connected failed attempt at reading the packet number 
				// 			//for(byte b:array) //and idea that might work
							
				// 			//while(bytesRead != -1){ // this is how the TCP one worked but its not working here
				// 				//bufff.flip(); THIS FLIP BREAKS IT
				// 				//System.out.println(Arrays.toString(bufff.array())); //printing contents of a buffer
								
		      	// 	    		fc.write(bufff); //writing the buffer into the file and the postion
		      	// 	    		bufff.compact();// compacting the buffer
		      		    		//bytesRead = dc.read(bufff); // connected to the TCP failed attempt
		      		    	//} //for the for loop
		      		    	//bufff.compact();//clears the buffer
						//}
						//else{
							//this packet is not inorder and we should save it into the array buffersthatwehave
							} 

					//}
					
				
			} catch(IOException e){
				System.out.println("Got an IO Exception");
				}
		}

		public void recievePackets(DatagramChannel dc, String ipStr, int portnum, String filenameStr){
			try{
			File newfile = new File(filenameStr); //make an empty file with that file name
			FileOutputStream outstream = new FileOutputStream(newfile); //set up an outputstream with that file
			FileChannel fc = outstream.getChannel();

			int currentPacketNum = 0;
			int lastPacketNum = -1;
			ByteBuffer packetNum = ByteBuffer.allocate(4);
			ByteBuffer data = ByteBuffer.allocate(1020);
			Boolean notDone = true;

			while(notDone){
				ByteBuffer packet = ByteBuffer.allocate(1024);
				dc.receive(packet);

				int pnumindex = 0;
				int dataIndex = 0;
				for(int i = 0; i < packet.position(); i++){
					if(i<4){ //Then we are looking at the packet number
						packetNum.put(pnumindex++, packet.get(i));
						
						
						//packetNum.put(i, (byte)0)
					} else { // We are looking at the data
						data.put(dataIndex++, packet.get(i)); // so put the packet contents in the data bytebuffer
					}
					
				}

				currentPacketNum = packetNum.getInt();
				System.out.println("Packet number: " + lastPacketNum);
				
				if(currentPacketNum == lastPacketNum + 1){
					fc.write(data);
					lastPacketNum = currentPacketNum;
					dc.send(packetNum,new InetSocketAddress(ipStr,portnum));
				} else {
					dc.send(packetNum,new InetSocketAddress(ipStr,portnum));
				}
				
			}
		} catch(IOException e){
			System.out.println("There was an exception: " + e);
		}

		}

}



	
	/*
	@TODO: for the sliding window:
			-get packet number
			-keep track of the last packet number we recived IN ORDER
			-send an aknowlagement for that last packet
			-keep all packets we recived in an array
			-ask for missing packets
			-fill in the spaces where the missing packets were but also utilize the ones we already have
	*/

