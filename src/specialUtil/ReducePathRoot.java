package specialUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
public class ReducePathRoot {

	private static IConfigManager configManager;

	private static Boolean isPrintOnly = false;
	private static Boolean isRenameByParent = false;
	
	
	public static void main(String[] args) {
		String FolderToHandle = "";

		configManager = ConfigManager.getConfigManager(ReducePathRoot.class.getResource("common.properties"));
//			p.load(new FileInputStream(this.getClass().getResource("./Bili2PCConverter.properties").getPath()));
		FolderToHandle = configManager.getString("FolderToHandle").trim();
		isPrintOnly = "true".equalsIgnoreCase(configManager.getString("isPrintOnly"));
		isRenameByParent = "true".equalsIgnoreCase(configManager.getString("isRenameByParent"));
		
		
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
        
		
		if("VIDEO_TS".equals(containerFolderName) || "BDMV".equals(containerFolderName)) {
			// do nothing for disk folder
			ComLogUtil.info("do nothing for disk folder: " + dir.getAbsolutePath());
			return;
		}
        
        
		int length = files.length;
		List<File> folders = new ArrayList<File>();
		List<File> nonFolders = new ArrayList<File>();
		List<File> bigVideos = new ArrayList<File>();
		List<File> pictures = new ArrayList<File>();
		List<File> subtitles = new ArrayList<File>();
		Boolean isDiskFolder = false;
		
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
					if("BDMV".equalsIgnoreCase(nameOnly) || "CERTIFICATE".equalsIgnoreCase(nameOnly)) {
						isDiskFolder = true;
						return;
					}
//				}
			} else {
				String ext = fileFileName.getExt(true);
				nonFolders.add(file);
				
				if(".IFO".equalsIgnoreCase(ext) || ".bup".equalsIgnoreCase(ext)) {
					isDiskFolder = true;
					return;
				}
				
				long filesizeMB = ComFileUtil.getFileSizeMB(file);
				if(ComMediaUtil.isVideo(file) && filesizeMB > 100) {
					bigVideos.add(file);
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

		if(isDiskFolder) {
			ComLogUtil.info("do nothing for disk folder: " + dir.getAbsolutePath());
		} else {
			if(foldersSize == 0) {
				for(int i = 0; i < nonFoldersSize; i++) {
					File nonFolder = nonFolders.get(i);
					if(isPrintOnly) {
						ComLogUtil.error("Need to move to parent folder: " + nonFolder.getPath() + "");
					} else {
						if(nonFolder.exists()) mvToParent(nonFolder);
					}
				}
				return;
			}
		}
		
		
		for(int i = 0; i < foldersSize; i++) {
			File nextFolder = folders.get(i);
			
			// Need to handle subtitle folder
			String nextFolderName = nextFolder.getName();
			
			if(nextFolderName.endsWith("_KEEP")) {
				ComLogUtil.debug("Skip reserved folder:" + containerFolderName);
				continue;
			}
			
			String absolutePath = nextFolder.getPath();
//			if(ComRegexUtil.test(absolutePath, "\\\\(VIDEO_TS|BDMV)\\\\")) {
//				// do nothing for disk folder
//				ComLogUtil.info("do nothing for disk folder: " + absolutePath);
//				continue;
//			}
			
			if(ComRegexUtil.test(absolutePath, "\\\\(VIDEO_TS|BDMV|CERTIFICATE)\\\\")) {
				// do nothing for disk folder
				ComLogUtil.info("do nothing for disk folder: " + absolutePath);
				continue;
			}
			
			if(ComRegexUtil.test(nextFolderName, "_SKIP(_KEEP)?$")) {
				// do nothing for disk folder
				ComLogUtil.info("skip for skip folder: " + nextFolderName);
			} else {
				doOneLevel(nextFolder);
			}
		}
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
		String logStr = "move source File to parent:\n" + file.getPath() + " - " + renameRet;
		if(renameRet) {
			ComLogUtil.error(logStr);
		} else {
			ComLogUtil.info(logStr);
			if(!isPrintOnly) {
				throw new Exception(logStr + " failed!!!");
			}
		}
	}
	
	
	private static void doRename(boolean needToDo, File oldFile, File newFile) throws Exception {
		String msg = "rename from:\n" + oldFile.getPath() + "\nto\n" + newFile.getPath();
		if(needToDo) {
			boolean renamed = oldFile.renameTo(newFile);
			if(renamed) {
				ComLogUtil.error(msg + " succeed");
			} else {
				ComLogUtil.error(msg + " failed");
				throw new Exception(msg + " failed");
			}
		} else {
			ComLogUtil.sysoCallStacks("needToRename??");
			ComLogUtil.error("Need to " + msg);
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

