package MuDamage;

import java.util.EventObject;

public class RoundParsedEvent extends EventObject {
	
	private double percentParsed;
	
	public RoundParsedEvent(Object source, double percentParsed) {
		super(source);
		this.percentParsed = percentParsed;
	}
	
	public double getPercent() {
		return percentParsed;
	}

}
