package sp.simulation.game.decision;

import sp.simulation.agent.PersonAgent;
import sp.simulation.game.Game;
import sp.simulation.tools.DoublePair;
import sp.simulation.tools.Tools;

/**
 * like in ChangingDecisionByMiImpl agent is changing his decision methods with changing parameter. 
 * This time param says what chances should be for getting value above 0 out of the game.
 * It's getting into account both Mi and Sigma (uncertain Mi and Sigma if agent has non-zero uncertainty)
 */
public class ChangingDecisionByMiAndSigmaImpl implements GameDecisionService{

	double param;
	double startParamValue;
	
	/**
	 * @param startParamValue It's usually 0.5, if someone wants more safety then more than 0.5 when reckless people have less than 0.5
	 */
	public ChangingDecisionByMiAndSigmaImpl(double startParamValue) {
		this.startParamValue = startParamValue;
		this.param = startParamValue;
	}
	public static void main(String... args) {
		ChangingDecisionByMiAndSigmaImpl x = new ChangingDecisionByMiAndSigmaImpl(0.0);
		System.out.println(Tools.round(x.nonstandardCdf(0.0, -0.49, 1.02)));
	}
	@Override
	public boolean ifPlayGame(PersonAgent personAgent, Game game/*, double trust*/) {
//		if(trust<=0.0) {
//			return false;
//		}
//		else {
			DoublePair uMi = game.getUncertainMi(personAgent.getUncertaintyMi());
			DoublePair uSigma = game.getUncertainSigma(personAgent.getUncertaintySigma());
	    	double meanUMi = Tools.round((uMi.getR()+uMi.getL())/2);
	    	double meanUSigma = Tools.round((uSigma.getR()+uSigma.getL())/2);
	    	
	    	if( nonstandardCdf(0.0, meanUMi, meanUSigma) >= param)
	    		return true;
	    	return false;	
//		}
	}

	@Override
	public void reactOnGameResult(double result, double wealth) {
		param = startParamValue -wealth/100;//TODO get some logical value
		if(param < startParamValue - 0.25)
			param = startParamValue - 0.25;
		else if(param > startParamValue + 0.25)
			param = startParamValue + 0.25;
		
	}
	
	public String toString() {
		return "ChangingDecisionByMiAndSigmaImpl," + startParamValue;
	}
	
	/**
	 * @return probability of getting higher number than given x in non-standard normal distribution
	 * 
	 * firstly standardize distribution and getting value by cdf function
	 */
	public double nonstandardCdf(double x, double mi, double sigma) {
		return (1-cdf((x-mi)/sigma));
	}

	/**
	 * 
	 * Cumulative distribution function
	 * http://en.wikipedia.org/wiki/Normal_distribution
	 * returns probability of getting lower number than given in standard normal distribution
	 */
	
	public double cdf(double x) {
		double sum = x;
		double value = x;
		for(int i = 1; i < 100; i++) {
			value = value*x*x/(2*i+1);
			sum = sum+value;
		}
		double result = 0.5 + (sum/Math.sqrt(2*Math.PI))*Math.exp(-(x*x)/2);
		return result;
	}

}
