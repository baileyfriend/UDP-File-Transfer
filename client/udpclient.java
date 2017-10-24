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
				} 
					
				
			} catch(IOException e){
				System.out.println("Got an IO Exception");
				}
		}

		public void recievePackets(DatagramChannel dc, String ipStr, int portnum, String filenameStr){
			try{
			File newfile = new File(filenameStr); //make an empty file with that file name
			FileOutputStream outstream = new FileOutputStream(newfile); //set up an outputstream with that file
			FileChannel fc = outstream.getChannel();

			int currentPacketNum = 1;
			int lastPacketNum = 0;
			ByteBuffer packetNum = ByteBuffer.allocate(4);
			ByteBuffer data = ByteBuffer.allocate(1020);
			Boolean notDone = true;

			while(notDone){


				ByteBuffer packet = ByteBuffer.allocate(1024);
				dc.receive(packet);
				
				packet.flip();
				currentPacketNum = packet.getInt();
				if(currentPacketNum < 0){//then we are done
					int endMarkerNum = -10;
					packetNum.putInt(endMarkerNum);
					dc.send(packetNum, new InetSocketAddress(ipStr,portnum));
					System.out.println("Done transferring file! ");
					return;
				}
				
				if(currentPacketNum == lastPacketNum + 1){
					fc.write(packet);
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
