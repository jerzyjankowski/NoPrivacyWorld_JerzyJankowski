package sp.simulation.game.decision;

import sp.simulation.agent.PersonAgent;
import sp.simulation.game.Game;
import sp.simulation.tools.DoublePair;
/**
 * works like SimpleDecisionByMiImple but mean must be greater or equal to some param which will be changing as agent will be winning or losing his wealth
 * when he is losing then he is more careful, the opposite when he is winning. param has min and max value so he want be changing forever
 *
 */
public class ChangingDecisionByMiImpl implements GameDecisionService{
	
	private double startParamValue;
	private double param;
	
	/**
	 * @param startParamValue usually 0.0, if someone wants more safety then more than 0.0 when reckless people have less than 0.0
	 */
	public ChangingDecisionByMiImpl(double startParamValue) {
		this.startParamValue = startParamValue;
	}
	
	@Override
	public boolean ifPlayGame(PersonAgent personAgent, Game game/*, double trust*/) {
//		if(trust<=0.0) {
//			return false;
//		}
//		else {
			DoublePair uMi = game.getUncertainMi(personAgent.getUncertaintyMi());
	    	double mean = (uMi.getR()+uMi.getL())/2;
	    	
	    	if( mean >= param)
	    		return true;
	    	return false;	
//		}
	}

	@Override
	public void reactOnGameResult(double result, double wealth) {
		double param = startParamValue - wealth/100;
		if(param < -1.5)
			param = -1.5;
		else if(param > 1.5)
			param = 1.5;
	}
	
	public String toString() {
		return "ChangingDecisionByMiImpl," + startParamValue;
	}

}
