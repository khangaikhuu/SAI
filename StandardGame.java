import bwapi.*;
import bwta.BWTA;
import bwta.BaseLocation;

public class TestBot1 extends DefaultBWListener {

    private Mirror mirror = new Mirror();

    private Game game;

    private Player self;
    
    Unit SCVForBS;
    Unit SCVForBB;
    Unit SCVForFinding;
    boolean flag;
    
    int frameNumber;
    
    int[] unitID;
    int[] remainNextAttack;
    
    int nUnitID;
    
    Position guessEnemy;
    Position myBase;
    
    int getRemain(int uID)
    {
    	nUnitID = Math.max(nUnitID, uID);
    	return remainNextAttack[uID];
    }
    
    void setRemain(int uID, int val)
    {
    	remainNextAttack[uID] = val;
    	nUnitID = Math.max(nUnitID, uID);
    }
    
    public void run() {

        mirror.getModule().setEventListener(this);
        mirror.startGame();

    }
    
    @Override
    public void onUnitCreate(Unit unit) {
        System.out.println("New unit " + unit.getType());
    }
    
    public Unit freeWorker()
    {
    	for (Unit myUnit : self.getUnits())
    	{
    		if(myUnit == SCVForBS) continue;
    		if(myUnit == SCVForBB) continue;
    		if(myUnit == SCVForFinding) continue;
    		if((myUnit.isGatheringMinerals() || myUnit.isCarryingMinerals() || myUnit.isIdle()) && myUnit.getType() == UnitType.Protoss_Probe)
    			return myUnit;
    	}
    	return null;
    }
    
    int nowLookAt;
    int nPossible;
    Position[] possiblePosition;
    boolean firstTime;
    boolean enemyPosKnown;
    boolean f1;
    
    @Override
    public void onStart() {
    	f1 = true;
    	enemyPosKnown = false;
    	nowLookAt = 1;
    	nPossible = 0;
    	possiblePosition = new Position[17];
    	firstTime = true;
    	
    	remainNextAttack = new int[1000001];
    	nUnitID = 0;
        game = mirror.getGame();
        self = game.self();
        flag = false;
        
        game.setLocalSpeed(0);
        
        System.out.println("Analyzing map...");
        BWTA.readMap();
        BWTA.analyze();
        System.out.println("Map data ready");
    }
    
    public void buildNear(Unit builder, int x, int y, UnitType buildingType)
    {
		int[] dx = {0, 1, 0, -1};
		int[] dy = {1, 0, -1, 0};
		for(int d = 7; d >= 0 ; d--)
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
					if(builder.hasPath(pos) && builder.build(p, buildingType))
						return;
				}
			}
		}		
    }
    
	int countMyUnit(UnitType uType)
	{
		int ret = 0;
    	for (Unit myUnit : self.getUnits())
    	{
    		if(myUnit.getType() == uType)
    			ret += 1;
    	}
    	return ret;
	}
	
	
	int recordVar = -100;
	
    @Override
    public void onFrame() {
    	
    	int MAX_BG = 5;
    	int MAX_WORKER = 25;
    	int ATTACK_ARMY = 18;
    	
    	for(int i = 1; i <= nUnitID; i++)
    		remainNextAttack[i] = Math.max(0, remainNextAttack[i] - 1);
        
        
        int population_total = self.supplyTotal();
        int population_used = self.supplyUsed();
        
        StringBuilder units = new StringBuilder("My units:\n");
        
        
        
        if(firstTime)
        {
        	firstTime = false;
        	
        	SCVForBS = freeWorker();
        	
	        for(BaseLocation b : BWTA.getStartLocations())
	        {
	        	nPossible += 1;
	        	possiblePosition[nPossible] = b.getPosition();
	        	
	        	Position pb = b.getPosition();
	        	if(myBase == null)
	        		myBase = pb;
	        	else
	        	{
	        		if(SCVForBS.distanceTo(pb.getX(), pb.getY()) < SCVForBS.distanceTo(myBase.getX(), myBase.getY()))
	        		{
	        			myBase = pb;
	        		}
	        	}
	        	
	        }
        }
        
        // build BS
        if(SCVForBS == null || SCVForBS.distanceTo(myBase.getX(), myBase.getY()) > 10000)
        	SCVForBS = freeWorker();
        if(SCVForBB == null || SCVForBB.distanceTo(myBase.getX(), myBase.getY()) > 10000)
        	SCVForBB = freeWorker();
        if(SCVForFinding == null || SCVForFinding.distanceTo(myBase.getX(), myBase.getY()) > 10000)
        	SCVForFinding = freeWorker();
        
        Position now = possiblePosition[nowLookAt];
        Position scvP = SCVForFinding.getPosition();
       
        game.setTextSize(10);
        game.drawTextScreen(10, 10, "Playing as " + self.getName() + " - " + self.getRace() + "nUnit = " + nUnitID);
        
        //game.drawLineMap(now.getX(), now.getY(), scvP.getX(), scvP.getY(), new Color(255,0,0));
        
        if(nowLookAt < nPossible && myBase.distanceTo(now.getX(), now.getY()) < 100)
        	nowLookAt += 1;
        if(nowLookAt < nPossible && SCVForFinding.getPosition().distanceTo(now.getX(), now.getY()) < 100)
        	nowLookAt += 1;
        
        now = possiblePosition[nowLookAt];
        
        //game.drawTextScreen(10, 30, "d = " + SCVForFinding.getOrderTargetPosition().distanceTo(now.getX(), now.getY()));
        if(enemyPosKnown == false)
        {
        	if(population_total > 10 * 2)
        		if(SCVForFinding.getOrderTargetPosition().distanceTo(now.getX(), now.getY()) > 2)
        			SCVForFinding.move(now);
        }
        else
        {
        	
        	if(f1)
        	{
        		SCVForFinding.move(myBase);
        		f1 = false;
        	}
        }
        
        if(enemyPosKnown == false)
        {
        	guessEnemy = now;
        	for(Unit u : game.getAllUnits())
        	{
        		if(self.isEnemy(u.getPlayer()))
        		{
        			if(u.getType() == UnitType.Protoss_Nexus || u.getType() == UnitType.Terran_Command_Center || u.getType() == UnitType.Zerg_Hatchery)
        			{
        				guessEnemy = u.getPosition();
        				enemyPosKnown = true;
        			}
        		}
        	}
        }
        
        
        
        Position middlePoint = new Position((myBase.getX() + guessEnemy.getX()) / 2, (myBase.getY() + guessEnemy.getY())/ 2);
        game.drawLineMap(middlePoint.getX(), middlePoint.getY(), guessEnemy.getX(), guessEnemy.getY(), new Color(255,0,0));
        
        
        int remain = population_total - population_used;
        game.drawTextScreen(10, 105, population_total + " - " +  population_used);
    	if(self.minerals() >= 100 && (remain < 6 * 2 || remain < population_total / 5)  && population_total < 200)
    	{
    		Unit selectedSCV = SCVForBS;
    		if(selectedSCV == null)
    		{
    		}
    		else
    		{
    			int before = self.minerals();
	    		TilePosition me = selectedSCV.getTilePosition();
	    		int meX = me.getX();
	    		int meY = me.getY();
	    		buildNear(selectedSCV, meX, meY, UnitType.Protoss_Pylon);
	    		game.drawTextScreen(10, 55, "Build BS!!");
	    		
	    		int after = self.minerals();
    			
    			recordVar = after - before;
    		}
    	}
    	
    	
    	// build BB
    	if(population_used > 10 * 2 && self.minerals() >= 150 && countMyUnit(UnitType.Protoss_Gateway) < MAX_BG)
    	{
    		Unit selectedSCV = SCVForBB;
    		if(selectedSCV == null)
    		{
    		}
    		else
    		{
	    		TilePosition me = selectedSCV.getTilePosition();
	    		int meX = me.getX();
	    		int meY = me.getY();
	    		buildNear(selectedSCV, meX, meY, UnitType.Protoss_Gateway);
	    		game.drawTextScreen(10, 55, "Build BB!!");
    		}
    	}
    	
    	
        
        //iterate through my units
        for (Unit myUnit : self.getUnits()) {
            units.append(myUnit.getType()).append(" ").append(myUnit.getTilePosition()).append("\n");

            
            
            //if there's enough minerals, train an SCV
            if (myUnit.getType() == UnitType.Protoss_Nexus && self.minerals() >= 50) {
            	if(myUnit.isTraining() == false)
            		if(countMyUnit(UnitType.Protoss_Probe) < MAX_WORKER)
            		{
            			int before = self.minerals();
            			myUnit.train(UnitType.Protoss_Probe);
            			int after = self.minerals();
            		}
            }
            //if it's a drone and it's idle, send it to the closest mineral patch
            if (myUnit.getType().isWorker() && myUnit.isIdle()) {
                Unit closestMineral = null;
                
                //find the closest mineral
                for (Unit neutralUnit : game.neutral().getUnits()) {
                    if (neutralUnit.getType().isMineralField()) {
                        if (closestMineral == null || myUnit.getDistance(neutralUnit) < myUnit.getDistance(closestMineral)) {
                            closestMineral = neutralUnit;
                        }
                    }
                }

                //if a mineral patch was found, send the drone to gather it
                if (closestMineral != null) {
                    myUnit.gather(closestMineral, false);
                }
            }
            
            if(self.minerals() >= 50 && myUnit.getType() == UnitType.Protoss_Gateway && myUnit.isTraining() == false)
            	myUnit.train(UnitType.Protoss_Zealot);
            
            if(myUnit.getType() == UnitType.Protoss_Zealot || myUnit.getType() == UnitType.Protoss_Probe)
            {
            	Unit atkUnt = null;
            	Position pos = new Position(10000, 10000);
            	double prevHP = 100000;
            		
            	{
                	for (Unit u : game.getAllUnits()) {
                		if(u.isVisible() == false)
                			continue;
                		if(u.getType() == UnitType.Zerg_Overlord)
                			continue;
                		
                		if(self.isEnemy(u.getPlayer()))
                		{
                			double distMeToEnemy = u.getPosition().distanceTo(myUnit.getPosition().getX(), myUnit.getPosition().getY());
                			double distMeToPrevAtkPoint = pos.distanceTo(myUnit.getPosition().getX(), myUnit.getPosition().getY());
                			/*if(distMeToPrevAtkPoint < 80)
                			{
                				if(u.getHitPoints() + u.getShields() < prevHP)
                				{
                					prevHP = u.getHitPoints() + u.getShields();
                					pos = u.getPosition();
                    				atkUnt = u;
                				}
                			}
                			else*/ if(distMeToEnemy < distMeToPrevAtkPoint)
                			{
                				pos = u.getPosition();
                				atkUnt = u;
                			}
                		}
                	}
            	}
            	
            	if(countMyUnit(UnitType.Protoss_Zealot) < ATTACK_ARMY || atkUnt == null)
            	{
	            	if(countMyUnit(UnitType.Protoss_Zealot) < ATTACK_ARMY)
	            		pos = middlePoint;
	            	else
	            		pos = guessEnemy;
            	}
            	
            	if(atkUnt != null)
            		if(myUnit.getType() == UnitType.Protoss_Probe && myUnit.distanceTo(atkUnt.getX(), atkUnt.getY()) > 100)
            			continue;
            	if(atkUnt == null && myUnit.getType() == UnitType.Protoss_Probe)
            		continue;
            	if(atkUnt != null && atkUnt.isAttacking() == false && myUnit.getType() == UnitType.Protoss_Probe)
            		continue;
            
            	if(atkUnt != null)
            	{
            		
            		if(getRemain(myUnit.getID()) == 0)
            		{
            			myUnit.attack(atkUnt);
            			setRemain(myUnit.getID(), 10);
            			game.drawLineMap(myUnit.getPosition().getX(), myUnit.getPosition().getY(), atkUnt.getPosition().getX(), atkUnt.getPosition().getY(), new Color(255,0,0));
            		}
            		else
            		{
            			game.drawLineMap(myUnit.getPosition().getX(), myUnit.getPosition().getY(), atkUnt.getPosition().getX(), atkUnt.getPosition().getY(), new Color(0,255,0));
            		}
            	}
            	else
            	{
            		myUnit.attack(pos);
            		game.drawTextScreen(10, 25, "attack to pos");
            	}
            }
            
        }
        
        
        //draw my units on screen
        game.drawTextScreen(10, 135, recordVar + "");
        //game.drawTextScreen(10, 155, dbgStr2);
        
    }

    public static void main(String[] args) {
        new TestBot1().run();
    }
}
