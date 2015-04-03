package task;

import java.util.ArrayList;
import java.util.List;

import information.BaseInfo;
import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import main.Bot;

public class BuildStruct extends Task {
	
	BaseInfo aroundBase;
	UnitType targetStruct;
	Unit worker;
	TilePosition buildPosition;
	int lastMoveFrame;
	int lastBuildFrame;
	
	public BuildStruct(Bot r, BaseInfo b, UnitType target) {
		super(r);
		aroundBase = b;
		targetStruct = target;
		lastMoveFrame = 0;
		lastBuildFrame = 0;
	}
	
	@Override
	public String getName() {
		
		return "BuildStruct " + targetStruct;
	}

	@Override
	public void onFrame() {
		
		if(worker != null && root.info.getUnitInfo(worker).destroy)
			worker = null;
		
		if(worker == null)
		{
			if(aroundBase.gatherResourceTask != null && aroundBase.gatherResourceTask.state == TaskState.ACTIVE)
			{
				List <Unit> t = aroundBase.gatherResourceTask.requestUnit(UnitType.Protoss_Probe, 1);
				if(t.size() > 0)
				{
					worker = t.get(0);
					root.info.setTask(worker, this);
				}
			}
			if(worker == null)
				for(int i = 0; i < root.blackboard.getNumberOfBase(); i++)
				{
					BaseInfo thisBase = root.info.bases[i];
					if(thisBase.gatherResourceTask != null && thisBase.gatherResourceTask.state == TaskState.ACTIVE)
					{
						List <Unit> t = thisBase.gatherResourceTask.requestUnit(UnitType.Protoss_Probe, 1);
						if(t.size() > 0 && worker == null)
						{
							worker = t.get(0);
							root.info.setTask(worker, this);
						}
					}
				}
			
			if(worker != null)
				root.info.getUnitInfo(worker).currentTask = this;
			else
				return;
		}
		
		if(worker.getDistance(root.util.toBuildingCenter(buildPosition, targetStruct)) > information.GlobalConstant.Building_Worker_distance)
		{
			if(root.game.getFrameCount() - lastMoveFrame > 30)
			{
				worker.move(root.util.toBuildingCenter(buildPosition, targetStruct));
				lastMoveFrame = root.game.getFrameCount();
			}	
		}
		else
		{
			if(root.game.getFrameCount() - lastBuildFrame > 30)
			{
				lastBuildFrame = root.game.getFrameCount();
				worker.build(targetStruct, buildPosition);
			}
		}
	}

	@Override
	public boolean checkPossible() {
		return (aroundBase.whereToBuild(targetStruct, false) != null);
	}

	@Override
	public void init() {
		state = TaskState.ACTIVE;
		buildPosition = aroundBase.whereToBuild(targetStruct, true);
		
	}

	@Override
	public void checkEnd() {
		
		if(root.info.myUnits.get(targetStruct) != null)
			for(Unit u : root.info.myUnits.get(targetStruct))
			{
				if(u.getTilePosition().getDistance(buildPosition) == 0)
				{
					state = TaskState.FINISHED;
					root.info.setTask(worker, null);
					if(root.util.isGasBuilding(targetStruct))
					{
						aroundBase.gasStation.add(u);
					}
					if(root.util.isBase(targetStruct))
					{
						aroundBase.myBase = u;
					}
				}
			}
		
	}

	@Override
	public int needMinerials() {
		
		return targetStruct.mineralPrice();
	}

	@Override
	public int needGas() {
		
		return targetStruct.gasPrice();
	}

	@Override
	public int needSupply() {
		
		return 0;
	}

	@Override
	public int provideSupply() {
		
		return targetStruct.supplyProvided();
	}

	@Override
	public int needUnit(UnitType u) {
		
		return 0;
	}

	@Override
	public int provideUnit(UnitType u) {
		if(u == targetStruct) return 1;
		return 0;
	}

	@Override
	public List<Unit> requestUnit(UnitType requestUnit, int numberNeeded) {
		List <Unit> ret = new ArrayList<Unit>(); 
		return ret;
	}
	
}
