package strategy;

import bwapi.UnitType;
import bwapi.UpgradeType;
import bwapi.Utils;
import main.Bot;

public class ThreeGatewayDragoon extends Strategy {

	public ThreeGatewayDragoon(Bot r) {
		super(r);
		
	}
	
	
	@Override
	public void onFrame() {
		
		if(root.info.getReadyBases() == 1)
		{
			root.goal.setGoal(UnitType.Protoss_Gateway, 3);
			root.goal.setGoalAtleast(UnitType.Protoss_Dragoon, 50);
			root.goal.setGoal(UpgradeType.Singularity_Charge);
		}
		
		if(root.info.getReadyBases() == 2)
		{
			//root.goal.setGoal(UnitType.Protoss_Observer, 2);
			root.goal.setGoal(UnitType.Protoss_Stargate, 5);
			root.goal.setGoal(UnitType.Protoss_Dragoon, 0);
			root.goal.setGoalAtleast(UnitType.Protoss_Scout, 50);
		
			
		}
		
		
		
		double powerMe = root.util.computePower(root.info.getMyFinishedCombatUnits());
		double powerEnemy = root.util.computePowerEnemy(root.enemyInfo.getEnemyCombatUnits());
		root.guiManager.addDebugInfo(Utils.formatText(powerMe + " v.s. " + powerEnemy, Utils.Green));
		
		if(root.blackboard.getIsAttacking())
		{
			if(powerMe < powerEnemy + 12)
				root.blackboard.setIsAttacking(false);
		}
		else
		{
			if(powerEnemy < powerMe - 24)
				root.blackboard.setIsAttacking(true);
		}
		
		if(root.blackboard.getIsAttacking())
		{
			if(root.enemyInfo.startPoint != null)
				root.blackboard.setAttackPosition(root.enemyInfo.startPoint);
		}
		else
		{
			root.blackboard.setAttackPosition(root.info.bases[1].position);
		}
		
		
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
