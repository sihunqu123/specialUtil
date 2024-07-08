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
import util.commonUtil.ComCMDUtil;
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
 * unzip all rar and zip files in given folder to another disk, with the folder structure retained
 * @author sihun
 *
 */
public class Unzip {
	
	private static IConfigManager configManager;
	
	private static Boolean isPrintOnly = true;
	
	public static void main(String[] args) throws Exception {
		String FolderToHandle = "";
		configManager = ConfigManager.getConfigManager(RenameFile.class.getResource("common.properties"));
		FolderToHandle = configManager.getString("FolderToHandle").trim();
		isPrintOnly = "true".equalsIgnoreCase(configManager.getString("isPrintOnly"));
		
		doOneLevelRm(new File(FolderToHandle));
	}
	
	
	public static String wrapWithQuote(String str) {
		return "\"" + str + "\"";
	}
	
	private static void unzipTo(File zipFile, String targetFolder) throws Exception {
		try {
			String cmd = wrapWithQuote("C:\\Program Files\\7-Zip\\7z.exe") + " x -o" + wrapWithQuote(targetFolder) + " " + wrapWithQuote(zipFile.getPath());
			System.out.println("cmd:" + cmd);
			String res = ComCMDUtil.runCMD(cmd);
			System.out.println("res:" + res);
		} finally {
			// change a.zip => a.zip.done to indicate this zip file has been extracted.
			zipFile.renameTo(new File(new FileName(zipFile).getFullPathName() + ".done"));
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
	private static void doOneLevelRm(File dir) throws Exception {
		File[] files = dir.listFiles();
        if(files == null) {
        	ComLogUtil.error("listed files is null, maybe the explorer.exe is hold the handler of this empty dir. dir:" + dir);
        	return;
        }
		int length = files.length;
		
		// first store all fileNames
		for(int i = 0; i < length; i++) {
			File file = files[i];
			FileName fileName = new FileName(file);
			String fileNameOnly = null;
			String fileExtension = null;
			if(file.isDirectory()) {
				doOneLevelRm(file);
			} else {
				fileNameOnly = fileName.getFileNameOnly();
				fileExtension = fileName.getExt(true);
				if(".rar".equals(fileExtension)  || ".zip".equals(fileExtension)) {
					FileName outputDir = new FileName(file);
					// save to drive D
//					System.out.println("path: " + file.getPath());
//					System.out.println("getParent: " + file.getParent());
//					outputDir.setDir();
					// unzipTo(file, ComRegexUtil.replaceByRegexI(file.getParent(), "^.{1}", "S")); // To disk S:
					unzipTo(file, ComRegexUtil.replaceByRegexI(file.getParent(), "^.{1}", "N")); // To disk N:
				} else {
					// ingore non-zip file
					ComLogUtil.info("ignore non-zip file: " + fileName);
				}
			}
		}
		
	}
	
}

