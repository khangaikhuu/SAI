package component;

import task.GatherResource;
import task.TrainUnit;
import main.Bot;
import bwapi.Unit;
import bwapi.UnitType;

public class BaseManager extends Component {
	
	public BaseManager(Bot r)
	{
		super(r);
	}
	
	@Override
	public int getResourcePriority() {
		return 300;
	}
	
	@Override
	public String getName() {
		return "BaseManager";
	}
	
	@Override
	public void onFrame() {
		
		//System.out.println("BaseManager have minerals = " + haveMinerals);
		
		/* Tasks:
		 * 1. Train worker
		 * 2. Gather Resource
		 */
		
		for(int i = 0; i < root.info.myBaseCount; i++)
		{
			Unit b = root.info.bases[i].myBase;
			if(b == null || root.info.getUnitInfo(b).destroy)
				continue;
			
			int myProbes = 0;
			if(root.info.myUnits.get(UnitType.Protoss_Probe) != null)
				myProbes += root.info.myUnits.get(UnitType.Protoss_Probe).size();
			
			if(b.isTraining() == false && myProbes < 3 * root.info.bases[0].minerals.size())
			{
				TrainUnit task = new TrainUnit(root, b, UnitType.Protoss_Probe);
				makeProposal(task);
			}
			
			if(root.info.bases[i].gatherResourceTask == null)
			{
				GatherResource task = new GatherResource(root, root.info.bases[i]);
				makeProposal(task);
			}
			
		}
		
	}

}
