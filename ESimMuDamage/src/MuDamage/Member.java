package MuDamage;

public class Member implements Comparable<Member> {
	private int id;
	private double battleDmg;
	private double t2Dmg;
	private double enemyDmg;
	private String name;
	
	public Member(int citizenId, String name) {
		this.id = citizenId;
		battleDmg = 0;
		t2Dmg = 0;
		enemyDmg = 0;
		this.name = name;
	}
	
	public double getBattleDmg() {
		return battleDmg;
	}
	
	public double getT2Dmg() {
		return t2Dmg;
	}
	
	public double getEnemyDmg() {
		return enemyDmg;
	}
	
	public void addBattleDmg(double dmg) {
		battleDmg += dmg;
	}
	
	public void addT2Dmg(double dmg) {
		t2Dmg += dmg;
	}
	
	public void addEnemyDmg(double dmg) {
		enemyDmg += dmg;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public void clearAll() {
		battleDmg = 0;
		enemyDmg = 0;
		t2Dmg = 0;
	}
	
	public int hashCode()
	{
		return id;
	}

	@Override
	public int compareTo(Member arg0) {
		
		double argDmg = arg0.getBattleDmg();
		
		if(argDmg < battleDmg) {
			return -1;
		}else if(argDmg > battleDmg) {
			return 1;
		}else {
			// if equal compare t2 dmg
			double argT2Dmg = arg0.getT2Dmg();
			if(argT2Dmg < t2Dmg) {
				return -1;
			}else if(argT2Dmg > t2Dmg) {
				return 1;
			}else {
				double argEnemyDmg = arg0.getT2Dmg();
				if(argEnemyDmg < enemyDmg) {
					return -1;
				}else if(argEnemyDmg > enemyDmg){
					return 1;
				}else {
					//if equal go on citizienId, lowest first
					int argId = arg0.getId();
					if(argId < id) {
						return 1;
					}else if(argId > id) {
						return -1;
					}else {
						return 0;
					}
				}
			}
		}
	}

}
