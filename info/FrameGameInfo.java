package info;

import java.util.ArrayList;
import java.util.List;

import bwapi.Unit;
import main.Bot;

public class FrameGameInfo
{
	public int remainMinerals;
	public int remainGas;
	public int frameNumber;
	public boolean[] isAlive;
	Bot root;
	int MaxID = 1000000;
	List <Integer> alives;
	
	public FrameGameInfo(Bot r) {
		root = r;
		isAlive = new boolean[MaxID];
		alives = new ArrayList<Integer>();
	}
	
	public void onFrameInit()
	{
		remainMinerals = root.self.minerals();
		remainGas = root.self.gas();
		frameNumber = root.game.getFrameCount();
		
		for(Integer i : alives)
			isAlive[i.intValue()] = false;
		alives.clear();
		
		for(Unit u : root.self.getUnits())
		{
			alives.add(u.getID());
			isAlive[u.getID()] = true;
		}
		
	}
}
