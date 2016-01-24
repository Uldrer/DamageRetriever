package MuDamage;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Vector;

public class DamageRetriever implements Runnable, Serializable {
	
	private MuParser muParser;
	private HashMap<Integer,MilitaryUnit> mus;
	private HashMap<BattleInfo,HashMap<Integer,MilitaryUnit>> battleInformation;
	private BattleParser battleParser;
	private String retrievedResult;
	private ArrayList<BattleInfo> battleInfos;
	private transient Vector<ProgressListener> listeners;
	private double battlePercentStep;
	private int currentBattleNr;
	private boolean doneParsingMembers;
	
	public DamageRetriever(ArrayList<Integer> countryIds, ArrayList<Integer> muIds) {
		
		battleInformation = new HashMap<BattleInfo,HashMap<Integer,MilitaryUnit>>();
		muParser = new MuParser(countryIds, muIds);
		mus = muParser.getMus();
		battleParser = new BattleParser();
		retrievedResult = "";
		battleInfos = null;
		currentBattleNr = 0;
		battlePercentStep = 1;
		doneParsingMembers = false;
		battleParser.addRoundParsedListener(new RoundParsedListener(){

			@Override
			public void roundParsed(RoundParsedEvent e) {
				
				fireProgressEvent(currentBattleNr*battlePercentStep + battlePercentStep*e.getPercent(),ParsingType.BATTLE);
				
			}
			
		});
		
		muParser.addMembersParsedListener(new MembersParsedListener() {

			@Override
			public void membersParsed(MembersParsedEvent e) {
				
				fireProgressEvent(1,ParsingType.MU);
				mus = muParser.getMus();
				doneParsingMembers = true;
				
			}
			
		});
		
		// Get mu-member information asynchrously
		Thread muParsingThread = new Thread(muParser);
		muParsingThread.start();
	}
	
	public DamageRetriever() {
		
		battleInformation = new HashMap<BattleInfo,HashMap<Integer,MilitaryUnit>>();
		muParser = new MuParser(new int[]{17, 19, 20, 25, 53}); // 26 USA, 25 Mexico, 20 Latvia, 19 Lithuania, 17 Sweden, 43 norway, 53 Estonia, flera MuParser(17,43)
		mus = muParser.getMus();
		battleParser = new BattleParser();
		retrievedResult = "";
		battleInfos = null;
		currentBattleNr = 0;
		battlePercentStep = 1;
		doneParsingMembers = false;
		battleParser.addRoundParsedListener(new RoundParsedListener(){

			@Override
			public void roundParsed(RoundParsedEvent e) {
				
				fireProgressEvent(currentBattleNr*battlePercentStep + battlePercentStep*e.getPercent(),ParsingType.BATTLE);
				
			}
			
		});
		
		muParser.addMembersParsedListener(new MembersParsedListener() {

			@Override
			public void membersParsed(MembersParsedEvent e) {
				
				fireProgressEvent(1,ParsingType.MU);
				mus = muParser.getMus();
				doneParsingMembers = true;
				
			}
			
		});
		
		// Get mu-member information asynchrously
		Thread muParsingThread = new Thread(muParser);
		muParsingThread.start();
	}
	
	
	public String[] getMilitaryUnitInfo() {
		// add mus to list
		String[] info = new String[mus.size()];
		
		TreeSet<MilitaryUnit> sortedMus = new TreeSet<MilitaryUnit>(mus.values());
		
		int i = 0;
		for(MilitaryUnit mu : sortedMus) {
			info[i] = mu.getName();
			i++;
		}
		return info;
	}
	
	public MemberStorage getMilitaryUnitMemberInfo(String muName) {
		// add members to list
		MilitaryUnit chosenMu = null;
		for (MilitaryUnit mu : mus.values()) {
			if(mu.getName().equals(muName)) {
				chosenMu = mu;
			}
		}
		
		if(chosenMu == null){
			return new MemberStorage();
		}
		
		Collection<Member> members = chosenMu.getAllMembers();
		String[] names = new String[members.size()];
		String[] battleDmg = new String[members.size()];
		String[] t2Dmg = new String[members.size()];
		String[] enemyDmg = new String[members.size()];
		
		TreeSet<Member> sortedMembers = new TreeSet<Member>(members);
		
		int i = 0;
		DecimalFormat df = new DecimalFormat("#");
		df.setMaximumFractionDigits(0);
		for(Member member : sortedMembers) {
			names[i] = member.getName();
			battleDmg[i] = df.format(member.getBattleDmg());
			t2Dmg[i] = df.format(member.getT2Dmg());
			enemyDmg[i] = df.format(member.getEnemyDmg());
			i++;
		}
		return new MemberStorage(names, battleDmg, t2Dmg, enemyDmg);
	}
	
	public void SetBattles(ArrayList<BattleInfo> battleInfos) {
		if(battleInfos.size() > 0) {
			this.battleInfos = battleInfos;
		}
	}
	
	public String[] getWeights() {
		return battleParser.getWeights();
	}
	
	public void setWeight(int index, double value) {
		battleParser.setWeight(index,value);
	}
	
	
	public String retrieveDamageForBattles(ArrayList<BattleInfo> battleInfos) {
		
		if(!doneParsingMembers) {
			return "Not done parsing MU-members";
		}
		
		for (BattleInfo info : battleInformation.keySet()) {
			//TODO: If any battle id already parsed then add them, otherwise remove them.
		}
		
		//clear all
		for (MilitaryUnit mu : mus.values()) {
			mu.clearDmg();
		}
		
		int i = 1;
		int size = battleInfos.size();
		battlePercentStep = (double) 1/size;
		for(BattleInfo info : battleInfos) {
			mus = battleParser.parseEntireBattle(mus, info);
			if(i != size){
				fireProgressEvent((double) i/size,ParsingType.BATTLE);
			}
			currentBattleNr++;
			i++;
		}
		
		TreeSet<MilitaryUnit> sortedMus = new TreeSet<MilitaryUnit>(mus.values());
		
		String result = "";
		
		for (MilitaryUnit mu : sortedMus) {
			result += mu.toString() + "\n";
		}
		
		
		return result;
		
	}
	
	public String getResult() {
		return retrievedResult;
	}


	@Override
	public void run() {
		
		if(battleInfos != null){
			retrievedResult = retrieveDamageForBattles(battleInfos);
			fireProgressEvent(1,ParsingType.BATTLE);
			currentBattleNr = 0;
		}
		
	}
	
	synchronized public void addProgressListener(ProgressListener listener) {
		if(listeners == null) {
			listeners = new Vector<ProgressListener>();
		}
		listeners.addElement(listener);
	}
	
	synchronized public void removeProgressListener(ProgressListener listener) {
		if(listeners == null) {
			listeners = new Vector<ProgressListener>();
		}
		listeners.removeElement(listener);
	}
	
	protected void fireProgressEvent(double percent, ParsingType type ) {
		
		if(listeners != null && !listeners.isEmpty()) {
			ProgressEvent event = new ProgressEvent(this,percent,type);
			
			Vector<ProgressListener> targets;
		    synchronized (this) {
		        targets = (Vector<ProgressListener>) listeners.clone();
		    }

		    // walk through the listener list and
		    //   call the sunMoved method in each
		    Enumeration<ProgressListener> e = targets.elements();
		    while (e.hasMoreElements()) {
		    	ProgressListener listener = (ProgressListener) e.nextElement();
		    	listener.progressEvent(event);
		    }
			
		}
		
	}
	
	

}
