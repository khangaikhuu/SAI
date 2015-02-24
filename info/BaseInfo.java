package info;

import java.util.ArrayList;
import java.util.List;

import bwapi.Position;
import bwapi.Unit;

public class BaseInfo
{
	public Unit base;
	public List <Unit> worker;
	public List <Unit> gasStation;
	public Position buildingArea;
	public boolean scheduledGasStation;
	
	public BaseInfo() {
		worker = new ArrayList<Unit>();
		gasStation = new ArrayList<Unit>();
	}
}
