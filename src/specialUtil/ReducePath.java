package specialUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.commonUtil.ComFileUtil;
import util.commonUtil.ComLogUtil;
import util.commonUtil.ComRegexUtil;
import util.commonUtil.ComStrUtil;
import util.commonUtil.ConfigManager;
import util.commonUtil.interfaces.IConfigManager;
import util.commonUtil.json.JSONObject;
import util.commonUtil.model.FileName;
import util.media.ComMediaUtil;

/**
 * Rduce uncessary foldering by moving only file to it's parent
 * @author sihun
 *
 */
public class ReducePath {

	private static IConfigManager configManager;

	private static Boolean isPrintOnly = false;
	private static Boolean isRenamePicByParent = false;
	private static Boolean isRenameByParent = false;
	private static Boolean isRenameByParent_prepend = false;
	private static Boolean isRenameByParent_force = false;
	private static Boolean isRenameByVideo = false;
	
	
	public static void main(String[] args) {
		String FolderToHandle = "";

		configManager = ConfigManager.getConfigManager(ReducePath.class.getResource("common.properties"));
//			p.load(new FileInputStream(this.getClass().getResource("./Bili2PCConverter.properties").getPath()));
		FolderToHandle = configManager.getString("FolderToHandle").trim();
		isPrintOnly = "true".equalsIgnoreCase(configManager.getString("isPrintOnly"));
		isRenamePicByParent = "true".equalsIgnoreCase(configManager.getString("isRenamePicByParent"));
		isRenameByParent = "true".equalsIgnoreCase(configManager.getString("isRenameByParent"));
		isRenameByParent_prepend = "true".equalsIgnoreCase(configManager.getString("isRenameByParent_prepend"));
		isRenameByParent_force = "true".equalsIgnoreCase(configManager.getString("isRenameByParent_force"));
		isRenameByVideo = "true".equalsIgnoreCase(configManager.getString("isRenameByVideo"));
		
		
		// properties file doesn't support Chinese
//		targetPath = "F:\\Downloads\\ing\\test\\";

		// print path result.
		ComLogUtil.info("FolderToHandle: " + FolderToHandle
				+ ", isPrintOnly: " + isPrintOnly
				);

		try {
			reduceUncessaryPath(new File(FolderToHandle));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

	}
	
	public static void reduceUncessaryPath(File targetPath) throws Exception {
		doOneLevel(targetPath);
		
	}
	

//	private static boolean isRealAction = false;

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
		String containerFolderName = dir.getName();
		String containerFolderNameShort = containerFolderName.substring(0, containerFolderName.length() - 1);
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
		List<File> nonFolders = new ArrayList<File>();
		List<File> bigVideos = new ArrayList<File>();
		List<File> pictures = new ArrayList<File>();
		List<File> subtitles = new ArrayList<File>();
		
		List<File> anchorVideos = new ArrayList<File>();
		
		for(int i = 0; i < length; i++) {
			File file = files[i];
			String getAbsolutePath = file.getPath();
			String nameOnly = file.getName();
			FileName fileFileName = new FileName(file);
//			ComLogUtil.info("file1:" + file.getAbsolutePath()); // file1:F:\Downloads\toMove\Mini传媒\mini01
//			ComLogUtil.info("file2:" + file.getName()); // file2:mini01
//			ComLogUtil.info("file3:" + file.getPath()); // file3:F:\Downloads\toMove\Mini传媒\mini01
			if(file.isDirectory()) {
//				boolean needToRm = isAdsFolder(file);
//				if(needToRm) {
//					doRemoveFolder(needToRm, file);
//				} else {
					folders.add(file);
//				}
			} else {
				nonFolders.add(file);
				long filesizeMB = ComFileUtil.getFileSizeMB(file);
				if(ComMediaUtil.isVideo(file) && filesizeMB > 100) {
					bigVideos.add(file);
					String videoID = ComRegexUtil.getMatchedString(fileFileName.getFileNameOnly(), "^[a-zA-Z0-9]{1,7}-\\d{3}(?=\\][^ ])");
					if(!ComStrUtil.isBlankOrNull(videoID)) {
						anchorVideos.add(file);
					}
					
				} else if(ComMediaUtil.isPicutre(file)) {
					pictures.add(file);
				} else if(ComMediaUtil.isSubtitle(file)) {
					subtitles.add(file);
				}
				
			}
		}

		
		int nonFoldersSize = nonFolders.size();
		int foldersSize = folders.size();
		int bigVideosSize = bigVideos.size();
		int picturesSize = pictures.size();
		int subtitlesSize = subtitles.size();
		
		Boolean isAlreadyProcessed = false;
		
		for(int i = 0; i < anchorVideos.size(); i++) {
			File anchorVideo = anchorVideos.get(i);
			FileName fileName = new FileName(anchorVideo);
			String videoID = ComRegexUtil.getMatchedString(fileName.getFileNameOnly(), "^[a-zA-Z0-9]{1,7}-\\d{3}(?=\\][^ ])");
			String videoMsg = "-" + fileName.getFileNameOnly().substring(videoID.length() + 1);
			
			for(int j = 0; j < nonFoldersSize; j++) {
				File nonFolder = nonFolders.get(j);
				FileName nonFolderFileName = new FileName(nonFolder);
				String fileNameAndExtension = nonFolderFileName.getFileNameAndExtension();
				String fileNameOnly = nonFolderFileName.getFileNameOnly();
				// if it's not this file itself, and fileNameOnly match this videoId, and fileNameOnly has not been tuned by videoMsg yet
				if(nonFolder != anchorVideo && ComRegexUtil.test(fileNameOnly, "^" + videoID + "[-_ ]") && fileNameOnly.indexOf(videoMsg) == -1) {
					String restFileName = fileNameOnly.substring(videoID.length());
					String newFileName = videoID + videoMsg + restFileName;
					nonFolderFileName.setFileName(newFileName);
					ComFileUtil.doRename(!isPrintOnly, nonFolder, nonFolderFileName.toFile(), "by anchorVideos");
					isAlreadyProcessed = true;
				}
			}
			
		}
		

		
		if(!isAlreadyProcessed && foldersSize <=1 && bigVideosSize <= 9) {
			for(int i = 0; i < bigVideosSize; i++) {
				File bigVideo = bigVideos.get(i);
				FileName bigVideoFileName = new FileName(bigVideo);
				
				if(bigVideoFileName.getFileNameAndExtension().indexOf(containerFolderName) == -1) {
					if(bigVideosSize == 1 && !isRenameByParent_force) {
						bigVideoFileName.setFileName(containerFolderName);
					} else {
						if(isRenameByParent_prepend) {
							bigVideoFileName.preAppend(containerFolderName + "_");
						} else {
							bigVideoFileName.append("_" + containerFolderName);
						}
					}
					String msg = "rename by parentFolder from/to:\n" + bigVideo.getPath() + "\n" + bigVideoFileName.toString();
					if(isRenameByParent) {
						ComFileUtil.doRename(!isPrintOnly, bigVideo, bigVideoFileName.toFile(), "isRenameByParent");
					} else {
						ComLogUtil.info(msg + " has been disabled!");
					}
				}
			}
		}
		
		// rename the only picture by the video group filename
		if(!isAlreadyProcessed && picturesSize == 1 && bigVideosSize > 0 && isRenameByVideo) {
			File picture = pictures.get(0);
			FileName pictureFileName = new FileName(picture);
			
			if(bigVideosSize == 1) {
				File video = bigVideos.get(0);
				FileName videoFileName = new FileName(video);
				if(!videoFileName.getFileNameOnly().equals(pictureFileName.getFileNameOnly())) {
					String oldFileName = pictureFileName.toString();
					pictureFileName.setFileName(videoFileName.getFileNameOnly());
					String msg = "rename by videoFileName from/to:\n" + oldFileName + "\n" + pictureFileName.toString();
					ComFileUtil.doRename(!isPrintOnly, picture, pictureFileName.toFile(), "by videoGroupFilename 1");
					isAlreadyProcessed = true;
				}
			} else {
				Map<Integer, File> videoParts = new HashMap<Integer, File>();
				int minIndex = 999;
				int maxIndex = -1;
				Boolean isVideoGroup = true;
				// iterate every videos see if all vides are in a group. e.g. filename-1.mp4, filename-2.mp4; Or filename-A.mp4, filename-B.mp4
				for(int i = 0; i < bigVideosSize; i++) {
					File video = bigVideos.get(i);
					FileName videoFileName = new FileName(video);
					String videoIndexNum = ComRegexUtil.getMatchedString(videoFileName.getFileNameOnly(), "(?<=.{1,150}[-_.])\\d{1,2}$");
					String videoIndexAlpha = ComRegexUtil.getMatchedString(videoFileName.getFileNameOnly(), "(?<=.{1,150}[-_.])[a-zA-Z]$");
					
					if(ComStrUtil.isBlankOrNull(videoIndexNum) && ComStrUtil.isBlankOrNull(videoIndexAlpha)) {
						// if not a video group
						isVideoGroup = false;
						break;
					} else {
						int videoIndex;
						if(!ComStrUtil.isBlankOrNull(videoIndexNum)) {
							videoIndex = Integer.parseInt(videoIndexNum, 10);
						} else {
							videoIndex = (int) (videoIndexAlpha.toLowerCase().charAt(0)) - 96;
						}
						if(videoIndex < minIndex) {
							minIndex = videoIndex;
						} else if(videoIndex > maxIndex) {
							maxIndex = videoIndex;
						}
						videoParts.put(videoIndex, video);
					}
					
				}
				if(isVideoGroup) {
					File video = videoParts.get(minIndex);
					FileName videoFileName = new FileName(video);
					if(!videoFileName.getFileNameOnly().equals(pictureFileName.getFileNameOnly())) {
						String oldFileName = pictureFileName.toString();
						pictureFileName.setFileName(videoFileName.getFileNameOnly());
						String msg = "rename by videoFileName from/to:\n" + oldFileName + "\n" + pictureFileName.toString();
						ComFileUtil.doRename(!isPrintOnly, picture, pictureFileName.toFile(), "by videoGroupFilename >1");
						isAlreadyProcessed = true;
					}
				}
				
			}
			
		}
		
		if(!isAlreadyProcessed && foldersSize == 0 && picturesSize == 1) {
			for(int i = 0; i < picturesSize; i++) {
				File picture = pictures.get(0);
				FileName pictureFileName = new FileName(picture);

				if(pictureFileName.getFileNameAndExtension().indexOf(containerFolderNameShort) == -1) {
					pictureFileName.setFileName(containerFolderName);
					String msg = "rename by parentFolder:\n" + picture.getPath() + "\nto\n" + pictureFileName.getFileNameAndExtension();
					if(isRenamePicByParent) {
						ComFileUtil.doRename(!isPrintOnly, picture, pictureFileName.toFile(), "isRenamePicByParent");
						isAlreadyProcessed = true;
					} else {
						ComLogUtil.info(msg + " has been disabled!");
					}
				}
			}
		}
		
		
		
		if(!isAlreadyProcessed && foldersSize == 0 && bigVideosSize == 1 && (subtitlesSize == 1 || picturesSize == 1)) {
			File bigVideo = bigVideos.get(0);
			if(isPrintOnly) {
				ComLogUtil.error("Need to move " + bigVideo.getPath() + " to parent folder");
			} else {
				if(bigVideo.exists()) {
					mvToParent(bigVideo);
					isAlreadyProcessed = true;
				}
			}

			if(picturesSize == 1) {
				File picture = pictures.get(0);
				if(isPrintOnly) {
					ComLogUtil.error("Need to move " + picture.getPath() + " to parent folder");
				} else {
					if(picture.exists()) {
						mvToParent(picture);
						isAlreadyProcessed = true;
					}
				}
			}
			
			if(subtitlesSize == 1) {
				File subtitle = subtitles.get(0);
				if(isPrintOnly) {
					ComLogUtil.error("Need to move " + subtitle.getPath() + " to parent folder");
				} else {
					if(subtitle.exists()) {
						mvToParent(subtitle);
						isAlreadyProcessed = true;
					}
				}
			}
			
			return;
		}
		
		
		if(!isAlreadyProcessed && foldersSize == 0 && nonFoldersSize == 1) {
			for(int i = 0; i < nonFoldersSize; i++) {
				File nonFolder = nonFolders.get(i);
				if(isPrintOnly) {
					ComLogUtil.error("Need to move " + nonFolder.getPath() + " to parent folder");
				} else {
					if(nonFolder.exists()) {
						mvToParent(nonFolder);
						isAlreadyProcessed = true;
					}
				}
			}
			return;
		}
		
		
		// move dvd files to root
		if(!isAlreadyProcessed && foldersSize == 1 && nonFoldersSize == 0 && "VIDEO_TS".endsWith(folders.get(0).getName())) {
			File[] vobs = folders.get(0).listFiles();
			for(int i = 0; i < vobs.length; i++) {
				File vob = vobs[i];
				if(isPrintOnly) {
					ComLogUtil.error("Need to move " + vob.getPath() + " to parent folder");
				} else {
					if(vob.exists()) {
						mvToParent(vob);
						isAlreadyProcessed = true;
					}
				}
			}
			return;
		}
		
		
		for(int i = 0; i < foldersSize; i++) {
			File nextFolder = folders.get(i);
			
			// Need to handle subtitle folder
			String nextFolderName = nextFolder.getName();
			
			if("Subs".equals(nextFolderName) || "Subtitles".equals(nextFolderName)) {
				File[] subFiles = nextFolder.listFiles();
				for(int j = 0; j < subFiles.length; j++) {
					File subFile = subFiles[j];
					if(ComMediaUtil.isSubtitle(subFile)) {
						FileName subfileName = new FileName(subFile);
						if(subfileName.getFileNameOnly().indexOf(containerFolderName) == -1) {
							File newFile = null; 
							if(subFiles.length == 1) {
								newFile = subfileName.setFileName(containerFolderName).toFile();
							} else {
								newFile = subfileName.preAppend(containerFolderName + "_").toFile();
							}
//							String msg = "rename subtitles in Subs : " + subFile.getPath() + " to " + newFile.getPath();
							ComFileUtil.doRename(!isPrintOnly, subFile, newFile, "renameSubtitle");
							isAlreadyProcessed = true;
							subFile = newFile; // update subfile, so that it could be move to parent folder later 
						}
						if(isPrintOnly) {
							ComLogUtil.error("Need to move " + subFile.getPath() + " to parent folder");
						} else {
							mvToParent(subFile);
							isAlreadyProcessed = true;
						}
					}
				}
			}
			doOneLevel(nextFolder);
		}
	}
	
	private static List getVideosInFolder(File folder) {
		File[] files = folder.listFiles();
		List<File> ret = new ArrayList();
		for(int i = 0; i < files.length; i++) {
			File file = files[i];
			if(!file.isDirectory() && ComMediaUtil.isVideo(file)) {
				ret.add(file);
			}
		}
		return ret;
	}
	
	private static String findAvailableName(String path) throws Exception {
		String pathNew = path;
		int i = 0;
		while (i < 9999 && new File(pathNew).exists()) {
			ComLogUtil.info("pathNew: " + pathNew + " already exists");
			pathNew = path + "_" + i++;
		}
		
		if(new File(pathNew).exists()){
			throw new Exception("Unable to find an available name for path" + path);
		} else {
			return pathNew;
		}
		
	}
	
	private static void mvToParent(File file) throws Exception {
		File oriFile = file;
		
		String getAbsolutePath = oriFile.getPath();
		String nameOnly = oriFile.getName();
		// TODO: Need to take the root folder into consideration.
		File parent = oriFile.getParentFile();
		String parentPath = parent.getPath();
		String parentName = parent.getName();
		// if current file name is the same with it's parent folder
		if(parentName.equals(nameOnly)) {
			// then need to rename it's parent folder first
			String parentPathNew = findAvailableName(parentPath);
			boolean renamed = parent.renameTo(new File(parentPathNew));
			String msg = "move to parent from:\n" + parentPath + "\nto\n" + parentPathNew;
			if(renamed) {
				ComLogUtil.error(msg + " succeed");
				parent = new File(parentPathNew);
				oriFile = new File(parent, nameOnly);
			} else {
				ComLogUtil.error(msg + " failed");
				throw new Exception(msg + " failed");
			}
		}
		
		File grandParent = parent.getParentFile();
		String nameOnlyNew = nameOnly;
		File fileNew = ComRenameUtil.findAndAddNumberSuffix(new File(grandParent, nameOnlyNew));

		boolean renameRet = (!isPrintOnly ? oriFile.renameTo(fileNew) : false);
		String logStr = "move source File:\n" + file.getPath() + "\nto\n" + fileNew.getPath() + ":" + renameRet;
		if(renameRet) {
			ComLogUtil.error(logStr);
		} else {
			ComLogUtil.info(logStr);
			if(!isPrintOnly) {
				throw new Exception(logStr + " failed!!!");
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
}
