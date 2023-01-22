package specialUtil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import util.commonUtil.ComFileUtil;
import util.commonUtil.ComFileUtil.DupFileRet;
import util.commonUtil.ComFileUtil.DupFileStatus;
import util.commonUtil.ComLogUtil;
import util.commonUtil.ComRegexUtil;
import util.commonUtil.ComStrUtil;
import util.commonUtil.ConfigManager;
import util.commonUtil.interfaces.IConfigManager;
import util.commonUtil.json.JSONObject;
import util.commonUtil.model.FileName;
import util.media.ComMediaUtil;

/**
 * Remove Ads folder/file and duplicate files
 * @author sihun
 *
 */
public class AdsVideoRm {
	
	private static IConfigManager configManager;
	
	private static Boolean isPrintOnly = true;
	private static Boolean isRemoveDupSuffix = true;
	private static Boolean isRemoveNumbericPic = true;
	private static Integer dupSizeThrottleInKB = 0;
	
	
	public static void main(String[] args) throws Exception {
//		doOneLevelRm(new File("F:\\Downloads\\toMove\\蜜桃影像传媒_102部全集\\"));
//		doOneLevelRm(new File("F:\\Downloads\\toMove\\糖心VLOG\\"));
		
		String FolderToHandle = "";
		configManager = ConfigManager.getConfigManager(RenameFile.class.getResource("common.properties"));
		FolderToHandle = configManager.getString("FolderToHandle").trim();
		isPrintOnly = "true".equalsIgnoreCase(configManager.getString("isPrintOnly"));
		isRemoveDupSuffix = "true".equalsIgnoreCase(configManager.getString("isRemoveDupSuffix"));
		isRemoveNumbericPic = "true".equalsIgnoreCase(configManager.getString("isRemoveNumbericPic"));
		dupSizeThrottleInKB = Integer.parseInt(configManager.getString("dupSizeThrottleInKB"), 10);
		
		doOneLevelRm(new File(FolderToHandle));
	}
	
	public static void rmAds(File targetPath) throws Exception {
		doOneLevelRm(targetPath);
		
	}
	
	
	private static List<String> adsFolder = Arrays.asList(
			"找到我们",
			"宣传文本",
			"論壇文本",
			"論壇文宣"
	);
	
	private static List<String> adsPrefixes = Arrays.asList(
			"安卓二维码",
			"N房间精彩直播",
			"★★★点击观看",
			"UU67",
			"歡迎加入",
			"华人美女荷官",
			"國 产 原 创 种 子"
	);
	
	private static List<String> adsKeywords = Arrays.asList(
			"下载须知",
			"地址获取",
			"资源声明",
			"安卓二维码",
			"N房间精彩直播",
			"★★★点击观看",
			"华人美女荷官"
	);
	
	public static boolean isAdsFolder(File file) {
		String fileName = file.getName();
		for(int i = 0; i < adsFolder.size(); i ++) {
			String adsKeyword = adsFolder.get(i);
			if(fileName.indexOf(adsKeyword) >= 0) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isAdsFile(File file) {
		String fileName = file.getName();
		int size = adsPrefixes.size();
		for(int i = 0; i < size; i++) {
			String adsPrefix = adsPrefixes.get(i);
			if(fileName.startsWith(adsPrefix)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isAdsTxt(File file) {
		String fileName = file.getName();
		if(ComMediaUtil.isTxt(fileName)) {
			for(int i = 0; i < adsKeywords.size(); i ++) {
				String adsKeyword = adsKeywords.get(i);
				if(fileName.indexOf(adsKeyword) >= 0) {
					return true;
				}
			}
		}
		return false;
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
		List<File> folders = new ArrayList<File>();
		HashMap<Long, File> fileSizeMap = new HashMap<Long, File>();
		HashMap<Long, ArrayList<File>> dupMap = new HashMap<Long, ArrayList<File>>();
		
		HashMap<String, ArrayList<String>> fileNameMap = new HashMap<String, ArrayList<String>>();
		
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
		}
		
		Set<String> keySet = fileNameMap.keySet();
		String[] fileNameArr = (String[]) keySet.toArray(new String[0]);
		
		// then iteration all files to handle them
		for(int i = 0; i < length; i++) {
			File file = files[i];
			String getAbsolutePath = file.getPath();
			String nameOnly = file.getName();

//			ComLogUtil.info("file1:" + file.getAbsolutePath());
//			ComLogUtil.info("file2:" + file.getName());
//			ComLogUtil.info("file3:" + file.getPath());
			if(file.isDirectory()) {
				boolean needToRm = isAdsFolder(file);
				if(needToRm) {
					doRemoveFolder(needToRm, file);
				} else {
					folders.add(file);
				}
			} else {
				FileName fileName = new FileName(file);
				String ext = fileName.getExt(true);
				DupFileRet dupFileRet = ComFileUtil.isDuplicateFile(file, dupSizeThrottleInKB);
				DupFileStatus dupStatus = dupFileRet.getDupFileStatus();
				if(dupStatus == DupFileStatus.WITH_SAME_SIZE) {
					boolean needToRm = true;
					File dupFile = dupFileRet.getDupFile();
					long fileLength = file.length();
					long dupFileLength = dupFile.length();
					long sizeDiff = fileLength - dupFileLength;
					File file2Remove;
					String grepMark;
					if(sizeDiff == 0) {
						file2Remove = file;
						grepMark = "dupFile";
					} else if (sizeDiff > 0) {
						file2Remove = dupFile;
						grepMark = "dupFile sizeDiff(" + fileLength + "(" + fileLength/1024 + "KB) - " + dupFileLength + "(" + dupFileLength/1024 + "KB) " + " = " + sizeDiff + "Bytes)>0";
					} else {
						file2Remove = file;
						grepMark = "dupFile sizeDiff(" + fileLength + "(" + fileLength/1024 + "KB) - " + dupFileLength + "(" + dupFileLength/1024 + "KB) " + " = " + sizeDiff + "Bytes)<0";
					}
					doRemove(needToRm, file2Remove, grepMark);
				} else if(dupStatus == DupFileStatus.WITH_DUP_SUFFIX_ONLY) {
					boolean needToRm = !isPrintOnly;
					String newFile = ComFileUtil.removeDuplicateFileNameNum(file);
					if(isRemoveDupSuffix) {
						ComFileUtil.doRename(needToRm, file, new File(newFile), "isRemoveDupSuffix");
					} else {
						String msg = "remove duplicate suffix from/to:\n" + getAbsolutePath + "\n" + newFile;
						ComLogUtil.info(msg + " has been disabled!");
					}
//				} else if(dupStatus == DupFileStatus.WITH_DIFFERENT_SIZE) {
					
					
				} else {
					boolean needToRm = false;
					if(ComMediaUtil.isReservedFile(nameOnly)) {
						// do nothing for reserved files
						ComLogUtil.info("won't remove this reservedFile:" + nameOnly);
					} else if(ComMediaUtil.isVideo(nameOnly)){
						// also take the filesize into account.
						long filesizeMB = ComFileUtil.getFileSizeMB(file);
						String grepMark = "";

						if(filesizeMB < 100) {
							needToRm = true;
							grepMark = "videoSize<100MB";
						}

						if(isAdsFile(file)) {
							needToRm = true;
							grepMark = "video is Ads";
						};
						doRemove(needToRm, file, grepMark);
					} else if(ComMediaUtil.isPicutre(nameOnly)){
//					boolean needToRm = isAdsFile(file);
						String noExtension = fileName.getFileNameOnly();
						ArrayList<String> arrayList = fileNameMap.get(noExtension);
						String grepMark = "";
						if(arrayList.size() > 1) { // has same name file for this picutre(e.g. samename.mp4), so we should keep this picture as a thumbnail for this mp4 file
							// then keep this picture file
							needToRm = false;
						} else {
							String noSuffixNumber = noExtension.replaceFirst("_{0,9}\\d+$", "");
							if(noSuffixNumber.length() == 0) { // remove meaningless name picture
								grepMark = "meaningless name pic";
								needToRm = true;
							} else {
								int nameArrLength = fileNameArr.length;
								needToRm = true; // set default as true, then try to find the matched another file
								grepMark = "no mp4 file for this picture found";
								for(int k = 0; k < nameArrLength; k++) {
									String name = fileNameArr[k];
									if(name.startsWith(noSuffixNumber)) {
										ArrayList<String> matchedList = fileNameMap.get(name);
										if(matchedList.size() > 1) {
											needToRm = false;
											break;
										} else {
											String matchedExtension = matchedList.get(0);
											
											if(matchedExtension.equals(ext)) { // if the only match is this picture itself
												
											} else { // if find another matched one
												needToRm = false;
												break;
											}
										}
									}
									
								}
							}
							
							if(!isRemoveNumbericPic) {
								if(ComRegexUtil.test(noExtension, "^\\d+$")) {
									needToRm = false;
								}
							}
							
							doRemove(needToRm, file, grepMark);
						}
					} else if(ComMediaUtil.isTxt(nameOnly)){
//					boolean needToRm = isAdsTxt(file);
						needToRm = true;
						doRemove(needToRm, file, "text file");
					} else if(ComMediaUtil.isRichText(nameOnly)){
						needToRm = true;
						doRemove(needToRm, file, "richtext file");
					} else if(ComMediaUtil.isTorrent(nameOnly)){
						needToRm = true;
						doRemove(needToRm, file, "torrent file");
					} else if(ComMediaUtil.isURL(nameOnly)){
						needToRm = true;
						doRemove(needToRm, file, "URL file");
//					} else if(ComMediaUtil.isCompressedFile(nameOnly)){
//						needToRm = true;
//						doRemove(needToRm, file, "compressed file");
					} else if(ComMediaUtil.isJunkFileType(nameOnly)){
						needToRm = true;
						doRemove(needToRm, file, "junk file");
					} else if("gif".equalsIgnoreCase(ComFileUtil.getFileExtension(nameOnly, false))){
						needToRm = true;
						doRemove(needToRm, file, "gif file");
					} else {
						// do nothing for none-video files
					}
					
					if(!needToRm) { // if file is not removed, check its' size for duplication
						if(!ComMediaUtil.isReservedFile(nameOnly)) { // skip duplication check for VOB IFO BUP files
							long bytes = file.length();
							File sameSizeFile = fileSizeMap.get(bytes);
							if(sameSizeFile == null) {
								fileSizeMap.put(bytes, file);
							} else {
								ArrayList<File> arrayList = dupMap.get(bytes);
								if(arrayList == null) {
									arrayList = new ArrayList<File>();
									arrayList.add(sameSizeFile);
									dupMap.put(bytes, arrayList);
								}
								arrayList.add(file);
							}
						}
						
					}
				}
				
			}
		}
		
//		printAllFileSize(fileSizeMap);
		
		printDupFiles(dupMap);
		
		
		int size = folders.size();
		for(int i = 0; i < size; i++) {
			File nextFolder = folders.get(i);
			
			// Need to handle disk folder
			String nextFolderName = nextFolder.getName();
			
//			if("VIDEO_TS".equals(nextFolderName) || "BDMV".equals(nextFolderName)) {
//				// do nothing for disk folder
//				ComLogUtil.info("do nothing for disk folder: " + nextFolderName);
//			} else {
//				doOneLevelRm(nextFolder);
//			}
			doOneLevelRm(nextFolder);
		}
	}
	
	private static void printAllFileSize(HashMap<Long, File> fileSizeMap) {
		Set<Entry<Long, File>> set = fileSizeMap.entrySet();
		Iterator<Entry<Long, File>> it = set.iterator();
		while(it.hasNext()) {
			Entry<Long, File> next = it.next();
			Long key = next.getKey();
			File value = next.getValue();
			ComLogUtil.error("file size: " + key + " | " + value.getPath());
		}

	}
	
	private static void printDupFiles(HashMap<Long, ArrayList<File>> dupMap) {
		Set<Entry<Long, ArrayList<File>>> dupSet = dupMap.entrySet();
		Iterator<Entry<Long, ArrayList<File>>> dupIt = dupSet.iterator();
		while(dupIt.hasNext()) {
			Entry<Long, ArrayList<File>> next = dupIt.next();
			Long key = next.getKey();
			ArrayList<File> value = next.getValue();
			int size = value.size();
			for(int i = 0; i < size; i++) {
				File file = value.get(i);
				ComLogUtil.error("******* Dulicate files with size: " + key + " | " + file.getPath());
			}
		}

	}
	
	private static void doRemoveFolder(boolean needToRm, File file) throws Exception {
		String absolutePath = file.getPath();
		if (needToRm) {
			ComLogUtil.error("***********NeedToRm - file:" + absolutePath);
			File[] files = file.listFiles();
			ComLogUtil.printArr(files, "folderContent");
			if(!isPrintOnly) {
				ComFileUtil.delFileAndFolder(file);
			} else {
				
			}
		} else {
//			ComLogUtil.info("common - file:" + getAbsolutePath);
		}
	}
	
	private static void doRemove(boolean needToRm, File file, String grepMark) {
		String absolutePath = file.getPath();
		if (needToRm) {
			long filesizeMB = ComFileUtil.getFileSizeMB(file);
			ComLogUtil.sysoCallStacks("needToRename??");
			ComLogUtil.error("NeedToRm  [" + grepMark + "] - file:" + absolutePath + ", size(MB): " + filesizeMB);
			if(!isPrintOnly) file.delete(); 
		} else {
//			ComLogUtil.info("common - file:" + getAbsolutePath);
		}
	}
}

