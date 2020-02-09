package me.common.json;

public class Token {

	private String value;

	private TokenType type;

	public Token(TokenType type, String value) {
		this.type = type;
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public TokenType getType() {
		return type;
	}

	public void setType(TokenType type) {
		this.type = type;
	}
	
	public enum TokenType {

	    START_OBJ, END_OBJ, START_ARRAY, END_ARRAY, NULL, NUMBER, STRING, BOOLEAN, COLON, COMMA, END_DOC

	}
}