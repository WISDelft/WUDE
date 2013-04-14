package nl.wisdelft.data;

public class TimelineParserTask {
	TimelineParserTask(long userId, int taskId) {
		this.userId = userId;
		this.taskId = taskId;
	}

	private long userId;

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	private int taskId;
}