package sp.simulation.game.creation;

import sp.simulation.agent.PersonAgent;
import sp.simulation.game.Game;
import sp.simulation.game.GamePair;
import sp.simulation.tools.Fairness;
import ec.util.MersenneTwisterFast;

public class SimpleGameCreationImpl implements GameCreationService {

	@Override
	public GamePair createGame(PersonAgent personAgent,
			MersenneTwisterFast random) {
		Game game1 = new Game(random, Fairness.FAIR), game2 = new Game(random, personAgent.getFairness());

		return new GamePair(game1, game2);
	}
	
	public String toString() {
		return "SimpleGameCreationImpl";
	}
	
}
