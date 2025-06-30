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
import java.util.Collection;
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
public class OculusTVMux {
	
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
	
	
	public static void main(String[] args) throws Exception {
		String FolderToHandle = "";
		configManager = ConfigManager.getConfigManager(RenameFile.class.getResource("common.properties"));

		try {
			isPrintOnly = "true".equalsIgnoreCase(configManager.getString("isPrintOnly"));
			FolderToHandle = configManager.getString("FolderToHandle").trim();
			muxSameDurationFiles(ComFileUtil.convertToArrDir(FolderToHandle));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private static void muxSameDurationFiles(File[] dirs) throws Exception {
		List<File> files = ComFileUtil.unionDirs(dirs);
//		File[] files = dir.listFiles();
        
		int length = files.size();
		List<File> videoFiles = new ArrayList<File>();
		HashMap<File, VideoDuration> fileDurationMap = new HashMap<File, VideoDuration>();
		
		// first store all durations
		for(int i = 0; i < length; i++) {
			File file = files.get(i);
			FileName fileName = new FileName(file);
			VideoDuration duration = null;
			if(file.isDirectory()) {
			} else if(ComMediaUtil.isVideo(file)){
				duration = ComMediaUtil.getVideoDuration(file);
				fileDurationMap.put(file,  duration);
				videoFiles.add(file);
			}
		}
		
		// then group by
		Map<Object, List<File>> collect = videoFiles.stream().collect(Collectors.groupingBy(item -> {
			Object retVal = fileDurationMap.get(item).getSec();
//			ComLogUtil.info(retVal);
			retVal = ComNumUtil.number2Integer(retVal);
			return retVal;
		}));
		
		// then iteration all files to handle them
//		Object[] array = collect.values().stream().filter(List -> List.size() > 1).toArray();
		collect.values().stream().filter(list -> {
			if(list.size() > 1) {
				String msg = list.stream().map(file -> 
					fileDurationMap.get(file).getSexagesimal() + " | "
						+ ComFileUtil.getFileSizeReadable(file) + " | "
						+ file.getPath()).reduce("--------", (x, y) -> x + "\n" + y
				);
				ComLogUtil.info("durationDup: " + msg + "\n--------");
				return true;
			}
			return false; 
		}).forEach(list -> { // for those file that has dup duration files.
			Optional<File> max = list.stream().max(Comparator.comparing(File::length));
			Optional<File> min = list.stream().min(Comparator.comparing(File::length));
			if(max.isPresent()) {
				File maxFile = max.get();
				File minFile = min.get();
				if(maxFile.exists()) {
					ComMediaUtil.mergeAV(maxFile, minFile, new FileName(maxFile).setExt(".mkv").toString(), false);
				}
			} else {
				ComLogUtil.printCollection(list, "max not present");
			}
		});
		
//		RemoveSameDurationFiles(dupMap);
		return;
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
	private static void doOneLevel(File dir) throws Exception {
		File[] files = dir.listFiles();
        if(files == null) {
        	ComLogUtil.error("listed files is null, maybe the explorer.exe is hold the handler of this empty dir. dir:" + dir);
        	return;
        }
        if(files.length == 0) {
        	ComLogUtil.error("will skip this empty dir:" + dir);
        	if(!isPrintOnly) ComFileUtil.delFileAndFolder(dir);
        	return;
        } else {
//        	ComLogUtil.info("won't remove this none-empty dir:" + dir);
        }
		int length = files.length;
		Map<String, ArrayList<File>> durationMap = new HashMap<String, ArrayList<File>>();
		
		List<String> vidoeFileNamesWithExt = new ArrayList<String>();
		
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
				String groupKey = ComMediaUtil.getVideoDuration(file).toString();

				if(durationMap.containsKey(groupKey)) {
					durationMap.get(groupKey).add(file);
				} else {
					ArrayList<File> mediaList = new ArrayList<File>();
					mediaList.add(file);
					durationMap.put(groupKey, mediaList);
				}
			}
		}
		
		
		ComLogUtil.info("----------------------------------------Result(TODO)------------------------------------------");
//		ComLogUtil.info(ComLogUtil.objToString(durationMap));
//		Collection<ArrayList<File>> values = durationMap.values();
//		ComLogUtil.printCollection(values, "matchedVideos");

		Set<String> keySet = durationMap.keySet();
		Iterator<String> iterator2 = keySet.iterator();
		while(iterator2.hasNext()) {
			String next = iterator2.next();
			ArrayList<File> arrayList = durationMap.get(next);
			ComLogUtil.printCollection(arrayList, next);
		}
		
		/**/
//		Iterator<ArrayList<File>> iterator = values.iterator();
//		while(iterator.hasNext()) {
//			ArrayList<File> next = iterator.next();
//			if(next.size() > 1) {
//				File file0 = next.get(0);
//				File fileLast = next.get(next.size() - 1);
//				
//			}
//		}
		
	}
	
	
	
}

