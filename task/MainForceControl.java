package task;

import java.util.ArrayList;
import java.util.List;

import bwapi.Color;
import bwapi.Position;
import bwapi.PositionOrUnit;
import bwapi.Unit;
import bwapi.UnitType;
import main.Bot;

public class MainForceControl extends Task {

	List <Unit> zealots;
	
	public MainForceControl(Bot r) {
		super(r);
		zealots = new ArrayList<Unit>();
		
	}
	
	@Override
	public String getName() {
		
		return "MainForceControl";
	}
	
	@Override
	public void onFrame() {
		root.guiManager.addDebugInfo("Attack!");
		root.game.drawCircleMap(root.blackboard.getAttackPosition().getX(), root.blackboard.getAttackPosition().getY(), (int)information.GlobalConstant.Squad_Radius, new Color(255, 0, 0));
		
		ArrayList<Unit> t = new ArrayList<Unit>();
		for(Unit u : zealots)
		{
			if(root.info.getUnitInfo(u).destroy)
				continue;
			t.add(u);
		}
		zealots = t;
		for(UnitType ut : root.goal.armyUnit)
		{
			if(root.info.myUnits.get(ut) != null)
				for(Unit u : root.info.myUnits.get(ut))
					if(root.info.canStartNewTask(u))
					{
						root.info.getUnitInfo(u).currentTask = this;
						zealots.add(u);
					}
		}
		
		
		if(root.blackboard.getIsEndingGame())
		{
			for(int i = 0; i < zealots.size(); i++)
			{
				Position p = null;
				if(root.enemyInfo.getEnemyBuildings().size() > 0)
				{
					p = root.enemyInfo.getEnemyBuildings().get(i % root.enemyInfo.getEnemyBuildings().size()).lastPosition;
				}
				else
				{
					p = root.info.bases[i % root.info.bases.length].position;
				}
				if(root.game.getFrameCount() - root.info.getUnitInfo(zealots.get(i)).lastCommandFrame > 50)
				{
					root.info.getUnitInfo(zealots.get(i)).lastCommandFrame = root.game.getFrameCount();
					zealots.get(i).attack(p);
				}
			}
		}
		else
		{
			for(Unit u : zealots)
			{
				
				
				int x = root.guiManager.attackPosition.x;
				int y = root.guiManager.attackPosition.y;
				if(u.getDistance(new Position(x,  y)) > information.GlobalConstant.Squad_Radius && root.blackboard.getIsAttacking() == false)
				{
					if(root.game.getFrameCount() % 40 == 0)
						u.move(new Position(x, y));
				}
				else
				{
					if(root.game.getFrameCount() - root.info.getUnitInfo(u).lastCommandFrame > 50)
					{
						List <Unit> opponentInRange = new ArrayList<Unit>();
						for(Unit e : root.enemyInfo.enemies)
						{
							if(root.util.isCombatUnit(e.getType()))
								if(e.getPosition().getDistance(new Position(x, y)) < information.GlobalConstant.Squad_Radius)
								{
									opponentInRange.add(e);
								}
						}
						if(opponentInRange.size() == 0)
						{
							u.attack(new Position(x, y));
						}
						else
						{
							Unit near = opponentInRange.get(0);
							for(Unit e : opponentInRange)
							{
								if(e.getPosition().getDistance(new Position(x, y)) < near.getPosition().getDistance(new Position(x, y)))
									near = e;
							}
							u.attack(near.getPosition());
						}
						root.info.getUnitInfo(u).lastCommandFrame = root.game.getFrameCount();
					}
				}
			}
		}
		
	}

	@Override
	public boolean checkPossible() {
		return true;
	}

	@Override
	public void init() {
		state = TaskState.ACTIVE;
	}

	@Override
	public void checkEnd() {
		
	}

	@Override
	public int needMinerials() {
		
		return 0;
	}

	@Override
	public int needGas() {
		
		return 0;
	}

	@Override
	public int needSupply() {
		
		return 0;
	}

	@Override
	public int needUnit(UnitType u) {
		
		return 0;
	}

	@Override
	public int provideUnit(UnitType u) {
		
		return 0;
	}

	@Override
	public int provideSupply() {
		
		return 0;
	}

	@Override
	public List<Unit> requestUnit(UnitType requestUnit, int numberNeeded) {
		List<Unit> ret = new ArrayList<Unit>();
		List <Unit> nextZealots = new ArrayList<Unit>();
		for(int i = 0; i < zealots.size(); i++)
		{
			if(numberNeeded > 0 && zealots.get(i).getType() == requestUnit)
			{
				numberNeeded --;
				ret.add(zealots.get(i));
				root.info.setTask(zealots.get(i), null);
			}
			else
				nextZealots.add(zealots.get(i));
		}
		zealots = nextZealots;
		return ret;
	}
	
}
