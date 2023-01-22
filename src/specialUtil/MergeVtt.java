package specialUtil;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import specialUtil.model.StringBufferLine;
import specialUtil.model.VTT;
import specialUtil.model.VTTParagraph;
import util.commonUtil.ComFileUtil;
import util.commonUtil.ComLogUtil;
import util.commonUtil.ComRegexUtil;
import util.commonUtil.ComStrUtil;
import util.commonUtil.ConfigManager;
import util.commonUtil.interfaces.IConfigManager;
import util.commonUtil.json.JSONObject;
import util.media.ComMediaUtil;

/**
 * Merge 2 vtt files into one. We use this to produce dual-language vtt subtitles.
 * @author sihun
 *
 */
public class MergeVtt {
	
	private static IConfigManager configManager;
	
	private static Boolean isPrintOnly = true;
	
	private static String languageMain = null;
	private static String language2nd = null;
	private static String languageDual = null;
	
	public static void main(String[] args) {
		String FolderToHandle = "";

		configManager = ConfigManager.getConfigManager(MergeVtt.class.getResource("common.properties"));
//			p.load(new FileInputStream(this.getClass().getResource("./Bili2PCConverter.properties").getPath()));
		FolderToHandle = configManager.getString("FolderToHandle").trim();
		isPrintOnly = "true".equalsIgnoreCase(configManager.getString("isPrintOnly"));
		
		languageMain = "D:\\Download\\srt\\ONE language, THREE accents - UK vs. USA vs. AUS English! (+ Free PDF) [66aG5P0kQpU].en.vtt";
		language2nd = "D:\\Download\\srt\\ONE language, THREE accents - UK vs. USA vs. AUS English! (+ Free PDF) [66aG5P0kQpU].zh-Hans.vtt";
		languageDual = "D:\\Download\\srt\\ONE language, THREE accents - UK vs. USA vs. AUS English! (+ Free PDF) [66aG5P0kQpU].vtt";
		
		
		
		// properties file doesn't support Chinese
//		targetPath = "F:\\Downloads\\ing\\test\\";

		// print path result.
		ComLogUtil.info("FolderToHandle: " + FolderToHandle
				+ ", isPrintOnly: " + isPrintOnly
				);

		try {
//			reduceUncessaryPath(new File(FolderToHandle));
			mergeVtt(new File(languageMain), new File(language2nd), new File(languageDual));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

	}
	
	
	public static void mergeVtt(File fileMain, File file2nd, File targetFile) throws Exception {
		VTT vttMain = new VTT(fileMain);
		VTT vtt2nd = new VTT(file2nd);
		vttMain.merge(vtt2nd);
		ComFileUtil.writeString2File(vttMain.toString(), targetFile, "UTF-8");
	}
	
	
	/**
	 * Then generate new vtt file with given vttPs
	 * @param vttPs
	 * @param targetFile
	 * @throws Exception 
	 */
	public static void vttsToFile(List<VTTParagraph> vttPs, File targetFile) throws Exception {
		StringBufferLine finalStr = new StringBufferLine("WEBVTT")
				.append("Kind: captions")
				.append("Language: zh-Hans,en");
		finalStr.append("");
		Iterator<VTTParagraph> it = vttPs.iterator();
		while(it.hasNext()) {
			VTTParagraph vttP = it.next();
			finalStr.append(vttP.toString());
		}
		ComFileUtil.writeString2File(finalStr.toString(), targetFile, "UTF-8");
		
	}
}

