package com.amplifino.nestor.dot;


/**
 * Utility class to help building a dot source
 *
 */
public class DigraphBuilder {
	private final StringBuilder builder;
	
	private DigraphBuilder(String name) {
		this.builder = new StringBuilder("digraph ");
		quote(name);
		builder.append(" {\n");
	}
	
	/**
	 * sets the digraph name
	 * @param name
	 * @return
	 */
	public static DigraphBuilder name(String name) {
		return new DigraphBuilder(name);
	}
	
	/**
	 * append the argument to the source
	 * @param string
	 * @return this
	 */
	public DigraphBuilder append(String string) {
		builder.append(string);
		return this;
	}
	
	/**
	 * append a line to the source
	 * @param string
	 * @return this
	 */
	public DigraphBuilder println(String string) {
		builder.append(string);
		builder.append("\n");
		return this;
	}
	
	/**
	 * append the argument to the source as a quoted string
	 * @param string
	 * @return this
	 */
	public DigraphBuilder quote(String string) {
		builder.append('"');
		for (int i = 0 ; i < string.length(); i++) {
			if (string.charAt(i) == '"') {
				builder.append('\\');
				builder.append('"');
			} else {
				builder.append(string.charAt(i));
			}
		}
		builder.append('"');
		return this;
	}
	
	/**
	 * adds an arrow ( -> ) to the source
	 * @return this
	 */
	public DigraphBuilder addDepends() {
		builder.append(" -> ");
		return this;
	}
	
	/**
	 * adds a ; character to the source
	 * @return this
	 */
	public DigraphBuilder semiColumn() {
		builder.append(";");
		return this;
	}
	
	/**
	 * adds a newline character (\n) to the source
	 * @return this
	 */
	public DigraphBuilder newLine() {
		builder.append("\n");
		return this;
	}
	
	
	/**
	 * terminates the source, by closing the digraph section
	 * @return this
	 */
	public DigraphBuilder endGraph() {
		builder.append("\n}");
		return this;
	}
	
	/**
	 * adds n tabs to the source
	 * @param n
	 * @return this
	 */
	public DigraphBuilder tab(int n) {
		for (int i = 0 ; i < n ; i++) {
			builder.append("\t");
		}
		return this;
	}
	
	/**
	 * adds a tab to the source
	 * @return this
	 */
	public DigraphBuilder tab() {
		return tab(1);
	}
	
	/**
	 * adds a { character to the source
	 * @return this
	 */
	public DigraphBuilder openCurly() {
		builder.append("{");
		return this;
	}

	/**
	 * adds a } character to the source
	 * @return this
	 */
	public DigraphBuilder closeCurly() {
		builder.append("}");
		return this;
	}
	
	/**
	 * returns the source
	 * @return 
	 */
	public String build() {
		return builder.toString();
	}
	
	/**
	 * returns a node builder for a node with the given name
	 * @param name
	 * @return
	 */
	public NodeBuilder node(String name) {
		return new NodeBuilder(name);
	}
	
	/**
	 * Helps in building a node statement
	 *
	 */
	public class NodeBuilder {
		private final String name;
		private String label;
		private Shape shape;
		private String url;
		
		private NodeBuilder(String name) {
			this.name = name;			
		}
		
		/**
		 * sets the node's label
		 * @param label
		 * @return this
		 */
		public NodeBuilder label(String label) {
			this.label = label;
			return this;
		}
		
		/**
		 * sets the node's shape
		 * @param shape
		 * @return this
		 */
		public NodeBuilder shape(Shape shape) {
			this.shape = shape;
			return this;
		}
		
		/**
		 * sets the node's hyperlink url
		 * @param url
		 * @return this
		 */
		public NodeBuilder url(String url) {
			this.url = url;
			return this;
		}
		
		/**
		 * adds the node to the source
		 */
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

	/**
	 * enumaration of supported shapes
	 *
	 */
	public enum Shape {
		BOX,
		POLYGON,
		ELLIPSE,
		OVAL,
		CIRCE;
	}
}
