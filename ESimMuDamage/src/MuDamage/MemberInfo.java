package MuDamage;

public class MemberInfo {
	
	private int strength;
	private double damageToday;
	private double economySkill;
	private double totalDamage;
	private boolean organization;
	private int citizenship;
	private int level;
	private double xp;
	private String name;
	private int id;
	private String rank;

	public MemberInfo(int id, String name) {
		this.id = id;
		this.name = name;
	}
	
	// Add constructor + getters for others if needed

	public String getName() {
		return name;
	}

	public int getId() {
		return id;
	}

}
