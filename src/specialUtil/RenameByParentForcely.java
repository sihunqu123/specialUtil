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
public class RenameByParentForcely {

	private static IConfigManager configManager;

	private static Boolean isPrintOnly = false;
	private static Boolean isRenamePicByParent = false;
	private static Integer isRenameByParent = 0;
	private static Boolean isRenameByParent_prepend = false;
	private static Boolean isRenameByParent_force = false;
	private static Boolean isRenameByVideo = false;
	private static Integer bigVideoSizeFloorInMB = 100;
	
	
	
	public static void main(String[] args) {
		String FolderToHandle = "";

		configManager = ConfigManager.getConfigManager(ReducePath.class.getResource("common.properties"));
//			p.load(new FileInputStream(this.getClass().getResource("./Bili2PCConverter.properties").getPath()));
		FolderToHandle = configManager.getString("FolderToHandle").trim();
		isPrintOnly = "true".equalsIgnoreCase(configManager.getString("isPrintOnly"));
		isRenamePicByParent = "true".equalsIgnoreCase(configManager.getString("isRenamePicByParent"));
		isRenameByParent = Integer.parseInt(configManager.getString("isRenameByParent"), 10);
		isRenameByParent_prepend = "true".equalsIgnoreCase(configManager.getString("isRenameByParent_prepend"));
		isRenameByParent_force = "true".equalsIgnoreCase(configManager.getString("isRenameByParent_force"));
		isRenameByVideo = "true".equalsIgnoreCase(configManager.getString("isRenameByVideo"));
		bigVideoSizeFloorInMB = Integer.parseInt(configManager.getString("bigVideoSizeFloorInMB"), 10);
		
		
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
		
		for(int i = 0; i < length; i++) {
			File file = files[i];
			String absolutePath = file.getPath();
			String nameOnly = file.getName();
			
			FileName fileFileName = new FileName(file);
			String fileNameAndExtension = fileFileName.getFileNameAndExtension();
//			ComLogUtil.info("file1:" + file.getAbsolutePath()); // file1:F:\Downloads\toMove\Mini传媒\mini01
//			ComLogUtil.info("file2:" + file.getName()); // file2:mini01
//			ComLogUtil.info("file3:" + file.getPath()); // file3:F:\Downloads\toMove\Mini传媒\mini01
			if(file.isDirectory()) {
				doOneLevel(file);
			} else {
				String fileNameOnly = fileFileName.getFileNameOnly();
				String lastChar = fileNameOnly.substring(fileNameOnly.length() - 1, fileNameOnly.length()); // get the last char
				fileFileName.setFileName(containerFolderName + "-" + lastChar);
				ComFileUtil.doRename(!isPrintOnly, file, fileFileName.toFile(), "renameByParentForcely");
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

