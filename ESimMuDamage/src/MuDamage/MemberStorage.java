package MuDamage;

public class MemberStorage {
	
	private String[] names;
	private String[] battelDmg;
	private String[] t2Dmg;
	private String[] enemyDmg;
	
	public MemberStorage() {
		this.names = new String[]{""};
		this.battelDmg = new String[]{""};
		this.t2Dmg = new String[]{""};
		this.enemyDmg = new String[]{""};
	}

	public MemberStorage(String[] names, String[] battelDmg, String[] t2Dmg, String[] enemyDmg) {
		this.names = names;
		this.battelDmg = battelDmg;
		this.t2Dmg = t2Dmg;
		this.enemyDmg = enemyDmg;
	}
	
	public String[] getNames() {
		return names;
	}
	
	public String[] getBattelDmg() {
		return battelDmg;
	}
	
	public String[] getT2Dmg() {
		return t2Dmg;
	}
	
	public String[] getEnemyDmg() {
		return enemyDmg;
	}

}
