package info;

import java.util.ArrayList;
import java.util.List;

import bwapi.Position;
import bwapi.Unit;

public class GlobalGameInfo
{
	public List <BaseInfo> myBase;
	public Position opponentMainBase;
    int MaxID = 1000000;
    public boolean[] unitOnDuty;
    public int[] lastAttackFrame;
    
    public Unit myFirstBase()
    {
    	if(myBase.size() == 0)
    		return null;
    	return myBase.get(0).base;
    }
    
    public void init()
    {
    	unitOnDuty = new boolean[MaxID];
    	lastAttackFrame = new int[MaxID];
    	
    	for(int i = 0; i < MaxID; i++)
    	{
    		unitOnDuty[i] = false;
    		lastAttackFrame[i] = 0;
    	}
    	myBase = new ArrayList<BaseInfo>();
    }
}