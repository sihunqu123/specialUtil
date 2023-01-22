package specialUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import util.commonUtil.ComFileUtil;
import util.commonUtil.ComLogUtil;
import util.commonUtil.ComRegexUtil;
import util.commonUtil.ComStrUtil;
import util.commonUtil.ConfigManager;
import util.commonUtil.interfaces.IConfigManager;
import util.commonUtil.model.FileName;
import util.media.ComMediaUtil;

/**
 *
 */
public class RenameFile {
	private static final String GREPMARK = "RenameFile";

	private static IConfigManager configManager;
	
	private static String[] adPrefixRegs = new String [] {
			"(?<=\\\\)Tokyo.{0,1}Hot[-_ ]{1}(?=[^\\\\]+)",
			"(?<=\\\\)\\[thz\\.la\\](?=[^\\\\]+)",
			"(?<=\\\\)\\[c0e0\\.com\\](?=[^\\\\]+)",
			"(?<=\\\\)91制片厂(最新出品)? ?(?=[^\\\\]+$)",
			"(?<=\\\\)(最新)?国产AV佳作 ?(?=[^\\\\]+$)",
			"(?<=\\\\)阳光电影www.ygdy8.com.(?=[^\\\\]+$)",
			"(?<=\\\\)最新出品(?=[^\\\\]+)",
			"(?<=\\\\)【酷吧电影下载kuba222.com】(?=[^\\\\]+$)",
			"(?<=\\\\)\\[电影天堂www.dytt89.com\\](?=[^\\\\]+$)",
			"(?<=\\\\)\\[电影湾dy196.com\\](?=[^\\\\]+$)",
			"(?<=\\\\)\\[BD影视分享bd2020.com\\](?=[^\\\\]+$)",
			"(?<=\\\\)bbs2048.org出品@(?=[^\\\\]+$)",
			"(?<=\\\\)guochan2048.com -(?=[^\\\\]+$)",
			"(?<=\\\\)\\[电影狗www.dydog.org\\](?=[^\\\\]+$)",
			"(?<=\\\\)\\[99杏\\]\\[香港三级\\](?=[^\\\\]+$)",
			"(?<=\\\\)@\\[香港\\]\\[三级\\](?=[^\\\\]+$)",
			"(?<=\\\\)\\d+\\.\\[香港_?三级\\](?=[^\\\\]+$)",
			"(?<=\\\\)【品色堂p4av.com】\\[?(?=[^\\\\]+$)",
			"(?<=\\\\)香?港(经典)?(三级|影片)[-~]?(?=[^\\\\]+$)",
			
			"(?<=\\\\)石狮影视论坛www.mndvd.cn@(?=[^\\\\]+$)",
			"(?<=\\\\)石狮影视论坛www.mndvd.cn】(?=[^\\\\]+$)",
			
			
			"(?<=\\\\)\\d+@www\\.[a-z0-9.]+@(?=[^\\\\]+$)",
			"(?<=\\\\)第一會所新片@SIS001@(?=[^\\\\]+$)",
			"(?<=\\\\)icao.me@(?=[^\\\\]+$)",
			"(?<=\\\\)2048社区 - \\w{1,3}2048.com@(84)(?=[^\\\\]+$)",
			"(?<=\\\\)JAVVR-Miyu Saito-84(?=[^\\\\]+$)",
			"(?<=\\\\)JAVVR-Nana Fukada-(?=[^\\\\]+$)",
			
			"(?<=\\\\)mhd1080.com@(?=[^\\\\]+$)",
			
			"(?<=\\\\)84(?=KMVR-[^\\\\]+$)",
			"(?<=\\\\)84(?=kmvr-[^\\\\]+$)",
			
			"(?<=\\\\)SLR_(?=[^\\\\]+$)",
			
			// "(?<=\\\\[^\\\\]{1,99})_1\\[0x1e0\\]_closedCaption_condensed_translaste(?=[^\\\\]+$)",
			
			"(?<=\\\\)zma-zenra-stark_(?=[^\\\\]+$)",
			"(?<=\\\\)AV文檔(?=[^\\\\]+$)",
			
			"(?<=\\\\)\\w+.com@(?=[^\\\\]+$)",
			"(?<=\\\\)\\w+\\d{0,4}.org@(?=[^\\\\]+$)",
			
			"(?<=\\\\)\\w{3,4}.me\\](?=[^\\\\]+$)",
			"(?<=\\\\)\\w{3}.la\\](?=[^\\\\]+$)",
			
			"(?<=\\\\)\\[xSinsVR.com;(?=[^\\\\]+$)",
			
			"(?<=\\\\)【南方电影网www.77woo.com】(?=[^\\\\]+$)",
			
//			"(?<=\\\\)\\(IDEAPOCKET\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\([A-Z-]+\\)(?=[^\\\\]+$)",
			
			"(?<=\\\\)\\[duopan\\.LA\\]-(?=[^\\\\]+$)",
			
			
			
			"(?<=\\\\)FLY999原創@單掛D.C.資訊交流網(?=[^\\\\]+$)",
			"(?<=\\\\)FLY999原創@(?=[^\\\\]+$)",
			"(?<=\\\\)第一流氓@18P2P(?=[^\\\\]+$)",
			"(?<=\\\\)社区 - (?=[^\\\\]+$)",
			

			
//			"(?<=\\\\.{1,90})@\\d{3}[a-z]{3}\\.com(?=\\.[^.\\\\]+$)",
			
			
//			"(?<=\\\\)\\d{1,3}(?=[^.\\\\\\d]{1,}[^\\\\]+$)",
			"(?<=\\\\)h_\\d{4}(?=[^\\\\]+$)",
			

			
			
			"(?<=\\\\)\\[Arbt.us\\]?@(?=[^\\\\]+$)",
			"(?<=\\\\)\\[?Arbt.xyz\\]?@(?=[^\\\\]+$)",
			
			"(?<=\\\\)\\[?FHD\\](?=[^\\\\]+$)",
			
			"(?<=\\\\)\\[fbfb.me\\](?=[^\\\\]+$)",
			"(?<=\\\\)fbfb.me@(?=[^\\\\]+$)",
			"(?<=\\\\)bhd1080.com@(?=[^\\\\]+$)",
			"(?<=\\\\)\\(kawaii\\)(?=[^\\\\]+$)",
			"(?<=\\\\SLR_)SLR (?=[^\\\\]+$)",
			
			"(?<=\\\\)1030xx.com-(?=[^\\\\]+$)",
			"(?<=\\\\)duopan.LA\\]-(?=[^\\\\]+$)",
			
			
			"(?<=\\\\)\\(Madonna\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\(CASANOVA\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\(S1\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\(KMP\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\(MOODYZ\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\(PRESTIGE\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\(V＆R PRODUCE\\)(?=[^\\\\]+$)",
			"(?<=\\\\)\\(KMP(VR)?\\)(?=[^\\\\]+$)",
			
			"(?<=\\\\.{1,30})-VR(?=\\.[^.\\\\]+$)",
			
			
			
			"(?<=\\\\)\\((?=[^\\\\]+$)",
			
			
			"(?<=\\\\)SLR_(?=SLR_[^\\\\]+$)",
			"(?<=\\\\SLR_)SLR (?=[^\\\\]+$)",
			
			"(?<=\\\\.{1,150})\\[fuckbe\\.com\\](?=[^\\\\]+$)",
			"(?<=\\\\.{1,150})【HQ超高画質！】(?=[^\\\\]+$)",
			"(?<=\\\\.{1,150})【革新的タイムリープVR】(?=[^\\\\]+$)",
			"(?<=\\\\.{1,150})【VR】(?=[^\\\\]+$)",
			
			
			"(?<=\\\\.{1,99})@18p2p(?=\\.[^\\\\]+$)",
			
			"(?<=\\\\.{1,99})-4k60fps(?=\\.[^\\\\]+$)",
			"(?<=\\\\.{1,99})-4k60fps$",  // for folder
			
			"(?<=\\\\.{1,99})_4K(?=\\.[^\\\\]+$)",
			"(?<=\\\\.{1,99})_4K$", // for folder
			
			"(?<=\\\\.{1,99})\\.XXX(?=\\.[^\\\\]+$)",
			
			
			
			"(?<=\\\\.{1,90})--更多视频访问\\[[^]]+\\](?=\\.[^.\\\\]+$)",
			"(?<=\\\\.{1,90})-2x-RIFE(?=\\.[^.\\\\]+$)",
			"(?<=\\\\.{1,90})-fuckbe.com(?=\\.[^.\\\\]+$)",
			"(?<=\\\\.{1,90})\\.VR(?=\\.[^.\\\\]+$)",
			"(?<=\\\\.{1,90})-RIFE(?=\\.[^.\\\\]+$)",
			"(?<=\\\\.{1,90})\\s+\\([a-zA-Z]+\\.com\\)(?=\\.[^.\\\\]+$)",
			
			"(?<=\\\\.{1,150})\\.MP4-[A-Z]+\\[rarbg\\](?=\\.[^.\\\\]+$)",
			
			
			
			
			
			"(?<=\\\\)www\\.xBay\\.me\\s+-\\s+(?=[^\\\\]+$)",
			"(?<=\\\\)www\\.IPTV\\.memorial\\s+-\\s+(?=[^\\\\]+$)",
			
			"(?<=\\\\)\\[LS\\](?=[^\\\\]+$)",
			"(?<=\\\\)\\.(?=[^\\\\]+$)",
			"(?<=\\\\)\\,(?=[^\\\\]+$)",
			"(?<=\\\\)\\&(?=[^\\\\]+$)",
			
			
			"(?<=\\\\)\\w+@18p2p(@022)?(?=[^\\\\]+$)",
			"(?<=\\\\)d4b4\\.com(?=[^\\\\]+$)",
			"(?<=\\\\)t3u3\\.com(?=[^\\\\]+$)",
			
			"(?<=\\\\)[a-z]\\d+\\.4KMV-(?=[^\\\\]+$)",
			"(?<=\\\\)[a-z]\\d+\\.(?=[^\\\\]+$)",
			"(?<=\\\\)60FPS - (?=[^\\\\]+$)",
			"(?<=\\\\)\\d{3,}(?=[^\\\\]+$)",
			
			
			"(?<=\\\\)Marica.Hase\\s+-\\s+(?=[^\\\\]+$)",
			"(?<=\\\\)Marica.Hase\\s?(?=[^\\\\]+$)",
			"(?<=\\\\)h.d\\d00.com@(?=[^\\\\]+$)",
			"(?<=\\\\)\\[ThZu\\.Cc\\](?=[^\\\\]+$)",
			"(?<=\\\\)\\[[^\\]]+.com\\](?=[^\\\\]+$)",
			"(?<=\\\\)w2yuqing@(?=[^\\\\]+$)",
			"(?<=\\\\)FISH321@18P2P ?(?=[^\\\\]+$)",
			"(?<=\\\\)【每日更新[^】]*】(?=[^\\\\]+$)",
			"(?<=\\\\)【Weagogo】(?=[^\\\\]+$)",
			
			"(?<=\\\\)香港：(?=[^\\\\]+$)",
			"(?<=\\\\)\\[更多电影尽在outdy.com\\] ?\\[?(?=[^\\\\]+$)",
			"(?<=\\\\)wws1983@18P2P(?=[^\\\\]+$)",
			"(?<=\\\\)www\\.[a-z]+[0-9]+\\.xyz(?=[^\\\\]+$)",
			"(?<=\\\\)\\[BT-btt.com\\](?=[^\\\\]+$)",
			"(?<=\\\\)mfgc2\\.com (?=[^\\\\]+)",
			"(?<=\\\\)mfgc7\\.com (?=[^\\\\]+)",
			"(?<=\\\\)QueenSnake - (?=[^\\\\]+)",
			"(?<=\\\\)Queensnake_-_(?=[^\\\\]+)",
			"(?<=\\\\)QueenSnake_(?=[^\\\\]+)",
			
			
			// TODO: comment out below one line
			"(?<=\\\\)\\[[^]]+\\] {0,}(?=[^\\\\]+$)",
			"(?<=\\\\)【[^】]+】(?=[^\\\\]+$)",
			"(?<=\\\\)[【\\[《]+(?=[^\\\\]+$)",
			
			"(?<=\\\\.{1,150}\\d{3})pl(?=\\.[^\\\\]+)"
	};
	
	private static String[][] adReplaceRegs = new String [][] {
//		{"(?<=\\\\\\w{1,9})0+(?=[1-9]\\d{0,9}[^\\\\]+$)", "-"},
		{"(?<=\\\\\\w{1,9})0{2}(?=\\d{3}[^\\\\]+$)", "-"},
		{"(?<=\\\\.{1,99})_4K-(?=[^\\\\]+$)", "-"},
		{"(?<=\\\\.{1,99}\\d{3,5})hhb(?=\\d{1,}[^\\\\]+$)", "-"},
		{"(?<=\\\\.{1,99}-)000(?=\\d{1,}[^\\\\]+$)", "0"},
		{"(?<=\\\\.{1,150})\\.part(\\d{1,2})(?=\\.[^\\\\]+$)", "-$1"},
		{"(?<=\\\\.{1,150})\\s{1,99}part\\s{1,99}(\\d{1,2})(?=\\.[^\\\\]+$)", "-$1"},
		
		{"(?<=\\\\.{1,150})-A(?=\\.[^\\\\]+$)", "-1"},
		{"(?<=\\\\.{1,150})-B(?=\\.[^\\\\]+$)", "-2"},
		{"(?<=\\\\.{1,150})-C(?=\\.[^\\\\]+$)", "-3"},
		{"(?<=\\\\.{1,150})-D(?=\\.[^\\\\]+$)", "-4"},
		{"(?<=\\\\.{1,150})-E(?=\\.[^\\\\]+$)", "-5"},
		{"(?<=\\\\.{1,150})-F(?=\\.[^\\\\]+$)", "-6"},
		{"(?<=\\\\.{1,150})-G(?=\\.[^\\\\]+$)", "-7"},
		{"(?<=\\\\.{1,150})-H(?=\\.[^\\\\]+$)", "-8"},
		{"(?<=\\\\.{1,150})-I(?=\\.[^\\\\]+$)", "-9"},
		{"(?<=\\\\.{1,150})-J(?=\\.[^\\\\]+$)", "-10"},
		{"(?<=\\\\.{1,150})-K(?=\\.[^\\\\]+$)", "-11"},
		{"(?<=\\\\.{1,150})-L(?=\\.[^\\\\]+$)", "-12"},
		{"(?<=\\\\.{1,150})-M(?=\\.[^\\\\]+$)", "-13"},
		{"(?<=\\\\.{1,150})-N(?=\\.[^\\\\]+$)", "-14"},
		
		{"(?<=\\\\.{1,150})_A(?=\\.[^\\\\]+$)", "-1"},
		{"(?<=\\\\.{1,150})_B(?=\\.[^\\\\]+$)", "-2"},
		{"(?<=\\\\.{1,150})_C(?=\\.[^\\\\]+$)", "-3"},
		{"(?<=\\\\.{1,150})_D(?=\\.[^\\\\]+$)", "-4"},
		{"(?<=\\\\.{1,150})_E(?=\\.[^\\\\]+$)", "-5"},
		{"(?<=\\\\.{1,150})_F(?=\\.[^\\\\]+$)", "-6"},
		{"(?<=\\\\.{1,150})_G(?=\\.[^\\\\]+$)", "-7"},
		{"(?<=\\\\.{1,150})_H(?=\\.[^\\\\]+$)", "-8"},
		{"(?<=\\\\.{1,150})_I(?=\\.[^\\\\]+$)", "-9"},
		{"(?<=\\\\.{1,150})_J(?=\\.[^\\\\]+$)", "-10"},
		{"(?<=\\\\.{1,150})_K(?=\\.[^\\\\]+$)", "-11"},
		{"(?<=\\\\.{1,150})_L(?=\\.[^\\\\]+$)", "-12"},
		{"(?<=\\\\.{1,150})_M(?=\\.[^\\\\]+$)", "-13"},
		{"(?<=\\\\.{1,150})_N(?=\\.[^\\\\]+$)", "-14"},
		
		{"(?<=\\\\[a-zA-Z0-9]{2,7}-\\d{3}) (?=\\d{1,2}\\.[^\\\\]+$)", "-"},
		{"(?<=\\\\[a-zA-Z0-9]{2,7}-\\d{3})([a-z])(?=\\.[^\\\\]+$)", "-$1"},
		
		//TODO: comment out below line
//		{"(?<=\\\\.{1,99})【[^】]+】(?=[^\\\\]+$)", " "},
		
//		{"(?<=\\\\.{1,99})(-\\d+)([a-z])(?=[^\\\\]+$)", "$1-$2"},
		
	};
	

	public static File removeAdsPrefix(File file) {
		String oriAbsPath = file.getPath();
		String newAbsPath = oriAbsPath;

//		ComLogUtil.info("oriAbsPath: " + oriAbsPath);
//		ComLogUtil.info("oriAbsPath over");
		
		for(int i = 0; i < adPrefixRegs.length; i++) {
			String currentReg = adPrefixRegs[i];
			
//			if(currentReg.indexOf("rarbg") > -1) {
//				ComLogUtil.info("oriAbsPath: " + oriAbsPath + ", currentReg:" + currentReg);
//			}
			
//			ComLogUtil.info("oriAbsPath: " + oriAbsPath + ", currentReg:" + currentReg);
			newAbsPath = ComRegexUtil.replaceByRegexI(oriAbsPath, currentReg, "");
			
			

			if(!oriAbsPath.equals(newAbsPath)) { // only do rename when needed
				File newFile = ComRenameUtil.findAndAddNumberSuffix(new File(newAbsPath));
				boolean renameRet = (!isPrintOnly ? file.renameTo(newFile) : false);
				String logStr = "Need to rename from/to" + " ret:" + renameRet + " by reg: " + currentReg + "\n" + oriAbsPath + "\n" + newFile;
				if(renameRet) {
					ComLogUtil.error(logStr);
					return newFile;
				} else {
					ComLogUtil.error(logStr);
					return file;
				}
			}
		}
		return file;
	}
	
	public static File removePreSuffSpace(File file) {
		String oriAbsPath = file.getPath();
		
		// remove prefix space
//		String newAbsPath = ComRegexUtil.replaceByRegexI(oriAbsPath, "(?<=\\\\)\\s+[^ ](?=[^\\\\]+)", "");
		String newAbsPath = ComRegexUtil.replaceByRegexI(oriAbsPath, "(?<=\\\\)\\s+(?=[^\\\\]+$)", "");
		
		// remove trailing space
		newAbsPath = ComRegexUtil.replaceByRegex(newAbsPath, "(?<=\\\\[^ \\\\]{0,99})\\s+(?=\\.[^ \\.\\\\]+)", "");

		if(!oriAbsPath.equals(newAbsPath)) { // only do rename when needed
			File newFile = ComRenameUtil.findAndAddNumberSuffix(new File(newAbsPath));
			boolean renameRet = (!isPrintOnly ? file.renameTo(newFile) : false);
			String logStr = "rename\n" + oriAbsPath + "\nto\n" + newFile + ":" + renameRet;
			if(renameRet) {
				ComLogUtil.error(logStr);
				return newFile;
			} else {
				ComLogUtil.info(logStr);
				return file;
			}
		}
		return file;
	}
	
	public static void video2Mp4(File file) {
		String oriAbsPath = file.getPath();
		String fileExtension = ComFileUtil.getFileExtension(file, false);
		
		if("webm".equalsIgnoreCase(fileExtension)) { // only do rename when needed
			ComLogUtil.info("will rename oriAbsPath to mp4");
			if(!isPrintOnly) {
				File newFile = ComRenameUtil.replaceFileExtention(file, ".mp4");
			}
			
		}
	}
	
	public static File replaceAds(File file) {
		String oriAbsPath = file.getPath();
		String newAbsPath = oriAbsPath;

//		ComLogUtil.info("oriAbsPath: " + oriAbsPath);
//		ComLogUtil.info("oriAbsPath over");
		
		for(int i = 0; i < adReplaceRegs.length; i++) {
			String[] currentRules = adReplaceRegs[i];
			String currentReg = currentRules[0];
			String currentReplacement = currentRules[1];
//			ComLogUtil.info("oriAbsPath: " + oriAbsPath + ", currentReg:" + currentReg);
			newAbsPath = ComRegexUtil.replaceByRegexIGroup(oriAbsPath, currentReg, currentReplacement);

			if(!oriAbsPath.equals(newAbsPath)) { // only do rename when needed
				File newFile = ComRenameUtil.findAndAddNumberSuffix(new File(newAbsPath));
				boolean renameRet = (!isPrintOnly ? file.renameTo(newFile) : false);
				String logStr = "Need to rename from/to" + ":" + renameRet + " by reg: " + currentReg + ", replacement: " + currentReplacement + "\n" + oriAbsPath + "\n" + newFile;
				if(renameRet) {
					ComLogUtil.error(logStr);
					return newFile;
				} else {
					ComLogUtil.error(logStr);
					return file;
				}
			}
		}
		return file;
	}
	
	private static void doOneLevel(File dir) throws Exception {
		File[] files = dir.listFiles();
        if(files == null) {
        	ComLogUtil.error("listed files is null, maybe the explorer.exe is hold the handler of this empty dir. dir:" + dir);
        	return;
        }
        if(files.length == 0) {
        	ComLogUtil.error("will remove this empty dir:" + dir);
        	if(!isPrintOnly) ComFileUtil.delFileAndFolder(dir);
        	return;
        } else {
//        	ComLogUtil.info("won't remove this none-empty dir:" + dir);
        }
		int length = files.length;
		List<File> folders = new ArrayList<File>();
		for(int i = 0; i < length; i++) {
			File file = files[i];
			String getAbsolutePath = file.getPath();
			String nameOnly = file.getName();
//			ComLogUtil.info("file1:" + file.getAbsolutePath());
//			ComLogUtil.info("file2:" + file.getName());
//			ComLogUtil.info("file3:" + file.getPath());
			
			if(file.isDirectory()) {
				folders.add(file);
			} else {
				file = removeAdsPrefix(file);
				file = removePreSuffSpace(file);
				file = replaceAds(file);
//				removeAdsPrefix(file);
//				removePreSuffSpace(file);
				file = UppercaseVideoID(file);
				
			}
		}


		int size = folders.size();
		for(int i = 0; i < size; i++) {
			doOneLevel(folders.get(i));
		}
	}
	
	private static File UppercaseVideoID(File file) throws Exception {
		FileName fileName = new FileName(file);
		String fileNameOnly = fileName.getFileNameOnly();
		String videoID = ComRegexUtil.getMatchedString(fileNameOnly, "^[a-z0-9]{1,7}-\\d{3}(?=[-_ ][^\\\\]+$)");
		File ret = file;
		
		if(!ComStrUtil.isBlankOrNull(videoID)) {
			String videoIDUpperCased = videoID.toUpperCase();
			String newfileNameOnly = videoIDUpperCased + fileNameOnly.substring(videoID.length());
			fileName.setFileName(newfileNameOnly);
			ret = fileName.toFile();
			ComFileUtil.doRename(!isPrintOnly, file, ret, "by uppercase");
		}
		
		return ret;
	}
	
	private static File dupSuffixToNumber(File file) {
		String oriAbsPath = file.getPath();
		String newAbsPath = oriAbsPath;

//		ComLogUtil.info("oriAbsPath: " + oriAbsPath);
//		ComLogUtil.info("oriAbsPath over");
		
		String currentReg = "(?<=\\\\.{1)-VR(?=\\.[^.\\\\]+$)";
		String currentReplacement = "-$1";
//			ComLogUtil.info("oriAbsPath: " + oriAbsPath + ", currentReg:" + currentReg);
		newAbsPath = ComRegexUtil.replaceByRegexIGroup(oriAbsPath, currentReg, currentReplacement);

		if(!oriAbsPath.equals(newAbsPath)) { // only do rename when needed
			File newFile = ComRenameUtil.findAndAddNumberSuffix(new File(newAbsPath));
			boolean renameRet = (!isPrintOnly ? file.renameTo(newFile) : false);
			String logStr = "Need to rename from/to" + ":" + renameRet + " by reg: " + currentReg + ", replacement: " + currentReplacement + "\n" + oriAbsPath + "\n" + newFile;
			if(renameRet) {
				ComLogUtil.error(logStr);
				return newFile;
			} else {
				ComLogUtil.error(logStr);
				return file;
			}
		}
		return file;
	}
	
	private static Boolean isPrintOnly = true;
	
	public static void main(String[] args) throws Exception {
		String FolderToHandle = "";

		configManager = ConfigManager.getConfigManager(RenameFile.class.getResource("common.properties"));
//		doOneLevel(new File("F:\\Downloads\\toMove\\91制片厂全集\\"));
//		doOneLevel(new File("F:\\Downloads\\toMove\\天美传媒全集\\"));
//		doOneLevel(new File("F:\\Downloads\\toMove\\蜜桃影像传媒_102部全集\\"));
		
//		doOneLevel(new File("F:\\Downloads\\toMove\\Mini传媒\\"));
		FolderToHandle = configManager.getString("FolderToHandle").trim();
		isPrintOnly = "true".equalsIgnoreCase(configManager.getString("isPrintOnly"));
		doOneLevel(new File(FolderToHandle));
	}
	
}