package component;

import main.Bot;
import bwapi.Unit;
import bwapi.UnitType;

public class ArmyProductionManager implements Component
{
	Bot root;
	
	public ArmyProductionManager(Bot r) {
		root = r;
	}
	
	@Override
	public String getComponentName() {
		return "ArmyProductionManager";
	}
	
	@Override
	public void onFrame() {
		
		for(Unit u : root.self.getUnits())
		{
			int myZealot = root.self.visibleUnitCount(UnitType.Protoss_Zealot);
			int myDragoon = root.self.visibleUnitCount(UnitType.Protoss_Dragoon);
			
			/*if(u.getType() == UnitType.Protoss_Gateway && u.isTraining() == false && root.frameInfo.remainGas > 300)
			{
				if(root.frameInfo.remainMinerals >= UnitType.Protoss_Dark_Templar.mineralPrice() && root.frameInfo.remainGas >= UnitType.Protoss_Dark_Templar.gasPrice())
				{
					u.train(UnitType.Protoss_Dark_Templar);
					root.frameInfo.remainMinerals -= UnitType.Protoss_Dark_Templar.mineralPrice();
					root.frameInfo.remainGas -= UnitType.Protoss_Dark_Templar.gasPrice();
				}
			}*/
			
			if(u.getType() == UnitType.Protoss_Gateway && u.isTraining() == false && (myZealot > myDragoon - 1 || root.frameInfo.remainGas > 300))
			{
				if(root.frameInfo.remainMinerals >= UnitType.Protoss_Dragoon.mineralPrice() && root.frameInfo.remainGas >= UnitType.Protoss_Dragoon.gasPrice())
				{
					u.train(UnitType.Protoss_Dragoon);
					root.frameInfo.remainMinerals -= UnitType.Protoss_Dragoon.mineralPrice();
					root.frameInfo.remainGas -= UnitType.Protoss_Dragoon.gasPrice();
				}
			}
			
			if(u.getType() == UnitType.Protoss_Gateway && u.isTraining() == false && myDragoon > myZealot - 1)
			{
				if(root.frameInfo.remainMinerals >= UnitType.Protoss_Zealot.mineralPrice() && root.frameInfo.remainGas >= UnitType.Protoss_Zealot.gasPrice())
				{
					u.train(UnitType.Protoss_Zealot);
					root.frameInfo.remainMinerals -= UnitType.Protoss_Zealot.mineralPrice();
					root.frameInfo.remainGas -= UnitType.Protoss_Zealot.gasPrice();
				}
			}
			



			if(u.getType() == UnitType.Protoss_Robotics_Facility && u.isTraining() == false)
			{
				if(root.frameInfo.remainMinerals >= UnitType.Protoss_Observer.mineralPrice() && root.frameInfo.remainGas >= UnitType.Protoss_Observer.gasPrice() && root.self.visibleUnitCount(UnitType.Protoss_Observer) < 2)
				{
					u.train(UnitType.Protoss_Observer);
					root.frameInfo.remainMinerals -= UnitType.Protoss_Dragoon.mineralPrice();
					root.frameInfo.remainGas -= UnitType.Protoss_Dragoon.gasPrice();
				}
			}
			
			if(u.getType() == UnitType.Protoss_Stargate && u.isTraining() == false)
			{
				
				if(root.frameInfo.remainMinerals >= UnitType.Protoss_Carrier.mineralPrice() && root.frameInfo.remainGas >= UnitType.Protoss_Carrier.gasPrice())
				{
					u.train(UnitType.Protoss_Carrier);
					root.frameInfo.remainMinerals -= UnitType.Protoss_Carrier.mineralPrice();
					root.frameInfo.remainGas -= UnitType.Protoss_Carrier.gasPrice();
				}
			}
			
			
		}
	}	
}
