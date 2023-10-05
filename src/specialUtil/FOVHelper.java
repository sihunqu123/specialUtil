package specialUtil;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
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
 * Customize the FOV of `Shin Sangokumusou 4 Special`
 */
public class FOVHelper{
	private static final String GREPMARK = "FOVHelper"; 
	private final static String[] launchFilePaths  = new String [] {
		"G:\\game\\354m\\ShinSangokumusou4_eternal\\SS4TC.exe",
		"G:\\game\\354m\\ShinSangokumusou4_eternal\\SS4TC_Eternal.exe",
		"G:\\game\\354m\\ShinSangokumusou4_eternal\\SS4TC_Maximum.exe",
		"G:\\game\\354m\\ShinSangokumusou4_eternal\\SS4TC_Supreme.exe",
		
//		"G:\\game\\354m\\2023年1月8日更新354C永恒版\\hard\\SS4TC.exe",
//		"G:\\game\\354m\\2023年1月8日更新354C永恒版\\hard\\SS4TC_Eternal.exe",
//		"G:\\game\\354m\\2023年1月8日更新354C永恒版\\hard\\SS4TC_Maximum.exe",
//		"G:\\game\\354m\\2023年1月8日更新354C永恒版\\hard\\SS4TC_Supreme.exe",
//
//		"G:\\game\\354m\\2023年1月8日更新354C永恒版\\easy\\SS4TC.exe",
//		"G:\\game\\354m\\2023年1月8日更新354C永恒版\\easy\\SS4TC_Eternal.exe",
//		"G:\\game\\354m\\2023年1月8日更新354C永恒版\\easy\\SS4TC_Maximum.exe",
//		"G:\\game\\354m\\2023年1月8日更新354C永恒版\\easy\\SS4TC_Supreme.exe",
//		
//		"G:\\game\\354m\\2023年1月8日更新354C永恒版\\normal\\SS4TC.exe",
//		"G:\\game\\354m\\2023年1月8日更新354C永恒版\\normal\\SS4TC_Eternal.exe",
//		"G:\\game\\354m\\2023年1月8日更新354C永恒版\\normal\\SS4TC_Maximum.exe",
//		"G:\\game\\354m\\2023年1月8日更新354C永恒版\\normal\\SS4TC_Supreme.exe",
//		
//		"G:\\game\\354m\\2023年1月8日更新354C永恒版\\mongBinZhuan\\hard\\SS4TC.exe",
//		"G:\\game\\354m\\2023年1月8日更新354C永恒版\\mongBinZhuan\\hard\\SS4TC_Eternal.exe",
//		"G:\\game\\354m\\2023年1月8日更新354C永恒版\\mongBinZhuan\\hard\\SS4TC_Maximum.exe",
//		"G:\\game\\354m\\2023年1月8日更新354C永恒版\\mongBinZhuan\\hard\\SS4TC_Supreme.exe",
//
//		"G:\\game\\354m\\2023年1月8日更新354C永恒版\\mongBinZhuan\\easy\\SS4TC.exe",
//		"G:\\game\\354m\\2023年1月8日更新354C永恒版\\mongBinZhuan\\easy\\SS4TC_Eternal.exe",
//		"G:\\game\\354m\\2023年1月8日更新354C永恒版\\mongBinZhuan\\easy\\SS4TC_Maximum.exe",
//		"G:\\game\\354m\\2023年1月8日更新354C永恒版\\mongBinZhuan\\easy\\SS4TC_Supreme.exe",
//		
//		"G:\\game\\354m\\2023年1月8日更新354C永恒版\\mongBinZhuan\\normal\\SS4TC.exe",
//		"G:\\game\\354m\\2023年1月8日更新354C永恒版\\mongBinZhuan\\normal\\SS4TC_Eternal.exe",
//		"G:\\game\\354m\\2023年1月8日更新354C永恒版\\mongBinZhuan\\normal\\SS4TC_Maximum.exe",
//		"G:\\game\\354m\\2023年1月8日更新354C永恒版\\mongBinZhuan\\normal\\SS4TC_Supreme.exe",
//		
//		
//		
//		"G:\\game\\354m\\ShinSangokumusou4_mongBinZhuan\\SS4TC.exe",
//		
//		"G:\\game\\354m\\ShinSangokumusou4_PLUS_2011\\SS4_Plus.exe",
//		"G:\\game\\354m\\ShinSangokumusou4_PLUS_2011\\SS4_Super.exe",
//		"G:\\game\\354m\\ShinSangokumusou4_PLUS_2011\\SS4TC.exe",
//		
//		
//		"G:\\game\\354m\\真三国无双4S_威力加强版_iorilucifers\\真三国无双4PK.exe",
//		
//		"G:\\game\\354m\\三国无双4幻侠传\\幻侠传全游戏版\\Sangokumusou4\\幻侠传.exe",
//		"G:\\game\\354m\\三国无双4幻侠传\\武侠传全游戏版\\Sangokumusou4\\武侠传.exe",
		
		
		
		"G:\\game\\354m\\ShinSangokumusou4_original\\SS4TC.exe"
	};
//	private static final String launchFilePath = "G:\\game\\354m\\ShinSangokumusou4_original\\SS4TC.exe";
	private static RandomAccessFile fh = null;

	private static final void writeToAddr(long addr, long valueToWrite) throws IOException {
		fh.seek(addr);
		fh.write((int)valueToWrite);
//		fh.write((long)valueToWrite);
	}
	
	// this works for 幻侠传.exe 武侠传.exe 真三国无双4PK.exe and SS4TC.exe, since they are NOT using the enbseries.ini
	// while for SS4TC_Supreme.exe, this doesn't work, because this has been override by the enbseries.ini
	// this is for launch file. And the addr for runtime is `00649424`, which should be set to value `3CFFFA35`
	final static long baseAddr = 0x00249426;
	// from 0x8e3c to 0xc03c
	final static long val = 0xFE; // use level 3 weapon, not level 4.
	
	public static void main(String args[]) throws Exception {
//		int val = 0;
//		ComLogUtil.info((int)val);
		
		for(int i = 0; i < launchFilePaths.length; i++) {
			String launchFilePath = launchFilePaths[i];
			updateOneFile(launchFilePath);
		}
		
		ComLogUtil.info("save updated!");
	}
	
	private static void updateOneFile(String filePath) throws Exception {
		try {
			fh = new RandomAccessFile(filePath, "rw");
			writeToAddr(baseAddr, val);
		} catch (Exception e) {
			throw e;
		} finally {
			if(fh != null) {
				fh.close();
				fh = null;
			}
		}

	}
	
}