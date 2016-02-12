package com.amplifino.nestor.dot;


public class DigraphBuilder {
	private final StringBuilder builder;
	
	private DigraphBuilder(String name) {
		this.builder = new StringBuilder("digraph ");
		quote(name);
		builder.append(" {\n");
	}
	
	public static DigraphBuilder name(String name) {
		return new DigraphBuilder(name);
	}
	
	public DigraphBuilder append(String string) {
		builder.append(string);
		return this;
	}
	
	public DigraphBuilder println(String string) {
		builder.append(string);
		builder.append("\n");
		return this;
	}
	
	public DigraphBuilder quote(String string) {
		builder.append("\"");
		builder.append(string);
		builder.append("\"");
		return this;
	}
	
	public DigraphBuilder addDepends() {
		builder.append(" -> ");
		return this;
	}
	
	public DigraphBuilder semiColumn() {
		builder.append(";");
		return this;
	}
	
	public DigraphBuilder newLine() {
		builder.append("\n");
		return this;
	}
	
	public DigraphBuilder endGraph() {
		builder.append("\n}");
		return this;
	}
	
	public DigraphBuilder tab(int n) {
		for (int i = 0 ; i < n ; i++) {
			builder.append("\t");
		}
		return this;
	}
	
	public DigraphBuilder tab() {
		return tab(1);
	}
	
	public DigraphBuilder openCurly() {
		builder.append("{");
		return this;
	}

	public DigraphBuilder closeCurly() {
		builder.append("}");
		return this;
	}
	
	public String build() {
		return builder.toString();
	}
	
	public NodeBuilder node(String name) {
		return new NodeBuilder(name);
	}
	
	public class NodeBuilder {
		private final String name;
		private String label;
		private Shape shape;
		private String url;
		
		private NodeBuilder(String name) {
			this.name = name;			
		}
		
		public NodeBuilder label(String label) {
			this.label = label;
			return this;
		}
		
		public NodeBuilder shape(Shape shape) {
			this.shape = shape;
			return this;
		}
		
		public NodeBuilder url(String url) {
			this.url = url;
			return this;
		}
		
		public void add() {
			quote(name);
			String separator = "";
			if (hasAttributes()) {
				append("[");
				if (shape != null) {
					append("shape=");
					append(shape.name().toLowerCase());
					separator= ",";
				}
				if (label != null) {
					append(separator);
					append("label=");
					quote(label);
					separator= ",";
				}
				if (url != null) {
					append(separator);
					append("URL=");
					quote(url);
					separator= ",";
				}
				append("]");
			}
			newLine();
		}
		
		private boolean hasAttributes() { 
			return label != null || shape != null;
		}
	}

	public enum Shape {
		BOX,
		POLYGON,
		ELLIPSE,
		OVAL,
		CIRCE;
	}
}
