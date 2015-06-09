package sp.simulation.tools;

public class DoublePair {

	private Double l;
	private Double r;
	
//	public DoublePair() {
//	}
	
	public DoublePair(Double l, Double r) {
		this.l = l;
		this.r = r;
	}
	
	@Override
	public String toString() {
		return "DoublePair [l=" + l + ", r=" + r + "]";
	}

	public Double getL() {
		return l;
	}
	
	public void setL(Double l) {
		this.l = l;
	}
	
	public Double getR() {
		return r;
	}
	
	public void setR(Double r) {
		this.r = r;
	}
	
}
