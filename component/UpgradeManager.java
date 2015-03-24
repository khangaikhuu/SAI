package component;

import bwapi.Unit;
import bwapi.UnitType;
import bwapi.UpgradeType;
import main.Bot;

public class UpgradeManager implements Component
{
	Bot root;
	
	public UpgradeManager(Bot r) {
		root = r;
	}
	
	@Override
	public String getComponentName() {
		return "UpgradeManager";
	}
	
	@Override
	public void onFrame() {
		for(Unit u : root.self.getUnits())
		{
			if(u.getType() == UnitType.Protoss_Fleet_Beacon && root.self.isUpgrading(UpgradeType.Carrier_Capacity) == false)
			{
				if(u.isUpgrading() == false)
				{
					u.upgrade(UpgradeType.Carrier_Capacity);
				}
			}
			if(u.getType() == UnitType.Protoss_Cybernetics_Core && root.self.isUpgrading(UpgradeType.Singularity_Charge) == false)
			{
				if(u.isUpgrading() == false)
				{
					u.upgrade(UpgradeType.Singularity_Charge);
				}
			}
			if(u.getType() == UnitType.Protoss_Citadel_of_Adun && root.self.isUpgrading(UpgradeType.Leg_Enhancements) == false)
			{
				if(u.isUpgrading() == false)
				{
					u.upgrade(UpgradeType.Leg_Enhancements);
				}
			}
		}
	}
	
}