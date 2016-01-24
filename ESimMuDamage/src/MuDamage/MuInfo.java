package MuDamage;

public class MuInfo {
	
	private double totalDamage;
	private String name;
	private int countryId;
	private int maxMembers;
	private int goldValue;
	private String militaryUnitType;
	private int id;

	public MuInfo(int countryId, String name, double totalDamage, int id) {
		this.countryId = countryId;
		this.name = name;
		this.totalDamage = totalDamage;
		this.id = id;
	}
	
	// Add constructor + getters for others if needed

	public String getName() {
		return name;
	}

	public int getCountryId() {
		return countryId;
	}
	
	public int getId() {
		return id;
	}
	
	public double getTotalDamage() {
		return totalDamage;
	}

}
