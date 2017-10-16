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

			byte a = bb.get();
			byte b = bb.get();
			byte c = bb.get();
			byte d = bb.get();

			int num = ((0xFF & a) << 24) | ((0xFF & b) << 16) | ((0xFF & c) << 8) | (0xFF & d);
			return num;
		
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
				int lastR = 0; //the last packet number recived IN ORDER from the server
				int packetNum = -1; // a variable to hold the values of the current packet number
				File newfile = new File(m); //make an empty file with that file name
				FileOutputStream outstream = new FileOutputStream(newfile); //set up an outputstream with that file
          		FileChannel fc = outstream.getChannel(); //make the channel for the stream
				ArrayList<ByteBuffer> buffersthatwehave = new ArrayList<ByteBuffer>();
				//while loop until the end of the packet
					//Packet1
					//for(int x=lastR; x < x+5; x++){ // this is a never ending loop but it is on the right track
					//	System.out.println("loop is here");
						ByteBuffer bufff = ByteBuffer.allocate(1024);
						//int bytesRead = dc.read(bufff);
						SocketAddress serverAddr = dc.receive(bufff); //receving the packet
						
						packetNum = getPNum(bufff); //getting the packet number
						
						//if(packetNum == 0){ // need and if statement with a break inorder to stop this loop
						//	break;
						//}
							
						ByteBuffer akn = ByteBuffer.allocate(4);
						akn.putInt(packetNum);
						//dc.send(akn,serverAddr);//sending the aknowlagement- a packet of 4 bytes with just the packet number
						
						//if(packetNum == lastR+1){ //making sure this is the next packet we expect to get
						
							lastR = packetNum; //incrementing the last recived so we know what in order packets we have
							//bufff.position(4);
							//System.out.println(Arrays.toString(bufff.array()));
							//bufff.flip(); //putting the buffer into reading mode
							//might need to change position
							//bufff.position(4);
							//
							//byte[] array = bufff.array();
							//for(byte b:array)
							//bufff.position(4);
							//buff.trim();
							
							//while(bytesRead != -1){
								//bufff.flip(); THIS FLIP BREAKS IT
								//System.out.println(Arrays.toString(bufff.array()));
								
		      		    		fc.write(bufff); //writing the buffer into the file and the postion
		      		    		bufff.compact();//
		      		    		//bytesRead = dc.read(bufff);
		      		    	//}
		      		    	//bufff.compact();//clears the buffer
						//}
						//else{} //this packet is not inorder and we should save it into some kind of array?

					//}
					
				
			}
		}catch(IOException e){
			System.out.println("Got an IO Exception");
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

