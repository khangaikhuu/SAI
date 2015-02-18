package task;

public interface Task
{
	String getTaskName();
	int getNeedMinerals();
	int getNeedGas();
	void onFrame();
	boolean isFinished();
}
