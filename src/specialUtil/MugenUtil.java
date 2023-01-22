package specialUtil;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import util.commonUtil.ComFileUtil;
import util.commonUtil.ComLogUtil;
import util.commonUtil.ComRegexUtil;
import util.commonUtil.ComStrUtil;
import util.commonUtil.ConfigManager;
import util.commonUtil.interfaces.IConfigManager;
import util.commonUtil.model.FileName;


public class MugenUtil {
	
	private static final String CNSEXTENSION = ".cns";
	
	private static final String DEFEXTENSION = ".def";
	
	private static final String BAKUPEXTENSION = ".bak";
	
	private static final String CNSENCODING = "Shift_JIS";
	
	private static final String DEFENCODING = ComFileUtil.UTF8;
	
	private static final String FILEEOL = "\n";
	
	private static final int defaultPowerVal = 1;
	
	private static int defaultVal = 0;
	
	private static int defaultVal_givePower_unguarded = 0;
	
	private static int defaultVal_givePower_guarded = 0;
	
	private static String defaultVal_givePower = defaultVal_givePower_unguarded + "," + defaultVal_givePower_guarded;
	
	private static int defaultVal_getPower_unguarded = 0;
	
	private static int defaultVal_getPower_guarded = 0;
	
	private static String defaultVal_getPower = defaultVal_getPower_unguarded + "," + defaultVal_getPower_guarded;
	
	private static IConfigManager configManager;

    public static void main(String[] args) {
//    	Properties p = new Properties();
		String originalAndroidPath = "";
		String targetPath = "";
		
		Map<Object, Object> recordMap = new HashMap<Object, Object>();
				
		try {
			configManager = ConfigManager.getConfigManager("MugenUtil.properties");
//			p.load(new FileInputStream(MugenUtil.class.getResource("./MugenUtil.properties").getPath()));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
        MugenUtil test1 = new MugenUtil();
        //String batName = "F:\\database_backup\\ngx_backup\\backup_ngx.bat";
       // String batName = "D:\\Program\" \"Files\\tomcat7\\bin\\startup.bat";
//        String path = "F:\\game\\mugen\\Mugen\\chars\\";
//        String path = "F:\\KenMeiling\\";
        
//        String path = "F:\\game\\mugen\\Mugen\\chars\\Kof\\Rugal2nd";
        
        //String path = "F:\\game\\mugen\\mugen世界整合923人版\\mugen世界整合923人版\\chars";
//        String path = "F:\\game\\mugen\\mugen高AI整合1.0【绯月】";
//        String path = "F:\\game\\mugen\\mugen高AI整合1.0【绯月】\\chars\\iorl xii-kofm";
        
        
//        String path = "F:\\game\\mugen\\mugen世界整合923人版\\mugen世界整合923人版\\chars\\litchi";
//        String path = "F:\\game\\mugen\\mugen团战整合加强版\\mugen团战整合加强版\\博丽尊者整合\\chars";
        
        
//        String path = "F:\\game\\mugen\\Mugen\\chars\\月华\\lb_M_washizuka_Bstyle";
        
//        String path = "F:\\tmp";
        try {
        	new CNSReplacer(new File("F:\\game\\mugen\\mugen高AI整合1.0【绯月】\\chars\\iorl xii-kofm\\Iori_Yagami-H.cns")).doReplace();
//        	recoverAll(path);
//        	replacePoweraddMultiThread(path);
//        	replacePower(path);
//        	new CNSReplacer(new File("F:\\game\\mugen\\Mugen\\chars\\Kof\\Rugal2nd\\Rugal2nd_AI.cns")).doReplace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static void recoverAll(String path) throws Exception {
    	File root = new File(path);
    	File[] files = root.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File file) {
				return file.isDirectory() || (CNSEXTENSION.equalsIgnoreCase(ComFileUtil.getFileExtension(file, true)) 
												&& !ComFileUtil.getFileName(file, false).startsWith("~$"));	// avoid failure due to system protected file
			}
		});
    	
    	for(File file : files) {
    		if(file.isDirectory()) {
    			recoverAll(file.getPath());
    		} else {
    			ensureBakup(file, true);
    		}
    	}
    	
    }
    
    public static void replacePower(String path) throws Exception {
    	File root = new File(path);
    	File[] files = root.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File file) {
				return file.isDirectory() || DEFEXTENSION.equalsIgnoreCase(ComFileUtil.getFileExtension(file, true));
			}
		});
    	
    	for(File file : files) {
    		if(file.isDirectory()) {
    			replacePower(file.getPath());
    		} else {
    			new CNSReplacer(file).doReplace();
    		}
    	}
    }
    
    public static void replacePoweraddMultiThread(String path) throws Exception {
    	File root = new File(path);
    	File[] files = root.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File file) {
				return file.isDirectory() || CNSEXTENSION.equalsIgnoreCase(ComFileUtil.getFileExtension(file, true));
			}
		});
    	
    	List<File> fileList = null;
    	final List<List<File>> threadList = new ArrayList<List<File>>();
    	for(File file : files) {
    		if(fileList == null) fileList = new ArrayList<File>();
    		fileList.add(file);
    		if(fileList.size() >= 5) {
    			threadList.add(fileList);
    			replacePoweradd(threadList.get(threadList.size() - 1));
    			fileList = null;
    		}
    	}
    	if(fileList != null) {
    		replacePoweradd(fileList);
    	}
    	
    }
    
    public static void replacePoweradd(final List<File> list) throws Exception {
    	new Thread(new Runnable() {
			
			@Override
			public void run() {
				for(int i = 0; i < list.size(); i++) {
					File file = list.get(i);
					try {
						if(file.isDirectory()) {
							replacePoweraddMultiThread(file.getPath());
						} else {
							new CNSReplacer(file).doReplace();
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}).start();
    }
    
//    
//    public static class DefReplacer {
//    	
//    	private File file;
//    	
//    	private final static String POWER = "power";
//    	
//    	private final static String GIVEPOWER = "givepower";
//    	
//    	private final static String GETPOWER = "getpower";
//    	
//    	private final static String VALUE = "value";
//    	
//    	StringBuilder res = null;
//    	
//    	public DefReplacer(File file) {
//    		this.file = file;
//    		this.res = new StringBuilder("");
//		}
//    	
//    	public void doReplace() throws Exception {
//    		ensureBakup(file, false);
//    		String oriString = ComFileUtil.readFile2String(file, DEFENCODING);
//    		String[] oriArr = oriString.split("(\\v|\\r|\\n){1}");
//    		String filePath = file.getPath();
//    		ComLogUtil.info("------------begin file:" + filePath + " write.------------");
//    		boolean hasPendingPoweraddState = false;
//    		boolean hasPendingPowersetState = false;
//    		boolean hasPendingTargetPoweraddState = false;
//    		
//    		String afterModify = null;
//    		String value = null;
//    		int line = 0;
//    		for(String str : oriArr) {
//    			line++;
////    			ComLogUtil.info("str:" + str);
//    			String powerDefined = ComRegexUtil.getMatchedString(str, "^power\\s*=\\s*[^;]*", Pattern.CASE_INSENSITIVE);
//    			if(
//    					//line == 14632
//    					!"".equals(ComRegexUtil.getMatchedString(str, "TargetPowerA", Pattern.CASE_INSENSITIVE))
////    					|| !"".equals(ComRegexUtil.getMatchedString(str, "PowerSet", Pattern.CASE_INSENSITIVE))
//    					) {
//    				line += 0;
//    			}
//				if(!ComStrUtil.isBlankOrNull(powerDefined)) {
//					afterModify = POWER + " = " + defaultPowerVal;
//					ComLogUtil.info("relace file:" + filePath + ", line:" + line + ", powerDefined str:" + str + ", with:" + afterModify);
//					addToRes(afterModify);
//				} else { //do not modify
//					addToRes(str);
//				}
//    		}
//    		ComFileUtil.writeString2File(res.toString(), file, CNSENCODING);
//    		ComLogUtil.info("------------done file:" + filePath + " wrote.------------");
//    	}
//    	
//    	private void addToRes(String str) {
//    		this.res.append(str).append(FILEEOL);
//    	}
//    	
//    	
//    	
//    }
    
    
    public static class CNSReplacer {
    	
    	private File file;
    	
    	private final static String POWERADD = "poweradd";
    	
    	private final static String GIVEPOWER = "givepower";
    	
    	private final static String GETPOWER = "getpower";
    	
    	private final static String VALUE = "value";
    	
    	StringBuilder res = null;
    	
    	public CNSReplacer(File file) {
    		this.file = file;
    		this.res = new StringBuilder("");
		}
    	
    	public void doReplace() throws Exception {
    		ensureBakup(file, false);
    		String oriString = ComFileUtil.readFile2String(file, CNSENCODING);
    		String[] oriArr = oriString.split("(\\v|\\r|\\n){1}");
    		String filePath = file.getPath();
    		ComLogUtil.info("------------begin file:" + filePath + " write.------------");
    		boolean hasPendingPoweraddState = false;
    		boolean hasPendingPowersetState = false;
    		boolean hasPendingTargetPoweraddState = false;
    		
    		String afterModify = null;
    		boolean hasModified = false;
    		String value = null;
    		int line = 0;
    		for(String str : oriArr) {
    			line++;
//    			ComLogUtil.info("str:" + str);
//    			if(true) continue;
    			
    			String powerDefine_directly = ComRegexUtil.getMatchedString(str, "^power\\s*=\\s*[^;]*", Pattern.CASE_INSENSITIVE);
    			String poweradd_directly = ComRegexUtil.getMatchedString(str, "^poweradd[^;]+", Pattern.CASE_INSENSITIVE);
    			String poweradd_state = ComRegexUtil.getMatchedString(str, "^type\\s*=\\s*PowerAdd[^;]*", Pattern.CASE_INSENSITIVE);
    			String valueMap = ComRegexUtil.getMatchedString(str, "^value\\s*=\\s*[^;]+", Pattern.CASE_INSENSITIVE);
    			String powerset_state = ComRegexUtil.getMatchedString(str, "^type\\s*=\\s*Powerset[^;]*", Pattern.CASE_INSENSITIVE);
    			String targetPoweradd_state = ComRegexUtil.getMatchedString(str, "^type\\s*=\\s*TargetPowerAdd[^;]*", Pattern.CASE_INSENSITIVE);
    			
    			String givePower = ComRegexUtil.getMatchedString(str, "^givepower\\s*=\\s*[^;]+", Pattern.CASE_INSENSITIVE);
    			String getPower = ComRegexUtil.getMatchedString(str, "^getpower\\s*=\\s*[^;]+", Pattern.CASE_INSENSITIVE);
    			if(
    					//line == 14632
    					!"".equals(ComRegexUtil.getMatchedString(str, "TargetPowerAdd", Pattern.CASE_INSENSITIVE))
//    					|| !"".equals(ComRegexUtil.getMatchedString(str, "PowerSet", Pattern.CASE_INSENSITIVE))
    					) {
    				line += 0;
    			}
    			if(hasPendingPoweraddState) {
    				if(!ComStrUtil.isBlankOrNull(valueMap)) {
    					hasPendingPoweraddState = false;
    					value = valueMap.substring(valueMap.indexOf("=") + 1).trim();
    					if(!value.startsWith("-") && !value.equals(defaultVal + "")) {
    						afterModify = VALUE + " = " + defaultVal;
    						ComLogUtil.info("relace file:" + filePath + ", line:" + line + ", hasPendingPoweraddState str:" + str + ", with:" + afterModify);
//    						addToRes(afterModify);
    						str = afterModify;
    						hasModified = true;
    					}
    				}
    			} else if(hasPendingPowersetState) {
    				if(!ComStrUtil.isBlankOrNull(valueMap)) {
    					hasPendingPowersetState = false;
    					value = valueMap.substring(valueMap.indexOf("=") + 1).trim();
    					if(!value.startsWith("-") && !value.equals(0)) {
    						afterModify = VALUE + " = 0";
    						ComLogUtil.info("relace file:" + filePath + ", line:" + line + ", hasPendingPowersetState str:" + str + ", with:" + afterModify);
    						str = afterModify;
//    						addToRes(afterModify);
    						hasModified = true;
//    						continue;
    					}
    				}
    			} else if(hasPendingTargetPoweraddState) {
    				if(!ComStrUtil.isBlankOrNull(valueMap)) {
    					hasPendingTargetPoweraddState = false;
    					value = valueMap.substring(valueMap.indexOf("=") + 1).trim();
    					if(!value.startsWith("-") && !value.equals(0)) {
    						afterModify = VALUE + " = 0";
    						ComLogUtil.info("relace file:" + filePath + ", line:" + line + ", hasPendingTargetPoweraddState str:" + str + ", with:" + afterModify);
//    						addToRes(afterModify);
    						str = afterModify;
    						hasModified = true;
//    						continue;
    					}
    				}
    			} else if(!ComStrUtil.isBlankOrNull(powerDefine_directly)) {
    				afterModify = "power = " + defaultPowerVal;
					ComLogUtil.info("relace file:" + filePath + ", line:" + line + ", powerDefined str:" + str + ", with:" + afterModify);
//					addToRes(afterModify);
					str = afterModify;
					hasModified = true;
//					continue;
    			} else if(!ComStrUtil.isBlankOrNull(poweradd_directly)) {
    				value = poweradd_directly.substring(poweradd_directly.indexOf("=") + 1).trim();
    				if(!value.startsWith("-") && !value.equals(defaultVal + "")) {
    					afterModify = POWERADD + " = " + defaultVal;
//    					addToRes(afterModify);
    					ComLogUtil.info("relace file:" + ", line:" + line + filePath + ", str:" + str + ", with:" + afterModify);
    					str = afterModify;
    					hasModified = true;
//    					continue;
    				} else {
    					// just use original String if it's a negtive number.
//    					ComLogUtil.info("ignore file:" + filePath + ", line:" + line + ", str:" + str + ", for it's <=0. value:" + value);
    				}
    			} else if(!ComStrUtil.isBlankOrNull(getPower)) {
    				value = getPower.substring(getPower.indexOf("=") + 1).trim();
    				if(!value.startsWith("-") && !value.equals(defaultVal_getPower)) {
    					afterModify = GETPOWER + " = " + defaultVal_getPower;
    					ComLogUtil.info("relace file:" + ", line:" + line + filePath + ", getpower, str:" + str + ", with:" + afterModify);
//    					addToRes(afterModify);
    					str = afterModify;
    					hasModified = true;
//    					continue;
    				} else {
    					// just use original String if it's a negtive number.
//    					ComLogUtil.info("ignore file:" + filePath + ", line:" + line + ", getpower, str:" + str + ", for it's <=0. value:" + value);
    				}
    			} else if(!ComStrUtil.isBlankOrNull(givePower)) {
    				value = givePower.substring(givePower.indexOf("=") + 1).trim();
    				if(!value.startsWith("-") && !value.equals(defaultVal_givePower)) {
    					afterModify = GIVEPOWER + " = " + defaultVal_givePower;
    					ComLogUtil.info("relace file:" + ", line:" + line + filePath + ", givePower, str:" + str + ", with:" + afterModify);
//    					addToRes(afterModify);
    					str = afterModify;
    					hasModified = true;
//    					continue;
    				} else {
    					// just use original String if it's a negtive number.
//    					ComLogUtil.info("ignore file:" + filePath + ", line:" + line + ", givePower, str:" + str + ", for it's <=0. value:" + value);
    				}
    			} else if(!ComStrUtil.isBlankOrNull(poweradd_state)){
    				hasPendingPoweraddState = true;
    			} else if(!ComStrUtil.isBlankOrNull(powerset_state)){
    				hasPendingPowersetState = true;
    			} else if(!ComStrUtil.isBlankOrNull(targetPoweradd_state)){
    				hasPendingTargetPoweraddState = true;
    			}
    			addToRes(str);
    		}
    		if(hasModified) {
    			ComFileUtil.writeString2File(res.toString(), file, CNSENCODING);
    			ComLogUtil.info("------------done file:" + filePath + " wrote.------------");
    		}
    	}
    	
    	private void addToRes(String str) {
    		this.res.append(str).append(FILEEOL);
    	}
    	
    	
    	
    }
    
    public static void ensureBakup(File file, boolean isBakup) throws Exception {
    	File bakupFile = new File(file.getPath() + BAKUPEXTENSION);
    	if(bakupFile.exists()) {
    		if(isBakup) {
    			ComFileUtil.delFile(file);
    			ComFileUtil.copyFile(bakupFile, file);
    			ComLogUtil.info("file:" + file + " recovered.");
    		} else {
    			
    		}
    	} else {
    		if(isBakup) {
    			ComLogUtil.info("bakupFile:" + bakupFile.getPath() + " doesn't exist, won't do bakup.");
    		} else {
    			ComFileUtil.copyFile(file, bakupFile);
    		}
    	}
    }
    
}