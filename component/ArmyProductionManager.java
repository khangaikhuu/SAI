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
			if(u.getType() == UnitType.Protoss_Gateway && u.isTraining() == false)
			{
				if(root.frameInfo.remainMinerals >= UnitType.Protoss_Dark_Templar.mineralPrice() && root.frameInfo.remainGas >= UnitType.Protoss_Dark_Templar.gasPrice() && root.self.isUnitAvailable(UnitType.Protoss_Dark_Templar))
				{
					u.train(UnitType.Protoss_Dark_Templar);
					root.frameInfo.remainMinerals -= UnitType.Protoss_Dark_Templar.mineralPrice();
					root.frameInfo.remainGas -= UnitType.Protoss_Dark_Templar.gasPrice();
				}
				else if(root.self.isUnitAvailable(UnitType.Protoss_Dark_Templar) == false && root.frameInfo.remainMinerals >= UnitType.Protoss_Dragoon.mineralPrice() && root.frameInfo.remainGas >= UnitType.Protoss_Dragoon.gasPrice() && root.self.isUnitAvailable(UnitType.Protoss_Dragoon))
				{
					u.train(UnitType.Protoss_Dragoon);
					root.frameInfo.remainMinerals -= UnitType.Protoss_Dragoon.mineralPrice();
					root.frameInfo.remainGas -= UnitType.Protoss_Dragoon.gasPrice();
				}
				else if((root.self.isUnitAvailable(UnitType.Protoss_Dark_Templar) == false && root.self.isUnitAvailable(UnitType.Protoss_Dragoon) == false && root.frameInfo.remainMinerals >= UnitType.Protoss_Zealot.mineralPrice() && root.frameInfo.remainGas >= UnitType.Protoss_Zealot.gasPrice() && root.self.isUnitAvailable(UnitType.Protoss_Zealot)) || root.frameInfo.remainMinerals > 300)
				{
					u.train(UnitType.Protoss_Zealot);
					root.frameInfo.remainMinerals -= UnitType.Protoss_Zealot.mineralPrice();
					root.frameInfo.remainGas -= UnitType.Protoss_Zealot.gasPrice();
				}
			}
		}	
	}	
}
