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
 * customize the savedata of `Shin Sangokumusou 4 Special`
 */
public class SavedaveHelper{
	private static final String GREPMARK = "SavedaveHelper";
	private static final String savedataFilePath = "C:\\Users\\tiantc\\Documents\\KOEI\\Shin Sangokumusou 4 Special\\Savedata\\save.dat";
	private static RandomAccessFile fh = null;
	private static final int effectMaxVal = 20;
	private static final int defaultEffectVal = 20;
	
	final static int[][] arrowPerson  = new int [][] {
		{3, defaultEffectVal, 5, defaultEffectVal, 8, defaultEffectVal, 9, defaultEffectVal, 4, defaultEffectVal},
		{3, defaultEffectVal, 5, defaultEffectVal, 8, defaultEffectVal, 9, defaultEffectVal, 1, defaultEffectVal},
		{3, defaultEffectVal, 5, defaultEffectVal, 8, defaultEffectVal, 9, defaultEffectVal, 7, defaultEffectVal},
	};
	
	final static int[][] horsePerson  = new int [][] {
		{3, defaultEffectVal, 0, defaultEffectVal, 8, defaultEffectVal, 6, defaultEffectVal, 4, defaultEffectVal},
		{3, defaultEffectVal, 0, defaultEffectVal, 8, defaultEffectVal, 6, defaultEffectVal, 1, defaultEffectVal},
		{3, defaultEffectVal, 0, defaultEffectVal, 8, defaultEffectVal, 6, defaultEffectVal, 7, defaultEffectVal},
	};
		
	final static Map<Integer, int[][]> customizePerson = new HashMap<Integer, int[][]>();
	
	static {
//		customizePerson.put(20, arrowPerson);
//		customizePerson.put(21, arrowPerson);
//		
//		customizePerson.put(19, horsePerson);
//		customizePerson.put(40, horsePerson);
	}
	
	/** powerful one
	final static  int[][] weaponEffectArr = new int [][] {
		{3, defaultEffectVal, 0, defaultEffectVal, 8, defaultEffectVal, 9, defaultEffectVal, 4, defaultEffectVal},
		{3, defaultEffectVal, 0, defaultEffectVal, 8, defaultEffectVal, 9, defaultEffectVal, 1, defaultEffectVal},
		{3, defaultEffectVal, 0, defaultEffectVal, 8, defaultEffectVal, 9, defaultEffectVal, 7, defaultEffectVal},
	};
	*/
	
	final static  int[][] weaponEffectArr = new int [][] {
		{0, defaultEffectVal, 1, defaultEffectVal, 3, defaultEffectVal, 4, defaultEffectVal, 7, defaultEffectVal},
		{0, defaultEffectVal, 3, defaultEffectVal, 4, defaultEffectVal, 5, defaultEffectVal, 7, defaultEffectVal},
		{1, defaultEffectVal, 2, defaultEffectVal, 5, defaultEffectVal, 6, defaultEffectVal, 9, defaultEffectVal},
	};
	
	final static long weaponsCntPerPerson = 0x04;
	final static long personGap = 0x58;
	final static long lightWeapon = 0x00;
	final static long effect1_code_offset = 0x04;
	final static long effect2_code_offset = 0x06;
	final static long effect3_code_offset = 0x08;
	final static long effect4_code_offset = 0x0a;
	final static long effect5_code_offset = 0x0c;
	
	private static final void writeToAddr(long addr, long valueToWrite) throws IOException {
		fh.seek(addr);
		fh.write((int)valueToWrite);
	}
	
	final static long baseAddr = 0x0000100;
	final static long firstWeaponVal = 0x02; // use level 3 weapon, not level 4.
	
	public static void main(String args[]) throws IOException {
		
		int val = 0;
		try {
			fh = new RandomAccessFile(savedataFilePath, "rw");
			
//		    fh.seek(0x0000100);
//		    val = fh.read(); // 0x18
//		    ComLogUtil.info("old: " + val);
//		    fh.seek(0x0000102);
//		    val = fh.read(); // 0x18
//		    ComLogUtil.info("old2: " + val);
//		    fh.write(0xBB);
		    
			
//			tuneWeapon();
			tuneUI();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} finally {
			if(fh != null) {
				fh.close();
				fh = null;
			}
		}

		ComLogUtil.info("save updated!");
	}
	
	private static void tuneWeapon() throws IOException {
		for(int i = 0; i < 48; i++) { // 192 weapons /4 = 48 persons
			handle1Index(i);
		}
		
		// handle exceptional guys
		Set<Integer> keySet = customizePerson.keySet();
		Iterator<Integer> iterator = keySet.iterator();
		while(iterator.hasNext()) {
			Integer next = iterator.next();
			int[][] customizedWeaponEffect = customizePerson.get(next);
			handle1Index(next, customizedWeaponEffect);
		}
	}
	
	private static void tuneUI() throws IOException {
//		writeIntChar(0x000000d6, new int [] {0, 1});
		
		// max resolution for 4k version exe, which is 3840x2160
		writeIntChar(0x000000da, new int [] {0x17, 1});
		// max resolution for original exe, which is 1280x960
//		writeIntChar(0x000000da, new int [] {0x03, 1});
	}
	
	private static void handle1Index(int i) throws IOException {
		long currentAddr = baseAddr + personGap * i;
		long currentWeaponCode = firstWeaponVal + weaponsCntPerPerson * i;
		
		handle1PersonWeapon(currentAddr, currentWeaponCode);
	}
	
	private static void handle1Index(int i, int[][] customizedWeaponEffect) throws IOException {
		long currentAddr = baseAddr + personGap * i;
		long currentWeaponCode = firstWeaponVal + weaponsCntPerPerson * i;
		
		handle1PersonWeapon(currentAddr, currentWeaponCode, customizedWeaponEffect);
	}
	
	
	private static void handle1PersonWeapon(long currentAddr, long currentWeaponCode) throws IOException {
		handle1PersonWeapon(currentAddr, currentWeaponCode, weaponEffectArr);
	}
	
	private static void handle1PersonWeapon(long currentAddr, long currentWeaponCode, int[][] customizedWeaponEffect) throws IOException {

		// handle weapon effect
		for(int i = 0; i < customizedWeaponEffect.length; i++) {
			long weaponBaseAddr = currentAddr + i*16;
			// update weapon level to 3, instead 4
			writeToAddr(weaponBaseAddr, currentWeaponCode);
			// change this weapon to light weapon.
			writeToAddr(weaponBaseAddr + 2, lightWeapon);

			long weaponEffectAddr = weaponBaseAddr + 4;
			
			int[] currentWeaponEffect = customizedWeaponEffect[i];
			writeIntChar(weaponEffectAddr, currentWeaponEffect);
		}
	}
	
	private static void writeIntChar(long currentWeaponAddr, int[] intArr) throws IOException {
		long currentAddr = currentWeaponAddr;
		for(int i = 0; i < intArr.length; i++) {
			int currentVal = intArr[i];
			writeToAddr(currentAddr, currentVal);
			currentAddr++;
		}
	}


}