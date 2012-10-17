package compiler;

import java.lang.reflect.Field;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;

import antlrGenerated.EsperLexer;
import antlrGenerated.EsperParser;

public class EsperCompiler {
	
	//Used when printing the syntax tree
	private static String depth = "";
	
	public boolean lexerSuccess = false;
	public int lexerErrors = 0;
	public boolean parserSuccess = false;
	public int parserErrors = 0;

	// http://www.antlr.org/wiki/pages/viewpage.action?pageId=789
	public EsperCompiler(String sourceCode) {

		//Strip whitespace, tabs - both are irrelevant
		sourceCode = sourceCode.replace("\n","").replace("\r", "").replace("\t", "").replace(" ", "");
		
		EsperLexer lexer = new EsperLexer(new ANTLRStringStream(sourceCode));
		lexerSuccess = (lexerErrors = lexer.getNumberOfSyntaxErrors()) <= 0;
		EsperParser parser = new EsperParser(new CommonTokenStream(lexer));
		parserSuccess = (parserErrors = parser.getNumberOfSyntaxErrors()) <= 0;

		EsperParser.program_return ret;

		try {
			ret = parser.program();
		} catch (RecognitionException e) {
			e.printStackTrace();
			return;
		}

		// Acquire parse result
		CommonTree ast = (CommonTree) ret.getTree();

		System.out.println("Lexer output: ");
		Token token;
		lexer = new EsperLexer(new ANTLRStringStream(sourceCode));
		while ((token = lexer.nextToken()).getType() != -1) {
			System.out.println("Token: " + token.getText() + " | " + this.getTokenName(token.getType()));
		}
		
		System.out.println("Parser output: ");
		printTree(ast);
	}
	
	//Uses reflection to get the token names from their types
	private static String getTokenName(int tokenType) {
		//Get all the fields of the lexical analyser - will be the token type variables
		Field[] fields = EsperLexer.class.getFields();
		
		//Iterate through the fields
		for (Field field : fields) {
			if (field.getType() == int.class) {
				try {
					//If the field matches the token type then that field is the token
					if (field.getInt(null) == tokenType) {
						return field.getName();
					}
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		
		return "UNKNOWN TOKEN";
	}

	private void printTree(CommonTree ast) {

		depth += ">";
		try {
			System.out.println(depth + " " + ast.getText() + " | " + getTokenName(ast.getToken().getType()));
		} catch (Exception ex) {
		}
			
		if (ast.getChildren() != null) {
			for (Object child : ast.getChildren()) {
				printTree((CommonTree) child);
			}
		}

		depth = depth.substring(depth.length() - 1);
	}

}