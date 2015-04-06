package strategy;

import bwapi.Race;
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
			if(root.util.countUnit(UnitType.Protoss_Zealot, false, false, true) >= 1)
				root.goal.setGoalAtleast(UnitType.Protoss_Gateway, 2);
			
			root.goal.setGoalAtleast(UnitType.Protoss_Gateway, 3);
			
			root.goal.setGoalAtleast(UnitType.Protoss_Zealot, Math.min(12, root.enemyInfo.getEnemyUnitByType(UnitType.Protoss_Zealot).size() + 1));
			
			if(root.enemy.getRace() == Race.Protoss)
			{
				if(root.util.countUnit(UnitType.Protoss_Zealot, false, false, true) >= root.enemyInfo.getEnemyUnitByType(UnitType.Protoss_Zealot).size())
				{
					root.goal.setGoalAtleast(UnitType.Protoss_Dragoon, 50);
					root.goal.setGoal(UpgradeType.Singularity_Charge);
				}
			}
			else
			{
				root.goal.setGoalAtleast(UnitType.Protoss_Dragoon, 50);
				root.goal.setGoal(UpgradeType.Singularity_Charge);
			}
		}
		
		if(root.info.getReadyBases() >= 2)
		{
			root.goal.setGoalAtleast(UnitType.Protoss_Zealot, 20);
			root.goal.setGoalAtleast(UnitType.Protoss_Dragoon, 20);
			root.goal.setGoal(UpgradeType.Leg_Enhancements);
			root.goal.setGoalAtleast(UnitType.Protoss_Gateway, 3 * root.info.getReadyBases());
		}
		
		double powerMe = root.util.computePower(root.info.getMyFinishedCombatUnits());
		double powerEnemy = root.util.computePowerEnemy(root.enemyInfo.getEnemyCombatUnits());
		root.guiManager.addDebugInfo(Utils.formatText(powerMe + " v.s. " + powerEnemy, Utils.Green) + "  attack = " + root.blackboard.getIsAttacking());
		
		if(root.self.supplyUsed() >= 50 * 2 && root.enemyInfo.getEnemyHaveBase() == false && root.enemyInfo.visitedStartPoint == true)
		{
			root.blackboard.setIsEndingGame(true);
		}
		else
		{
			root.blackboard.setIsEndingGame(false);
		}
		if(root.self.supplyUsed() < 100 * 2)
		{
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
		}
		else
		{
			if(root.blackboard.getIsAttacking())
			{
				if(powerMe < powerEnemy - 12 && root.self.supplyUsed() < 170 * 2)
					root.blackboard.setIsAttacking(false);
			}
			else
			{
				if(powerEnemy < powerMe - 24 || root.self.supplyUsed() > 180 * 2)
					root.blackboard.setIsAttacking(true);
			}
		}
		
		if(root.blackboard.getIsAttacking())
		{
			if(root.enemyInfo.startPoint != null)
			{
				if(root.enemyInfo.getEnemyBase().size() > 0)
					root.blackboard.setAttackPosition(root.enemyInfo.getEnemyBase().get(0).lastPosition);
				else
					root.blackboard.setAttackPosition(root.enemyInfo.startPoint);
			}
		}
		else
		{
			root.blackboard.setAttackPosition(root.info.bases[Math.min(2,root.blackboard.getNumberOfBase() - 1)].position);
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
