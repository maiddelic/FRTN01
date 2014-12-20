package client;

import jssc.SerialPort;
import jssc.SerialPortException;


/**
 * This class connects to the Atmega16 through the serialport.
 * Two nested thread classes handles the reading and writing to the Atmega
 *
 */
public class SerialConnect {
	private SerialPort serialPort;
	private String valIn;
	private byte[] valOut = new byte[8];

	/**
	 * When connected with USB use: "/dev/ttyUSB0". If connected in regular
	 * Serial Port use "/dev/ttyS0"
	 * */
	public SerialConnect(String portString) throws SerialPortException {
		serialPort = new SerialPort(portString);
		serialPort.openPort();// Open serial port
		serialPort.setParams(SerialPort.BAUDRATE_38400, 8, 1, 0);// Set params.
		serialPort.writeString("s");
		System.out.println("Öppen? "+serialPort.isOpened());
	}
	public synchronized void closePorts() throws SerialPortException {
		serialPort.closePort(); // Close serial port
		notifyAll();
	}
	/** 
	 * 
	 * Initializes the nested threads 
	 * 
	 * */
	public void start(){
		Write w = new Write();
		Read r = new Read();
		Thread atmegaRead = new Thread(r);
		Thread atemgaWrite = new Thread(w);
		atmegaRead.start();
		atemgaWrite.start();
	}
	
	/** 
	 * Sets the value to be sent to the Atmega/AVR Microcontroller
	 *  @param valIn The value to sent
	 * 
	 * */
	public synchronized void set(String valIn){
		this.valIn = valIn;
		notify();
	}
	
	
	/** 
	 * Gets the measurement values (angle and position)
	 *  @return byte vector with the measurement values
	 * 
	 * */
	public synchronized byte[] get(){
		return valOut;
	}


	/** 
	 * Nested Thread class.
	 * Sends the control value to the Atmega.
	 * */
	 class Write implements Runnable{
		
		@Override 
		public void run(){
			long duration;
			long t;
			while(true){
				try {
					t = System.currentTimeMillis();
					synchronized(serialPort){
						serialPort.writeBytes(valIn.getBytes());
						serialPort.notify();
					}
					duration = System.currentTimeMillis() - t;
					if(duration < 10) Thread.sleep(10-duration);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		}
	}
	 
	 
	/** 
	* Nested Thread class.
	* Reads the value from the Atmega and stores it in a byte array 
	* */
	class Read implements Runnable  {
		
		@Override 
		public void run(){
			long duration;
			long t;
			byte[] buffer;
			while(true){
				try {
					t = System.currentTimeMillis();
					synchronized(serialPort){
						buffer = serialPort.readBytes(1);
						if ((char) buffer[0] == 'a') {
							buffer = serialPort.readBytes(9);
							for (int i = 0; i < 4; i++) {
								valOut[i] = buffer[i]; 			// angle starts at 1
								valOut[4 + i] = buffer[5 + i];  // pos value starts at 5
							}
						}
						serialPort.notify();
					}		
					duration = System.currentTimeMillis() - t;
					if(duration < 10) Thread.sleep(10-duration);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
	



