/*Madison Brooks and Bailey Freund
  CIS 457-20 */
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;

class udpclient{

	public udpclient(){}
		/*
			Returns the packet number of a given buffer/packet
			@return Int packet number
		  */
		public static int getPNum(ByteBuffer bb){
			bb.position(bb.position() - 1024);

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

			// Console cons = System.console();
			// String ipStr = cons.readLine("Enter an IP Address: ");
			// String portStr = cons.readLine("Enter a port number: ");
			int portnum = Integer.parseInt(portStr);

			// @TODO change this so that it doesn't just send the filename as a message, but that it accepts a file back from the server and writes it to the hard drive.
			Boolean again = true; 
			while(again){
				String m = cons.readLine("Enter a filename to transfer: ");
				if(m.equals("exit")){
					again = false;				
				}
				ByteBuffer buf = ByteBuffer.wrap(m.getBytes());
				dc.send(buf,new InetSocketAddress(ipStr,portnum)); 
			
				ByteBuffer buffer = ByteBuffer.allocate(4096);
				SocketAddress clientaddr = dc.receive(buffer); //reading the socket
				String message = new String(buffer.array());  
				//returns a socket address of whoever sent the packet
				System.out.println(message);
				m = message;
			}
		}catch(IOException e){
			System.out.println("Got an IO Exception");
			}
		}
	}
