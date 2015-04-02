package strategy;

import main.Bot;

public class ProtossVersusTerran extends Strategy {

	public ProtossVersusTerran(Bot r) {
		super(r);
	}
	
	Strategy currentOpening;
	
	@Override
	public void onFrame() {
		
		if(currentOpening == null)
			currentOpening = new CyberneticsCoreOpening(root);
		if(currentOpening.ended())
		{
			if(currentOpening.getName() == "CyberneticsCoreOpening")
			{
				currentOpening = new ThreeGatewayDragoon(root);
				
				
				
			}
			
			
		}
		else
		{
			currentOpening.onFrame();
		}
		
	}

	@Override
	public boolean ended() {
		
		return false;
	}

	@Override
	public String getName() {
		return "ProtossVersusTerran";
	}

}
