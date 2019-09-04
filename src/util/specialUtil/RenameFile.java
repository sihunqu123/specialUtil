package util.specialUtil;

import java.io.File;

import util.commonUtil.ComRegexUtil;
import util.commonUtil.ComStrUtil;
import util.commonUtil.interfaces.IConfigManager;

/**
 *
 */
public class RenameFile {
	private static final String GREPMARK = "RenameFile";

	public static void main(String args[]) {


	}

	private static final String CONFIGNAME = "entry.json";

	private static final String CONFIG_INDEX_Name = "index.json";

	private static final String VIDEO_EXTENSION= ".blv";

	private static IConfigManager configManager;

	private static void renameFiles(File dir) {
		File[] files = dir.listFiles();
		for(int i = 0; i < files.length; i++) {
			String name = files[i].getName();
			if(ComStrUtil.isBlankOrNull(ComRegexUtil.getMatchedString(name, ""))) {

			}
		}
	}
}