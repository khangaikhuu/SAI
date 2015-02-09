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
    	/*
    	for(int i = 1; i <= nUnitID; i++)
    		if(unitID[i] == uID)
    			return remainNextAttack[i];
    	nUnitID += 1;
    	remainNextAttack[nUnitID] = 0;
    	unitID[nUnitID] = uID;
    	return 0;*/
    }
    
    void setRemain(int uID, int val)
    {
    	remainNextAttack[uID] = val;
    	nUnitID = Math.max(nUnitID, uID);
    	/*
    	for(int i = 1; i <= nUnitID; i++)
    		if(unitID[i] == uID)
    		{
    			remainNextAttack[i] = val;
    			return i;
    		}
    	return 0;*/
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
    
    @Override
    public void onStart() {
    	//unitID = new int[100001];
    	remainNextAttack = new int[100001];
    	nUnitID = 0;
        game = mirror.getGame();
        self = game.self();
        flag = false;
        //game.enableFlag(1);
        game.setLocalSpeed(0);
        //Use BWTA to analyze map
        //This may take a few minutes if the map is processed first time!
        System.out.println("Analyzing map...");
        BWTA.readMap();
        BWTA.analyze();
        System.out.println("Map data ready");
    }
    
    public void buildNear(Unit builder, int x, int y, UnitType buildingType)
    {
		int[] dx = {0, 1, 0, -1};
		int[] dy = {1, 0, -1, 0};		
		for(int d = 5; d >= 0 ; d--)
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
	
    @Override
    public void onFrame() {
    	
    	int MAX_BG = 5;
    	int MAX_WORKER = 25;
    	
    	for(int i = 1; i <= nUnitID; i++)
    		remainNextAttack[i] = Math.max(0, remainNextAttack[i] - 1);
    	
        game.setTextSize(10);
        game.drawTextScreen(10, 10, "Playing as " + self.getName() + " - " + self.getRace());

        
        
        int population_total = self.supplyTotal();
        int population_used = self.supplyUsed();
        
        StringBuilder units = new StringBuilder("My units:\n");
        
        
        // build BS
        if(SCVForBS == null)
        	SCVForBS = freeWorker();
        if(SCVForBB == null)
        	SCVForBB = freeWorker();
        if(SCVForFinding == null)
        	SCVForFinding = freeWorker();
        
        
        if(guessEnemy == null)
        {
	        for(BaseLocation b : BWTA.getBaseLocations())
	        {
	        	Position pb = b.getPosition();
	        	if(guessEnemy == null)
	        		guessEnemy = pb;
	        	else
	        	{
	        		if(SCVForBS.distanceTo(pb.getX(), pb.getY()) > SCVForBS.distanceTo(guessEnemy.getX(), guessEnemy.getY()))
	        		{
	        			guessEnemy = pb;
	        		}
	        	}
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
        
        SCVForFinding.move(guessEnemy);
        
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
	    		TilePosition me = selectedSCV.getTilePosition();
	    		int meX = me.getX();
	    		int meY = me.getY();
	    		buildNear(selectedSCV, meX, meY, UnitType.Protoss_Pylon);
	    		game.drawTextScreen(10, 55, "Build BS!!");
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
    	
    	
    	
    	String dbgStr = "";
    	String dbgStr2 = "";
        
        //iterate through my units
        for (Unit myUnit : self.getUnits()) {
            units.append(myUnit.getType()).append(" ").append(myUnit.getTilePosition()).append("\n");

            
            
            //if there's enough minerals, train an SCV
            if (myUnit.getType() == UnitType.Protoss_Nexus && self.minerals() >= 50) {
            	if(myUnit.isTraining() == false)
            		if(countMyUnit(UnitType.Protoss_Probe) < MAX_WORKER)
            			myUnit.train(UnitType.Protoss_Probe);
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
            
            if(myUnit.getType() == UnitType.Protoss_Zealot)
            {
            	Unit atkUnt = null;
            	Position pos = new Position(10000, 10000);
            	
            		
            	{
                	for (Unit u : game.getAllUnits()) {
                		
                		if(self.isEnemy(u.getPlayer()))
                		{
                			double distMeToEnemy = u.getPosition().distanceTo(myUnit.getPosition().getX(), myUnit.getPosition().getY());
                			double distMeToPrevAtkPoint = pos.distanceTo(myUnit.getPosition().getX(), myUnit.getPosition().getY());
                			if(distMeToEnemy < distMeToPrevAtkPoint)
                			{
                				pos = u.getPosition();
                				atkUnt = u;
                			}
                		}
                	}
            	}
            	
            	if(countMyUnit(UnitType.Protoss_Zealot) < 20 || atkUnt == null)
            	{
	            	if(countMyUnit(UnitType.Protoss_Zealot) < 20)
	            		pos = middlePoint;
	            	else
	            		pos = guessEnemy;
            	}
            		
            
            	if(atkUnt != null)
            	{
            		dbgStr += myUnit.getID() + " ";
            		dbgStr2 += getRemain(myUnit.getID()) + " ";
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
        game.drawTextScreen(10, 135, dbgStr);
        game.drawTextScreen(10, 155, dbgStr2);
        
    }

    public static void main(String[] args) {
        new TestBot1().run();
    }
}
