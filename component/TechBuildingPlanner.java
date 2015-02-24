package component;

import main.Bot;
import bwapi.UnitType;

public class TechBuildingPlanner implements Component
{
	
	Bot root;
	
	UnitType[] buildingOrder_type;
	int[] buildingOrder_condition;
	int p;
	
	public TechBuildingPlanner(Bot r)
	{
		root = r;
		
		buildingOrder_type = new UnitType[]{
											UnitType.Protoss_Nexus, 
											UnitType.Protoss_Assimilator,
											UnitType.Protoss_Gateway,
											UnitType.Protoss_Forge,
											UnitType.Protoss_Cybernetics_Core,
											UnitType.Protoss_Stargate,
											UnitType.Protoss_Fleet_Beacon
											};
		buildingOrder_condition = new int[]{15,
											15,
											16,
											16,
											18,
											21,
											23
											};
		p = 0;
	}
	
	@Override
	public String getComponentName() {
		return "TechBuildingPlanner";
	}

	@Override
	public void onFrame() {
		int populationCount = root.self.supplyUsed() / 2;
		if(p < buildingOrder_type.length)
		{
			if(populationCount >= buildingOrder_condition[p])
			{
				task.ScheduleBuilding t = new task.ScheduleBuilding(root, buildingOrder_type[p], root.gameInfo.myBase.get(0));
				root.currentTasks.add(t);
				p ++;
			}
		}
		
		if(util.General.isNotBeginning(root))
		{
			if(util.General.countBuildingIncludeScheduled(root, UnitType.Protoss_Stargate) < util.General.countBuilding(root, UnitType.Protoss_Nexus) * 2 && root.frameInfo.remainMinerals > 300 && root.frameInfo.remainGas > 200)
			{
				task.ScheduleBuilding t = new task.ScheduleBuilding(root, UnitType.Protoss_Stargate, root.gameInfo.myBase.get(0));
				root.currentTasks.add(t);
				root.frameInfo.remainMinerals -= UnitType.Protoss_Stargate.mineralPrice();
				root.frameInfo.remainGas -= UnitType.Protoss_Stargate.gasPrice();
			}

			if(util.General.countBuildingIncludeScheduled(root, UnitType.Protoss_Nexus) < 2 && (root.frameInfo.remainMinerals > 600 || root.self.supplyUsed() > 50 * 2))
			{
				task.ScheduleBuilding t = new task.ScheduleBuilding(root, UnitType.Protoss_Nexus, root.gameInfo.myBase.get(0));
				root.currentTasks.add(t);
				root.frameInfo.remainMinerals -= UnitType.Protoss_Nexus.mineralPrice();
				root.frameInfo.remainGas -= UnitType.Protoss_Nexus.gasPrice();
			}
			
			if(util.General.countBuildingIncludeScheduled(root, UnitType.Protoss_Nexus) < 3 && (root.frameInfo.remainMinerals > 600 || root.self.supplyUsed() > 100 * 2))
			{
				task.ScheduleBuilding t = new task.ScheduleBuilding(root, UnitType.Protoss_Nexus, root.gameInfo.myBase.get(0));
				root.currentTasks.add(t);
				root.frameInfo.remainMinerals -= UnitType.Protoss_Nexus.mineralPrice();
				root.frameInfo.remainGas -= UnitType.Protoss_Nexus.gasPrice();
			}
			
			if(util.General.countBuildingIncludeScheduled(root, UnitType.Protoss_Nexus) < 4 && (root.frameInfo.remainMinerals > 600 || root.self.supplyUsed() > 130 * 2))
			{
				task.ScheduleBuilding t = new task.ScheduleBuilding(root, UnitType.Protoss_Nexus, root.gameInfo.myBase.get(0));
				root.currentTasks.add(t);
				root.frameInfo.remainMinerals -= UnitType.Protoss_Nexus.mineralPrice();
				root.frameInfo.remainGas -= UnitType.Protoss_Nexus.gasPrice();
			}
			
			
			
		}
		
		
	}
}
