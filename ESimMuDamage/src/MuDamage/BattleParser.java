package MuDamage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;


import com.google.gson.stream.JsonReader;

public class BattleParser extends Parser{
	
	private final String BASE_API_URL = "http://primera.e-sim.org/apiFights.html?battleId=";
	private final String ROUND_URL = "&roundId=";
	private final String NO_ROUND_STRING = "{\"error\":\"No such round in database\"}";
	private final long T2_TIME = 120000; // 2 minutes in milliseconds
	private final long T3_TIME = 180000; // 3 minutes in milliseconds
	private final long T5_TIME = 300000; // 5 minutes in milliseconds
	private final long T10_TIME = 600000; // 10 minutes in milliseconds
	private final long T30_TIME = 1800000; // 30 minutes in milliseconds
	private final double MAX_ROUNDS = 16; // use 16 so that last update does not trigger finished to early
	private String apiUrl;
	private int currentRound;
	private Vector<RoundParsedListener> listeners;
	private Vector<Double> roundResultThreshold;
	private Vector<Double> resultModifier;
	private Vector<Double> timeModifier;
	private Date interestingDay;
	private boolean notUsingSpecificDay;
	
	public BattleParser() {
		currentRound = 1;
		roundResultThreshold = new Vector<Double>(3);
		roundResultThreshold.add(0, 0.55);
		roundResultThreshold.add(1, 0.6);
		roundResultThreshold.add(2, 0.99);
		resultModifier = new Vector<Double>(4);
		resultModifier.add(0,1.0);
		resultModifier.add(1,0.66);
		resultModifier.add(2,0.33);
		resultModifier.add(3,0.33);
		timeModifier = new Vector<Double>(6);
		timeModifier.add(0,1.0);
		timeModifier.add(1,1.0);
		timeModifier.add(2,0.9);
		timeModifier.add(3,0.7);
		timeModifier.add(4,0.5);
		timeModifier.add(5,0.5);
		String inputStr = "06-09-2013";
		notUsingSpecificDay = true;
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
		try {
			interestingDay = dateFormat.parse(inputStr);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public HashMap<Integer,MilitaryUnit> parseEntireBattle(HashMap<Integer,MilitaryUnit> mus, BattleInfo battleInfo) {
		
		boolean done = false;
		
		while(!done) {
			
			apiUrl = BASE_API_URL + battleInfo.getId() + ROUND_URL + currentRound;
			
			String result = getPage(apiUrl);
			
			if(result.equals(NO_ROUND_STRING)) {
				//System.out.println("Did not find round for url: " + apiUrl);
				done = true;
				break;
			}
			
			InputStream in;
			List<RoundInfo> infos = null;
			try {
				in = new ByteArrayInputStream(result.getBytes("UTF-8"));
				infos = readJsonStream(in);
			} catch (UnsupportedEncodingException e) {
				// In case primera server doesn't let us open to many pages
				try {
					Thread.sleep(2000, 0);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				System.out.println("Failed a battle round parsing");
				continue;
				
			} catch (IOException e) {
				// In case primera server doesn't let us open to many pages
				try {
					Thread.sleep(2000, 0);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				System.out.println("Failed a battle round parsing");
				continue;
			}
			SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");

			if(infos != null && infos.size() != 0) {
				long lastAttackTime =  infos.get(0).getTime().getTimeInMillis();
				
				double defenderDmg = 0;
				double attackerDmg = 0;
				
				//First check dmg on both sides during round
				for(RoundInfo info : infos) {
					if(info.isDefenderSide()) {
						defenderDmg += info.getDamage();
					}else {
						attackerDmg += info.getDamage();
					}
				}
				
				// Compute which type of round-battle it was
				double roundModifier = computeRoundResultModifier(defenderDmg,attackerDmg);
				
				//Compute mu dmg
				for(RoundInfo info : infos) {
					//Here get mu dmg and time.
					MilitaryUnit currentMU = mus.get(info.getMilitaryUnitId());
					
					if(currentMU != null) {
						if(battleInfo.isDefender() == info.isDefenderSide()) {
							if(notUsingSpecificDay || fmt.format(info.getTime().getTime()).equals(fmt.format(interestingDay))) {
								double weightedDmg = roundModifier*info.getDamage();
								currentMU.addBattleDmg(info.getDamage());
								currentMU.addMemberBattleDmg(info.getCitizenId(),info.getDamage());
								if(lastAttackTime - info.getTime().getTimeInMillis() < T2_TIME ) {
									currentMU.addT2Dmg(info.getDamage());
									currentMU.addMemberT2Dmg(info.getCitizenId(),info.getDamage());
									weightedDmg *= timeModifier.get(0);
								}else if(lastAttackTime - info.getTime().getTimeInMillis() < T3_TIME) {
									weightedDmg *= timeModifier.get(1);
								}else if(lastAttackTime - info.getTime().getTimeInMillis() < T5_TIME) {
									weightedDmg *= timeModifier.get(2);
								}else if(lastAttackTime - info.getTime().getTimeInMillis() < T10_TIME) {
									weightedDmg *= timeModifier.get(3);
								}else if(lastAttackTime - info.getTime().getTimeInMillis() < T30_TIME) {
									weightedDmg *= timeModifier.get(4);
								}else {
									weightedDmg *= timeModifier.get(5);
								}
								currentMU.addWeightedDmg(weightedDmg);
							}
						}else {
							currentMU.addEnemyDmg(info.getDamage());
							currentMU.addMemberEnemyDmg(info.getCitizenId(),info.getDamage());
							currentMU.addWeightedDmg(-info.getDamage());
						}
					}
					
				}
			}
			
			fireRoundParsedEvent(currentRound/MAX_ROUNDS);
			
			currentRound++;
			
		}
		
		
		// reset round
		currentRound = 1;
		
		return mus;
	}
	
	public String[] getWeights() {
		String[] weights = new String[roundResultThreshold.size() + resultModifier.size() + timeModifier.size()];
		int counter = 0;
		for(double val : roundResultThreshold) {
			weights[counter] = Double.toString(val);
			counter++;
		}
		for(double val : resultModifier) {
			weights[counter] = Double.toString(val);
			counter++;
		}
		for(double val : timeModifier) {
			weights[counter] = Double.toString(val);
			counter++;
		}
		return weights;
	}
	
	public void setWeight(int index, double value) {
		int counter = 0;
		for(int i = 0; i < roundResultThreshold.size(); i++) {
			if(counter == index) {
				roundResultThreshold.set(i, value);
				return;
			}
			counter++;
		}
		for(int i = 0; i < resultModifier.size(); i++) {
			if(counter == index) {
				resultModifier.set(i, value);
				return;
			}
			counter++;
		}
		for(int i = 0; i < timeModifier.size(); i++) {
			if(counter == index) {
				timeModifier.set(i, value);
				return;
			}
			counter++;
		}
	}
	
	private double computeRoundResultModifier(double defenderDmg, double attackerDmg) {
		double total = defenderDmg + attackerDmg;
		double defPercent = defenderDmg/total;
		double attackPercent = attackerDmg/total;
		BattleRoundResult result = BattleRoundResult.TIGHT;
		
		if(defPercent < roundResultThreshold.get(0) && attackPercent < roundResultThreshold.get(0)) {
			result = BattleRoundResult.TIGHT;
		}else if(defPercent < roundResultThreshold.get(1) && attackPercent < roundResultThreshold.get(1)){
			result = BattleRoundResult.ADVANTAGE;
		}else if(defPercent < roundResultThreshold.get(2) && attackPercent < roundResultThreshold.get(2)){
			result = BattleRoundResult.WASTE;
		}else {
			result = BattleRoundResult.OVERKILL;
		}
		
		switch(result) {
			case TIGHT:
				return resultModifier.get(0);
			case ADVANTAGE:
				return resultModifier.get(1);
			case WASTE:
				return resultModifier.get(2);
			case OVERKILL:
				return resultModifier.get(3);
			default:
				return 1;
		}
	}
	
	private List<RoundInfo> readJsonStream(InputStream in) throws IOException {
	     JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
	     try {
	       return readMessagesArray(reader);
	     }finally {
	       reader.close();
	     }
	   }

	private List<RoundInfo> readMessagesArray(JsonReader reader) throws IOException {
	     List<RoundInfo> messages = new ArrayList<RoundInfo>();

	     reader.beginArray();
	     while (reader.hasNext()) {
	       messages.add(readMessage(reader));
	     }
	     reader.endArray();
	     return messages;
	   }

	 private RoundInfo readMessage(JsonReader reader) throws IOException {
		 //init
		 String time = null;
		 int muId = -1;
		 boolean berserk = false;
		 boolean locationBonus = false;
		 double muBonus = 1;
		 int wep = -1;
		 boolean defSide = false;
		 double dmg = 0;
		 int citizenship = 0;
		 int citizenId = 0;
		 
	     reader.beginObject();
	     while (reader.hasNext()) {
	       String info = reader.nextName();
	       if (info.equals("time")) {
	    	 time = reader.nextString();
	       } else if (info.equals("militaryUnit")) {
	    	 muId = reader.nextInt();
	       } else if (info.equals("berserk")) {
	    	 berserk = reader.nextBoolean();
	       } else if (info.equals("localizationBonus")) {
	    	 locationBonus = reader.nextBoolean();
	       } else if (info.equals("militaryUnitBonus")) {
	    	 muBonus = reader.nextDouble();
	       } else if (info.equals("weapon")) {
	    	 wep = reader.nextInt();
	       } else if (info.equals("defenderSide")) {
	    	 defSide = reader.nextBoolean();
	       } else if (info.equals("damage")) {
	    	 dmg = reader.nextDouble();
	       } else if (info.equals("citizenship")) {
	    	 citizenship = reader.nextInt();
	       } else if (info.equals("citizenId")) {
	    	 citizenId = reader.nextInt();
	       } else {
	         reader.skipValue();
	       }
	     }
	     reader.endObject();
	     return new RoundInfo(time, muId, berserk, locationBonus,muBonus, wep,defSide,dmg,citizenship,citizenId);
	   }
	 
	 public void addRoundParsedListener(RoundParsedListener listener) {
			if(listeners == null) {
				listeners = new Vector<RoundParsedListener>();
			}
			listeners.addElement(listener);
		}
		
	 public void removeRoundParsedListener(RoundParsedListener listener) {
			if(listeners == null) {
				listeners = new Vector<RoundParsedListener>();
			}
			listeners.removeElement(listener);
		}
		
	 protected void fireRoundParsedEvent(double percent) {
			
			if(listeners != null && !listeners.isEmpty()) {
				RoundParsedEvent event = new RoundParsedEvent(this,percent);
				

			    // walk through the listener list and
			    //   call the sunMoved method in each
			    Enumeration<RoundParsedListener> e = listeners.elements();
			    while (e.hasMoreElements()) {
			    	RoundParsedListener listener = (RoundParsedListener) e.nextElement();
			    	listener.roundParsed(event);
			    }
				
			}
			
		}

}
