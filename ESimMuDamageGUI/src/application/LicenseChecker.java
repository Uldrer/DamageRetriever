package application;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import MuDamage.Parser;

public class LicenseChecker extends Parser {
	
	private final String ULDRER_BASE_URL = "http://primera.e-sim.org/profile.html?id=66935";
	private final String ULDRER_AVATAR_URL = "https://newprimera.e-sim.org:3000/avatars/94034_normal ";
	
	LicenseChecker() {
	}
	
	public boolean checkLicense() {
		
		String result = getPage(ULDRER_BASE_URL);
		
		if(result.equals("")) {
			return false;
		}
		
		
		Document doc = Jsoup.parse(result, ULDRER_BASE_URL);
		
		Elements avatarImg = doc.getElementsByClass("bigAvatar");
		
		Element avatarObject = avatarImg.get(0);
		Elements tags = avatarObject.getElementsByAttribute("src");
		
		for (Element link : tags) {
			String imgUrl =	link.attr("src");
			
			if(imgUrl.equals(ULDRER_AVATAR_URL)) {
				return true;
			}
		}
		return false;
	}

}
