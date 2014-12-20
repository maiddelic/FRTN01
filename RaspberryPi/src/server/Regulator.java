package server;


/**
 * 
 * Regulator class 
 *
 */

public class Regulator {
	private PI inner = new PI("PI");
	private PID outer = new PID("PID");
	
	final double sampling = 10.0;
	final double amp = 495.0;
	private double u, u1;
	private ReferenceGenerator refGen = new ReferenceGenerator(sampling, amp);

	public Regulator() {
		this.refGen.start();
	}

	/**
	 * Extracts the position or angle from the buffer
	 * @param pos the position in the buffer
	 * @param buf the buffer from the client
	 * 
	 * @return the value of the angle or position
	 */
	private double getVal(int pos, byte[] buf) {
		int vNeg = 1;
		if ((char) buf[pos] == '-')
			vNeg = -1;
		double v1 = (double) Character.getNumericValue((char) buf[pos + 1]);
		double v2 = (double) Character.getNumericValue((char) buf[pos + 2]);
		double v3 = (double) Character.getNumericValue((char) buf[pos + 3]);

		return (double) vNeg * (v1 * 100.0 + v2 * 10.0 + v3);
	}

	/**
	 * Saturation of the output
	 * @param v input
	 * @param min the minimum value of the output
	 * @param max the maximum value of the output
	 * 
	 * @return the saturated signal
	 */
	private int limit(int v, int min, int max) {
		if (v < min) {
			v = min;
		} else if (v > max) {
			v = max;
		}
		return v;
	}
	
	/**
	 * Update the Controller states 
	 * 
	 * @return void
	 */
	public void updateStates() {
		inner.updateState(u);
		outer.updateState(u1);
	}

	/**
	 * Calculation of the control signal
	 * @param line the byte vector with the angle and position values
	 * 
	 * @return the control signal
	 */
	public String set(byte[] line) {
		String cOut = null;
	
		try {
			double angle = getVal(0, line);
			double pos = getVal(4, line);
			double ref = refGen.getRef();
			synchronized (outer) {
				u1 = outer.calculateOutput(pos - 485, (ref - 200.0));

				synchronized (inner) {
					u = inner.calculateOutput(angle - 462.0, u1);
					double c = u + 540.0;
					int control = limit((int) c, 0, 999);
					String send = Integer.toString(control);
					StringBuilder sb = new StringBuilder();
					sb.append("w0");
					sb.append(send);
					cOut = sb.toString();
					System.out.println();

				}
				// Printing the measurement values and control signal 
				//System.out.println("Pos: " + pos + " to " + u1 + " Angle: "
					//	+ angle + " to " + u + " Ref val: " + cOut);
				return cOut;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cOut;

	}
}
