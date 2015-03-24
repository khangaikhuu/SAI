package component;

import main.Bot;
import task.Task;
import bwapi.Unit;
import bwapi.UnitType;

public class SupplyManager implements Component
{
	Bot root;
	
	public SupplyManager(Bot r) {
		root = r;
	}
	
	@Override
	public String getComponentName() {
		return "SupplyManager";
	}
	
	@Override
	public void onFrame() {
		int usedSupply = root.self.supplyUsed();
		int currentSupply = root.self.supplyTotal();
		int buildingSupply = 0, scheduledSupply = 0;
		for(Unit u : root.self.getUnits())
		{
			if(u.getType() == UnitType.Protoss_Pylon && u.isBeingConstructed())
				buildingSupply += 8 * 2;
		}
		for(Task t : root.currentTasks)
		{
			if(t.getTaskName().equals("ScheduleBuilding " + UnitType.Protoss_Pylon))
				scheduledSupply += 8 * 2;
		}
		int limit = (currentSupply + buildingSupply + scheduledSupply);
		if(limit < 20)
			limit = limit - 2;
		else if(limit < 40)
			limit = limit - 5;
		else {
			limit = limit * 6 / 7;
		}
			 
			
		if(usedSupply >= limit && (currentSupply + buildingSupply + scheduledSupply) < 200 * 2)
		{
			task.ScheduleBuilding t = new task.ScheduleBuilding(root, UnitType.Protoss_Pylon, root.gameInfo.myBase.get(root.gameInfo.myBase.size() - 1));
			root.currentTasks.add(t);
		}
		
		root.debug.addDebugInfo("\t+Suppy(used/now/build/task) = " + (usedSupply/2) + " / " + (currentSupply/2) + " / " + (buildingSupply/2) + " / " + (scheduledSupply/2) );
	}
	
}