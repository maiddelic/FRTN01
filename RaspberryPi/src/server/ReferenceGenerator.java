package server;

public class ReferenceGenerator extends Thread {
	private double amplitude;
	private int period;
	private double sign = -1.0;
	private double ref;

	public ReferenceGenerator(double h, double a) {
		amplitude = a;
		period = (int) (h * 1000 / 2);
	}

	public synchronized double getRef() {
		return ref;
	}

	public void run() {
		try {
			while (!isInterrupted()) {
				synchronized (this) {
					sign = -sign;
					ref = amplitude * sign;

				}
				sleep(period);
			}
		} catch (InterruptedException e) {
			// Requested to stop
		}
	}
}
