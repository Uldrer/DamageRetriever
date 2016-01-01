package MuDamage;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;

public class MilitaryUnit implements Comparable<MilitaryUnit>{
	
	private int id;
	private double totalDmg;
	private double battleDmg;
	private double t2Dmg;
	private double enemyDmg;
	private double weightedDmg;
	private String name;
	private HashMap<Integer,Member> members;
	
	MilitaryUnit() {
		id = 0;
		totalDmg = 0;
		name = "";
		battleDmg = 0;
		t2Dmg = 0;
		enemyDmg = 0;
		weightedDmg = 0;
		members = new HashMap<Integer,Member>();
	}
	
	MilitaryUnit(int id, double dmg, String name) {
		this.id = id;
		this.totalDmg = dmg;
		this.name = name;
		battleDmg = 0;
		t2Dmg = 0;
		enemyDmg = 0;
		weightedDmg = 0;
		members = new HashMap<Integer,Member>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getTotalDmg() {
		return totalDmg;
	}

	public void addBattleDmg(double dmg) {
		this.battleDmg += dmg;
	}
	
	public double getBattleDmg() {
		return battleDmg;
	}
	
	public void addT2Dmg(double dmg) {
		this.t2Dmg += dmg;
	}
	
	public double getT2Dmg() {
		return t2Dmg;
	}
	
	public void addEnemyDmg(double dmg) {
		this.enemyDmg += dmg;
	}
	
	public double getEnemyDmg() {
		return enemyDmg;
	}
	
	public void addWeightedDmg(double dmg) {
		this.weightedDmg += dmg;
	}
	
	public double getWeightedDmg() {
		return weightedDmg;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public void addMemberBattleDmg(int memberId, double dmg) {
		if(members.containsKey(memberId)) {
			members.get(memberId).addBattleDmg(dmg);
		}
	}
	
	public void addMemberT2Dmg(int memberId, double dmg) {
		if(members.containsKey(memberId)) {
			members.get(memberId).addT2Dmg(dmg);
		}
	}
	
	public void addMemberEnemyDmg(int memberId, double dmg) {
		if(members.containsKey(memberId)) {
			members.get(memberId).addEnemyDmg(dmg);
		}
	}
	
	public void addMember(Member member) {
		if(!members.containsKey(member.getId())) {
			members.put(member.getId(), member);
		}
	}
	
	public void removeMember(Member member) {
		if(members.containsKey(member.getId())) {
			members.remove(member.getId());
		}
	}
	
	public Collection<Member> getAllMembers() {
		return members.values();
	}
	
	public void clearDmg() {
		for( Member member : members.values()) {
			member.clearAll();
		}
		battleDmg = 0;
		t2Dmg = 0;
		enemyDmg = 0;
		weightedDmg = 0;
	}
	
	
	public int hashCode()
	{
		return id;
	}
	
	public String toString() {
		DecimalFormat df = new DecimalFormat("#");
		df.setMaximumFractionDigits(0);
		return name + ", dmg: " + df.format(battleDmg) + " T2: " + df.format(t2Dmg) + " Dmg for enemy: " + df.format(enemyDmg) + " Weighted dmg: " + df.format(weightedDmg);
	}

	@Override
	public int compareTo(MilitaryUnit arg0) {
		
		double argDmg = arg0.getBattleDmg();
		
		if(argDmg < battleDmg) {
			return -1;
		}else if(argDmg > battleDmg) {
			return 1;
		}else {
			// if equal compare total dmg
			double argTotalDmg = arg0.getTotalDmg();
			if(argTotalDmg < totalDmg) {
				return -1;
			}else if(argTotalDmg > totalDmg) {
				return 1;
			}else {
				int argId = arg0.getId();
				if(argId < id) return -1;
				else return 1;
			}
			
		}
	}

}
