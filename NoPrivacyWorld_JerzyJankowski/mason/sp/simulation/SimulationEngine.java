package sp.simulation;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sim.engine.*;
import sim.field.grid.*;
import sp.simulation.agent.PersonAgent;
import sp.simulation.agent.PersonAgentMultiGroup;
import sp.simulation.game.creation.GameCreationService;
import sp.simulation.game.creation.IdenticalSimpleGameCreationImpl;
import sp.simulation.game.creation.MiSymmetricalSimpleGameCreationImpl;
import sp.simulation.game.creation.SimpleGameCreationImpl;
import sp.simulation.game.decision.ChangingDecisionByMiAndSigmaImpl;
import sp.simulation.game.decision.ChangingDecisionByMiImpl;
import sp.simulation.game.decision.GameDecisionService;
import sp.simulation.game.decision.SimpleDecisionByMiImpl;
import sp.simulation.tools.Tools;

public class SimulationEngine extends SimState
{
    private static final long serialVersionUID = 1;
	
    private static int stepsLimit = 50; // number of steps, each step consists of each agent trying to initiate game with random agent (not himself)
    private static int worldsNumber = 3;
    private static int worldNumber;
    
    private Map<Integer, PersonAgent> personAgentMap;
    private Object[] agentIdValues; //used to randomize one of agent id for partner-in-game creating purposes
    
    private int rawAgentNumber = 0;
    private ArrayList<RawAgent> rawAgentList;
    
    private PrintWriter resultWriter;
    private PrintWriter agentsWriter;
    
    public static void main(String[] args){
    	
    	if(args.length > 1) {
    	    worldsNumber = Integer.parseInt(args[1])-1;    		
    	}

    	if(args.length > 0) {
    	    stepsLimit = Integer.parseInt(args[0]);    		
    	}
    	
        SimulationEngine simulationEngine = new SimulationEngine(System.currentTimeMillis());
        simulationEngine.runSimulation();
        System.exit(0);
    }
    
    public SimulationEngine(long seed) {
        super(seed);
    }

    private void runSimulation() {
    	
    	loadCSVToRawAgentList();
    	
    	try {
			resultWriter = new PrintWriter("output/results.csv", "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    	
    	for(int i = worldsNumber; i >= 0 ; i--) {

	    	start(i);	//also runs start()
	    	
	        while(schedule.getSteps() < stepsLimit){
	            if (!schedule.step(this))
	                break;
	        }
	        
	        finish();
    	}
    	resultWriter.close();
    }
    
    private void loadCSVToRawAgentList() {
    	ArrayList<List<String>> listOfListsDescribingAgentsInCSV = Tools.readCSV();
    	rawAgentList = new ArrayList<RawAgent>();
    	
		for(List<String> ls : listOfListsDescribingAgentsInCSV) {
			rawAgentList.add(new RawAgent(ls));
		}
    }

    /**
     * set writers for log files with different names between each iterations 
     */
    public void start(int i) {
    	worldNumber = i;
		try {
			agentsWriter = new PrintWriter("output/agents-" + i + ".csv", "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    	PersonAgent.setPrinter("output/log-" + i + ".csv");
    	start();
    }
    
    public void start(){
    	
        super.start();
    	PersonAgentMultiGroup.clear();
    	PersonAgentMultiGroup.setWorldNumber(worldNumber);
        seedPersonAgentArray();
    	agentIdValues = personAgentMap.keySet().toArray();
    }

    public void finish(){
    	
        super.finish();
        resultWriter.print(PersonAgentMultiGroup.getResults());
    	agentsWriter.close();
    	PersonAgent.closePrinter();
    }

    
    /**
     * randomly seed all the people and add them to schedule
     * could use multiplyPeopleAgent method to create sets of identical personAgents
     */
    void seedPersonAgentArray() {
    	
    	personAgentMap = new HashMap<Integer, PersonAgent>();
    	PersonAgent.Builder.resetLastNumber();//to start agent's id always from 1
    	
		for(RawAgent ra : rawAgentList) {
			multiplyPeopleAgent(ra.getPersonAgent(), ra.getMultiply(), ra.getGroupId());
		}
    }
    
    /**
     * multiply one agent and build group to aggregate wealth for that group
     * changes uncertaintyMi and uncertaintySigma according to present worldNumber and worldsNumber at all
     * @param patternPerson PersonAgent which is used as pattern to multiply
     * @param times number of cloned PersonAgents
     * @param groupId identifier of a group which could contain each of multiplied PersonAgent for summary purposes. At the end of each subsimulation there will be summary with mean wealth for groups.
     * @return
     */
    void multiplyPeopleAgent(PersonAgent patternPerson, int times, Integer groupId) {
    	
    	double wealth = patternPerson.getWealth();
        double willingnessToInitiateGame = patternPerson.getWillingnessToInitiateGame();
        double uncertaintyMi = Tools.round(patternPerson.getUncertaintyMi()*worldNumber/worldsNumber);
        double uncertaintySigma = Tools.round(patternPerson.getUncertaintySigma()*worldNumber/worldsNumber);
        GameDecisionService gds = patternPerson.getGameDecisionService();
        GameCreationService gcs = patternPerson.getGameCreationService();
        PersonAgent p;
        
    	for(int number=0; (number<times); number++) {
    		
    		p = new PersonAgent.Builder(random).wealth(wealth).willingnessToInitiateGame(willingnessToInitiateGame)
    				.uncertaintyMi(uncertaintyMi).uncertaintySigma(uncertaintySigma)
    				.gameCreationService(gcs).gameDecisionService(gds)
    				.build();
    		
    		personAgentMap.put(p.getNumber(), p);
    		if(groupId != null)
    			PersonAgentMultiGroup.getPersonAgentMultiGroup(groupId,this).addAgent(p.getNumber());
            schedule.scheduleRepeating(p);
            
            agentsWriter.println(p.toLogString());
    	}
    }
    
    public int getRandomPersonAgentId () {
    	return (int) agentIdValues[random.nextInt(agentIdValues.length)];
    }
    
	public int getStepsLimit() {
		return stepsLimit;
	}

	public void setStepsLimit(int stepsLimit) {
		this.stepsLimit = stepsLimit;
	}  
	
	public PersonAgent getPersonAgent(int index) {
		return personAgentMap.get(index);
	}
	
	public void setPersonAgent(int index, PersonAgent personAgent) {
		personAgentMap.put(index, personAgent);
	}
    /**
     * This subclass parses list of string from csv, checks correctness, builds PersonAgents, contains them to multiply in the future
     */
    private class RawAgent {
    	private int multiply;
    	private PersonAgent personAgent;
    	private Integer groupId;
    	
    	public RawAgent(List<String> as) {
    		PersonAgent.Builder builder = new PersonAgent.Builder(random);
    		int i = 0;
    		double parameter = 0.5;
    		rawAgentNumber++;
    		if(as.size() == 8) {
	    		if(as.get(i).trim().length()>0)
	    			groupId = Integer.parseInt(as.get(i).trim());
	    		else
	    			groupId = null;
	    		i++; //1
	    		if(as.get(i).trim().length()>0)
	    			multiply = Integer.parseInt(as.get(i).trim());
	    		else
	    			multiply = 1;
	    		i++; //2
	    		if(as.get(i).trim().length()>1)
	    			builder = builder.willingnessToInitiateGame(Double.parseDouble(as.get(i).trim()));
	    		i++; //3
	    		if(as.get(i).trim().length()>1)
	    			builder = builder.uncertaintyMi(Double.parseDouble(as.get(i).trim()));
	    		i++; //4
	    		if(as.get(i).trim().length()>1)
	    			builder = builder.uncertaintySigma(Double.parseDouble(as.get(i).trim()));
	    		i++; //5
	    		if(as.get(i).trim().length()>1)
		    		switch(as.get(i).trim()){
		    			case "SimpleGameCreationImpl" : {
		    				builder = builder.gameCreationService(new SimpleGameCreationImpl());
		    			} break;
		    			case "IdenticalSimpleGameCreationImpl" : {
		    				builder = builder.gameCreationService(new IdenticalSimpleGameCreationImpl());
						} break;
		    			case "MiSymmetricalSimpleGameCreationImpl" : {
		    				builder = builder.gameCreationService(new MiSymmetricalSimpleGameCreationImpl());
						} break;
						default : {
							System.out.println("[ERROR in input.csv at agent " + rawAgentNumber + "] Nieznana wartoœæ \"" + as.get(i).trim() + " dla agenta jako algorytm tworzenia gry");
						}
		    				
		    		}
	    		i++; //6
	    		if(as.get(i).trim().length()>1)
		    		switch(as.get(i).trim()){
		    			case "SimpleDecisionByMiImpl" : {
		    				builder = builder.gameDecisionService(new SimpleDecisionByMiImpl());
		    			} break;
		    			case "ChangingDecisionByMiImpl" : {
		    	    		if(as.get(i+1).trim().length()>1)
		    	    			parameter = Double.parseDouble(as.get(i+1).trim());
		    	    		else
								System.out.println("[ERROR in input.csv at agent " + rawAgentNumber + "] Brak wartoœci dla agenta jako parametr algorytmu wyboru gry");
		    	    			
		    				builder = builder.gameDecisionService(new ChangingDecisionByMiImpl(parameter));
						} break;
		    			case "ChangingDecisionByMiAndSigmaImpl" : {
		    	    		if(as.get(i+1).trim().length()>1)
		    	    			parameter = Double.parseDouble(as.get(i+1).trim());
		    	    		else
								System.out.println("[ERROR in input.csv at agent " + rawAgentNumber + "] Brak wartoœci dla agenta jako parametr algorytmu wyboru gry");
		    	    			
		    				builder = builder.gameDecisionService(new ChangingDecisionByMiAndSigmaImpl(parameter));
						} break;
						default : {
							System.out.println("[ERROR in input.csv at agent " + rawAgentNumber + "] Nieznana wartoœæ \"" + as.get(i).trim() + "\" dla agenta jako algorytm wyboru gry");
						}
		    				
		    		}
	    		i+=2; //8
	    		personAgent = builder.build();
    		}
    		else {
				System.out.println("[ERROR in input.csv at agent " + rawAgentNumber + "] Niepoprawna liczba parametrów, " + as.size() + " zamiast 8. SprawdŸ w dokumentacji.");
			}
		}
    	
		public int getMultiply() {
			return multiply;
		}
		public Integer getGroupId() {
			return groupId;
		}
		public PersonAgent getPersonAgent() {
			return personAgent;
		}
		
		public String toString() {
			return "multiply=" + multiply + ", groupId=" + groupId + ", " + personAgent;
		}
    	
    }

  
}
