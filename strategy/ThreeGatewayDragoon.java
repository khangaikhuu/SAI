package strategy;

import bwapi.UnitType;
import bwapi.UpgradeType;
import main.Bot;

public class ThreeGatewayDragoon extends Strategy {

	public ThreeGatewayDragoon(Bot r) {
		super(r);
	}
	
	@Override
	public void onFrame() {
		root.goal.setGoal(UnitType.Protoss_Gateway, 3);
		root.goal.setGoalAtleast(UnitType.Protoss_Dragoon, 50);
		root.goal.setGoal(UpgradeType.Singularity_Charge);
	}

	@Override
	public boolean ended() {
		
		return false;
	}

	@Override
	public String getName() {
		
		return "ThreeGatewayDragoon";
	}

}
