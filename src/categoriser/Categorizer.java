package categoriser;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import models.Statement;
import models.Tweet;
import ontologyCategories.EventType;
import ontologyCategories.EvidenceType;

public class Categorizer {

	// Evidence
	static String[] drugList = {"hasj","kokain","mdma","amfetamin","narkotika","narkotiske"," nark "};
	static String[] gunList = {"pistol","gevær","skudd","skutt","bevæpnet"};
	static String[] knifeList = {"kniv"};

	// Events
	static String[] robberyList = {"ran", "stjålet", "tyveri", "innbrudd","stjele"};
	static String[] violenceList = {"slagsmål", " slag", "spark"};
	static String[] fireList = {"røyk", "brann","påtent"};
	static String[] collisionList = {"kollisjon", "trafikkuhell", "trafikkulykke", "bilstans", "trafikkstans","kjøretøystans", "promillekjør","kjørte på","påkjørt","påkjørsel", "kjørt i ","bil med stans"};
	static String[] injuriList = {" skadd", "skadet", "skader", "sloss"," slått"," skade "};
	static String[] searchList = {"vi søker etter","vi leter etter"};
	
	public static List<Statement> extractCategories(List<Tweet> tweets){
		ArrayList<Statement> sts = new ArrayList<>();
		for(Tweet t : tweets){
			Statement st = extractCategories(t);
			if(st != null) sts.add(st);
		}
		return sts;
	}
	
	public static Statement extractCategories(Tweet t){
		Statement st = new Statement();
		
		String id = t.getId();
		String name = t.getName();
		String content = t.getContent();
		
		List<String> eventTypes = extractEventCategory(content);
		String evidenceType = extractEvidenceCategory(content);
		String weekDay = extractWeekday(t.getTimestamp());
		
		if(eventTypes.size() == 0 && evidenceType == null) return null;
		
		List<String> eventNames = new ArrayList<>();
		for(String eventType : eventTypes){
			String eventName = id + eventType;
			eventNames.add(eventName);
		}
		
		st.setStatementName(name);
		st.setEventTypes(eventTypes);
		st.setEventNames(eventNames);
		st.setEvidence(id+evidenceType);
		st.setEvidenceType(evidenceType);
		st.setDay(weekDay);
		
		return st;
	}
	
	public static String extractEvidenceCategory(String message){
		message = message.toLowerCase();
		if(containsDrugs(message)){
			return EvidenceType.DRUGS;
		}
		if(containsGuns(message)){
			return EvidenceType.GUN;
		}
		if(containsKnifes(message)){
			return EvidenceType.KNIFE;
		}
		return null;
	}
	
	public static List<String> extractEventCategory(String message){
		message = message.toLowerCase();
		
		ArrayList<String> types = new ArrayList<>();
		if(containsFire(message)){
			types.add(EventType.FIRE);
		}
		if(containsTrafficCollision(message)){
			types.add(EventType.TRAFFIC_COLLISION);
		}
		if(containsInjuri(message)){
			types.add(EventType.INJURY);
			//types.add(getInjuriCount(message)+"");
		}
		if(containsRobbery(message)){
			types.add(EventType.ROBBERY);
		}
		if(containsViolence(message)){
			types.add(EventType.VIOLENCE);
		}
		if(containsSearch(message)){
			types.add(EventType.SEARCH);
		}
		return types;
		
	}
	
	public static String extractWeekday(String timestamp){
		String[] data = timestamp.split(" ")[0].split("-");
		if(data.length < 3) return null;
		try{
			int year = Integer.valueOf(data[0]);
			int month = Integer.valueOf(data[1]);
			int dayOfMonth = Integer.valueOf(data[2]);			

			LocalDate date = LocalDate.of(year, month, dayOfMonth);
			DayOfWeek weekday = date.getDayOfWeek();
			String suffix = weekday.toString().toLowerCase().substring(1);
			String prefix = weekday.toString().charAt(0)+"";
			String formattedWeekday = prefix + suffix;
		
			return formattedWeekday;
		}
		catch(NumberFormatException e){
			return null;
		}
		
	}
	
	public static boolean containsSearch(String message){
		return genericContains(searchList, message);
	}
	
	public static boolean containsDrugs(String message){
		for(int i =0; i<drugList.length; i++){
			String drugWord = drugList[i];
			if (message.contains(drugWord)) return true;
		}
		
		return false;
	}
	
	public static boolean containsGuns(String message){
		for(int i =0; i<gunList.length; i++){
			String gunWord = gunList[i];
			if (message.contains(gunWord)) return true;
		}
		
		return false;
	}
	
	public static boolean containsKnifes(String message){
		for(int i =0; i<knifeList.length; i++){
			String knifeWord = knifeList[i];
			if (message.contains(knifeWord)) return true;
		}
		
		return false;
	}

	public static boolean containsFire(String message){
		for(int i =0; i<fireList.length; i++){
			String fireWord = fireList[i];
			if (message.contains(fireWord)) return true;
		}
		
		return false;
	}
	
	public static boolean containsTrafficCollision(String message){
		for(int i =0; i<collisionList.length; i++){
			String collisionWord = collisionList[i];
			if (message.contains(collisionWord)) return true;
		}
		
		return false;
	}

	public static boolean containsInjuri(String message){
		String[] negations = {"ingen", "uskadd"};
		return genericContains(injuriList, message, negations);
	}
	
	public static int getInjuriCount(String message){
		int injuriCount = 0;
		if(message.contains("mann") || message.contains("dame")){
			injuriCount = 1;
		}
		if(message.contains("personer")){
			if(message.contains(" to ") || message.contains(" 2 ")){
				injuriCount = 2;
			}
			if(message.contains(" tre ") || message.contains(" 3 ")){
				injuriCount = 3;
			}
			if(message.contains(" fire ") || message.contains(" 4 ")){
				injuriCount = 4;
			}
			if(message.contains(" fem ") || message.contains(" 5 ")){
				injuriCount = 5;
			}
			if(message.contains(" seks ") || message.contains(" 6 ")){
				injuriCount = 6;
			}
			if(message.contains(" syv ") || message.contains(" 7 ")){
				injuriCount = 7;
			}
			if(message.contains(" åtte ") || message.contains(" 8 ")){
				injuriCount = 8;
			}
			if(message.contains(" ni ") || message.contains(" 9 ")){
				injuriCount = 9;
			}
			if(message.contains(" ti ") || message.contains("10")){
				injuriCount = 10;
			}
		}
		return injuriCount;
	}
	
	public static boolean containsViolence(String message){
		return genericContains(violenceList, message);
	}
	
	public static boolean containsRobbery(String message){
		return genericContains(robberyList, message);
	}
	
	public static boolean genericContains(String[] words , String message){
		for(int i =0; i<words.length; i++){
			String word = words[i];
			if(message.contains(word)) return true;
		}
		return false;
	}
	
	public static boolean genericContains(String[] words, String message, String[] falseWords){
		for(int i =0; i<words.length; i++){
			String word = words[i];
			if(message.contains(word)){
				boolean haveNegation = false;
				for(int u=0; u<falseWords.length; u++){
					if(message.contains(falseWords[u])) haveNegation = true;
				}
				if(!haveNegation) return true;
			}
		}
		return false;
	}
	
}
