package component;

import java.util.ArrayList;
import java.util.List;

import main.Bot;
import bwapi.Unit;

public class ArmyCommander implements Component
{

	Bot root;
	
	public ArmyCommander(Bot r)
	{
		root = r;
	}
	
	@Override
	public String getComponentName() {
		return "ArmyCommander";
	}
	
	double getScore(double dist, boolean canDetect)
	{
		if(canDetect)
			return 2 + 1.0 / (dist + 1);
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
		
		for (Unit u : root.self.getUnits())
		{
			if(util.General.isBattleUnit(u) || u.isUnderAttack())
			{
				if(root.frameInfo.frameNumber - root.gameInfo.lastAttackFrame[u.getID()] > 30)
				{
					root.gameInfo.lastAttackFrame[u.getID()] = root.frameInfo.frameNumber;
					
					if(enemy.size() == 0)
					{
						u.attack(root.gameInfo.opponentMainBase);
					}
					else
					{
						Unit target = enemy.get(0);
						double maxScore = getScore(target.getDistance(u.getPosition()), target.getType().isDetector());
						for(Unit v : enemy)
						{
							if(getScore(v.getPosition().getDistance(u.getPosition()), v.getType().isDetector()) > maxScore)
							{
								maxScore = getScore(v.getPosition().getDistance(u.getPosition()), v.getType().isDetector());
								target = v;
							}
						}
						u.attack(target);
					}
				}			
			}
		}
	}
}