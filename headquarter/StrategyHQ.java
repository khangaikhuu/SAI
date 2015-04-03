package headquarter;

import strategy.ProtossVersusTerran;
import bwapi.Race;
import main.Bot;

public class StrategyHQ implements HQ {
	
	Bot root;
	
	public StrategyHQ(Bot r) {
		root = r;
	}
	
	@Override
	public void onFrame() {
		
		if(root.strategy == null)
		{
			//if(root.enemy.getRace() == Race.Terran)
				root.strategy = new ProtossVersusTerran(root);
		}
		
		if(root.strategy != null)
			root.strategy.onFrame();
		
	}

}
