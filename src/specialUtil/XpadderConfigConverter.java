package specialUtil;

import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import util.commonUtil.ComFileUtil;
import util.commonUtil.ComLogUtil;
import util.commonUtil.ComRegexUtil;
import util.commonUtil.ComStrUtil;
import util.commonUtil.ConfigManager;
import util.commonUtil.interfaces.IConfigManager;

/**
 * 
 */
public class XpadderConfigConverter{
	private static final String GREPMARK = "XpadderConfigConverter";
	
	private static final String NA = "N/A";
	
	private static IConfigManager configManager;
	
	/**
	 * step 1: load original map
	 * step 2: load target map
	 * step 3: load original key config into a string str
	 * step 4: replace all str with {repalceKey1} and save {repalceKey1} -> "value" 
	 * step 5: replace all str with {repalceKey1} back according to saved {repalceKey1} -> "value"
	 */
	public static void main(String args[]) {
		new XpadderConfigConverter().testPath();
		
	}
	
	public void testPath() {
//		Properties p = new Properties();
		String oriS2S = "";
		String oriP2X = "";
		String targetP2X = "";
		String targetS2S = "";
		
		Properties p_oriP2X = new Properties();
		Properties p_targetP2X = new Properties();
		String str_oriS2S = null;
		
		Map<Object, Object> recordMap = new HashMap<Object, Object>();

		try {
			configManager = ConfigManager.getConfigManager(XpadderConfigConverter.class.getResource("XpadderConfig.properties"));
//			p.load(new FileInputStream(this.getClass().getResource("./XpadderConfig.properties").getPath()));
			oriS2S = configManager.getString("oriS2S") + "";
			System.out.println(oriS2S);
			oriP2X = configManager.getString("oriP2X") + "";
			targetP2X = configManager.getString("targetP2X") + "";
			targetS2S = configManager.getString("targetS2S") + "";
			p_oriP2X.load(new FileInputStream(oriP2X));
			p_targetP2X.load(new FileInputStream(targetP2X));
			revertMap(p_oriP2X);
			revertMap(p_targetP2X);
			removeAllSlots(p_oriP2X);
			removeAllSlots(p_targetP2X);
			removeAllNA(p_oriP2X);
			removeAllNA(p_targetP2X);
			str_oriS2S = ComFileUtil.readFile2String(oriS2S);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		Enumeration<Object> keys = null;
		String key = null;
		int replaceIndex = 0;
		while(!p_targetP2X.isEmpty()) {
			keys = p_targetP2X.keys();
			while(keys.hasMoreElements()) {
				key = keys.nextElement() + "";
				String value_targetP2X = p_targetP2X.get(key) + "";
				String value_oriP2X = p_oriP2X.getProperty(key);
				if(isSubStrOfAnyElementInEnumeration(value_oriP2X, p_oriP2X)) {
					ComLogUtil.info("will do key:" + key + " later, for value_oriP2X:" + value_oriP2X);
					continue;
				}
				
				if(value_oriP2X.equals(value_targetP2X)) {
					ComLogUtil.info("key:" + key + " with the same value:" + value_oriP2X);
					p_targetP2X.remove(key);
					p_oriP2X.remove(key);
					keys = p_targetP2X.keys();
					continue;
				}
				
				String recordKey = "{str2replce#" + replaceIndex++ + "}";
				ComLogUtil.info("temp replace from:" + value_oriP2X + ", to:" + recordKey);
				str_oriS2S.replaceAll(value_oriP2X, recordKey);
				str_oriS2S = ComStrUtil.replaceAllLiteral(str_oriS2S, value_oriP2X, recordKey);
				recordMap.put(recordKey, value_targetP2X);
				p_targetP2X.remove(key);
				p_oriP2X.remove(key);
				keys = p_targetP2X.keys();
			}
		}
		
		
		Set<Object> keySet = recordMap.keySet();
		Iterator<Object> it = keySet.iterator();
		
		String value = null;
		while(it.hasNext()) {
			key = it.next() + "";
			value = recordMap.get(key) + "";
			ComLogUtil.info("final replace from:" + key + ", to:" + value);
			str_oriS2S = ComStrUtil.replaceAllLiteral(str_oriS2S, key, value);
		}
		
		try {
			ComFileUtil.writeString2File(str_oriS2S, targetS2S, ComFileUtil.UTF8, false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static boolean isSubStrOfAnyElementInEnumeration(String key, Map<Object, Object> map) {
		Set<Object> keys = map.keySet();
		Iterator<Object> it = keys.iterator();
		String next = null;
		while(it.hasNext()) {
			next = it.next() + "";
			if(next.contains(key) && !next.equals(key)) {
				return true;
			}
		}
		return false;
	}
	
	public static Map<Object, Object> removeAllNA(Map<Object, Object> srcMap) {
		Iterator<Object> it = srcMap.keySet().iterator();
		String key = null;
		while(it.hasNext()) {
			key = it.next() + "";
			if(key.equalsIgnoreCase(NA)) {
				ComLogUtil.info("remove NA key with value:" + srcMap.get(key));
				srcMap.remove(key);
				it = srcMap.keySet().iterator();
			}
		}
		return srcMap;
	}
	
	public static Map<Object, Object> removeAllSlots(Map<Object, Object> srcMap) {
		Set<Object> keySet = srcMap.keySet();
		Iterator<Object> it = keySet.iterator();
		Object key = null;
		String value = null;
		while(it.hasNext()) {
			key = it.next();
			value = srcMap.get(key) + "";
			if(value.endsWith("Slots")) {
				srcMap.put(key, ComRegexUtil.replaceByRegex(value, "Slots$", ""));
			}
		}
		return srcMap;
	}
	
	public static Map<Object, Object> revertMap(Map<Object, Object> srcMap) {
		Set<Object> keySet = srcMap.keySet();
		Iterator<Object> it = keySet.iterator();
		Map<Object, Object> map = new HashMap<Object, Object>();
		Object key = null;
		while(it.hasNext()) {
			key = it.next();
			map.put(srcMap.get(key), key);
		}
		srcMap.clear();
		srcMap.putAll(map);
		return srcMap;
	}
	
}