import bwapi.*;
import bwta.BWTA;
import bwta.BaseLocation;

import java.time.Year;
import java.util.*;

public class TestBot1 extends DefaultBWListener {

    private Mirror mirror = new Mirror();

    private Game game;
    
    private Player self;
    
    public void run() {

        mirror.getModule().setEventListener(this);
        mirror.startGame();

    }
    
    @Override
    public void onUnitCreate(Unit unit) {
        System.out.println("New unit " + unit.getType());
    }

	class BaseInfo
	{
		Unit base;
		List <Unit> worker;
		List <Unit> gasStation;
		Position buildingArea;
		
		public BaseInfo() {
			worker = new ArrayList<Unit>();
			gasStation = new ArrayList<Unit>();
		}
	}
	
    class GlobalGameInfo
    {
    	List <BaseInfo> myBase;
    	Unit myFirstBase;
    	Position opponentMainBase;
	    int MaxID = 1000000;
	    boolean[] unitOnDuty;
	    int[] lastAttackFrame;
	    void init()
	    {
	    	unitOnDuty = new boolean[MaxID];
	    	lastAttackFrame = new int[MaxID];
	    	for(int i = 0; i < MaxID; i++)
	    	{
	    		unitOnDuty[i] = false;
	    		lastAttackFrame[i] = 0;
	    	}
	    	myBase = new ArrayList<BaseInfo>();
	    }
    }
    
    class FrameGameInfo
    {
    	int remainMinerals;
    	int remainGas;
    	int frameNumber;
    	void onFrameInit()
    	{
    		remainMinerals = self.minerals();
    		remainGas = self.gas();
    		frameNumber = game.getFrameCount();
    	}
    }
    
    GlobalGameInfo gameInfo;
    FrameGameInfo frameInfo;
    
    List <Task> currentTasks;
    List <Component> components;
    List <String> debugInfo;
    
    @Override
    public void onStart() {
    	
        game = mirror.getGame();
        self = game.self();
        
        game.setLocalSpeed(0);
        
        System.out.println("Analyzing map...");
        BWTA.readMap();
        BWTA.analyze();
        System.out.println("Map data ready");
        
        gameInfo = new GlobalGameInfo();
        gameInfo.init();
        frameInfo = new FrameGameInfo();
        debugInfo = new ArrayList<String>();
        currentTasks = new ArrayList<Task>();
        components = new ArrayList<Component>();
        components.add(new BaseManager());
        components.add(new Reconnoiter());
        components.add(new SupplyManager());
        components.add(new TechBuildingPlanner());
        components.add(new ArmyProductionManager());
        components.add(new ArmyCommander());
    }
    
    interface Task
    {
    	String getTaskName();
    	int getNeedMinerals();
    	int getNeedGas();
    	void onFrame();
    	boolean isFinished();
    }
    
    interface Component
    {
    	String getComponentName();
    	void onFrame();
    }
    
    void addDebugInfo(String s)
    {
    	debugInfo.add(s);
    }
    
    void outputDebugInfoToScreen()
    {
        game.setTextSize(10);
        int startX = 10;
        int startY = 10;
        for (String s : debugInfo)
        {
        	game.drawTextScreen(startX, startY, s);
        	startY += 15;
        }        
    }
    
    boolean isBase(Unit u)
    {
    	if(u.getType() == UnitType.Protoss_Nexus) return true;
    	if(u.getType() == UnitType.Terran_Command_Center) return true;
    	if(u.getType() == UnitType.Zerg_Hatchery) return true;
    	// TODO: Zerg 2nd / 3rd base?
    	return false;
    }
    
    boolean isWorker(Unit u)
    {
    	if(u.getType() == UnitType.Protoss_Probe) return true;
    	if(u.getType() == UnitType.Terran_SCV) return true;
    	if(u.getType() == UnitType.Zerg_Drone) return true;
    	return false;
    }
    
    class BaseManager implements Component
    {
    	int Radius = 300;
    	
    	public BaseManager() {
		}
    	
    	void calculateBaseInfo() {
    		gameInfo.myBase.clear();
    		
    		for(Unit u : self.getUnits())
    		{
    			if(isBase(u))
    			{
    				BaseInfo b = new BaseInfo();
    				b.base = u;
    				gameInfo.myBase.add(b);
    				if(gameInfo.myFirstBase == null)
    					gameInfo.myFirstBase = u;
    			}
    		}
    		
    		for(BaseInfo b : gameInfo.myBase)
    		{
    			for(Unit u : self.getUnits())
    				if(u.distanceTo(b.base.getPosition().getX(), b.base.getPosition().getY()) <= Radius)
	    			{
	    				if(isWorker(u))
	    				{
	    					b.worker.add(u);
	    				}
	    				if(u.getType() == UnitType.Protoss_Assimilator && u.isBeingConstructed() == false)
	    					b.gasStation.add(u);
	    			}
    			int nMineral = 0;
    			int sumX = 0, sumY = 0;
    			for (Unit neutralUnit : game.neutral().getUnits()) {
                    if (neutralUnit.getType().isMineralField()) {
                    	if(neutralUnit.getPosition().getDistance(b.base.getPosition()) < Radius)
                    	{
                    		nMineral += 1;
                    		sumX += neutralUnit.getPosition().getX();
                    		sumY += neutralUnit.getPosition().getY();
                    	}
                    }
    			}
                
    			b.buildingArea = new Position(2 * b.base.getPosition().getX() - sumX / nMineral, 2 * b.base.getPosition().getY() - sumY / nMineral);
    			
    		}
		}
    	
		@Override
		public void onFrame() {
			
			calculateBaseInfo();
			
			int baseNumber = 0;
			for(BaseInfo b : gameInfo.myBase)
			{
				
				game.drawCircleMap(b.buildingArea.getX(), b.buildingArea.getX(), Radius, new Color(0, 0, 255));
				
				baseNumber += 1;
				game.drawCircleMap(b.base.getPosition().getX(), b.base.getPosition().getY(), Radius, new Color(0, 255, 0));
				int cntGatheringMinerals = 0, cntGatheringGas = 0;
				
				int countGasWorker = 0;
				for(Unit u : b.worker)
				{
					if(u.isGatheringGas())
						countGasWorker += 1;
				}
				
				for(Unit u : b.worker)
				{
					if(u.isGatheringMinerals())
						cntGatheringMinerals += 1;
					if(u.isGatheringGas())
						cntGatheringGas += 1;
					//addDebugInfo("worker #" + u.getID() + " : " + gameInfo.unitOnDuty[u.getID()]);
					if(u.isIdle() && gameInfo.unitOnDuty[u.getID()] == false)
					{
		                Unit closestMineral = null;
		                //find the closest mineral
		                for (Unit neutralUnit : game.neutral().getUnits()) {
		                    if (neutralUnit.getType().isMineralField()) {
		                        if (closestMineral == null || u.getDistance(neutralUnit) < u.getDistance(closestMineral)) {
		                            closestMineral = neutralUnit;
		                        }
		                    }
		                }
		                //if a mineral patch was found, send the drone to gather it
		                if (closestMineral != null) {
		                    u.gather(closestMineral, false);
		                }
					}
					
					//TODO: what if some gas-working died.
					if(gameInfo.unitOnDuty[u.getID()] == false && b.gasStation.size() > 0 && countGasWorker < 3)
					{
						u.gather(b.gasStation.get(0));
						countGasWorker += 1;
					}
					
				}
				addDebugInfo("	+Base #" + baseNumber + ": worker(Minerals/Gas/All) = " + cntGatheringMinerals + " / " + cntGatheringGas + " / " + b.worker.size());
				if(b.base.isTraining() == false && frameInfo.remainMinerals >= 50 && b.worker.size() < 25)
				{
					b.base.train(UnitType.Protoss_Probe);
				}
			}
		}

		@Override
		public String getComponentName() {
			return "BaseManager";
		}
    }
    
    class SupplyManager implements Component
    {

		@Override
		public String getComponentName() {
			return "SupplyManager";
		}
		
		@Override
		public void onFrame() {
			int usedSupply = self.supplyUsed();
			int currentSupply = self.supplyTotal();
			int buildingSupply = 0, scheduledSupply = 0;
			for(Unit u : self.getUnits())
			{
				if(u.getType() == UnitType.Protoss_Pylon && u.isBeingConstructed())
					buildingSupply += 8 * 2;
			}
			for(Task t : currentTasks)
			{
				if(t.getTaskName().equals("ScheduleBuilding " + UnitType.Protoss_Pylon))
					scheduledSupply += 8 * 2;
			}
			int limit = (currentSupply + buildingSupply + scheduledSupply);
			limit = limit * 8 / 10;
				
			if(usedSupply >= limit)
			{
				ScheduleBuilding t = new ScheduleBuilding(UnitType.Protoss_Pylon);
				currentTasks.add(t);
			}
			
			addDebugInfo("\t+Suppy(used/now/build/task) = " + (usedSupply/2) + " / " + (currentSupply/2) + " / " + (buildingSupply/2) + " / " + (scheduledSupply/2) );
		}
    	
    }
    
    Position buildNear(Unit builder, int x, int y, UnitType buildingType)
    {
		int[] dx = {0, 1, 0, -1};
		int[] dy = {1, 0, -1, 0};
		for(int d = 8; d >= 0 ; d--)
		{
			for(int f = 0; f < 4; f++)
			{
				int nf = (f + 1) % 4;
				for(int v = -d; v <= d; v++)
				{
					int nx = x + dx[f] * v + dx[nf] * d;
					int ny = y + dy[f] * v + dy[nf] * d;
					TilePosition p = new TilePosition(nx, ny);
					Position pos = new Position(p.getX() * 32, p.getY() * 32);
					//game.drawLineMap(pos.getX(), pos.getY(), builder.getPosition().getX(), builder.getPosition().getY(), new Color(255, 0, 0));
					if(builder.hasPath(pos) && builder.build(p, buildingType))
						return pos;
				}
			}
		}
		return null;
    }
    
    class ScheduleBuilding implements Task
    {
    	int needMinerals, needGas;
    	UnitType buildingType;
    	Unit onDutyWorkUnit;
    	Position scheduledPosition;
    	int lastTry;
    	
    	public ScheduleBuilding(UnitType b) {
			buildingType = b;
			needMinerals = b.mineralPrice();
			needGas = b.gasPrice();
			onDutyWorkUnit = getNearestFreeWorker(gameInfo.myFirstBase.getPosition());
			gameInfo.unitOnDuty[onDutyWorkUnit.getID()] = true;
			lastTry = 0;
		}
    	
		@Override
		public String getTaskName() {
			return "ScheduleBuilding " + buildingType;
		}

		@Override
		public int getNeedMinerals() {
			return needMinerals;
		}

		@Override
		public int getNeedGas() {
			return needGas;
		}
		
		@Override
		public void onFrame() {
			
			
			if(frameInfo.frameNumber - lastTry > 10 && (onDutyWorkUnit.isIdle() || onDutyWorkUnit.isGatheringMinerals() || onDutyWorkUnit.isGatheringMinerals()))
			{
				scheduledPosition = buildNear(onDutyWorkUnit, gameInfo.myFirstBase.getTilePosition().getX(), gameInfo.myFirstBase.getTilePosition().getY(), buildingType);
				lastTry = frameInfo.frameNumber;
			}
			
			game.drawTextMap(onDutyWorkUnit.getPosition().getX(), onDutyWorkUnit.getPosition().getY(), "Task: " + "ScheduleBuilding " + buildingType);
			if(scheduledPosition != null)
				game.drawLineMap(onDutyWorkUnit.getPosition().getX(), onDutyWorkUnit.getPosition().getY(), scheduledPosition.getX(), scheduledPosition.getY(), new Color(255, 255, 0));
			
		}

		@Override
		public boolean isFinished() {
			
			if(scheduledPosition != null)
				for(Unit u : self.getUnits())
					if(u.getType() == buildingType)
					{
						//game.drawLineMap(scheduledPosition.getX(), scheduledPosition.getY(), u.getPosition().getX(), u.getPosition().getY(), new Color(255, 0, 0));
						//addDebugInfo((int)u.getPosition().distanceTo(scheduledPosition.getX(), scheduledPosition.getY()) + "");
						if(u.getPosition().getX() > scheduledPosition.getX() && u.getPosition().getY() > scheduledPosition.getY() &&  u.getPosition().distanceTo(scheduledPosition.getX(), scheduledPosition.getY()) < 100)
						{
							gameInfo.unitOnDuty[onDutyWorkUnit.getID()] = false;
							onDutyWorkUnit.move(gameInfo.myFirstBase.getPosition());
							return true;
						}
					}
			
			return false;
		}
    	
    }
    
    boolean isAlive(Unit u)
    {
    	if(Math.abs(u.getPosition().getX()) + Math.abs(u.getPosition().getY()) > 10000000)
    		return false;
    	return true;
    }
    
    Unit getNearestFreeWorker(Position p)
    {
    	Unit ret = null;
    	for(Unit u : self.getUnits())
    		if(isWorker(u) && (u.isIdle() || u.isGatheringMinerals()) && gameInfo.unitOnDuty[u.getID()] == false)
    		{
    			//if(isAlive(u) == false) continue;
    			if(ret == null)
    				ret = u;
    			else {
    				if(u.getPosition().distanceTo(p.getX(), p.getY()) < ret.getPosition().distanceTo(p.getX(), p.getY()))
    					ret = u;
				}
    		}
    	
    	return ret;
    }
    
    class FindOpponentMainBase implements Task
    {
    	
    	List<Position> possiblePosition;
    	int currentFinding;
    	Unit onDutyWorker;
    	boolean firstFrame;
    	
    	public FindOpponentMainBase() {
			possiblePosition = new ArrayList<Position>();
			for(BaseLocation b : BWTA.getStartLocations())
			{
				if(b.getPosition().distanceTo(gameInfo.myFirstBase.getX(), gameInfo.myFirstBase.getY()) < 100)
					continue;
				possiblePosition.add(b.getPosition());
			}
			onDutyWorker = getNearestFreeWorker(gameInfo.myFirstBase.getPosition());
			
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
			
			//TODO: find one building --> Done
			//TODO: what if this worker died before finding?
			
			addDebugInfo("\t+PossiblePositions = " + possiblePosition.size());
			if(firstFrame)
			{
				firstFrame = false;
				gameInfo.unitOnDuty[onDutyWorker.getID()] = true;
				onDutyWorker.move(possiblePosition.get(currentFinding));
			}
			if(onDutyWorker.getPosition().distanceTo(gameInfo.myFirstBase.getPosition().getX(), gameInfo.myFirstBase.getPosition().getY()) < 100)
				onDutyWorker.move(possiblePosition.get(currentFinding));
			
			for(Unit u : game.getAllUnits())
			{
				if(self.isEnemy(u.getPlayer()))
				{
					gameInfo.opponentMainBase = possiblePosition.get(currentFinding);
					
					return;
				}
			}
			
			game.drawTextMap(onDutyWorker.getPosition().getX(), onDutyWorker.getPosition().getY(), "Task:FindOpponentMainBase");
			game.drawLineMap(onDutyWorker.getPosition().getX(), onDutyWorker.getPosition().getY(), possiblePosition.get(currentFinding).getX(), possiblePosition.get(currentFinding).getY(), new Color(255,255,0));
			if(currentFinding == possiblePosition.size() - 1)
				gameInfo.opponentMainBase = possiblePosition.get(possiblePosition.size() - 1);
			if(onDutyWorker.getPosition().distanceTo(possiblePosition.get(currentFinding).getX(), possiblePosition.get(currentFinding).getY()) < 100)
			{
				currentFinding += 1;
				onDutyWorker.move(possiblePosition.get(currentFinding));
			}
		}

		@Override
		public boolean isFinished() {
			if(gameInfo.opponentMainBase != null)
			{
				gameInfo.unitOnDuty[onDutyWorker.getID()] = false;
				onDutyWorker.move(gameInfo.myFirstBase.getPosition());
				return true;
			}
			return false;
		}
    	
    }
    
    class Reconnoiter implements Component
    {
    	
    	boolean alreadyFindingOpponents;
    	
    	public Reconnoiter() {
    		alreadyFindingOpponents = false;
		}
    	
		@Override
		public String getComponentName() {
			return "Reconnoiter";
		}
		
		@Override
		public void onFrame() {
			if(gameInfo.opponentMainBase == null)
			{
				if(alreadyFindingOpponents == false && self.supplyUsed() >= 9 * 2)
				{
					alreadyFindingOpponents = true;
					FindOpponentMainBase t = new FindOpponentMainBase();
					currentTasks.add(t);
				}
				
				addDebugInfo("\t+OpponentMainBasse = (unknown)");
			}
			else
				addDebugInfo("\t+OpponentMainBasse = (" + gameInfo.opponentMainBase.getX() + ", " + gameInfo.opponentMainBase.getY() + ")");
			if(gameInfo.opponentMainBase != null)
				game.drawCircleMap(gameInfo.opponentMainBase.getX(), gameInfo.opponentMainBase.getY(), 100, new Color(255, 0, 0));
			
		}
    	
    }
    
    class TechBuildingPlanner implements Component
    {
    	UnitType[] buildingOrder_type;
    	int[] buildingOrder_condition;
    	int p;
    	
    	TechBuildingPlanner()
    	{
    		buildingOrder_type = new UnitType[]{UnitType.Protoss_Assimilator, 
    											UnitType.Protoss_Gateway,
    											UnitType.Protoss_Cybernetics_Core,
    											UnitType.Protoss_Gateway,
    											UnitType.Protoss_Citadel_of_Adun,
    											UnitType.Protoss_Gateway,
    											UnitType.Protoss_Templar_Archives,
    											UnitType.Protoss_Gateway,
    											UnitType.Protoss_Gateway};
    		buildingOrder_condition = new int[]{11,
    											12,
    											15,
    											15,
    											16,
    											18,
    											18,
    											21,
    											25};
    		p = 0;
    	}
    	
		@Override
		public String getComponentName() {
			return "TechBuildingPlanner";
		}

		@Override
		public void onFrame() {
			int populationCount = self.supplyUsed() / 2;
			if(p < buildingOrder_type.length)
			{
				if(populationCount >= buildingOrder_condition[p])
				{
					ScheduleBuilding t = new ScheduleBuilding(buildingOrder_type[p]);
					currentTasks.add(t);
					p ++;
				}
				
			}
		}
    }
    
    class ArmyProductionManager implements Component
    {
		@Override
		public String getComponentName() {
			return "ArmyProductionManager";
		}
		
		@Override
		public void onFrame() {
			
			for(Unit u : self.getUnits())
			{
				if(u.getType() == UnitType.Protoss_Gateway && u.isTraining() == false)
				{
					if(frameInfo.remainMinerals >= UnitType.Protoss_Dark_Templar.mineralPrice() && frameInfo.remainGas >= UnitType.Protoss_Dark_Templar.gasPrice() && self.isUnitAvailable(UnitType.Protoss_Dark_Templar))
					{
						u.train(UnitType.Protoss_Dark_Templar);
						frameInfo.remainMinerals -= UnitType.Protoss_Dark_Templar.mineralPrice();
						frameInfo.remainGas -= UnitType.Protoss_Dark_Templar.gasPrice();
					}
					else if(self.isUnitAvailable(UnitType.Protoss_Dark_Templar) == false && frameInfo.remainMinerals >= UnitType.Protoss_Dragoon.mineralPrice() && frameInfo.remainGas >= UnitType.Protoss_Dragoon.gasPrice() && self.isUnitAvailable(UnitType.Protoss_Dragoon))
					{
						u.train(UnitType.Protoss_Dragoon);
						frameInfo.remainMinerals -= UnitType.Protoss_Dragoon.mineralPrice();
						frameInfo.remainGas -= UnitType.Protoss_Dragoon.gasPrice();
					}
					else if((self.isUnitAvailable(UnitType.Protoss_Dark_Templar) == false && self.isUnitAvailable(UnitType.Protoss_Dragoon) == false && frameInfo.remainMinerals >= UnitType.Protoss_Zealot.mineralPrice() && frameInfo.remainGas >= UnitType.Protoss_Zealot.gasPrice() && self.isUnitAvailable(UnitType.Protoss_Zealot)) || frameInfo.remainMinerals > 300)
					{
						u.train(UnitType.Protoss_Zealot);
						frameInfo.remainMinerals -= UnitType.Protoss_Zealot.mineralPrice();
						frameInfo.remainGas -= UnitType.Protoss_Zealot.gasPrice();
					}
				}
			}
			
			
		}
    	
    	
    }
    
    boolean isBattleUnit(Unit u)
    {
    	if(u.getType() == UnitType.Protoss_Zealot) return true;
    	if(u.getType() == UnitType.Protoss_Dragoon) return true;
    	if(u.getType() == UnitType.Protoss_Dark_Templar) return true;
    	return false;
    }
    
    class ArmyCommander implements Component
    {

		@Override
		public String getComponentName() {
			return "ArmyCommander";
		}

		@Override
		public void onFrame() {
			List<Unit> enemy = new ArrayList<Unit>();
			
			for (Unit u : game.getAllUnits()) {
        		if(u.isVisible() == false)
        			continue;
        		if(u.getType().isFlyer() == true)
        			continue;
        		if(self.isEnemy(u.getPlayer()))
        		{
        			enemy.add(u);
        		}
			}
			
			for (Unit u : self.getUnits())
			{
				if(isBattleUnit(u))
				{
					if(frameInfo.frameNumber - gameInfo.lastAttackFrame[u.getID()] > 30)
					{
						gameInfo.lastAttackFrame[u.getID()] = frameInfo.frameNumber;
						if(enemy.size() == 0)
						{
							u.attack(gameInfo.opponentMainBase);
						}
						else
						{
							Unit target = enemy.get(0);
							double minDistance = target.getDistance(u.getPosition());
							for(Unit v : enemy)
							{
								if(v.getPosition().getDistance(u.getPosition()) < minDistance)
								{
									minDistance = v.getPosition().getDistance(u.getPosition());
									target = v;
								}
							}
							u.attack(target);
						}
					}
					
					
				}
			}
			
			
			
		}
    	
    }
    
    
    @Override
    public void onFrame() {
    	
        debugInfo.clear();
        addDebugInfo("Frame #" + frameInfo.frameNumber + "");
        frameInfo.onFrameInit();
        List <Task> remainTasks = new ArrayList<Task>();
        for(Task t : currentTasks)
        {
        	if(t.isFinished())
        		continue;
        	frameInfo.remainMinerals -= t.getNeedMinerals();
        	frameInfo.remainGas -= t.getNeedGas();
        	remainTasks.add(t);
        }
        currentTasks = remainTasks;
        
        int componentID = 0;
        for (Component comp : components)
        {
        	componentID += 1;
        	addDebugInfo("[Component #" + componentID + ": " + comp.getComponentName() + "]");
        	comp.onFrame();
        }
        
        int taskID = 0;
        for(Task t : currentTasks)
        {
        	taskID += 1;
        	addDebugInfo("<Task #" + taskID + ": " + t.getTaskName() + "> : Minerals = " + t.getNeedMinerals() + ", Gas = " + t.getNeedGas());
        	t.onFrame();
        }
        
        outputDebugInfoToScreen();
    }

    public static void main(String[] args) {
        new TestBot1().run();
    }
}
