package MuDamage;

public class BattleInfo {
	private Integer id;
	private boolean defender;
	
	public BattleInfo() {
		this.id = 0;
		this.defender = true;
	}
	
	public BattleInfo(int id) {
		this.id = id;
		this.defender = false;
	}
	
	public BattleInfo(int id, boolean defender) {
		this.id = id;
		this.defender = defender;
	}
	
	public Integer getId() {
		return id;
	}
	
	public boolean isDefender() {
		return defender;
	}
	
	@Override public boolean equals(Object other) {
	    boolean result = false;
	    if (other instanceof BattleInfo) {
	    	BattleInfo that = (BattleInfo) other;
	        result = that.getId().equals(id);
	    }
	    return result;
	}

}
