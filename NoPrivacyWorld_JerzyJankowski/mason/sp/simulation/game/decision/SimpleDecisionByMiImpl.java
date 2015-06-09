package sp.simulation.game.decision;

import sp.simulation.agent.PersonAgent;
import sp.simulation.game.Game;
import sp.simulation.tools.DoublePair;

public class SimpleDecisionByMiImpl implements GameDecisionService{
	/**
     * simple check if agent want to play - he checks if mean of left constraint and right constraint is greater than 0.0.
     */
	@Override
	public boolean ifPlayGame(PersonAgent personAgent, Game game/*, double trust*/) {
//		if(trust<=0.0) {
//			return false;
//		}
//		else {
			DoublePair uMi = game.getUncertainMi(personAgent.getUncertaintyMi());
	    	double mean = (uMi.getR()+uMi.getL())/2;
	    	
	    	if( mean >= 0.0)
	    		return true;
	    	return false;	
//		}
	}

	@Override
	public void reactOnGameResult(double result, double wealth) {
		// do nothing for that implementation
		
	}
	
	public String toString() {
		return "SimpleDecisionByMiImpl,";
	}

}
