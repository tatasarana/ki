package com.docotel.ki.enumeration;

import com.docotel.ki.util.ObjectMapperUtil;
import com.fasterxml.jackson.core.type.TypeReference;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

public enum LanguageTranslatorEnum {
	GOOGLE(true, "Google", "https://translate.googleapis.com/translate_a/single?client=gtx&sl={sl}&tl={tl}&dt=t&q={q}", "{sl}", "{tl}", "{q}"),
	BING(false, "Bing", "https://www.bing.com/ttranslate?&category=&IG=22D5786A219845A6865F4261200A8A83&IID=translator.5034.6", "from", "to", "text"),
	;
	private boolean defaultTranslator;
	private String label;
	private String url;
	private String sourceLangKey;
	private String translateLangKey;
	private String queryKey;

	LanguageTranslatorEnum(boolean defaultTranslator, String label, String url, String sourceLangKey, String translateLangKey, String queryKey) {
		this.defaultTranslator = defaultTranslator;
		this.label = label;
		this.url = url;
		this.sourceLangKey = sourceLangKey;
		this.translateLangKey = translateLangKey;
		this.queryKey = queryKey;
	}

	public boolean isDefaultTranslator() {
		return defaultTranslator;
	}

	public String label() {
		return this.label;
	}

	public String getUrl() {
		return url;
	}

	public String getSourceLangKey() {
		return sourceLangKey;
	}

	public String getTranslateLangKey() {
		return translateLangKey;
	}

	public String getQueryKey() {
		return queryKey;
	}

	public String getTranslation(String sourceLang, String translateLang, String query) {
		try {
			if (name().equalsIgnoreCase(GOOGLE.name())) {
				String request = url.replace(getSourceLangKey(), URLEncoder.encode(sourceLang, "UTF-8"))
						.replace(getTranslateLangKey(), URLEncoder.encode(translateLang, "UTF-8"))
						.replace(getQueryKey(), URLEncoder.encode(query, "UTF-8"));
				HttpsURLConnection urlConnection = (HttpsURLConnection) new URL(request).openConnection();
				urlConnection.setRequestMethod("GET");
				urlConnection.setReadTimeout(30000);
				urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0");

				BufferedReader responseReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
				String inputLine;
				StringBuffer responseMessage = new StringBuffer();

				while ((inputLine = responseReader.readLine()) != null) {
					responseMessage.append(inputLine);
				}
				responseReader.close();

				// [[["uji","test",null,null,1],[null,null,null,"test"]],[["verb",["menguji","mencoba","membenarkan secara resmi"],[["menguji",["test","examine","verify","try","quiz","tempt"],[36251],0.62578398],["mencoba",["try","attempt","try out","test","try on","sample"],[36251],0.00057064113],["membenarkan secara resmi",["test"],null,4.1573821e-06]],"test",2],["noun",["tes","uji","ujian","percobaan","cobaan"],[["tes",["test"],[52938],0.34023288],["uji",["test","check"],[50084],0.20316233],["ujian",["exam","test","examination","quiz","paper","check"],[50084,52938],0.036424998],["percobaan",["trial","experiment","test","attempt","experimentation","probation"],null,0.0093547134],["cobaan",["trial","temptation","test"],null,0.00057962746]],"test",1]],"en",null,null,[["test",null,[["uji",1000,true,false],["tes",1000,true,false],["menguji",1000,true,false],["ujian",1000,true,false]],[[0,4]],"test",0,0]],0.95929444,null,[["en"],null,[0.95929444],["en"]],null,null,[["noun",[[["trial","experiment","test case","case study","pilot study","trial run","tryout","dry run","check","examination","assessment","evaluation","appraisal","investigation","inspection","analysis","scrutiny","study","probe","exploration","screening","workup","assay"],"m_en_us1297943.001"],[["exam","examination","quiz"],"m_en_us1297943.002"],[["trial","run"],""],[["trial run","trial","tryout"],""],[["exam","examination"],""],[["trial"],""],[["mental test"],""]],"test"],["verb",[[["try out","put to the test","put through its paces","experiment with","pilot","check","examine","assess","evaluate","appraise","investigate","analyze","scrutinize","study","probe","explore","trial","sample","screen","assay"],"m_en_us1297943.009"],[["put a strain on","strain","tax","try","make demands on","stretch","challenge"],"m_en_us1297943.010"],[["essay","try","prove","examine","try out"],""],[["screen"],""],[["quiz"],""]],"test"]],[["noun",[["a procedure intended to establish the quality, performance, or reliability of something, especially before it is taken into widespread use.","m_en_us1297943.001","no sparking was visible during the tests"],["a movable hearth in a reverberating furnace, used for separating gold or silver from lead.","m_en_us1297943.008","When fully prepared, the test is allowed to dry, and is then placed in a furnace, constructed in all respects like a common reverberator)' furnace, except that a space is left open in the bed of it to receive the test, and that the width of the arch is much reduced."],["the shell or integument of some invertebrates and protozoans, especially the chalky shell of a foraminiferan or the tough outer layer of a tunicate.","m_en_us1297944.001","The tests of the shells are recrystallized, but the original ornamentation is preserved in very good detail."]],"test"],["verb",[["take measures to check the quality, performance, or reliability of (something), especially before putting it into widespread use or practice.","m_en_us1297943.009","this range has not been tested on animals"]],"test"],["abbreviation",[["testator.","m_en_us1297946.001"]],"test."]],[[["It was meant to be a crucial exam to \u003cb\u003etest\u003c/b\u003e the basic skills of ambulance workers who wanted to step up the career ladder and become paramedics.",null,null,null,3,"m_en_us1297943.012"],["The Bishop came to \u003cb\u003etest\u003c/b\u003e us on our knowledge and woe betide the boy who failed to give an instant answer to his theological queries.",null,null,null,3,"m_en_us1297943.011"],["this is the first serious \u003cb\u003etest\u003c/b\u003e of the peace agreement",null,null,null,3,"m_en_us1297943.003"],["The best way to \u003cb\u003etest\u003c/b\u003e a chilli for strength is to munch a bit before cooking.",null,null,null,3,"m_en_us1297943.016"],["The new \u003cb\u003etest\u003c/b\u003e can identify the presence of anthrax in less than one hour instead of days.",null,null,null,3,"m_en_us1297943.005"],["One \u003cb\u003etest\u003c/b\u003e for the presence of silver ions in solution is to add chloride ions to the solution.",null,null,null,3,"m_en_us1297943.005"],["But the original building was opened in 1867 by Bradford Corporation to \u003cb\u003etest\u003c/b\u003e the weight and quality of wool.",null,null,null,3,"m_en_us1297943.009"],["I answered the question, took a seat, wrote the \u003cb\u003etest\u003c/b\u003e , handed it in and left.",null,null,null,3,"m_en_us1297943.002"],["The agency has also announced sweeping measures to tag and \u003cb\u003etest\u003c/b\u003e US cattle and other steps to boost confidence.",null,null,null,3,"m_en_us1297943.009"],["When introducing a fresh cupel or \u003cb\u003etest\u003c/b\u003e , the fire must be low and heat must be applied with great caution, or otherwise the bone ash will split to pieces; and for the same reason the bone ash must be dried very gently.",null,null,null,3,"m_en_us1297943.008"],["The \u003cb\u003etest\u003c/b\u003e is positive if both samples grow bacteria and if the catheter sample grows at least three times as many bacteria as the peripheral blood sample.",null,null,null,3,"m_en_us1297943.006"],["Women who have a positive \u003cb\u003etest\u003c/b\u003e result for the human papilloma virus are also at increased risk of cervical cancer.",null,null,null,3,"m_en_us1297943.006"],["The actual probability depends not only on the reliability of the \u003cb\u003etest\u003c/b\u003e , but also the number of infections in the population to begin with.",null,null,null,3,"m_en_us1297943.006"],["four fax modems are on \u003cb\u003etest\u003c/b\u003e",null,null,null,3,"m_en_gb0854610.001"],["The year ahead will \u003cb\u003etest\u003c/b\u003e our political establishment to the limit.",null,null,null,3,"m_en_us1297943.010"],["The final 26 were interviewed and ranked based on their combined performance in the \u003cb\u003etest\u003c/b\u003e and interview.",null,null,null,3,"m_en_us1297943.002"],["If the HPV \u003cb\u003etest\u003c/b\u003e is positive for the high risk type, then the patient warrants a closer look.",null,null,null,3,"m_en_us1297943.006"],["After an ultrasound \u003cb\u003etest\u003c/b\u003e and a physical examination, ovarian cancer was diagnosed.",null,null,null,3,"m_en_us1297943.004"],["We've said in the pilot that if you have a positive \u003cb\u003etest\u003c/b\u003e , colonoscopy must be made available within four weeks.",null,null,null,3,"m_en_us1297943.006"],["this is the first serious \u003cb\u003etest\u003c/b\u003e of the peace agreement",null,null,null,3,"m_en_gb0854610.003"],["On the other hand, there may be potential adverse psychological effects from a positive \u003cb\u003etest\u003c/b\u003e .",null,null,null,3,"m_en_us1297943.006"],["a spelling \u003cb\u003etest\u003c/b\u003e",null,null,null,3,"m_en_gb0854610.002"],["Maybe, but knowing, and knowing when to know is the true \u003cb\u003etest\u003c/b\u003e of knowledge.",null,null,null,3,"m_en_us1297943.003"],["a \u003cb\u003etest\u003c/b\u003e for HIV",null,null,null,3,"m_en_us1297943.004"],["Validity standards are based on test content, not on which groups of students take the \u003cb\u003etest\u003c/b\u003e .",null,null,null,3,"m_en_us1297943.002"],["Using the same pan, fry a small patty of the meat mixture and taste to \u003cb\u003etest\u003c/b\u003e the seasoning.",null,null,null,3,"m_en_us1297943.016"],["The \u003cb\u003etest\u003c/b\u003e , when placed in position, forms the bed of the furnace, with the long diameter transversely.",null,null,null,3,"m_en_us1297943.008"],["a positive \u003cb\u003etest\u003c/b\u003e for protein",null,null,null,3,"m_en_gb0854610.006"],["This week's What's Your Decision will really \u003cb\u003etest\u003c/b\u003e you and show you just how hard umpiring can be.",null,null,null,3,"m_en_us1297943.011"],["It is, unquestionably the greatest \u003cb\u003etest\u003c/b\u003e of mental strength this present side has ever faced.",null,null,null,3,"m_en_us1297943.003"]]]]
				// get array[0][0][0]

				List<Object> responseObject = ObjectMapperUtil.fromJson(responseMessage.toString(), new TypeReference<List<Object>>(){});
				List<Object> responseObject0 = (List<Object>) responseObject.get(0);
				List<Object> responseObject00 = (List<Object>) responseObject0.get(0);
				return (String) responseObject00.get(0);
			} else {
				HttpsURLConnection urlConnection = (HttpsURLConnection) new URL(url).openConnection();
				urlConnection.setDoOutput(true);
				urlConnection.setDoInput(true);
				urlConnection.setRequestMethod("POST");
				urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0");
				urlConnection.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
				urlConnection.setReadTimeout(30000);

				DataOutputStream outputStream = new DataOutputStream(urlConnection.getOutputStream());
				outputStream.writeBytes(
						getSourceLangKey() + "=" + sourceLang +
						"&" + getTranslateLangKey() + "=" + translateLang +
						"&" + getQueryKey() + "=" + query
				);
				outputStream.flush();
				outputStream.close();

				BufferedReader responseReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
				String inputLine;
				StringBuffer responseMessage = new StringBuffer();

				while ((inputLine = responseReader.readLine()) != null) {
					responseMessage.append(inputLine);
				}
				responseReader.close();
//				{
//					"statusCode": 200,
//						"translationResponse": "Kertas putih"
//				}

				Map<String, Object> responseObject = ObjectMapperUtil.fromJson(responseMessage.toString(), new TypeReference<Map<String, Object>>(){});
				if (((int) responseObject.get("statusCode")) == 200) {
					return (String) responseObject.get("translationResponse");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
}
