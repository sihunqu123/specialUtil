package specialUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Documented;
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
 * Remove Ads folder/file and duplicate files
 * @author sihun
 *
 */
public class AdsVideoRm {
	
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
	
	
	public static void main(String[] args) throws Exception {
//		doOneLevelRm(new File("F:\\Downloads\\toMove\\蜜桃影像传媒_102部全集\\"));
//		doOneLevelRm(new File("F:\\Downloads\\toMove\\糖心VLOG\\"));
		
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
		
		doOneLevelRm(new File(FolderToHandle));
	}
	
	public static void rmAds(File targetPath) throws Exception {
		doOneLevelRm(targetPath);
	}
	
	
	private static List<String> adsFolder = Arrays.asList(
			"找到我们",
			"宣传文本",
			"論壇文本",
			"Sample"
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
		
		// then iteration all files to handle them
		for(int i = 0; i < length; i++) {
			File file = files[i];
			String absolutePath = file.getPath();
			String nameOnly = file.getName();

//			ComLogUtil.info("file1:" + file.getAbsolutePath());
//			ComLogUtil.info("file2:" + file.getName());
//			ComLogUtil.info("file3:" + file.getPath());
			if(file.isDirectory()) {
				boolean needToRm = isAdsFolder(file);
				if(needToRm) {
					doRemoveFolder(needToRm, file, "ad folder");
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
					} else if (sizeDiff > 0) { // remove the smaller one
						file2Remove = dupFile;
						grepMark = "dupFile sizeDiff(" + fileLength + "(" + ComStrUtil.humanByte(fileLength, true) + ") - " + dupFileLength + "(" + ComStrUtil.humanByte(dupFileLength, true) + ") " + " = " + sizeDiff + "Bytes)>0";
					} else { // remove the smaller one
						file2Remove = file;
						grepMark = "dupFile sizeDiff(" + fileLength + "(" + ComStrUtil.humanByte(fileLength, true)+ ") - " + dupFileLength + "(" + ComStrUtil.humanByte(dupFileLength, true) + ") " + " = " + sizeDiff + "Bytes)<0";
					}
					doRemove(needToRm, file2Remove, grepMark);
				} else if(dupStatus == DupFileStatus.WITH_DUP_SUFFIX_ONLY) {
					boolean needToRm = !isPrintOnly;
					String newFile = ComFileUtil.removeDuplicateFileNameNum(file);
					if(isRemoveDupSuffix) {
						ComFileUtil.doRename(needToRm, file, new File(newFile), "isRemoveDupSuffix");
					} else {
						String msg = "remove duplicate suffix from/to:\n" + absolutePath + "\n" + newFile;
						ComLogUtil.info(msg + " has been disabled!");
					}
//				} else if(dupStatus == DupFileStatus.WITH_DIFFERENT_SIZE) {
					
					
				} else if(ComRegexUtil.test(absolutePath, "\\\\(VIDEO_TS|BDMV)\\\\")) {
					// do nothing for disk folder
					ComLogUtil.info("do nothing for disk folder: " + absolutePath);
				} else {
					
					
					
					boolean needToRm = false;
					if(ComMediaUtil.isReservedFile(nameOnly)) {
						// do nothing for reserved files
//						ComLogUtil.info("won't remove this reservedFile:" + nameOnly);
					} else {
						if(ComMediaUtil.isVideo(nameOnly)){
							// also take the filesize into account.
							long filesizeMB = ComFileUtil.getFileSizeMB(file);
							String grepMark = "";

							if(filesizeMB < videoAdSizeLimitInMB) {
								needToRm = true;
								grepMark = "videoSize<" + videoAdSizeLimitInMB + "MB";
							}

							if(isAdsFile(file)) {
								needToRm = true;
								grepMark = "video is Ads";
							};
							doRemove(needToRm, file, grepMark);
						}
						
						if(ComMediaUtil.isPicutre(nameOnly)){
//							boolean needToRm = isAdsFile(file);
								String noExtension = fileName.getFileNameOnly();
								String widthExtension = fileName.getFileNameAndExtension();
								ArrayList<String> arrayList = fileNameMap.get(noExtension);
								String grepMark = "";
								if(arrayList.size() > 1) { // has same name file for this picutre(e.g. samename.mp4), so we should keep this picture as a thumbnail for this mp4 file
									// then keep this picture file
									needToRm = false;
								} else {
									
									if(vidoeFileNamesWithExt.contains(noExtension)) { // move a.mkv.jpg to a.mkv
										String oldFilePath = fileName.toString();
										fileName.setFileName(ComFileUtil.getFileName(noExtension, false));
										boolean renameRet = (!isPrintOnly ? file.renameTo(fileName.toFile()) : false);
										grepMark = "filename.mkv.jpg";
										String logStr = "Need to rename[" + grepMark + "] from/to" + " ret:" + renameRet + "\n" + oldFilePath + "\n" + fileName.toString();
										if(renameRet) {
											ComLogUtil.error(logStr);
										} else {
											ComLogUtil.error(logStr);
										}
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
										
										if(containerFolderName.equalsIgnoreCase(noExtension)) {
											grepMark = "picName=FolderName";
											needToRm = false;
										}
										
										if(!isRemoveNumbericPic) {
											if(ComRegexUtil.test(noExtension, "^\\d+$")) {
												grepMark = "numbericPic";
												needToRm = false;
											}
										}
										
										
										// TODO:
										if(isSkipPic) {
											needToRm = false;
										}
										
										if(ComMediaUtil.checkJunkFile(nameOnly).getResult() == 1){
											needToRm = true;
											grepMark = "junk file";
										}
										
										doRemove(needToRm, file, grepMark);
									}
								}
							} else if(ComMediaUtil.isTxt(nameOnly)){ 
//							boolean needToRm = isAdsTxt(file);
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
							} else if(ComMediaUtil.isCompressedFile(nameOnly)){
								needToRm = true;
								doRemove(needToRm, file, "compressed file-" + ComFileUtil.getFileSizeReadable(file));
							} else if(ComMediaUtil.isJunkFileType(nameOnly)){
								needToRm = true;
								doRemove(needToRm, file, "junk fileType");
							} else if(ComMediaUtil.checkJunkFile(nameOnly).getResult() == 1){
								needToRm = true;
								doRemove(needToRm, file, "junk file");
							} else if("gif".equalsIgnoreCase(ComFileUtil.getFileExtension(nameOnly, false))){
								needToRm = true;
								doRemove(needToRm, file, "gif file");
							} else {
								// do nothing for none-video files
							}
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
		
		RemoveSameSizeFiles(dupMap);
		removeSameDurationFiles(dir);
		
		
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
			
			if(ComRegexUtil.test(nextFolderName, "_SKIP(_KEEP)?$")) {
				// do nothing for disk folder
				ComLogUtil.info("skip for skip folder: " + nextFolderName);
			} else {
				CheckResult checkJunkFolder = ComMediaUtil.checkJunkFolder(nextFolderName);
				if(checkJunkFolder.getResult() == 1) {	// is junkFolder
					doRemoveFolder(true, nextFolder, "junk folderType: " + checkJunkFolder.getReason());
				} else {
					doOneLevelRm(nextFolder);
				}
			}
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
	
	private static void removeSameDurationFiles(File dir) throws Exception {
		if(!isRemoveSameDurationfile) {
			return;
		};
		File[] files = dir.listFiles();
        if(files == null) {
        	ComLogUtil.error("listed files is null, maybe the explorer.exe is hold the handler of this empty dir. dir:" + dir);
        	return;
        }
        if(files.length == 0) {
        	ComLogUtil.error("empty dir:" + dir);
        	return;
        }
        
		int length = files.length;
		List<File> videoFiles = new ArrayList<File>();
		HashMap<File, VideoDuration> fileDurationMap = new HashMap<File, VideoDuration>();
		
		// first store all durations
		for(int i = 0; i < length; i++) {
			File file = files[i];
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
		}).forEach(list -> {
			Optional<File> max = list.stream().max(Comparator.comparing(File::length));
			if(max.isPresent()) {
				File maxFile = max.get();
				if(maxFile.exists()) {
					list.stream().forEach(file -> {
//						ComLogUtil.info("check file: " + file);
						boolean needToRm = file != maxFile;
						try {
							doRemove(needToRm, file, "removeDupDuration", true, true);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					});
				}
			} else {
				ComLogUtil.printCollection(list, "max not present");
			}
		});
		
//		RemoveSameDurationFiles(dupMap);
		return;
	}
	
	private static void RemoveSameSizeFiles(HashMap<Long, ArrayList<File>> dupMap) throws Exception {
		Set<Entry<Long, ArrayList<File>>> dupSet = dupMap.entrySet();
		Iterator<Entry<Long, ArrayList<File>>> dupIt = dupSet.iterator();
		while(dupIt.hasNext()) {
			Entry<Long, ArrayList<File>> next = dupIt.next();
			Long key = next.getKey();
			ArrayList<File> sameSizefileList = next.getValue();
			int size = sameSizefileList.size();
			File ceilFile = null;
			File floorFile = null;
			
			for(int i = 0; i < size; i++) {
				File file = sameSizefileList.get(i);
//				ComLogUtil.error("******* Dulicate files with size: " + key + " | " + file.getPath());
				String name = file.getName();
				int length = name.length();
				if(ceilFile == null || length >= ceilFile.getName().length()) {
					ceilFile = file;
				}
				if(floorFile == null || length >= floorFile.getName().length()) {
					floorFile = file;
				}
			}

			String grepMark = "removeDupSizeFile";
			Boolean needToRm = true;
			if(isRemoveSameSizefile) {
				final File  fileToKeep = floorFile;
				VideoDuration videoResolution = ComMediaUtil.getVideoDuration(fileToKeep);
				sameSizefileList.forEach((file2Remove) -> {
//					ComLogUtil.info(file2Remove.getAbsolutePath());
//					ComLogUtil.info(ceilFileFinal.getAbsolutePath());
					String msg = "******* Dulicate files with size: " + key + " | " + file2Remove.getPath();
					// TODO: check their duration
					if(!file2Remove.getAbsolutePath().equals(fileToKeep.getAbsolutePath())) {
						VideoDuration file2RemoveDuration;
						try {
							file2RemoveDuration = ComMediaUtil.getVideoDuration(fileToKeep);
							Float durationDiff = videoResolution.getSec() - file2RemoveDuration.getSec();
							if(durationDiff == 0) {
								ComLogUtil.error(msg + " | theOneRemove | duration: " + file2RemoveDuration + " END");
								doRemove(needToRm, file2Remove, grepMark, false, false);
							} else if(Math.abs(durationDiff) < dupDurationThrottleInSec) {
								ComLogUtil.error(msg + " | theOneRemove diff less than dupDurationThrottleInSec: " + dupDurationThrottleInSec + " END");
							} else {
								ComLogUtil.error(msg + " | theOneRemove diff greater than dupDurationThrottleInSec: " + dupDurationThrottleInSec + " END");
//								doRemove(needToRm, file2Remove, grepMark, false);
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else {
						ComLogUtil.info(msg + " | " + videoResolution + " END");
					}
				});
				ComLogUtil.info("-------------------------------------------------------------------------");
			}
		}

	}
	
	private static void doRemoveFolder(boolean needToRm, File file, String grepMark) throws Exception {
		String absolutePath = file.getPath();
		if (needToRm) {
			ComLogUtil.error("***********NeedToRm[" + grepMark + "] - doRemoveFolder:" + absolutePath);
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
	
	private static void doRemove(boolean needToRm, File file, String grepMark) throws IOException {
		doRemove(needToRm, file, grepMark, true, false);
	}
	
	private static void doRemove(boolean needToRm, File file, String grepMark, Boolean isPrint, Boolean isNeedConsent) throws IOException {
		String absolutePath = file.getPath();
		String itemType = file.isDirectory() ? "folder" : "file";
		if (needToRm) {
			String filesize = ComFileUtil.getFileSizeReadable(file);
//			ComLogUtil.sysoCallStacks("needToRename??");
			if(isPrint) ComLogUtil.error("NeedToRm  [" + grepMark + "] - " + itemType + ":" + absolutePath + ", size: " + filesize);
			if(!isPrintOnly) {
				if(isNeedConsent) {
					BufferedReader br = new BufferedReader(new InputStreamReader(System.in)); 
			        System.out.println("Enter Y/y or N/n: ");
			        String str = br.readLine();
			        char charAt = (str.trim() + "n").trim().toLowerCase().charAt(0);
			        if(charAt == 'y') {
			        	ComLogUtil.info("delete confirmed");
			        } else {
			        	ComLogUtil.info("delete canceled");
			        }
				} else {
					file.delete(); 
				}
			}
		} else {
//			ComLogUtil.info("common - file:" + getAbsolutePath);
		}
	}
}

