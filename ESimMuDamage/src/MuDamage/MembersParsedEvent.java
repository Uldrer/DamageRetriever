package MuDamage;

import java.util.EventObject;

public class MembersParsedEvent extends EventObject {
	
	private boolean finished;
	
	public MembersParsedEvent(Object source, boolean finished) {
		super(source);
		this.finished = finished;
	}
	
	public boolean isFinished() {
		return finished;
	}

}
