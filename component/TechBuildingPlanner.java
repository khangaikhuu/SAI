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
		
		buildingOrder_type = new UnitType[]{UnitType.Protoss_Assimilator, 
											UnitType.Protoss_Gateway,
											UnitType.Protoss_Cybernetics_Core,
											UnitType.Protoss_Citadel_of_Adun,
											UnitType.Protoss_Gateway,
											UnitType.Protoss_Templar_Archives,
											UnitType.Protoss_Gateway,
											UnitType.Protoss_Gateway,
											UnitType.Protoss_Pylon,
											UnitType.Protoss_Gateway,
											};
		buildingOrder_condition = new int[]{10,
											12,
											15,
											17,
											18,
											20,
											23,
											25,
											28,
											33};
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
				task.ScheduleBuilding t = new task.ScheduleBuilding(root, buildingOrder_type[p]);
				root.currentTasks.add(t);
				p ++;
			}
			
		}
	}
}
