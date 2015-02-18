package main;
import bwapi.*;
import bwta.BWTA;

import java.util.*;

import task.Task;
import util.Debug;
import component.Component;

public class Bot extends DefaultBWListener {
	
    private Mirror mirror = new Mirror();
    public Game game;
    public Player self;
    
    public void run() {
        mirror.getModule().setEventListener(this);
        mirror.startGame();
    }
    
    @Override
    public void onUnitCreate(Unit unit) {
        System.out.println("New unit " + unit.getType());
    }
    
    public info.GlobalGameInfo gameInfo;
    public info.FrameGameInfo frameInfo;
    
    public List <task.Task> currentTasks;
    public List <component.Component> components;
    
    public util.Debug debug;
    
    
    @Override
    public void onStart() {
    	
        game = mirror.getGame();
        self = game.self();
        
        game.setLocalSpeed(0);
        
        System.out.println("Analyzing map...");
        BWTA.readMap();
        BWTA.analyze();
        System.out.println("Map data ready");
        
        gameInfo = new info.GlobalGameInfo();
        gameInfo.init();
        frameInfo = new info.FrameGameInfo(this);
        debug = new Debug(this);
        
        currentTasks = new ArrayList<task.Task>();
        components = new ArrayList<component.Component>();
        components.add(new component.BaseManager(this));
        components.add(new component.Reconnoiter(this));
        components.add(new component.SupplyManager(this));
        components.add(new component.TechBuildingPlanner(this));
        components.add(new component.ArmyProductionManager(this));
        components.add(new component.ArmyCommander(this));
    }
    
    @Override
    public void onFrame() {
    	
        debug.debugInfo.clear();
        debug.addDebugInfo("Frame #" + frameInfo.frameNumber + "");
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
        	debug.addDebugInfo("[Component #" + componentID + ": " + comp.getComponentName() + "]");
        	comp.onFrame();
        }
        
        int taskID = 0;
        for(Task t : currentTasks)
        {
        	taskID += 1;
        	debug.addDebugInfo("<Task #" + taskID + ": " + t.getTaskName() + "> : Minerals = " + t.getNeedMinerals() + ", Gas = " + t.getNeedGas());
        	t.onFrame();
        }
        
        debug.outputDebugInfoToScreen();
    }

    public static void main(String[] args) {
        new Bot().run();
    }
}
