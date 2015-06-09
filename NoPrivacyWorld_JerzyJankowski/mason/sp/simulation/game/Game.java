package sp.simulation.game;

import sp.simulation.tools.DoublePair;
import sp.simulation.tools.Fairness;
import sp.simulation.tools.Tools;
import ec.util.MersenneTwisterFast;

public class Game {
	
	private double mi, sigma;
	private MersenneTwisterFast random = new MersenneTwisterFast();
	private DoublePair lastUncertainMiPair = new DoublePair(null, null), lastUncertainSigmaPair = new DoublePair(null, null);
	private Fairness fairness;

	public Game(MersenneTwisterFast random, Fairness fairness) {
		
		this.random = random;
		randomizeMi();
		randomizeSigma();
		this.fairness = fairness;
	}
	
	public Game(MersenneTwisterFast random, Game game, Fairness fairness) {
		this.setMi(game.getMi());
		this.setSigma(game.getSigma());
		this.fairness = fairness;
	}
	
	public void setGameMiSymmetrical(Game game) {
		this.setMi(-game.getMi());
		this.setSigma(game.getSigma());
	}

	public double play() {
		
		double winning = (random.nextGaussian() * sigma) + mi;
		winning = Tools.round(winning);
		return winning;
	}
	
	/**
	 * for sake of nextGaussian() mi = 0.0 and sigma = 1.0
	 */
	private void randomizeMi() {
		
		mi = Tools.round(random.nextGaussian());
	}
	
	/**
	 * simple random [0.0, 4.0)
	 */
	private void randomizeSigma() {
		sigma = Tools.round(random.nextDouble()*4);
	}
	
	/**
	 * 
	 * 
	 * randomize number y from (-uncertaintyMi, uncertaintyMi), add to real mi and uncertaintyMi
	 * <p>
	 * i.e. for uncertaintyMi = 2, and mi = 1, there could be y from [-2,2] 
	 * and as a result minimum DoublePair(-3,1) or maximum DoublePair(1,5) or anything inbetween i.e. DoublePair(-1.5, 2.5)
	 * <p>
	 * real mi always lies in and mean of L and R is always only uncertaintyMi far from real mi
	 * of course L or R could lie 2*uncertaintyMi from real mi 
	 * @param uncertaintyMi
	 * @return DoublePair - left constraint and right constraint
	 */
	public DoublePair getUncertainMi(double uncertaintyMi) {

		double x, y, uncertainty;
//		switch(fairness) {
//			case FAIR: {
//				uncertainty = (nextGaussianFromOneToOne())*uncertaintyMi;
//				x = mi - uncertaintyMi + uncertainty;
//				y = mi + uncertaintyMi + uncertainty;
//			}break;
//			case SEMIFAIR: {
//				//TODO
//			}break;
//			case UNFAIR: {
//				x = mi;
//				y = mi + uncertaintyMi;
//			}break;
//			default:
//			{
//				System.out.println("NIEPOPRAWNA WARTOŒÆ FAIRNESS");
//				x = y = mi;
//			}
//		}
		uncertainty = (nextGaussianFromOneToOne())*uncertaintyMi;
		x = mi - uncertaintyMi + uncertainty;
		y = mi + uncertaintyMi + uncertainty;
		
		x = Tools.round(x);
		y = Tools.round(y);
		lastUncertainMiPair = new DoublePair(x, y);
		return new DoublePair(x, y);
	}

	public DoublePair getUncertainSigma(double uncertaintySigma) {

		double x, y, g = nextGaussianFromOneToOne();
		double uncertainty = (g)*uncertaintySigma;
		x = Math.max(Tools.round(sigma - uncertaintySigma + uncertainty),0);
		y = Tools.round(sigma + uncertaintySigma + uncertainty);
		lastUncertainSigmaPair = new DoublePair(x, y);
    	
		return new DoublePair(x, y);
	}
	
	private Double nextGaussianFromOneToOne() {
		Double result;
		do {
			result = random.nextGaussian();
		} while (result<-1 || result>1);
		return result;
	}
	
	private void setMi(double mi) {
		this.mi = mi;
	}

	public String toString() {
		return "Mi=" + mi + "; Sigma=" + sigma + "; " + toStringLastUncertains();
	}
	
	public String toStringLastUncertains() {
		return "uMi=" + lastUncertainMiPair + "; uSigma=" + lastUncertainSigmaPair + ";";
	}
	
	public String toLogString() {
		return mi + "," + lastUncertainMiPair.getL() + "," + lastUncertainMiPair.getR() + "," + 
				sigma + "," + lastUncertainSigmaPair.getL() + "," + lastUncertainSigmaPair.getR();
	}
	
	public double getMi() {
		return mi;
	}
	
	private void setSigma(double sigma) {
		this.sigma = sigma;
	}

	public double getSigma() {
		return sigma;
	}
	
	public Fairness getFairness() {
		return fairness;
	}

	private void setFairness(Fairness fairness) {
		this.fairness = fairness;
	} 
}
