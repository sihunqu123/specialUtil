package specialUtil;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.commonUtil.ComFileUtil;
import util.commonUtil.ComRegexUtil;
import util.commonUtil.ConfigManager;
import util.commonUtil.interfaces.IConfigManager;
import util.commonUtil.model.FileInfo;
import util.commonUtil.model.FileName;

/**
 *
 */
public class ComRenameUtil{

	private static IConfigManager configManager;

	public static void main(String args[]) {
		String originalAndroidPath = "";
		String targetPath = "";
		try {
			configManager = ConfigManager.getConfigManager(Bili2PCConverter.class.getResource("Bili2PCConverter.properties"));
//			p.load(new FileInputStream(this.getClass().getResource("./Bili2PCConverter.properties").getPath()));
			originalAndroidPath = configManager.getString("originalAndroidPath");
			System.out.println(originalAndroidPath);

			
//			removeTokyoHotPrefix(new File("F:\\Downloads\\test\\Tokyo Hot jup0022  -.mp4"));
//			replaceFlvBlv(originalAndroidPath);
//			replaceTokyo(originalAndroidPath);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
	}

	public static void replaceFlvBlv(String originalAndroidPath) {
		File[] files = new File(originalAndroidPath).listFiles();
		int length = files.length;
		for(int i = 0; i < length; i++) {
			File newFile = new File(ComRegexUtil.replaceByRegex(files[i].getPath(), "\\.flv\\.blv", ".blv"));
//			File newFile = new File(ComRegexUtil.replaceByRegex(files[i].getPath(), "-80\\.blv", ".blv"));
			System.out.println("rename " + files[i] + " to " + newFile + ":"
					+ files[i].renameTo(newFile)
					);
		}
	}

	public static void replace80(String originalAndroidPath) {
		File[] files = new File(originalAndroidPath).listFiles();
		int length = files.length;
		for(int i = 0; i < length; i++) {
			File newFile = new File(ComRegexUtil.replaceByRegex(files[i].getPath(), "-80\\.blv", ".blv"));
			System.out.println("rename " + files[i] + " to " + newFile + ":" + files[i].renameTo(newFile));
		}
	}

	public static void replaceTokyo(String originalAndroidPath) {
		File dir = new File(originalAndroidPath);
		File[] files = dir.listFiles();
		int length = files.length;
		for(int i = 0; i < length; i++) {
			if(!files[i].isDirectory()) {
				continue;
			}
			//File newFile = new File(ComRegexUtil.replaceLiterally(files[i].getPath(), "Tokyo Hot ", ""));
//			File newFile = new File(ComRegexUtil.replaceAllByRegex(files[i].getPath(), "Tokyo(\\s|\\.\\-)Hot", ""));

			String fileName = ComFileUtil.getFileName(files[i], true);
			System.out.println("fileName:" + fileName);
			Matcher matcher = Pattern.compile("东京热 ", Pattern.CASE_INSENSITIVE).matcher(fileName);
			if (matcher.find()) { // 最多只匹配替换一次
				fileName = matcher.replaceAll(Matcher.quoteReplacement(""));
				File newFile = new File(dir, fileName);
				System.out.println("rename " + files[i] + " to " + newFile + ":" + files[i].renameTo(newFile));
			}

		}
	}
	
	public static File findAndAddNumberSuffix(File file) {
//		int index = 1;
		while(file.exists()
//				&& index++ < 999
				) {
			file = new FileName(file).increaseTailNum().toFile();
		}
		return file;
	}
	
	public static File replaceFileExtention(File file, String targetExtion) {
		FileInfo fileInfo = ComFileUtil.getFileInfo(file);
		fileInfo.setFileExt(targetExtion);
		File targetFile = fileInfo.toFile();
		file.renameTo(targetFile);
		return targetFile;
	}
	
}