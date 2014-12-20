package client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

/**
 * This class establishes connection with the Server. 
 * Two nested Thread classes, one for reading from the server and one for sending to the server.
 * 
 */
public class Client{
	private SocketChannel channel;
	private ByteBuffer bufferA;
	private SerialConnect sc;
	
	public Client(int port, SerialConnect sc, String ip) throws IOException{
		this.sc = sc;
		channel = SocketChannel.open();
		channel.configureBlocking(false);
        channel.connect(new InetSocketAddress(ip, port));
        while (!channel.finishConnect()) {
             System.out.println("still connecting");
        }
        System.out.println("Connected");
	}
	/**
	 * Initialize the nested threads.
	 */
	public void start(){
		Read r = new Read();
		Write w = new Write();
		Thread tcpRead = new Thread(r);
		Thread tcpWrite = new Thread(w);
		tcpWrite.start();
		tcpRead.start();
	}
	/**
	 * Closes the communication
	 * @throws IOException
	 */
	public void close() throws IOException{
		channel.socket().close();
		channel.close();
	}
	
	/**
	 * Nested Thread class
	 * Reads the value sent from the server and 
	 * sends it to the SerialConnector 
	 */
	private class Read implements Runnable{
		@Override 	
		public void run(){
			long duration;
			long t;
			while(true){
				try{
					t = System.currentTimeMillis();
					bufferA = ByteBuffer.allocate(5);    
			        String message = "";
			        int count = 0;
			        while ((count = channel.read(bufferA)) > 0) {
			            // flip the buffer to start reading
			            bufferA.flip();
			            message += Charset.defaultCharset().decode(bufferA);
			        }
			        bufferA.clear();
		        	sc.set(message);
					duration = System.currentTimeMillis() - t;
					if(duration < 10) Thread.sleep(10-duration);
					
				} catch (Exception e){
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Writes the measurement values from the SerialConnector  
	 * to the server
	 */
	private class Write implements Runnable{
		@Override 
		public void run(){
			long duration;
			long t;
			while(true){
				try{
					t = System.currentTimeMillis();
					ByteBuffer buffer;
					buffer = ByteBuffer.wrap(sc.get());
			        while (buffer.hasRemaining()) {
			            channel.write(buffer);
			        }
					
					duration = System.currentTimeMillis() - t;
					if(duration < 10) Thread.sleep(10-duration);
				} catch (Exception e){
					e.printStackTrace();
				}
			}
		}
	}
	
}