package component;

import task.FindOpponentMainBase;
import bwapi.Color;
import main.Bot;

public class Reconnoiter implements Component
{
	Bot root;
	
	boolean alreadyFindingOpponents;
	
	public Reconnoiter(Bot r) {
		alreadyFindingOpponents = false;
		root = r;
	}
	
	@Override
	public String getComponentName() {
		return "Reconnoiter";
	}
	
	@Override
	public void onFrame() {
		if(root.gameInfo.opponentMainBase == null)
		{
			if(alreadyFindingOpponents == false && root.self.supplyUsed() >= 9 * 2)
			{
				alreadyFindingOpponents = true;
				FindOpponentMainBase t = new FindOpponentMainBase(root);
				root.currentTasks.add(t);
			}
			
			root.debug.addDebugInfo("\t+OpponentMainBasse = (unknown)");
		}
		else
			root.debug.addDebugInfo("\t+OpponentMainBasse = (" + root.gameInfo.opponentMainBase.getX() + ", " + root.gameInfo.opponentMainBase.getY() + ")");
		if(root.gameInfo.opponentMainBase != null)
			root.game.drawCircleMap(root.gameInfo.opponentMainBase.getX(), root.gameInfo.opponentMainBase.getY(), 100, new Color(255, 0, 0));
		
	}
	
}
