import java.util.Collection;
import java.util.HashMap;
import java.util.TreeSet;
import MuDamage.*;

public class MuScript {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MuParser muParser = new MuParser(17);
		HashMap<Integer,MilitaryUnit> mus = muParser.getMus();
		BattleInfo battle = new BattleInfo(8014,true);
		
		BattleParser btParser = new BattleParser();
		mus = btParser.parseEntireBattle(mus, battle);
		
		Collection<MilitaryUnit> muSet = mus.values();
		
		
		// Sort according to battle dmg
		TreeSet<MilitaryUnit> sortedMus = new TreeSet<MilitaryUnit>();
		for (MilitaryUnit mu : muSet) {
			sortedMus.add(mu);
		}
		
		// Print
		System.out.print("Mu dmg in battle: " + battle.getId());
		if(battle.isDefender()) {
			System.out.println(" for the defenders.");
		}else {
			System.out.println(" for the attackers.");
		}
		for(MilitaryUnit mu : sortedMus) {
			System.out.println(mu.toString());
		}

	}

}
