import bwapi.*;
import bwta.BWTA;
import bwta.BaseLocation;

public class TestBot1 extends DefaultBWListener {

    private Mirror mirror = new Mirror();

    private Game game;

    private Player self;
    
    
    int[] remainNextAttack;
    int nUnit;
    
    int getRemain(int uID)
    {
    	nUnit = Math.max(nUnit, uID);
    	return remainNextAttack[uID];
    }
    
    void setRemain(int uID, int val)
    {
    	nUnit = Math.max(nUnit, uID);
    	remainNextAttack[uID] = val;
    }
    
    public void run() {

        mirror.getModule().setEventListener(this);
        mirror.startGame();

    }
    
    @Override
    public void onUnitCreate(Unit unit) {
        System.out.println("New unit " + unit.getType());
    }
    
    
    @Override
    public void onStart() {
    	//unitID = new int[100001];
    	remainNextAttack = new int[100001];
    	nUnit = 0;
        game = mirror.getGame();
        self = game.self();
        
        //game.enableFlag(1);
        //game.setLocalSpeed(0);
        //Use BWTA to analyze map
        //This may take a few minutes if the map is processed first time!
        System.out.println("Analyzing map...");
        BWTA.readMap();
        BWTA.analyze();
        System.out.println("Map data ready");
    }
    
	
    @Override
    public void onFrame() {
    	
    	for(int i = 1; i <= nUnit; i++)
    		remainNextAttack[i] = Math.max(0, remainNextAttack[i] - 1);
    	
        game.setTextSize(10);
        game.drawTextScreen(10, 10, "Playing as " + self.getName() + " - " + self.getRace());

        
        
        
        //iterate through my units
        String delays = "";
        
        for (Unit allUnit : game.getAllUnits())
        {
        	if(allUnit.getType() == UnitType.Protoss_Pylon)
        		continue;
        	int HP = allUnit.getHitPoints() + allUnit.getShields();
        	game.drawCircleMap(allUnit.getPosition().getX(), allUnit.getPosition().getY(), HP / 10, new Color(0,255,0));
        
        }
        
        for (Unit myUnit : self.getUnits()) {
            
        	delays += "" + getRemain(myUnit.getID()) + " | ";
        	
            if(myUnit.getType() != UnitType.Protoss_Pylon)
            {
            	Unit atkUnt = null;
            	Position pos = new Position(10000, 10000);
            	int minimalHP = 10000;
            	{
                	for (Unit u : game.getAllUnits()) {
                		
                		if(self.isEnemy(u.getPlayer()) && u.getType() != UnitType.Protoss_Pylon)
                		{
                			double distMeToEnemy = u.getPosition().distanceTo(myUnit.getPosition().getX(), myUnit.getPosition().getY());
                			double distMeToPrevAtkPoint = pos.distanceTo(myUnit.getPosition().getX(), myUnit.getPosition().getY());
                			if(atkUnt == null || distMeToEnemy < distMeToPrevAtkPoint)
                			//if(atkUnt == null || u.getHitPoints() + u.getShields() < minimalHP)
                			{
                				minimalHP = u.getHitPoints() + u.getShields();
                				atkUnt = u;
                				pos = u.getPosition();
                			}
                		}
                	}
            	}
            	

            	if(atkUnt != null)
            	{
            		if(getRemain(myUnit.getID()) == 0)
            		{
            			
            			if(myUnit.getOrderTarget() != null)
            				if(myUnit.getOrderTarget().getID() == atkUnt.getID())
	            			{
	            				game.drawLineMap(myUnit.getPosition().getX(), myUnit.getPosition().getY(), atkUnt.getPosition().getX(), atkUnt.getPosition().getY(), new Color(0,255,0));
	            				continue;
	            			}
            			myUnit.attack(atkUnt);
            			game.drawLineMap(myUnit.getPosition().getX(), myUnit.getPosition().getY(), atkUnt.getPosition().getX(), atkUnt.getPosition().getY(), new Color(255,0,0));
            		}
            	}
            }
            
        }
        
        
        game.drawTextScreen(10, 125, delays);
        
        //draw my units on screen
        
    }

    public static void main(String[] args) {
        new TestBot1().run();
    }
}
