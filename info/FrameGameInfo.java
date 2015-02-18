package info;

import main.Bot;

public class FrameGameInfo
{
	public int remainMinerals;
	public int remainGas;
	public int frameNumber;
	
	Bot root;
	
	public FrameGameInfo(Bot r) {
		root = r;
	}
	
	public void onFrameInit()
	{
		remainMinerals = root.self.minerals();
		remainGas = root.self.gas();
		frameNumber = root.game.getFrameCount();
	}
}
