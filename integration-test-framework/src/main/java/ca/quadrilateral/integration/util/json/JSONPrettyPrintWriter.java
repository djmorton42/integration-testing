package ca.quadrilateral.integration.util.json;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.commons.lang3.StringUtils;

public class JSONPrettyPrintWriter extends Writer {
    private static final int INDENT_SPACES = 4;
    
    private final Writer wrappedWriter;
    
    private int indentationLevel = 0;
    private boolean isInString = false;
    private Character previousChar = null;
    private boolean hasWrittenFirstChar = false;
    
    private char[] newlineAndIndent = new char[] { '[', '{' };
    private char[] newline = new char[] { ',' };
    private char[] deindent = new char[] { ']', '}' };
    
    public JSONPrettyPrintWriter(final Writer wrappedWriter) {
        this.wrappedWriter = wrappedWriter;
    }   
    
    public static void main(String[] args) throws Exception {
        final StringWriter stringWriter = new StringWriter();
        
        final JSONPrettyPrintWriter writer = new JSONPrettyPrintWriter(stringWriter);
        
//        final String jsonText = "{\"id\": \"0001\",\"type\": \"donut\",\"name\": \"Cake\",\"ppu\": 0.55,\"batters\":{\"batter\":[{ \"id\": \"1001\", \"type\": \"Regular\" },{ \"id\": \"1002\", \"type\": \"Chocolate\" },{ \"id\": \"1003\", \"type\": \"Blueberry\" },   { \"id\": \"1004\", \"type\": \"Devil's Food\" }]},\"topping\":[{ \"id\": \"5001\", \"type\": \"None\" },{ \"id\": \"5002\", \"type\": \"Glazed\" },{ \"id\": \"5005\", \"type\": \"Sugar\" },{ \"id\": \"5007\", \"type\": \"Powdered Sugar\" },{ \"id\": \"5006\", \"type\": \"Chocolate \\\"with\\\" Sprinkles\" },{ \"id\": \"5003\", \"type\": \"Chocolate\" },{ \"id\": \"5004\", \"type\": \"Maple\" }]}";
//        final String jsonText = "[\"Item 1\", \"Item 2\", \"Item 3\"]";
        //final String jsonText = "[]";
        final String jsonText = "[\"Item 1\", \"Item {[2]}\", \"Item 3\"]";
        
        writer.write(jsonText);
        
        System.out.println(stringWriter.toString());
        writer.close();
        
    }
    
    
    @Override
    public synchronized void write(char[] cbuf, int off, int len) throws IOException {
        
        for(int i = off; i < off + len; i++) {
            final char currentChar = cbuf[i];
            
            
            if (isInString) {
                if (currentChar == '"' && previousChar.charValue() != '\\') {
                    isInString = false;
                }
                wrappedWriter.write(currentChar);
            } else {
                if (currentChar == '"') {
                    isInString = true;
                    wrappedWriter.write(currentChar);
                } else if (currentChar == ' ' || currentChar == '\t') {
                    continue;
                } else if (isCharNewlineAndIndent(currentChar) && !hasWrittenFirstChar) {
                    indentationLevel++;
                    wrappedWriter.write(currentChar);
                    wrappedWriter.write('\n');
                    wrappedWriter.write(StringUtils.repeat(' ', indentationLevel * INDENT_SPACES));
                    hasWrittenFirstChar = true;
                } else if (isCharNewlineAndIndent(currentChar) && hasWrittenFirstChar) {
                    indentationLevel++;       
                    wrappedWriter.write(currentChar);
                    wrappedWriter.write('\n');
                    wrappedWriter.write(StringUtils.repeat(' ', indentationLevel * INDENT_SPACES));
                } else if (isCharNewline(currentChar)) {
                    wrappedWriter.write(currentChar);
                    wrappedWriter.write('\n');
                    wrappedWriter.write(StringUtils.repeat(' ', indentationLevel * INDENT_SPACES));
                } else if (isCharDeindent(currentChar)) {
                    indentationLevel--;
                    wrappedWriter.write('\n');
                    wrappedWriter.write(StringUtils.repeat(' ', indentationLevel * INDENT_SPACES));
                    wrappedWriter.write(currentChar);
                } else if (currentChar == ':') {
                    wrappedWriter.write(currentChar);
                    wrappedWriter.write(' ');
                } else {
                    wrappedWriter.write(currentChar);
                }
            }
            
            previousChar = currentChar;
        }
    }
    
    private boolean isCharNewlineAndIndent(final char character) {
        for (final char c : newlineAndIndent) {
            if (character == c) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isCharDeindent(final char character) {
        for (final char c : deindent) {
            if (character == c) {
                return true;
            }   
        }
        return false;
    }
    
    private boolean isCharNewline(final char character) {
        for (final char c : newline) {
            if (character == c) {
                return true;
            }   
        }
        return false;        
    }

    @Override
    public void flush() throws IOException {
        wrappedWriter.flush();
    }

    @Override
    public void close() throws IOException {
        wrappedWriter.close();
    }

}
