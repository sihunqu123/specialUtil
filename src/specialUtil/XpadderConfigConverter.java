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
 * Convert the Source Xpadder config file(e.g. x360Btn.xpadderprofile) specified in `./XpadderConfig.properties`
 *   to the target Xpadder config file(e.g. mouseBetop3s.xpadderprofile)
 *   according to their button number map(e.g. betopOldMap.properties)
 * Background: the button number mapped to the PS2 standard button layout differs a lot from each game pad.
 * So with original one for XBox game pad, we need to convert our xpadderConfig to other game pad. e.g. Betop3s, PS3, PS4.
 * i.e. The 'Start' button for XBox360 is Button8, while it's Button12 for Betop3s. So we need to replce the Button8 in XBox360.xpadderprofile
 *   to Button12 for Betop3s.xpadderprofile
 */
public class XpadderConfigConverter {
	private static final String GREPMARK = "XpadderConfigConverter";
	
	private static IConfigManager configManager;
	
	/**
	 * step 1: load original map
	 * step 2: load target map
	 * step 3: replace each button map(L1/L2/L3/R1...) from the original map in the source xpadder config.
	 * step 4: remove the prefix 'REMOVEME_'
	 * step 5: write the new Xpadder config file
	 */
	public static void main(String args[]) {
		try {
			new XpadderConfigConverter().testPath();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void testPath() throws Exception {
		// load the path source/target config/property file
		configManager = ConfigManager.getConfigManager(XpadderConfigConverter.class.getResource("XpadderConfig.properties"));
		String oriSoftware2System = configManager.getString("oriSoftware2System") + ""; // the file path of the source software to System button map file
		String oriPad2Xpadder = configManager.getString("oriSoftware2System") + "";  // the file path of the source game pad to Xpadder button map file
		String targetPad2Xpadder = configManager.getString("targetPad2Xpadder") + ""; // the file path of the target software to System button map file
		String targetSoftware2System = configManager.getString("targetSoftware2System") + ""; // the file path of the target game pad to Xpadder button map file
		
		Properties p_oriPad2Xpadder = new Properties();
		Properties p_targetPad2Xpadder = new Properties();
		
		// load the content of game pad to Xpadder mapping files
		p_oriPad2Xpadder.load(new FileInputStream(oriPad2Xpadder));
		p_targetPad2Xpadder.load(new FileInputStream(targetPad2Xpadder));
		
		// only load the content of the source property file.
		String str_oriSoftware2System = ComFileUtil.readFile2String(oriSoftware2System);; 
		
		
		Enumeration<Object> keysEnu = p_oriPad2Xpadder.keys();
		
		String resultStr = str_oriSoftware2System;
		
		ComLogUtil.info("Convert source 'software to system' xpadder config file:\n'"
		  + oriSoftware2System + "'(with 'game pad to Xpadder' config file: '" + oriPad2Xpadder + "')"
		  + "\nto:\n'"
		  + targetSoftware2System + "'(with 'game pad to Xpadder' config file: '" + targetPad2Xpadder + "')"
		  + "\nbegin..."
				);
		
		// iterate each game pad to Xpadder map key
		while(keysEnu.hasMoreElements()) {
			String buttonKey = keysEnu.nextElement() + "";
			
			// find the corresponding game pad to Xpadder map key in source and target.
			String buttonKeyInXpadder_ori = p_oriPad2Xpadder.get(buttonKey) + "";
			String buttonKeyInXpadder_target = p_targetPad2Xpadder.get(buttonKey) + "";
			// replace the source map key with the target map key
			// we add a prefix "REMOVEME_" to avoid this map key being replaced by the replacements happened in following iteration
			// and we will remove this prefix "REMOVEME_" when iteration is over
			resultStr = ComRegexUtil.replaceAllByRegex(resultStr, "(?<=Set\\d)" + buttonKeyInXpadder_ori + "(?=[^=\\d])", "REMOVEME_"+ buttonKeyInXpadder_target);
		}
		
		//remove this prefix "REMOVEME_" when iteration is over
		resultStr = ComRegexUtil.replaceAllLiterally(resultStr, "REMOVEME_", "");
		
		
		ComFileUtil.writeString2File(resultStr, targetSoftware2System, ComFileUtil.UTF8, false);
		ComLogUtil.info("Convert done successfully!");
	}
}