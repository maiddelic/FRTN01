package server;



// PID class to be written by you
public class PID {
	// Current PID parameters
	private PIDParameters p;
	private double e;	// Current control error
	private double v;	// Desired control signal
	private double D, I, yOld, ad, bd;
	//private double ad, bd;
	private double y, u;
	// Constructor
	public PID(String name) {
		PIDParameters p = new PIDParameters();
		p.K = -0.08;
		p.Ti = 0.0;
		p.integratorOn = false;
		p.H = 0.01;
		p.Beta = 1.0;
		p.Tr = 10.0;
		p.Td= 1.7;
		p.N = 7.0;
		ad = p.Td/(p.Td + p.N*p.H);
		bd = p.K * ad * p.N;
		
		setParameters(p);
		
		this.yOld = 0.0;
		this.I = 0.0;
		this.D = 0.0;
		this.v = 0.0;	// Desired control signal
		this.e = 0.0;	// Current control error
		this.y = 0.0;
		this.u = 0.0;

	}

	// Calculates the control signal v.
	// Called from BallAndBeamRegul.
	public synchronized double calculateOutput(double y1, double yref) {
		y = y1;
		e = yref - y;
		D = ad * D - bd * (y - yOld);
		v = p.K * (p.Beta * yref - y) + I + D; // I is 0.0 if integratorOn
													// is
		yOld = y;											// false
		return v;
	}

	// Updates the controller state.
	// Should use tracking-based anti-windup
	// Called from BallAndBeamRegul.
	public synchronized void updateState(double u) {
		if (p.integratorOn) {
			I = I + (p.K * p.H / p.Ti) * e + (p.H / p.Tr) * (u - v);
	
		} else {
			I = 0.0;
			
		}
	}

	// Returns the sampling interval expressed as a long.
	// Explicit type casting needed.
	public synchronized long getHMillis() {
		return (long) (p.H * 1000.0);

	}

	// Sets the PIDParameters.
	// Called from PIDGUI.
	// Must clone newParameters.
	public synchronized void setParameters(PIDParameters newParameters) {
		p = (PIDParameters) newParameters.clone();
		if (!p.integratorOn) {
			I = 0.0;
		}
	}
}