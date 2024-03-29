package compiler;

import java.lang.reflect.Field;
import java.util.ArrayList;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;

import antlrGenerated.EsperLexer;
import antlrGenerated.EsperParser;

public class EsperCompiler {

	public boolean lexerSuccess = false;
	public int lexerErrors = 0;
	public boolean parserSuccess = false;
	public int parserErrors = 0;
	
	//Output from the stages of the compilation process
	private EsperLexer lexer;
	private ParseTree parseRoot;
	private ArrayList<VariableInformation> variableList;
	
	/**
	 * Performs the lexical analysis stage of the compiler
	 * @param sourceCode The code of the program
	 */
	private void lexicalAnalysis(String sourceCode) {
		lexer = new EsperLexer(new ANTLRStringStream(sourceCode));
		lexerSuccess = (lexerErrors = lexer.getNumberOfSyntaxErrors()) <= 0;
	}
	
	/**
	 * Performs the parsing stage of the compiler
	 * @param tokens The tokens from the lexical analysis
	 */
	private void parseProcess(CommonTokenStream tokens) {
		//ANTLR parse
		EsperParser parser = new EsperParser(tokens);		
		EsperParser.program_return ret;

		try {
			ret = parser.program();
		} catch (RecognitionException e) {
			System.out.println("Exception occurred in parser!");
			e.printStackTrace();
			return;
		}
		parserSuccess = (parserErrors = parser.getNumberOfSyntaxErrors()) <= 0;
		
		//Acquire parse result
		CommonTree ast = (CommonTree) ret.getTree();
		
		//Post parse
		EsperPostParser postParser = new EsperPostParser();
		parseRoot = postParser.getParseTree(ast);
		postParser.getVariableList();
		variableList = postParser.variableList;
	}
	

	// http://www.antlr.org/wiki/pages/viewpage.action?pageId=789
	public EsperCompiler(String sourceCode, boolean print) {

		//Strip whitespace, tabs - both are irrelevant
		sourceCode = sourceCode.replace("\n", "").replace("\r", "").replace("\t", "");

		//Lexical analysis
		lexicalAnalysis(sourceCode);

		//Parser
		parseProcess(new CommonTokenStream(lexer));

		if (print) {
			// Print Lexical Output
			System.out.println("Lexer output: ");
			Token token;
			EsperLexer tokensOut = new EsperLexer(new ANTLRStringStream(sourceCode));
			while ((token = tokensOut.nextToken()).getType() != -1) {
				// Ignore whitespace
				if (token.getType() != EsperLexer.WHITESPACE)
					System.out.println("Token: " + token.getText() + " | "
							+ getTokenName(token.getType()));
			}
			
			// Print parser output
			parseRoot.print(0);
		}
		
		EsperCGenerator cgen = new EsperCGenerator();
		System.out.println(cgen.generate(parseRoot,variableList));
	}

	// Uses reflection to get the token names from their types
	public static String getTokenName(int tokenType) {
		// Get all the fields of the lexical analyser - will be the token type
		// variables
		Field[] fields = EsperLexer.class.getFields();

		// Iterate through the fields
		for (Field field : fields) {
			if (field.getType() == int.class) {
				try {
					// If the field matches the token type then that field is
					// the token
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
	
	//Given the root node of an abstract syntax tree, prints it
	private void printTree(CommonTree ast, int indent) { 
		if (ast != null) {
			//The string to represent child nodes of the AST
			String indentString = "";
			for (int i = 0; i < indent; i++)
				indentString += "  ";
			
			//Recursively print this node's children
			for (int i = 0; i < ast.getChildCount(); i++) {
				System.out.println(indentString + ast.getChild(i).toString() + " [ " + getTokenName(ast.getChild(i).getType()) + " ] ");
				printTree((CommonTree)ast.getChild(i), indent+1);
			}
		}
	}


}