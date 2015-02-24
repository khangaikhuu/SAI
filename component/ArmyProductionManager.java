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
				
				if(root.frameInfo.remainMinerals >= UnitType.Protoss_Zealot.mineralPrice() + 300 && root.frameInfo.remainGas >= UnitType.Protoss_Zealot.gasPrice())
				{
					u.train(UnitType.Protoss_Zealot);
					root.frameInfo.remainMinerals -= UnitType.Protoss_Zealot.mineralPrice();
					root.frameInfo.remainGas -= UnitType.Protoss_Zealot.gasPrice();
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
