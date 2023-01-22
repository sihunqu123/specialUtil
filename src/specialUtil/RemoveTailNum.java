package specialUtil;

import java.io.File;
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

public class RemoveTailNum {
	
	private static IConfigManager configManager;
	
	public static void main(String[] args) {
		String originalAndroidPath = "";
		String targetPath = "";
		String originalVideoPartsPath = "";
		configManager = ConfigManager.getConfigManager(Bili2PCConverter.class.getResource("ADsVideoRm.properties"));
//			p.load(new FileInputStream(this.getClass().getResource("./Bili2PCConverter.properties").getPath()));
		targetPath = configManager.getString("targetPath");
		
		// properties file doesn't support Chinese
		targetPath = "E:\\Downloads\\a";

		// print path result.
		ComLogUtil.info("originalAndroidPath: " + originalAndroidPath
				+ ", originalVideoPartsPath: " + originalVideoPartsPath
				+ ", targetPath: " + targetPath
				);

		try {
			restore(new File(targetPath));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

	}
	
	public static void restore(File targetPath) throws Exception {
		doOneLevel(targetPath);
		
	}
	

	private static boolean isRealAction = false;

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
        	ComLogUtil.error("will remove this empty dir:" + dir);
        	if(isRealAction) ComFileUtil.delFileAndFolder(dir);
        	return;
        } else {
        	ComLogUtil.info("won't remove this none-empty dir:" + dir);
        }
		int length = files.length;
		List<File> folders = new ArrayList<File>();
		List<File> nonFolders = new ArrayList<File>();
		for(int i = 0; i < length; i++) {
			File file = files[i];
			String fileFullPath = file.getPath();
			String nameOnly = file.getName();
//			ComLogUtil.info("file1:" + file.getAbsolutePath()); // file1:F:\Downloads\toMove\Mini传媒\mini01
//			ComLogUtil.info("file2:" + file.getName()); // file2:mini01
//			ComLogUtil.info("file3:" + file.getPath()); // file3:F:\Downloads\toMove\Mini传媒\mini01
			if(file.isDirectory()) {
				folders.add(file);
			} else {
				File targetFile = new FileName(file).setFileName("tx-" + i).toFile();
				file.renameTo(targetFile);
			}
		}
		
		int nonFoldersSize = nonFolders.size();
		int foldersSize = folders.size();
		
		for(int i = 0; i < foldersSize; i++) {
			doOneLevel(folders.get(i));
		}
	}
	
	private static String findOriginalName(File file) throws Exception {
		String strings = ComFileUtil.readFile2String(file, "gb2312");
		String[] lines = strings.split("\n\r?");
		for(int i = 0; i < lines.length; i++) {
			String line = lines[i];
			if(line.startsWith("果冻传媒 ")) {
				return line.replaceFirst("果冻传媒\\s+", "");
			}
		}
		throw new Exception("faile to find originalName!");
	}
}

