package specialUtil.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import util.commonUtil.ComCollectionUtil;
import util.commonUtil.ComFileUtil;
import util.commonUtil.ComLogUtil;
import util.commonUtil.ComRegexUtil;
import util.commonUtil.ComStrUtil;

public class VTT {
	private String[] lines;
	private List<String> lanuages;
	List<VTTParagraph> vttPs = new ArrayList<VTTParagraph>();
	
	
	public VTT(File vttFile) throws Exception {
		super();
		String[] lines = ComFileUtil.readFile2String(vttFile, "UTF-8").split("\n");
		this.lines = lines;
		// will cause UnsupportedOperationException, since the return value of Arrays.asList is read-only. 
//		this.lanuages = Arrays.asList(ComRegexUtil.getMatchedString(lines[2], "(?<=^Language: )[^ ]+").split(","));
		this.lanuages = new ArrayList<String>(Arrays.asList(ComRegexUtil.getMatchedString(lines[2], "(?<=^Language: )[^ ]+").split(",")));
		readAsParagraph();
//		"(?<=\\\\)【品色堂p4av.com】\\[?(?=[^\\\\]+$)",
	}

	public List<String> getLanuages() {
		return lanuages;
	}

	public void setLanuages(List<String> lanuages) {
		this.lanuages = lanuages;
	}

	public List<VTTParagraph> getVttPs() {
		return vttPs;
	}

	private void readAsParagraph() {
		List<VTTParagraph> vttPs = new ArrayList<VTTParagraph>();
		VTTParagraph vttP = null;
		
		for(int i = 4; i < this.lines.length - 1; i++) { // start from line 5 to skip the title 
			String mainString = this.lines[i];
			// 00:00:11.860 --> 00:00:15.239 align:start position:0%
			// if it's a timeline
			if(mainString.matches("^\\d{2}:\\d{2}:\\d{2}\\.\\d{3}\\s+-->\\s+\\d{2}:\\d{2}:\\d{2}.\\d{3} .*")) {
				if(vttP != null) {
					vttPs.add(vttP);
				}
				
				vttP = new VTTParagraph(mainString, new ArrayList<String>());
			} else {
				vttP.addline(mainString);
			}
		}
		
		if(vttP != null) { // add the final VTTParagraph
			vttPs.add(vttP);
		}
		
		this.vttPs = vttPs;
	}
	
	/**
	 * merge to vtt as one. (NOT concat)
	 * @param vtt
	 * @return
	 */
	public VTT merge(VTT vtt) {
		/**
		 * first merge lanuages
		 */
		List<String> lanuages2 = vtt.getLanuages();
//		lanuages2
		this.lanuages.addAll(lanuages2);
//		this.lanuages.addAll(Arrays.asList(lanuages2.toArray()));
//		ArrayList<String> newLanuageContainer = new ArrayList<String>();
//		newLanuageContainer.addAll(this.lanuages);
//		newLanuageContainer.addAll(lanuages2);
//		this.lanuages = newLanuageContainer;

		/**
		 * Then handles VTTParagraph
		 */
		int sizeMain = this.vttPs.size();
//		ComLogUtil.info("size: " + size + ", vttPs2nd size: " + vttPs2nd.size());
		for(int i = 0; i < sizeMain - 1; i++) {
			VTTParagraph vttParagraphMain = this.vttPs.get(i);
			vttParagraphMain.setLine("0");
			vttParagraphMain.setAlign("start");
			vttParagraphMain.setPosition("0%");
			vttParagraphMain.setSize("100%");
//			vttParagraphMain.addlines(vttParagraph2ndLines);
		}
		
		List<VTTParagraph> vttPs2nd = vtt.getVttPs();
		int size2nd = vttPs2nd.size();
		for(int i = 0; i < size2nd - 1; i++) {
			VTTParagraph vttParagraph2nd = vttPs2nd.get(i);
			vttParagraph2nd.setLine("-1");
			vttParagraph2nd.setAlign("start");
			vttParagraph2nd.setPosition("0%");
			vttParagraph2nd.setSize("100%");
			this.vttPs.add(vttParagraph2nd);
		}
		return this;
	}

	@Override
	public String toString() {
		StringBufferLine finalStr = new StringBufferLine("WEBVTT")
				.append("Kind: captions")
				.append("Language: " + ComCollectionUtil.arrayJoin(this.lanuages, ","));
//				.append("Language: en");
		finalStr.append("");
		int size = this.vttPs.size();
		for(int i = 0; i < size; i++) {
			VTTParagraph vttParagraph = this.vttPs.get(i);
			finalStr.append(vttParagraph);
		}
		return finalStr.toString();
	}
	
	public static void mergeVtt(File fileMain, File file2nd, File targetFile) throws Exception {
		VTT vttMain = new VTT(fileMain);
		VTT vtt2nd = new VTT(file2nd);
		vttMain.merge(vtt2nd);
		ComFileUtil.writeString2File(vttMain.toString(), targetFile, "UTF-8");
	}

	public static void main(String[] args) {
		try {
//			VTT vtt = new VTT(new File("D:\\Download\\srt\\ONE language, THREE accents - UK vs. USA vs. AUS English! (+ Free PDF) [66aG5P0kQpU].en.vtt"));
//			ComLogUtil.info("language: " + vtt.getLanuages());
			
		
//			ComLogUtil.info(new ArrayList<String>().addAll(Arrays.asList(new String[] { "1", "2"})));
			
			String languageMain = "D:\\Download\\srt\\en.vtt";
			String language2nd = "D:\\Download\\srt\\zh-cn.vtt";
			String languageDual = "D:\\Download\\srt\\ONE language, THREE accents - UK vs. USA vs. AUS English! (+ Free PDF) [66aG5P0kQpU].vtt";
			mergeVtt(new File(languageMain), new File(language2nd), new File(languageDual));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
