package util.specialUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import util.commonUtil.ComFileUtil;
import util.commonUtil.ComLogUtil;


public class EncodeTest {

	private static final String CNSENCODING = "Shift_JIS";

	private static final String FILEEOL = "\n";

    	public static void main (String[] args) throws Exception {
    		StringBuilder resStr = new StringBuilder();
    		File file =  new File("E:\\2B-Kyo-2-3.cns");
    		File outputFile =  new File("E:\\zzzz.cns");
    		String encoding = "";
    		FileInputStream fileInputStream = new FileInputStream(file);
    		InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "shift_jis");
    		BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
    		String readLine = null;
    		while((readLine = bufferedReader.readLine()) != null) {
				resStr.append(readLine).append("\n");
			}
    		System.out.println("res:" + resStr);
    		byte[] byteBuf = null;
            InputStream fis = null;
            OutputStream fos = null;
            OutputStreamWriter osw = null;
            PrintWriter pw = null;
            try {
        		fis = new FileInputStream(file);
        		fos = new FileOutputStream(outputFile);
        		FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
        		byteBuf = new byte[1024];
	            int readByteNumber = -1;
	            while((readByteNumber = fis.read(byteBuf)) > -1) {
	            	// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	            	// 在这里 osw 和 pw 都不能直接把字节数组写出去.
	            	// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

	            	fos.write(byteBuf, 0, readByteNumber);
	            }
	            fos.flush();
            } finally {
                if (fis != null) fis.close();
                if (fos != null) fos.close();
                byteBuf = null;
            }


    		System.out.println(encoding);
//    		if(true) return;

    		StringBuilder res = new StringBuilder();
    		String oriString = ComFileUtil.readFile2String(file, encoding);
    		String[] oriArr = oriString.split("(\\v|\\r|\\n){1}");
    		String filePath = file.getPath();
    		ComLogUtil.info("------------begin file:" + filePath + " write.------------");
    		int line = 0;
    		for(String str : oriArr) {
    			line++;
    			res.append(str).append(FILEEOL);
//    			ComLogUtil.info("str:" + str);
//    			if(true) continue;
    			System.out.println(str);
			ComFileUtil.writeString2File(res.toString(), new File("e:\\zzz.cns"), encoding);
			ComLogUtil.info("------------done file:" + filePath + " wrote.------------");
    		}
    	}


}