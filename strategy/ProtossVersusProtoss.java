package strategy;

import bwapi.UnitType;
import main.Bot;

public class ProtossVersusProtoss extends Strategy {

	public ProtossVersusProtoss(Bot r) {
		super(r);
	}
	
	Strategy currentOpening;
	
	@Override
	public void onFrame() {
		
		if(currentOpening == null)
			currentOpening = new TwoGatewayOpening(root);
		if(currentOpening.ended())
		{
			if(currentOpening.getName() == "TwoGatewayOpening")
			{
				currentOpening = new ThreeGatewayDragoon(root);
			}
		}
		else
		{
			currentOpening.onFrame();
		}
		
		if(root.self.supplyUsed() >= 90 * 2)
			root.blackboard.setNumberOfBaseAtLesst(2);
		if(root.self.supplyUsed() >= 120 * 2)
			root.blackboard.setNumberOfBaseAtLesst(3);
		if(root.self.supplyUsed() >= 160 * 2)
			root.blackboard.setNumberOfBaseAtLesst(4);
		
		
		
	}

	@Override
	public boolean ended() {
		
		return false;
	}

	@Override
	public String getName() {
		return "ProtossVersusProtoss";
	}

}
