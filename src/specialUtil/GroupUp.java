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
 * Group all videos up to it's folder
 * @author sihun
 *
 */
public class GroupUp {
	
	private static IConfigManager configManager;
	
	private static Boolean isPrintOnly = true;
	private static Boolean isRemoveDupSuffix = true;
	private static Boolean isRemoveNumbericPic = true;
	private static Boolean isSkipPic = false;
	private static Long dupSizeThrottleInKB = 0l;
	private static Long dupDurationThrottleInSec = 0l;
	private static Long videoAdSizeLimitInMB = 100l;
	private static Boolean isRemoveSameSizefile = false;
	private static Boolean isRemoveSameDurationfile = false;
	private static Map<String, String> folderMap = new HashMap<String, String>();
	
	static {
		URL resource = GroupUp.class.getResource("folderList.txt");

		try {
			String readFile2String = ComFileUtil.readFile2String(resource.getFile(), "UTF-8");
			String[] split = readFile2String.split("\n");
			for(int i = 0; i < split.length; i++) {
				String string = split[i];
//				ComLogUtil.info("String: " + string);
				String key = ComRegexUtil.getMatchedString(string, "(?<=/)[^\\/]+$");
				folderMap.put(key, string);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public static void main(String[] args) throws Exception {
		String FolderToHandle = "";
		configManager = ConfigManager.getConfigManager(RenameFile.class.getResource("common.properties"));
		FolderToHandle = configManager.getString("FolderToHandle").trim();
		isPrintOnly = "true".equalsIgnoreCase(configManager.getString("isPrintOnly"));
		isRemoveDupSuffix = "true".equalsIgnoreCase(configManager.getString("isRemoveDupSuffix"));
		isRemoveNumbericPic = "true".equalsIgnoreCase(configManager.getString("isRemoveNumbericPic"));
		isSkipPic = "true".equalsIgnoreCase(configManager.getString("isSkipPic"));
		dupSizeThrottleInKB = Long.parseLong(configManager.getString("dupSizeThrottleInKB"), 10);
		dupDurationThrottleInSec = Long.parseLong(configManager.getString("dupDurationThrottleInSec"), 10);
		videoAdSizeLimitInMB = Long.parseLong(configManager.getString("videoAdSizeLimitInMB"), 10);
		isRemoveSameSizefile = "true".equalsIgnoreCase(configManager.getString("isRemoveSameSizefile"));
		isRemoveSameDurationfile = "true".equalsIgnoreCase(configManager.getString("isRemoveSameDurationfile"));
		
		
		ComLogUtil.printMap(folderMap, "folderMap");
		
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
		String containerFolderName = dir.getName();
		List<File> folders = new ArrayList<File>();
		HashMap<Long, File> fileSizeMap = new HashMap<Long, File>();
		HashMap<Long, ArrayList<File>> dupMap = new HashMap<Long, ArrayList<File>>();
		
		HashMap<String, ArrayList<String>> fileNameMap = new HashMap<String, ArrayList<String>>();
		
		List<String> vidoeFileNamesWithExt = new ArrayList<String>();
		
		
		// first store all fileNames
		for(int i = 0; i < length; i++) {
			File file = files[i];
			FileName fileName = new FileName(file);
			String fileNameOnly = null;
			String fileExtension = null;
			if(file.isDirectory()) {
				fileNameOnly = fileName.getFileNameAndExtension();
				fileExtension = "";
			} else {
				fileNameOnly = fileName.getFileNameOnly();
				fileExtension = fileName.getExt(true);
			}
			ArrayList<String> arrayList = fileNameMap.get(fileNameOnly);
			if(arrayList == null) {
				arrayList = new ArrayList<String>();
				fileNameMap.put(fileNameOnly, arrayList);
			}
			arrayList.add(fileExtension);
			if(ComMediaUtil.isVideo(fileName.getFileNameAndExtension())) {
				vidoeFileNamesWithExt.add(fileName.getFileNameAndExtension());
			}
		}
		
		Set<String> keySet = fileNameMap.keySet();
		String[] fileNameArr = (String[]) keySet.toArray(new String[0]);
		
		List<String> noMatchList = new ArrayList<String>();
		
		// then iteration all files to handle them
		for(int i = 0; i < length; i++) {
			File file = files[i];
			String absolutePath = file.getPath();
			String nameOnly = file.getName();

//			ComLogUtil.info("file1:" + file.getAbsolutePath());
//			ComLogUtil.info("file2:" + file.getName());
//			ComLogUtil.info("file3:" + file.getPath());
			if(file.isDirectory()) {
				// do nothing for folder
			} else {
				FileName fileName = new FileName(file);
				String ext = fileName.getExt(true);
				String groupKey = ComRegexUtil.getMatchedString(nameOnly, "^[^-_ .]+(?=[-_ .])");
				if(!folderMap.containsKey(groupKey)) {
					String errMsg = "no matched groupKey for file: " + nameOnly + ", groupKey: " + groupKey;
					ComLogUtil.error(errMsg);
					noMatchList.add(errMsg);
					continue;
				}
				String additionalPath = folderMap.get(groupKey);
				String targetFolderStr = dir + additionalPath;
				File targetFolder = new File(targetFolderStr);
				ComFileUtil.ensureDir(!isPrintOnly, targetFolder, "groupFolder");
				File targetFile = new File(targetFolderStr + "\\" + fileName.getFileNameAndExtension());
				ComFileUtil.doRename(!isPrintOnly, file, targetFile, "groupFile");
			}
		}
		
		
		ComLogUtil.info("----------------------------------------Result(TODO)------------------------------------------");
		ComLogUtil.printCollection(noMatchList, "noMatch");
	}
	
	
	
}

