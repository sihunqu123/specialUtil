package specialUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Documented;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import model.VideoDuration;
import model.VideoResolution;
import util.commonUtil.ComFileUtil;
import util.commonUtil.ComFileUtil.DupFileRet;
import util.commonUtil.ComFileUtil.DupFileStatus;
import util.commonUtil.ComLogUtil;
import util.commonUtil.ComNumUtil;
import util.commonUtil.ComRegexUtil;
import util.commonUtil.ComStrUtil;
import util.commonUtil.ConfigManager;
import util.commonUtil.interfaces.IConfigManager;
import util.commonUtil.json.JSONObject;
import util.commonUtil.model.CheckResult;
import util.commonUtil.model.FileName;
import util.media.ComMediaUtil;

/**
 * Extractor all magnet info from folders
 * @author sihun
 *
 */
public class TorrentInfoExtractor {
	
	private static IConfigManager configManager;
	
	private static Boolean isPrintOnly = true;
	
	public static void main(String[] args) throws Exception {
		String FolderToHandle = "";
		configManager = ConfigManager.getConfigManager(RenameFile.class.getResource("common.properties"));
		FolderToHandle = configManager.getString("FolderToHandle").trim();
		isPrintOnly = "true".equalsIgnoreCase(configManager.getString("isPrintOnly"));
		
		doOneLevelRm(new File(FolderToHandle));
	}
	
	/**
	 * step 1: find all entry.json file
	 * step 2: iterate every entry.json
	 * step 3: [in iteration]extract fileName info from entry.json
	 * step 4: [in iteration]locate the *.blv file in the brother folder of entry.json. If there are > 1 brother folder
	 * 							of entry.json, print out this entry and "continue".
	 * step 5: [in iteration]rename the fond blv file to extract filename.
	 */
	/**
	 * rename all video to meaningful name
	 * @param dir
	 * @throws Exception
	 */
	private static void doOneLevelRm(File dir) throws Exception {
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
		
		List<String> links = new ArrayList<String>();
		
		// first store all fileNames
		for(int i = 0; i < length; i++) {
			File file = files[i];
			FileName fileName = new FileName(file);
			String fileNameOnly = null;
			String fileExtension = null;
			if(!file.isDirectory()) {
				ComLogUtil.error("Ignore file in the root folder: " + fileName);
			} else {
				if(ComRegexUtil.testIg(file.getPath(), "[\\/\\\\]images$")) {
					ComLogUtil.error("Ignore the images folder in the root folder: " + file.getPath());
					continue;
				}
//				if(ComRegexUtil.testIg(file.getPath(), "[\\/\\\\]rar$")) {
//					ComLogUtil.error("Ignore the rar folder in the root folder: " + file.getPath());
//					continue;
//				}
				List<String> extract1Folder = extract1Folder(file);
				links.addAll(extract1Folder);
			}
		}
		
		ComLogUtil.info("----------------------------------------Result------------------------------------------");
//		ComLogUtil.printCollection(links, "");
		ComLogUtil.info("In total count: " + links.size());
		for(int i = 0; i < links.size(); i++) {
			System.out.println(links.get(i));
		}
	}
	
	private static List<String> extractMagnetFromFile(File file) throws Exception {
		String readFile2String = ComFileUtil.readFile2String(file, "utf-8");
		String[] split = readFile2String.split("\n");
		List<String> links = new ArrayList<String>();
		
		Boolean is8KMode = !ComStrUtil.isBlankOrNull(ComRegexUtil.getMatchedStringIg(readFile2String, "_8k"));
		
		for(int i = 0; i < split.length; i++) {
			String line = split[i];
			String ed2kLink = ComRegexUtil.getMatchedStringIg(line, "(?<=\\s*)ed2k:[^\\n]+");
			Boolean is8KLink = !ComStrUtil.isBlankOrNull(ComRegexUtil.getMatchedStringIg(line, "_8k"));
			Boolean isRar = line.indexOf(".rar") > 0;
		
			if(ComStrUtil.isBlankOrNull(ed2kLink)) {
				// skip useless line.
				continue;
			}
			
			Boolean is8kOnly = true;
			
			if(is8kOnly) {
				// save 8k only rar
				if(is8KMode && isRar) {
					links.add(ed2kLink);
					ComLogUtil.info("adding link: " + ed2kLink + " from file: " + file.getPath());
				}
			} else {
				// save non-8k only rar
				if(!is8KMode && isRar) {
					links.add(ed2kLink);
				}
			}
			
			/*
			if(isRar) {
				 links.add(ed2kLink);
			} else if(is8KMode && is8KLink) { // if it's a 8k link
				links.add(ed2kLink);
			} else {
				// do nothing for non-8k link
//				if(!is8KMode) links.add(ed2kLink);
			}
			*/
		}
		if(links.size() == 0) {
//			throw new Exception("Error extrating magnet links - No link found in file: " + file);
		}
		return links;
	}
	
	private static List<String> extract1Folder(File dir) throws Exception {
		File[] files = dir.listFiles();
        if(files == null) {
        	throw new Exception("Error - Empty dir:" + dir);
        }
        if(files.length == 0) {
        	throw new Exception("Error - Empty dir:" + dir);
        }
		int length = files.length;
		List<String> links = new ArrayList<String>();
		// first store all fileNames
		for(int i = 0; i < length; i++) {
			File file = files[i];
			FileName fileName = new FileName(file);
			String fileNameOnly = fileName.getFileNameAndExtension();
			String fileExtension = null;
			if(file.isDirectory()) {
				throw new Exception("Error - unexpected subFolder:" + fileName);
			}
			fileNameOnly = fileName.getFileNameAndExtension();
			fileExtension = fileName.getExt();
			if(!".txt".equals(fileExtension)) {
				ComLogUtil.info("skip none-text file type : " + fileName);
				continue;
			}
			if(fileNameOnly.startsWith("解壓密碼")) {
				ComLogUtil.info("skip 解壓密碼: " + fileName);
				continue;
			}
			
			
			List<String> extractMagnetFromFile = extractMagnetFromFile(file);
			links.addAll(extractMagnetFromFile);
			break;
		}
		return links;
	}
	
	
	
}

