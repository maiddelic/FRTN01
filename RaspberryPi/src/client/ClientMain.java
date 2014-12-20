package client;

/**
 * 
 * Main class to initialize connection to the server
 * and to initialize communication to the Atmega 
 * through the serailport
 *
 */
 
public class ClientMain {
     
    public static void main(String[] args){
        int port = 4446;
        String comPort = "/dev/ttyUSB0"; //The SerialPort
        String ip = "localhost"; //Change to given IP
        try{
	        SerialConnect sc = new SerialConnect(comPort);
	        Client c = new Client(port, sc, ip);
	        sc.start();
	        c.start();
        }catch(Exception e){
        	return;
        }
    }
}
