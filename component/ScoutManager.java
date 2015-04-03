package component;

import task.FirstTimeScout;
import main.Bot;

public class ScoutManager extends Component {
	
	boolean firstTimeScout;
	
	public ScoutManager(Bot r) {
		super(r);
		
		firstTimeScout = false;
	}
	
	@Override
	public int getResourcePriority() {
		
		return 0;
	}
	
	@Override
	public String getName() {
		return "ScoutManager";
	}
	
	
	
	@Override
	public void onFrame() {
		
		if(firstTimeScout == false && root.blackboard.getFirstTimeScout())
		{
			firstTimeScout = true;
			FirstTimeScout task = new FirstTimeScout(root);
			makeProposal(task);
		}
		
	}
	
}
