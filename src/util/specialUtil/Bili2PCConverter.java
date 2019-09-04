package util.specialUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import util.commonUtil.ComFileUtil;
import util.commonUtil.ComLogUtil;
import util.commonUtil.ComRegexUtil;
import util.commonUtil.ComStrUtil;
import util.commonUtil.ConfigManager;
import util.commonUtil.interfaces.IConfigManager;
import util.commonUtil.interfaces.IJSONArray;
import util.commonUtil.interfaces.IJSONObject;
import util.commonUtil.json.JSONObject;
import util.commonUtil.model.FileInfo;
import util.media.ComMediaUtil;

/**
 *
 */
public class Bili2PCConverter{
	private static final String GREPMARK = "Bili2PCConverter";

	private static boolean isInJar() {
		String resource = Bili2PCConverter.class.getResource("Bili2PCConverter.class").toString();
		boolean isInJar = resource.startsWith("jar");
		ComLogUtil.info("resource:" + resource
				+ ", isInJar:" + isInJar
				);
		return isInJar;
	}

//	private static boolean isInJar = false;
//	static {
//		String resource = Bili2PCConverter.class.getResource("Bili2PCConverter.class").toString();
//		isInJar = resource.startsWith("jar");
//		ComLogUtil.info("resource:" + resource
//				+ ", isInJar:" + isInJar
//				);
//		if(isInJar) {
//			//gets program.exe from inside the JAR file as an input stream
//			InputStream is;
//			try {
//				is = Bili2PCConverter.class.getResource("/resource/ffmpeg.exe").openStream();
//				//sets the output stream to a system folder
//				OutputStream os = new FileOutputStream("./ffmpeg.exe");
//
//				//2048 here is just my preference
//				byte[] b = new byte[2048];
//				int length;
//
//				while ((length = is.read(b)) != -1) {
//				    os.write(b, 0, length);
//				}
//
//				is.close();
//				os.close();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			ComLogUtil.info("absolute ffmpeg.exe page: " + new File("ffmpeg.exe").getAbsolutePath());
//
//		}
//	}

	public static void main(String args[]) {
//		List<File> folders = new ArrayList<File>();
//		folders.add(new File("E:\\360Downloads\\ori\\50979962\\1\\64"));
//		new File("doOneEntryJson");
//		try {
//			doOneEntryJson("thisIsTitle", folders);
//		} catch (Exception e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		if(true) return;
		String originalAndroidPath = "";
		String targetPath = "";
		String originalVideoPartsPath = "";
		if(isInJar()) { // then read from command line args
			if(args.length == 0 || !(new File(args[0])).exists()) {
				ComLogUtil.error("Please specify a correct video path\n"
						+ "e.g:\n"
						+ "  java -jar e:\\video\\"
						);
				return;
			} else {
				originalAndroidPath = args[0];
				originalVideoPartsPath = args[0];
			}
		} else { // otherwise read from config
			configManager = ConfigManager.getConfigManager(Bili2PCConverter.class.getResource("Bili2PCConverter.properties"));
//			p.load(new FileInputStream(this.getClass().getResource("./Bili2PCConverter.properties").getPath()));
			originalAndroidPath = configManager.getString("originalAndroidPath");
			originalVideoPartsPath = configManager.getString("originalVideoPartsPath");
		}

		// print path result.
		ComLogUtil.info("originalAndroidPath: " + originalAndroidPath
				+ ", originalVideoPartsPath: " + originalVideoPartsPath
				);

		try {
			doOneLevelRename(new File(originalAndroidPath));
			concatVideo(new File(originalVideoPartsPath));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}



	}

	private static final String CONFIGNAME = "entry.json";

	private static final String CONFIG_INDEX_Name = "index.json";

	private static final String VIDEO_EXTENSION = ".blv";

	private static final String SPLIT_AV_EXTENSION = ".m4s";

	private static final String SPLIT_AUDIO_FILENAME = "audio.m4s";
	private static final String SPLIT_VIDEO_FILENAME= "video.m4s";

	private static IConfigManager configManager;

	private static final String fileNameEndMark = "$end$";

	private static void concatVideo(File dir) throws Exception {
		// Step1:instant 2 container to collect files.

		// <String> videoName
		List<String> videoNameList = new ArrayList<String>();
		// <String, String> videoName, videoDes
//		Map<String, String> videoDesMap = new HashMap<String, String>();
		// <String, LinkedList<Integer>> videoName, an LinkedList about the index of all video parts for this video.
//		Map<String, LinkedList<Integer>> concateList = new HashMap<String, LinkedList<Integer>>();
		Map<String, Video2Merge> concatList = new HashMap<String, Video2Merge>();

		// Step2: collect files to be concat
		doOneLevelConcat(dir, videoNameList, concatList);

		// Step3: do concat
		Set<String> keys = concatList.keySet();
		Iterator<String> it = keys.iterator();
		while(it.hasNext()) {
			String videoName = it.next();
			Video2Merge video2Merge = concatList.get(videoName);
			// one concat video parts >= 2. Since there's no sense to concat video that only have one part.
			if(video2Merge.needMerge()) {
				mergeOneVideo(dir, video2Merge);
			} else {
				System.out.println("skip video " + videoName + " for no need to merge");
			}
		}
	}

	private static void doOneLevelConcat(File dir, List<String> videoNameList, Map<String, Video2Merge> concatList) {
		File[] files = dir.listFiles();
		int length = files.length;
		for(int i = 0; i < length; i++) {
			if(files[i].isDirectory()) doOneLevelConcat(files[i], videoNameList, concatList);
			String fileName = files[i].getName();
			String index = ComRegexUtil.getMatchedString(fileName, "(?<=-)\\d+(?=\\.blv$)");
			if(ComStrUtil.isBlank(index)) {
				System.out.println(fileName + " doesn't need concat");
				continue;
			}

			int endMark = fileName.indexOf(fileNameEndMark);
			int endPosition = fileName.length() - ("-" + index + VIDEO_EXTENSION).length();
			/**
			 * not including file extension
			 */
			String videoName = fileName.substring(0, endMark < 0 ? endPosition : endMark);
			String videoDes = null;
			if(endMark > 0) {
				videoDes = fileName.substring(endMark + fileNameEndMark.length(), endPosition);
			}

			if(videoNameList.contains(videoName)) {
				concatList.get(videoName).addVideoComponent(Integer.parseInt(index), files[i]);
			} else {
				videoNameList.add(videoName);
				concatList.put(videoName, new Video2Merge(videoName, videoDes).addVideoComponent(Integer.parseInt(index), files[i]));
//				videoDesMap.put(videoName, videoDes);
//				oneVideoList = new LinkedList<Integer>();
//				concateList.put(videoName, oneVideoList);
			}
//			oneVideoList.add(Integer.parseInt(index));
		}
	}

	static class Video2Merge {
		private String videoName;
		private String videoDes;
		private String targetFileName;

		TreeSet<Integer> oneVideoIndexSet = new TreeSet<Integer>();
		Map<Integer, File> videoComponentsFiles = new HashMap<Integer, File>();

		Video2Merge(String videoName, String videoDes) {
			this.videoName = videoName;
			this.videoDes = videoDes;
			targetFileName = videoName  + (ComStrUtil.isBlankOrNull(videoDes) ? "" : ("_" + videoDes)) + VIDEO_EXTENSION;
		}

		Video2Merge addVideoComponent(Integer index, File file) {
			oneVideoIndexSet.add(index);
			videoComponentsFiles.put(index, file);
			return this;
		}

		boolean needMerge() {
			return oneVideoIndexSet.size() > 1;
		}

		public String getVideoName() {
			return videoName;
		}

		public String getVideoDes() {
			return videoDes;
		}

		public String getTargetFileName() {
			return targetFileName;
		}

		public File[] getFileCompontentsFies() {
			File[] fileComponents = new File[oneVideoIndexSet.size()];
			Iterator<Integer> it = oneVideoIndexSet.iterator();
			int i = 0;
			while (it.hasNext()) {
				fileComponents[i++] = videoComponentsFiles.get(it.next());
			}
			return fileComponents;
		}

	}

	private static File mergeOneVideo(File dir, Video2Merge video2Merge) throws Exception {
		String dirStr = dir.getPath() + ComFileUtil.SEPARATOR;
		File targetFile = new File(dirStr + video2Merge.getTargetFileName());

		File[] fileComponents = video2Merge.getFileCompontentsFies();
		if(ComMediaUtil.concatMedia(targetFile, true, fileComponents)) {
			// if success, delete all parts file.
			for(int i = 0; i < fileComponents.length; i++) {
				System.out.println("ready to delete " + fileComponents[i]);
				fileComponents[i].delete();
			}
			return targetFile;
		} else {
			return null;
		}
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
	private static void doOneLevelRename(File dir) throws Exception {
		File[] files = dir.listFiles();

		int length = files.length;
		List<File> folders = new ArrayList<File>();
		String jsonStr = null;
		String title = null;
		for(int i = 0; i < length; i++) {
			if(files[i].isDirectory()) {
				folders.add(files[i]);
			} else if(files[i].getPath().endsWith(CONFIGNAME)){
				jsonStr = ComFileUtil.readFile2String(files[i]);
				ComLogUtil.info("set " + CONFIGNAME + " contents:" + jsonStr);
				JSONObject jsonObject = new JSONObject(jsonStr);
				title = jsonObject.getString("title");

//				Object page_data = jsonObject.get("page_data");
//				if(page_data != null && page_data instanceof IJSONObject) {
//					title += "_" + ((IJSONObject)page_data).get("part");
//				}
				title = ComRegexUtil.replaceAllLiterally(title, ":", "_");
				title = ComRegexUtil.replaceAllLiterally(title, "/", "_");
				title = ComRegexUtil.replaceAllLiterally(title, "*", "_");
				title = ComRegexUtil.replaceAllLiterally(title, "\"", "_");
				title = ComRegexUtil.replaceAllLiterally(title, "|", "_");
				title = ComRegexUtil.replaceAllLiterally(title, "?", "_");
				title = ComRegexUtil.replaceAllLiterally(title, ">", "_");
				title = ComRegexUtil.replaceAllLiterally(title, "<", "_");

			}
		}
		if(title != null) {
			doOneEntryJson(title, folders);
		}

		int size = folders.size();
		for(int i = 0; i < size; i++) {
			doOneLevelRename(folders.get(i));
		}
	}

	public static void doOneEntryJson(String title, List<File> folders) throws Exception {
		int size = folders.size();
		for(int i = 0; i < size; i++) {	// iterate every folder
			File folder = folders.get(i);
			File[] files = folder.listFiles();
//			Map<String, File> blvFiles = new HashMap<String, File>();
			TreeMap<Integer, File> blvFiles = new TreeMap<Integer, File>();
			String jsonStr = null;
			IJSONArray urlArray = null;
			Boolean hasSplitedAuido = false;
			File splitedAudioFile = null;
			Boolean hasSplitedVideo = false;
			String splitedAVName = null;
			File splitedVideoFile = null;
			// prepare file list
			for(int j = 0; j < files.length; j++) { // iterate every file
				String fileFullPathName = files[j].getPath();
				if(fileFullPathName.endsWith(VIDEO_EXTENSION)) {
					try {
						blvFiles.put(Integer.parseInt(ComFileUtil.getFileName(fileFullPathName, false)), files[j]);
					} catch (Exception e) {
						ComLogUtil.info(ComFileUtil.getFileName(fileFullPathName, false) + " already converted. skip");
						return;
					}
				} else if(fileFullPathName.endsWith(CONFIG_INDEX_Name)) {
					jsonStr = ComFileUtil.readFile2String(files[j]);
					try {
						JSONObject jsonObject = new JSONObject(jsonStr);
						if(jsonObject.has("segment_list")) {
							urlArray = jsonObject.getJSONArray("segment_list");
						} else if(jsonObject.has("video") && jsonObject.has("audio")){
							IJSONArray ja = jsonObject.getJSONArray("video");
							if(ja.length() != 1) {
								ComLogUtil.error("Split AV contians many(not one) video!!! Please Check! " + folder.getAbsolutePath());
							}
							IJSONObject jo = (IJSONObject) ja.get(0);
							String video_url = (String) jo.get("base_url");
							splitedAVName = ComRegexUtil.getMatchedString(video_url, "(?<=/)[^/]+(?=\\.[^\\?/]+\\?)");
							ComLogUtil.info("splitedAVName:" + splitedAVName);
						}
//						urlArray = new JSONObject(jsonStr).getJSONArray("segment_list");
					} catch (Exception e) {
						ComLogUtil.error("Invalid json String. file:" + files[j].getAbsolutePath() + ", str:" + jsonStr);
						e.printStackTrace();
						return;
					}
				} else if(fileFullPathName.endsWith(SPLIT_AUDIO_FILENAME)) {
					hasSplitedAuido = true;
					splitedAudioFile = files[j];
				} else if(fileFullPathName.endsWith(SPLIT_VIDEO_FILENAME)) {
					hasSplitedVideo = true;
					splitedVideoFile = files[j];
				}
			}

			if(hasSplitedAuido || hasSplitedVideo) {
				if(hasSplitedAuido && hasSplitedVideo) {
//					folder.getAbsolutePath() + "//";
					File outputFile = new File(folder, splitedAVName + ".mp4");
					if(ComMediaUtil.mergeAV(splitedVideoFile, splitedAudioFile, outputFile)) {
						splitedAudioFile.delete();
						splitedVideoFile.delete();
					} else {
						ComLogUtil.error("MerageAV Failed! videoFile:" + splitedVideoFile + ", audioFile: " + splitedAudioFile);
						throw new Error("mergeAV failed!");
					}
					renameAFile(title, outputFile, splitedAVName);
				} else {
					ComLogUtil.error("Error! Ether audio, or video file missed! folder:" + folder.getAbsolutePath());
				}
			}



			//
			if(urlArray == null) {
				ComLogUtil.info("non segment_list found in title:" + title + ", folders:" + folders.get(i));
				continue;
			}
			for(int k = 0; k < urlArray.length(); k++) {

				String url = ((JSONObject)urlArray.get(k)).getString("url");
				String videoName = ComRegexUtil.getMatchedString(url, "(?<=/)[^/]+(?=\\.[^\\?/]+\\?)");

				ComLogUtil.info("getUrl:" + videoName + ", from url:" + url);

				Entry<Integer, File> firstEntry = blvFiles.firstEntry();
				// if no files anymore, break.
				// this might happens when we haven't download all videos since it's too big.
				if(firstEntry == null) break;

				blvFiles.remove(firstEntry.getKey());
				File fileToRename = firstEntry.getValue();
				renameAFile(title, fileToRename, videoName);
			}
		}
	}

	private static void renameAFile(String title, File fileToRename, String videoName) {

		String path = fileToRename.getPath();
		String[] strArr = ComStrUtil.splitLiterally(path, ComFileUtil.SEPARATOR);

		// E:\360Downloads\biliOri\9436596\1\lua.flv.bili2api.80\0.blv to 满满黄腔的黑人要教你不要吸毒（中文字幕）_P1_15594569-1-80.flv
		// locate "lua.flv.bili2api.80"
//		int g;
//		for(g = strArr.length - 1; !strArr[g].equals("lua.flv.bili2api.80") && g > 0; g--);
//
//		if(g == 0) throw new Exception("error with:" + path);

		FileInfo newFileInfo = ComFileUtil.getFileInfo(fileToRename);

		String newFileName =  title 	// items title	e.g: 满满黄腔的黑人要教你不要吸毒（中文字幕）
									+ "_" + strArr[strArr.length - 4]	// items id 	e.g: 9436596
									+ "_" + strArr[strArr.length - 3]	// item index 	e.g: 1
									+ fileNameEndMark + videoName	// video name2 	e.g: p1
									+ "-" + newFileInfo.getFileName();
		newFileInfo.setFileName(newFileName);

		ComLogUtil.info("rename " + fileToRename + " to " + newFileName + " in path:" + newFileInfo.getDir());
		ComLogUtil.info("rename result:" + fileToRename.renameTo(new File(newFileInfo.getFullFilePath())));
//		Files.move(fileToRename.toPath(), new File(newFileInfo.getFullFilePath()).toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
	}

}