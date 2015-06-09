package sp.simulation.game;

/**
 * container for two games. l - for initiator, r - for his partner
 *
 */
public class GamePair {

	private Game l;
	private Game r;
	
	public GamePair(Game l, Game r) {
		this.l = l;
		this.r = r;
	}
	
	@Override
	public String toString() {
		return "DoublePair [l=" + l + ", r=" + r + "]";
	}

	public Game getL() {
		return l;
	}
	
	public void setL(Game l) {
		this.l = l;
	}
	
	public Game getR() {
		return r;
	}
	
	public void setR(Game r) {
		this.r = r;
	}
	
}
