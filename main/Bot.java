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
        components.add(new component.UpgradeManager(this));
    }
    
    @Override
    public void onFrame() {
    	
    	util.Debug.root = this;
    	util.General.root = this;
    	
    	long frameStart = System.nanoTime();
    	
        debug.debugInfo.clear();
        debug.debugLong = 0;
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
        
        
        long [] timeUseage = new long[components.size()];
        long total = 0;
        
        int componentID = 0;
        for (Component comp : components)
        {
        	long prev = System.nanoTime();
        	componentID += 1;
        	debug.addDebugInfo("[Component #" + componentID + ": " + comp.getComponentName() + "]");
        	comp.onFrame();
        	long now = System.nanoTime();
        	total += now - prev;
        	timeUseage[componentID - 1] = now - prev;
        }
        
        int taskID = 0;
        for(Task t : currentTasks)
        {
        	taskID += 1;
        	debug.addDebugInfo("<Task #" + taskID + ": " + t.getTaskName() + "> : Minerals = " + t.getNeedMinerals() + ", Gas = " + t.getNeedGas());
        	t.onFrame();
        }
        
        String timePercents = "[";
        for(int i = 0; i < components.size(); i++)
        {
        	if(timeUseage[i] * 100 / total < 10)
        		timePercents += "0";
        	timePercents += "" + (timeUseage[i] * 100 / total) + " | ";
        }
        timePercents += "]";
        debug.addDebugInfo(timePercents);
        
        long frameUses = System.nanoTime() - frameStart;
        String utilUseage = "" + (debug.debugLong * 100 / frameUses);
        debug.addDebugInfo(debug.debugLong + " / " + frameUses + " = " + utilUseage);
        String fps = "" + (1000000000 / frameUses);
        debug.addDebugInfo(fps + " FPS.");
        debug.outputDebugInfoToScreen();
        
        
        
        game.drawCircleMap(util.General.getNextBasePosition(this).getX(), util.General.getNextBasePosition(this).getY(), 100, new Color(0, 255, 0));
        game.drawLineMap(util.General.getNextBasePosition(this).getX(), util.General.getNextBasePosition(this).getY(), gameInfo.myFirstBase().getX(), gameInfo.myFirstBase().getY(), new Color(255, 255, 0));
        
    }

    public static void main(String[] args) {
        new Bot().run();
    }
}
