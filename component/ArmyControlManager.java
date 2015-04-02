package component;

import task.MainForceControl;
import task.Task;
import main.Bot;

public class ArmyControlManager extends Component {

	Task mainForceControlTask;
	
	public ArmyControlManager(Bot r) {
		super(r);
	}
	
	@Override
	public int getResourcePriority() {
		return 0;
	}

	@Override
	public String getName() {
		return "ArmyControlManager";
	}

	@Override
	public void onFrame() {

		if(mainForceControlTask == null)
		{
			mainForceControlTask = new MainForceControl(root);
			makeProposal(mainForceControlTask);
		}
	}

}
