package sp.simulation.game.decision;

import sp.simulation.agent.PersonAgent;
import sp.simulation.game.Game;

public interface GameDecisionService {
	public boolean ifPlayGame(PersonAgent personAgent, Game game/*, double trust*/);
	public void reactOnGameResult(double result, double wealth);
}
