package MuDamage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class RoundInfo {
	
	private Calendar time;
	private int militaryUnitId;
	private boolean berserk;
	private boolean locationBonus;
	private double militaryUnitBonus;
	private int weapon;
	private boolean defenderSide;
	private double damage;
	private int citizenship;
	private int citizenId;
	
	RoundInfo(String time, int militaryUnitId, boolean berserk, boolean locationBonus, double militaryUnitBonus, int weapon
			, boolean defenderSide, double damage, int citizenship, int citizenId) {
		SimpleDateFormat  format = new SimpleDateFormat("dd-MM-yyy HH:mm:ss:SSS"); 
		this.time = Calendar.getInstance();
		try {
			this.time.setTime(format.parse(time));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.militaryUnitId = militaryUnitId;
		this.berserk = berserk;
		this.locationBonus = locationBonus;
		this.militaryUnitBonus = militaryUnitBonus;
		this.weapon = weapon;
		this.defenderSide = defenderSide;
		this.damage = damage;
		this.citizenship = citizenship;
		this.citizenId = citizenId;
	}

	public int getMilitaryUnitId() {
		return militaryUnitId;
	}

	public boolean isDefenderSide() {
		return defenderSide;
	}

	public double getDamage() {
		return damage;
	}


	public int getCitizenship() {
		return citizenship;
	}


	public int getCitizenId() {
		return citizenId;
	}

	public Calendar getTime() {
		return time;
	}
	
	
	
	

}
