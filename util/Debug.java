package util;

import java.util.ArrayList;
import java.util.List;

public class Debug {
	
	public static main.Bot root;
	public long debugLong;
	
	public Debug(main.Bot r) {
		root = r;
		debugInfo = new ArrayList<String>();
	}
	
	public List <String> debugInfo;
	
    public void addDebugInfo(String s)
    {
    	debugInfo.add(s);
    }
    
    public void outputDebugInfoToScreen()
    {
    	root.game.setTextSize(10);
        int startX = 10;
        int startY = 10;
        for (String s : debugInfo)
        {
        	root.game.drawTextScreen(startX, startY, s);
        	startY += 15;
        }        
    }
    
}
