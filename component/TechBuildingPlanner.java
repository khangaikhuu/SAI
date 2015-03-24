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
											UnitType.Protoss_Gateway,
											UnitType.Protoss_Assimilator,
											UnitType.Protoss_Cybernetics_Core,
											UnitType.Protoss_Gateway,
											UnitType.Protoss_Citadel_of_Adun,
											UnitType.Protoss_Templar_Archives,
											};
		buildingOrder_condition = new int[]{12,
											12,
											15,
											16,
											50,
											80,
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
			if(util.General.countBuilding(root, UnitType.Protoss_Gateway)  < 3 * root.gameInfo.myBase.size() &&  (root.frameInfo.remainMinerals > 150))
			{
				task.ScheduleBuilding t = new task.ScheduleBuilding(root, UnitType.Protoss_Gateway, root.gameInfo.myBase.get(root.gameInfo.myBase.size()-1));
				root.currentTasks.add(t);
				root.frameInfo.remainMinerals -= UnitType.Protoss_Gateway.mineralPrice();
				root.frameInfo.remainGas -= UnitType.Protoss_Gateway.gasPrice();
			}
			
			if(util.General.countBuildingIncludeScheduled(root, UnitType.Protoss_Nexus) < 2 && (root.frameInfo.remainMinerals > 600 || root.self.supplyUsed() > 45 * 2))
			{
				task.ScheduleBuilding t = new task.ScheduleBuilding(root, UnitType.Protoss_Nexus, root.gameInfo.myBase.get(root.gameInfo.myBase.size()-1));
				root.currentTasks.add(t);
				root.frameInfo.remainMinerals -= UnitType.Protoss_Nexus.mineralPrice();
				root.frameInfo.remainGas -= UnitType.Protoss_Nexus.gasPrice();
			}
			
			if(util.General.countBuildingIncludeScheduled(root, UnitType.Protoss_Nexus) < 3 && (root.frameInfo.remainMinerals > 600 || root.self.supplyUsed() > 85 * 2))
			{
				task.ScheduleBuilding t = new task.ScheduleBuilding(root, UnitType.Protoss_Nexus, root.gameInfo.myBase.get(root.gameInfo.myBase.size()-1));
				root.currentTasks.add(t);
				root.frameInfo.remainMinerals -= UnitType.Protoss_Nexus.mineralPrice();
				root.frameInfo.remainGas -= UnitType.Protoss_Nexus.gasPrice();
			}
			
			if(util.General.countBuildingIncludeScheduled(root, UnitType.Protoss_Nexus) < 4 && (root.frameInfo.remainMinerals > 600 || root.self.supplyUsed() > 105 * 2))
			{
				task.ScheduleBuilding t = new task.ScheduleBuilding(root, UnitType.Protoss_Nexus, root.gameInfo.myBase.get(root.gameInfo.myBase.size()-1));
				root.currentTasks.add(t);
				root.frameInfo.remainMinerals -= UnitType.Protoss_Nexus.mineralPrice();
				root.frameInfo.remainGas -= UnitType.Protoss_Nexus.gasPrice();
			}
		}
		
	}
}
