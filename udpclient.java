/*Madison Brooks and Bailey Freund
  CIS 457-20 */
import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;

class udpclient{

//new
public udpclient(){}

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

//endnew





	public static void main(String args[]){
		try{
			DatagramChannel sc = DatagramChannel.open(); 
//changing the transport layer protocal


			Console cons = System.console();
			String ipStr = cons.readLine("Enter an IP Address: ");
		//new
			ipStr = "127.0.0.1"; // Default ip address
      	    boolean valid = false;
      	    while(valid == false){
          		ipStr = cons.readLine("Enter target IP address: ");
              	valid = isValidIP(ipStr.trim());
          		if(!valid){
          		    System.out.println("IP address " + ipStr + " is not valid.");
          		    continue;
          		} else{
          		    valid = true;
          		}
      	    }
	
			String portStr = cons.readLine("Enter a port number: ");
		
			int portnum=9876;
			Boolean portNotValid = false;
            if(portStr.matches("^[0-9]*$")){
            	portnum = Integer.parseInt(portStr);
            	if(portnum<1024 || 49151<portnum){
					portNotValid = true;
				}
            }else{
            	portNotValid = true;
            }
			while(portNotValid){
				System.out.println("INVALID PORT NUMBER!");
				portStr = cons.readLine("Enter port number to listen on: ");
				if(portStr.matches("^[0-9]*$")){
            		 portnum = Integer.parseInt(portStr);
            		if(1024<portnum && portnum<49151){
						portNotValid = false;
					}
            	}else{
            		portNotValid = true;
            	}
			}
			System.out.println("Now using port number "+portnum);
		//end new

			Boolean again = true; 
			while(again){
				String m = cons.readLine("Enter your message: ");
				if(m.equals("exit")){
					again = false;				
				}
				ByteBuffer buf = ByteBuffer.wrap(m.getBytes());
				sc.send(buf,new InetSocketAddress(ipStr,portnum)); 
			
				ByteBuffer buffer = ByteBuffer.allocate(4096);
				SocketAddress clientaddr = sc.receive(buffer); //reading the socket
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