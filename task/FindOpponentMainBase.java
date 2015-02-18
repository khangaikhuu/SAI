package task;

import java.util.ArrayList;
import java.util.List;

import main.Bot;
import bwapi.Color;
import bwapi.Position;
import bwapi.Unit;
import bwta.BWTA;
import bwta.BaseLocation;

public class FindOpponentMainBase implements Task
{
	Bot root;
	
	List<Position> possiblePosition;
	int currentFinding;
	Unit onDutyWorker;
	boolean firstFrame;
	
	public FindOpponentMainBase(Bot r) {
		root = r;
		possiblePosition = new ArrayList<Position>();
		for(BaseLocation b : BWTA.getStartLocations())
		{
			if(b.getPosition().distanceTo(root.gameInfo.myFirstBase.getX(), root.gameInfo.myFirstBase.getY()) < 100)
				continue;
			possiblePosition.add(b.getPosition());
		}
		onDutyWorker = util.General.getNearestFreeWorker(root, root.gameInfo.myFirstBase.getPosition());
		
		currentFinding = 0;
		firstFrame = true;
		
	}

	@Override
	public String getTaskName() {
		return "FindOpponentMainBase";
	}

	@Override
	public int getNeedMinerals() {
		return 0;
	}

	@Override
	public int getNeedGas() {
		return 0;
	}

	@Override
	public void onFrame() {
		
		//TODO: what if this worker died before finding?
		
		root.debug.addDebugInfo("\t+PossiblePositions = " + possiblePosition.size());
		if(firstFrame)
		{
			firstFrame = false;
			root.gameInfo.unitOnDuty[onDutyWorker.getID()] = true;
			onDutyWorker.move(possiblePosition.get(currentFinding));
		}
		if(onDutyWorker.getPosition().distanceTo(root.gameInfo.myFirstBase.getPosition().getX(), root.gameInfo.myFirstBase.getPosition().getY()) < 100)
			onDutyWorker.move(possiblePosition.get(currentFinding));
		
		for(Unit u : root.game.getAllUnits())
		{
			if(root.self.isEnemy(u.getPlayer()) && u.getType().isBuilding() && u.getDistance(onDutyWorker) < 200)
			{
				root.gameInfo.opponentMainBase = possiblePosition.get(currentFinding);
				return;
			}
		}
		
		root.game.drawTextMap(onDutyWorker.getPosition().getX(), onDutyWorker.getPosition().getY(), "Task:FindOpponentMainBase");
		root.game.drawLineMap(onDutyWorker.getPosition().getX(), onDutyWorker.getPosition().getY(), possiblePosition.get(currentFinding).getX(), possiblePosition.get(currentFinding).getY(), new Color(255,255,0));
		if(currentFinding == possiblePosition.size() - 1)
			root.gameInfo.opponentMainBase = possiblePosition.get(possiblePosition.size() - 1);
		if(onDutyWorker.getPosition().distanceTo(possiblePosition.get(currentFinding).getX(), possiblePosition.get(currentFinding).getY()) < 100)
		{
			currentFinding += 1;
			onDutyWorker.move(possiblePosition.get(currentFinding));
		}
	}

	@Override
	public boolean isFinished() {
		if(root.gameInfo.opponentMainBase != null)
		{
			root.gameInfo.unitOnDuty[onDutyWorker.getID()] = false;
			onDutyWorker.move(root.gameInfo.myFirstBase.getPosition());
			return true;
		}
		return false;
	}
	
}