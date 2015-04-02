package main;
import bwapi.*;
import bwta.BWTA;
import gui.GUIManager;
import headquarter.EconomyHQ;
import headquarter.HQ;
import headquarter.StrategyHQ;
import information.GameInfo;
import information.Goal;

import java.util.*;

import strategy.Strategy;
import task.Task;
import task.TaskManager;
import component.ArmyControlManager;
import component.ArmyProductionManager;
import component.BaseManager;
import component.Component;
import component.SupplyManager;
import component.TechBuildingManager;
import component.UpgradeManager;
import computation.General;

public class Bot extends DefaultBWListener {
	
    private Mirror mirror = new Mirror();
    public Game game;
    public Player self, enemy;
    
    boolean isFirstFrame;
    
    public computation.General util;
    public gui.GUIManager guiManager;
    public task.TaskManager taskManager;
    public information.GameInfo info;
    public information.Goal goal;
    public Strategy strategy;
    
    // HQ
    public List <HQ> listOfHQ;
    public EconomyHQ economyHQ;
    public StrategyHQ strategyHQ;
    
    // Component
    public List <Component> listOfComponents;
    public BaseManager baseManager;
    public ArmyControlManager armyControlManager;
    public ArmyProductionManager armyProductionManager;
    public SupplyManager supplyManager;
    public TechBuildingManager techBuildingManager;
    public UpgradeManager upgradeManager;
    
    // Task
    public List <Task> listOfTasks;
    
    public void run() {
        mirror.getModule().setEventListener(this);
        mirror.startGame();
    }
    
    @Override
    public void onUnitMorph(Unit unit)
    {
    	info.onUnitCreate(unit);
    	//System.out.println("New unit " + unit.getType());
    }
    
    @Override
    public void onUnitCreate(Unit unit)
    {
    	info.onUnitCreate(unit);
        //System.out.println("New unit " + unit.getType());
    }
    
    @Override
    public void onUnitDestroy(Unit unit) 
    {
    	//System.out.println("destory unit " + unit.getType());
    	info.onUnitDestroy(unit);
    };
    
    @Override
    public void onStart() {
    	
    	isFirstFrame = true;
    	
        game = mirror.getGame();
        self = game.self();
        enemy = game.enemies().get(0);
        
        game.setLocalSpeed(0);
        game.enableFlag(1);
        
        util = new General(this);
        info = new GameInfo(this);
        guiManager = new GUIManager(this);
        taskManager = new TaskManager(this);
        goal = new Goal(this);
        
        listOfHQ = new ArrayList<HQ>();
        listOfComponents = new ArrayList<Component>();
        listOfTasks = new ArrayList<Task>();
        
        // HQ
        economyHQ = new EconomyHQ(this);
        listOfHQ.add(economyHQ);
        strategyHQ = new StrategyHQ(this);
        listOfHQ.add(strategyHQ);
        
                
        // Component
        baseManager = new BaseManager(this);
        listOfComponents.add(baseManager);
        armyControlManager = new ArmyControlManager(this);
        listOfComponents.add(armyControlManager);
        armyProductionManager = new ArmyProductionManager(this);
        listOfComponents.add(armyProductionManager);
        supplyManager = new SupplyManager(this);
        listOfComponents.add(supplyManager);
        techBuildingManager = new TechBuildingManager(this);
        listOfComponents.add(techBuildingManager);
        upgradeManager = new UpgradeManager(this);
        listOfComponents.add(upgradeManager);
        
        
        System.out.println("Analyzing map...");
        BWTA.readMap();
        BWTA.analyze();
        System.out.println("Map data ready");
    }
    
    @Override
    public void onFrame() {
    	
    	guiManager.onFrameStart();
    	
    	if(isFirstFrame)
    	{
    		isFirstFrame = false;
    		info.updateBasesFirstFrame();
    	}
    	
    	info.onFrameStart();
    	
    	taskManager.onFrameStart();
    	for(Component c : listOfComponents)
    		c.onFrameStart();
    	
    	guiManager.addDebugInfo("#Tasks = " + listOfTasks.size());
    	for(Task t : listOfTasks)
    	{
    		guiManager.addDebugInfo("(" + t.needMinerials() + ") " + t.getName());
    	}
    	guiManager.addDebugInfo("#BaseLocations = " + bwta.BWTA.getBaseLocations().size());
    	
    	
    	strategyHQ.onFrame();
    	economyHQ.onFrame();
    	
    	
    	for(Task t : listOfTasks)
			t.onFrame();
    	
    	guiManager.onFrameEnd();
    	
    	for(Unit u : self.getUnits())
    	{
    		if(info.getTask(u) != null)
    			if(info.getTask(u).creator != null)
    			{
    				game.setTextSize(1);
    				game.drawTextMap(u.getPosition().getX() + 10, u.getPosition().getY() + 10, info.getTask(u).getName());
    				game.drawTextMap(u.getPosition().getX() + 10, u.getPosition().getY() + 25, info.getTask(u).creator.getName());
    			}
    	}
    	
    }
    
    public static void main(String[] args) {
    	//try{
    		new Bot().run();
    	/*}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		
    		try{
    			PrintWriter writer = new PrintWriter((new Date(System.currentTimeMillis())).toString().replace(' ', '_').replace(':', '_') +  ".txt");
    			e.printStackTrace(writer);
    			writer.close();
    		} catch (FileNotFoundException e1) {
				
				e1.printStackTrace();
			}
    	}*/
    }
}
