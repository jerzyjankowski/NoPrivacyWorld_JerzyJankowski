package sp.simulation.agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sp.simulation.SimulationEngine;
import sp.simulation.tools.Tools;

/**
 * It's whole purpose is to make readable summary at the and of each subsimulation when is printed for each group mean of their agent's wealth.
 * Agent doesn't have to belong to any of group but he can belong to maximum one group.
 */
public class PersonAgentMultiGroup {
	
	private int groupId;
	private double meanWealth;
	private SimulationEngine se;
	
	private List<Integer> personAgentIdList = new ArrayList<Integer>();
	
	static private int worldNumber;
	static private Map<Integer, PersonAgentMultiGroup> pamgMap = new HashMap<Integer, PersonAgentMultiGroup>();

	private PersonAgentMultiGroup(int groupId, SimulationEngine se) {
		this.groupId = groupId;
		this.se = se;
	}
	
	public void addAgent(int agentId) {
		if( !personAgentIdList.contains(agentId))
			personAgentIdList.add(agentId);
	}
	
	/**
	 * 
	 * @param groupId identifier of group to which later we will try to add identifier of agent
	 * @param se SimulationEngine reference to get to the agent's wealth later on for summary purpose
	 * @return new PersonAgentMultiGroup if there were not any or old PersonAgentMultiGroup if there was
	 */
	public static PersonAgentMultiGroup getPersonAgentMultiGroup(int groupId, SimulationEngine se) {
		if(!pamgMap.containsKey(new Integer(groupId))) {
			pamgMap.put(new Integer(groupId), new PersonAgentMultiGroup(groupId, se));
		}
		return pamgMap.get(groupId);
	}
	
	private double getMeanWealth() {
		double sum = 0.0;
		for(int i : personAgentIdList)
			sum += se.getPersonAgent(i).getWealth();
		meanWealth = sum/personAgentIdList.size();
		return meanWealth;
	}
	
	public void getAgentList() {
		System.out.println("getAgentList " + groupId + ", " + personAgentIdList);
	}
	
	public static String getResults() {
		String result = "";
		for(PersonAgentMultiGroup pamg : pamgMap.values())
			result += pamg;
		return result;
	}
	
	public static void clear() {
		pamgMap.clear();
	}
	
	public static int getWorldNumber() {
		return worldNumber;
	}

	public static void setWorldNumber(int worldNumber) {
		PersonAgentMultiGroup.worldNumber = worldNumber;
	}
	
	public String toString() {
		return worldNumber + "," + groupId + "," + Tools.round(getMeanWealth()) + "\n";
	}
}
