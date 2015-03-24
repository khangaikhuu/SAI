package component;

import java.util.ArrayList;
import java.util.List;

import main.Bot;
import bwapi.Position;
import bwapi.Unit;
import bwapi.UnitType;
import bwta.BWTA;

public class ArmyCommander implements Component
{

	Bot root;
	boolean attack;
	
	public ArmyCommander(Bot r)
	{
		root = r;
		attack = false;
	}
	
	@Override
	public String getComponentName() {
		return "ArmyCommander";
	}
	
	double getScore(double dist, boolean canDetect)
	{
		//if(canDetect)
		//	return 2 + 1.0 / (dist + 1);
		return 1.0 / (dist + 1);
	}

	@Override
	public void onFrame() {
		List<Unit> enemy = new ArrayList<Unit>();
		
		for (Unit u : root.game.getAllUnits()) {
    		if(u.isVisible() == false)
    			continue;
    		if(u.getType().isFlyer() == true)
    			continue;
    		if(root.self.isEnemy(u.getPlayer()))
    		{
    			enemy.add(u);
    		}
		}
		
		boolean haveMyUnitAroundBase = util.General.haveMyUnitAround(root, root.gameInfo.opponentMainBase);
		
		for (Unit u : root.self.getUnits())
		{
			if(u.getType() == UnitType.Protoss_Carrier)
			{
				if(u.getInterceptorCount() < 8 && u.isTraining() == false)
				{
					u.train(UnitType.Protoss_Interceptor);
				}
			}
			
			
			if(attack == false)
			{
				if(root.self.visibleUnitCount(UnitType.Protoss_Dragoon) + root.self.visibleUnitCount(UnitType.Protoss_Zealot) + root.self.visibleUnitCount(UnitType.Protoss_Dark_Templar) > 25)
					attack = true;
			}
			else
			{
				if(root.self.visibleUnitCount(UnitType.Protoss_Dragoon) + root.self.visibleUnitCount(UnitType.Protoss_Zealot) + root.self.visibleUnitCount(UnitType.Protoss_Dark_Templar) > 10)
					attack = false;
			}
			
			
			if(util.General.isBattleUnit(u) || (u.getType() == UnitType.Protoss_Probe &&  u.isUnderAttack()))
			{

				
				if(root.frameInfo.frameNumber - root.gameInfo.lastAttackFrame[u.getID()] > 30)
				{
					root.gameInfo.lastAttackFrame[u.getID()] = root.frameInfo.frameNumber;
					
					if(enemy.size() == 0)
					{
						if(!attack)
							continue;
						
						Position attackPosition = null;
						
						if(root.gameInfo.opponentMainBase != null && (haveMyUnitAroundBase == false))
							attackPosition = root.gameInfo.opponentMainBase;
						else {
							int n = BWTA.getBaseLocations().size();
							attackPosition = BWTA.getBaseLocations().get(u.getID() % n).getPosition();
						}
						u.attack(attackPosition);
					}
					else
					{
						Unit target = enemy.get(0);
						double maxScore = getScore(target.getDistance(u.getPosition()), target.getType().isDetector());
						for(Unit v : enemy)
						{
							if(v.getType().isFlyingBuilding() && u.getType() != UnitType.Protoss_Dragoon) continue;
							if(v.isCloaked()) continue;
							if(getScore(v.getPosition().getDistance(u.getPosition()), v.getType().isDetector()) > maxScore)
							{
								maxScore = getScore(v.getPosition().getDistance(u.getPosition()), v.getType().isDetector());
								target = v;
							}
						}
						
						if(util.General.distanceToMyBase(root, target.getPosition()) > 1000)
							if(!attack)
								continue;
						
						u.attack(target);
					}
				}			
			}
		}
	}
}