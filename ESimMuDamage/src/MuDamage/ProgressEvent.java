package MuDamage;

import java.util.EventObject;

public class ProgressEvent extends EventObject {

	private double percentFinished;
	private ParsingType type;
	
	public ProgressEvent(Object source, double percentFinished, ParsingType type) {
		super(source);
		this.percentFinished = percentFinished;
		this.type = type;
	}
	
	public double getPercent() {
		return percentFinished;
	}
	
	public ParsingType getType() {
		return type;
	}
}
