package specialUtil.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import util.commonUtil.ComLogUtil;
import util.commonUtil.ComRegexUtil;
import util.commonUtil.ComStrUtil;

public class VTTParagraph {
	private String header;
	private List<String> lines;
	
	private String begin;
	private String end;
	
	private String line;
	private String position;
	private String align;
	private String size;

	
	public VTTParagraph(String header, List<String> lines) {
		super();
		this.header = header;
		this.lines = lines;
		
//		this.begin = ComRegexUtil.getMatchedString(header, "^\\d{2}:\\d{2}:\\d{2}\\.\\d{3}(?>=\\s+-->\\s+.*)");
		this.begin = ComRegexUtil.getMatchedString(header, "\\d{2}:\\d{2}:\\d{2}\\.\\d{3}(?=\\s+-->\\s+)");
		this.end = ComRegexUtil.getMatchedString(header, "(?<=\\s+-->\\s{1,9})\\d{2}:\\d{2}:\\d{2}.\\d{3}(?=\\s+)");
		
		
		this.line = getField(header, "line", "0");
		this.align = getField(header, "align", "start");
		this.position = getField(header, "postion", "0%");
		this.size = getField(header, "size", "100%");
	}
	
	private String getField(String header, String fieldName, String defaultVal) {
		String val = ComRegexUtil.getMatchedString(header, "(?<= " + fieldName+ "):[^ ]+");
		return ComStrUtil.isBlankOrNull(val) ? defaultVal : val;
	}
	
	public void addline(String line) {
		this.lines.add(line);
	}
	
	public void addlines(String[] lines) {
		this.lines.addAll(Arrays.asList(lines));
	}
	
	public void addlines(List<String> lines) {
		this.lines.addAll(lines);
	}
	
	public List<String> getLines() {
		return lines;
	}
	

	public String getBegin() {
		return begin;
	}

	public void setBegin(String begin) {
		this.begin = begin;
	}

	public String getEnd() {
		return end;
	}

	public void setEnd(String end) {
		this.end = end;
	}

	public String getLine() {
		return line;
	}

	public void setLine(String line) {
		this.line = line;
	}

	public String getAlign() {
		return align;
	}

	public void setAlign(String align) {
		this.align = align;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	@Override
	public String toString() {
		int size = lines.size();
//		StringBufferLine linesStr = new StringBufferLine();
		StringBuffer linesStr = new StringBuffer();
		for(int i = 0; i < size; i++) {
			String line = lines.get(i);
			if(!ComStrUtil.isBlankOrNull(line)) {
				linesStr.append(line).append(" ");
			}
		}
		StringBuffer retVal = new StringBuffer(this.begin).append(" --> ").append(this.end)
				.append(" line:").append(this.line)
				.append(" position:").append(this.position)
				.append(" align:").append(this.align)
				.append(" size:").append(this.size)
//				.append("\n")
				.append("\n")
				.append(linesStr)
//				.append("\n")
				.append("\n");
		return retVal.toString();
	}
	
	public String toStringDebug() {
		StringBufferLine retVal = new StringBufferLine("header" + this.header)
			.append(", begin:" + this.begin)
			.append(", end:" + this.end)
			.append(", line:" + this.line)
			.append(", position:" + this.position)
			.append(", align:" + this.align)
			.append(", size:" + this.size)
			.append(", lines:" + this.lines);
		return retVal.toString();
	}
	
	public static void main(String[] args) {
		VTTParagraph vttParagraph = new VTTParagraph("00:19:40.980 --> 00:19:40.990 align:start position:0%", new ArrayList<String>());
		ComLogUtil.info(vttParagraph.toStringDebug());
		ComLogUtil.info(vttParagraph);
	}
}
