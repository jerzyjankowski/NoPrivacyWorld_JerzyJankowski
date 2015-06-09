package sp.simulation.agent;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import ec.util.MersenneTwisterFast;
import sim.engine.*;
import sp.simulation.SimulationEngine;
import sp.simulation.game.Game;
import sp.simulation.game.GamePair;
import sp.simulation.game.creation.GameCreationService;
import sp.simulation.game.creation.MiSymmetricalSimpleGameCreationImpl;
import sp.simulation.game.creation.SimpleGameCreationImpl;
import sp.simulation.game.decision.GameDecisionService;
import sp.simulation.game.decision.SimpleDecisionByMiImpl;
import sp.simulation.tools.Fairness;
import sp.simulation.tools.Tools;

public class PersonAgent implements Steppable
{
    private static final long serialVersionUID = 1;
    
	private MersenneTwisterFast random = new MersenneTwisterFast();
    private int step = 0;
    private double lastWinning;

    private int number;
    private double wealth;
    private double willingnessToInitiateGame;//[0,1]
    private double uncertaintyMi;
    private double uncertaintySigma;
    private Fairness fairness;

	static private double winningInitiator;
	static private double winningPartner;

	private GameDecisionService gameDecisionService;
	private GameCreationService gameCreationService;
    
    private static PrintWriter writer;
    
//    private double[] trustTable;

    
	/**	in builder pattern we get MersenneTwisterFast random because as there is stated in documentation:
	 * 
	 * "The standard MT199937 seeding algorithm uses one of Donald Knuth’s plain-jane linear
	 * congruential generators to fill the Mersenne Twister’s arrays. This means that for a short while the algorithm
	 * will initially be outputting a (very slightly) lower quality random number stream until it warms up. After
	 * about 624 calls to the generator, it’ll be warmed up sufficiently. As a result, in SimState.start(), MASON
	 * primes the MT generator for you by calling nextInt() 1249 times."
	 * 
	 */
    public static class Builder {
    	
    	private static int lastNumber = 0;
    	private final int number;
    	private final MersenneTwisterFast random;
        private double wealth = 0.0;
        private double willingnessToInitiateGame = 0.5; //[0,1]
        private double uncertaintyMi = 0.6;
        private double uncertaintySigma = 1.2;
        private Fairness fairness = Fairness.FAIR;
        private GameDecisionService gameDecisionService = new SimpleDecisionByMiImpl();
        private GameCreationService gameCreationService = new SimpleGameCreationImpl();
        
		public Builder(MersenneTwisterFast random) {
			this.number = this.lastNumber++;
			this.random = random;
		}
		
		public Builder wealth(double wealth) {
			this.wealth = wealth;
			return this;
		}
		
		public Builder willingnessToInitiateGame(double willingnessToInitiateGame) {
			this.willingnessToInitiateGame = willingnessToInitiateGame;
			return this;
		}
		
		public Builder uncertaintyMi(double uncertaintyMi) {
			this.uncertaintyMi = uncertaintyMi;
			return this;
		}
		
		public Builder uncertaintySigma(double uncertaintySigma) {
			this.uncertaintySigma = uncertaintySigma;
			return this;
		}
		
		public Builder fairness(Fairness fairness) {
			this.fairness = fairness;
			return this;
		}
		
		public Builder gameDecisionService(GameDecisionService gameDecisionService) {
			this.gameDecisionService = gameDecisionService;
			return this;
		}
		
		public Builder gameCreationService(GameCreationService gameCreationService) {
			this.gameCreationService = gameCreationService;
			return this;
		}
		
		public PersonAgent build() {
			PersonAgent personAgent = new PersonAgent(this);
			return new PersonAgent(this);
		}  
		
		public static void resetLastNumber() {
			lastNumber = 0;
		}
    }
    
    private PersonAgent(Builder builder) {
    	this.number = builder.number;
    	this.random = builder.random;
    	this.wealth = builder.wealth;
    	this.willingnessToInitiateGame = builder.willingnessToInitiateGame;
    	this.uncertaintyMi = builder.uncertaintyMi;
    	this.uncertaintySigma = builder.uncertaintySigma;
    	this.fairness = builder.fairness;
    	this.gameDecisionService = builder.gameDecisionService;
    	this.gameCreationService = builder.gameCreationService;
    	
//    	trustTable = new double[1000];
//    	for(int i = 0; i < 1000; i++) {
//    		trustTable[i]=0.01;
//    	}
    }
    
    /**
     * try to initiate the game - sometimes agents doesn't want to, willingnessToInitateGame decides how often he would initiate
     * then check if agent wants to play - some games have to small chances to win in agent's opinion
     * then propose game to random other agent and he checks if want to play that game
     * then play
     */
    public void step(SimState state){
    	step++;
        SimulationEngine simulationEngine = (SimulationEngine)state;
        
//        for(double i : trustTable) {
//    		i+=0.1;
//    	}
    	GamePair gamePair = gameCreationService.createGame(this, random);
    	Game game = gamePair.getL();
    	Game opponentsGame = gamePair.getR();

        int personAgentIndex = getRandomPersonNumber(simulationEngine);
        String type = "";
        winningInitiator = winningPartner = 0.0;
        
        if(ifInitiateGame()) {
        	if(gameDecisionService.ifPlayGame(this, game/*, trustTable[personAgentIndex]*/)) {
        		if(simulationEngine.getPersonAgent(personAgentIndex)
        				.playGameWithMe(number, opponentsGame)) {
        			winningInitiator = playGame(game);
                	type = "PLAYED";
//        			trustTable[personAgentIndex] = Tools.round(trustTable[personAgentIndex]+lastWinning);
        		}
        		else {
                	type = "NOT_PLAYED";
        		}
        	}
        	else {
            	type = "NOT_CHOSEN";
        	}
        }
        else {
        	type = "NOT_INITIATED";
        }
        
        writer.println(step + "," + type + "," + number + "," + winningInitiator + "," + game.toLogString() + "," + 
        		personAgentIndex + "," + winningPartner + "," + opponentsGame.toLogString());
        
    }
       
    public double playGame(Game game) {
    	double value = game.play();
    	changeWealth(value);
    	lastWinning = value;
    	return value;
    }
    
    /**
     * 
     * @param gameInitiatorNumber identifier of game initiating agent
     * @param game game proposed by game initiating agent
     * @return information if game partner of game initiating agent wants to play proposed game
     */
    public boolean playGameWithMe(int gameInitiatorNumber, Game game) {
    	if(gameDecisionService.ifPlayGame(this, game/*, trustTable[gameInitiatorNumber]*/)) {
    		winningPartner = playGame(game);
    		return true;
    	}
    	else return false;
    }
    
    private boolean ifInitiateGame() {
    	double x = random.nextDouble();
        return (x < willingnessToInitiateGame);
    }
    
    private int getRandomPersonNumber(SimulationEngine se){
    	
    	int randomPersonNumber;
    	do{
    		randomPersonNumber = se.getRandomPersonAgentId();
    	}while(randomPersonNumber == number);
    	return randomPersonNumber;
    }
    
	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public double getWealth() {
		return wealth;
	}
	
	public double changeWealth(double delta) {
		wealth = Tools.round(wealth + delta);
		return wealth;
	}
	
	public double getWillingnessToInitiateGame() {
		return willingnessToInitiateGame;
	}

	public void setWillingnessToInitiateGame(double willingnessToInitiateGame) {
		this.willingnessToInitiateGame = willingnessToInitiateGame;
	}
	
	public double getUncertaintyMi() {
		return uncertaintyMi;
	}

	public void setUncertaintyMi(double uncertaintyMi) {
		this.uncertaintyMi = uncertaintyMi;
	}

	public double getUncertaintySigma() {
		return uncertaintySigma;
	}

	public void setUncertaintySigma(double uncertaintySigma) {
		this.uncertaintySigma = uncertaintySigma;
	}
	
    public Fairness getFairness() {
		return fairness;
	}

	public void setFairness(Fairness fairness) {
		this.fairness = fairness;
	}

    public GameDecisionService getGameDecisionService() {
		return gameDecisionService;
	}

	public void setGameDecisionService(GameDecisionService gameDecisionService) {
		this.gameDecisionService = gameDecisionService;
	}

	public GameCreationService getGameCreationService() {
		return gameCreationService;
	}

	public void setGameCreationService(GameCreationService gameCreationService) {
		this.gameCreationService = gameCreationService;
	}
	
	public String toString() {
		
		return "Agent number=" + this.number + "; willingnessToInitiate=" + this.willingnessToInitiateGame + "; uncertaintyMi=" + this.uncertaintyMi + "; uncertaintySigma=" + this.uncertaintySigma 
				+ "; fairness=" + fairness + "; gameCreationService=" + this.gameCreationService + "; gameDecisionService=" + this.gameDecisionService + ";";
	}
	
	public String toLogString() {
		return this.number + "," + willingnessToInitiateGame + "," + uncertaintyMi + "," + uncertaintySigma 
				+ "," + gameCreationService + "," + gameDecisionService;
	}
	
	public static void setPrinter(String fileName) {
		try {
			writer = new PrintWriter(fileName, "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public static void closePrinter() {
		writer.close();
	}
	
	
}
