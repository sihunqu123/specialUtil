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
 * In a given folder(e.g. 'f:\\pFolder\\'), if fond redundant duplicate path, then move those up until there is no redundant folder hierarchy.
 * e.g.
 *  + "f:\\pFolder\\dupPath\\dupPath\\file1.txt"
 *  + "f:\\pFolder\\dupPath\\dupPath\\file2.txt"
 *  + "f:\\pFolder\\dupPath\\dupPath\\dupPath\\file3.txt"
 * will be renamed as:
 *  + "f:\\pFolder\\dupPath\\file1.txt"
 *  + "f:\\pFolder\\dupPath\\file2.txt"
 *  + "f:\\pFolder\\dupPath\\file3.txt"
 * @author sihun
 *
 */
public class ReduceRedundent {

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
			doOneLevel(new File(FolderToHandle));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

	}
	
	private static void doOneLevel(File dir) throws Exception {
		if (dir == null || !dir.exists() || !dir.isDirectory()) {
			ComLogUtil.error("doOneLevel: invalid dir: " + dir);
			return;
		}

		File[] children = dir.listFiles();
		if (children == null || children.length == 0) {
			// empty directory: nothing to do
			return;
		}

		// First, recurse into subdirectories so inner redundancies are fixed before we handle this level
		for (File child : children) {
			if (child.isDirectory()) {
				doOneLevel(child);
			}
		}

		// After recursion, re-evaluate children
		children = dir.listFiles();
		if (children == null || children.length == 0) {
			return;
		}

		// If directory contains exactly one directory and no files, and names are identical -> collapse
		List<File> subDirs = new ArrayList<File>();
		int fileCount = 0;
		for (File f : children) {
			if (f.isDirectory()) subDirs.add(f);
			else fileCount++;
		}

		if (subDirs.size() == 1 && fileCount == 0) {
			File onlyChild = subDirs.get(0);
			String parentName = dir.getName();
			String childName = onlyChild.getName();
			if (parentName.equalsIgnoreCase(childName)) {
				// Move all files/subfolders from onlyChild up to dir, then delete onlyChild
				File[] inner = onlyChild.listFiles();
				if (inner != null) {
					for (File f : inner) {
						File dest = new File(dir, f.getName());
						// if dest exists, try to make a unique name
						if (dest.exists()) {
							String base = f.getName();
							int idx = 1;
							while (dest.exists()) {
								dest = new File(dir, base + "(" + idx + ")");
								idx++;
							}
						}
						ComLogUtil.info("Collapse: move " + f.getAbsolutePath() + " -> " + dest.getAbsolutePath());
						if (!isPrintOnly) {
							ComFileUtil.doRename(true, f, dest, "collapseMove");
						}
					}
				}
				// delete the now-empty child folder
				if (!isPrintOnly) {
					ComFileUtil.delFileAndFolder(onlyChild);
				}
				return;
			}
		}

		// Additionally, try to detect deeper redundant chains like a\a\a and collapse iteratively by recursion above
		// No other action for mixed content dirs

	}
}

