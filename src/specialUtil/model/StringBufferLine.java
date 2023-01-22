package specialUtil.model;

public class StringBufferLine {
	private StringBuffer sb;

	public StringBufferLine(String line) {
		super();
		this.sb = new StringBuffer(line).append("\n");
	}
	
	public StringBufferLine() {
		super();
		this.sb = new StringBuffer("");
	}
	
	public StringBufferLine append(Object line) {
		this.sb.append(line).append("\n");
		return this;
	}
	

	public String toString() {
		return this.sb.toString();
	}
}
