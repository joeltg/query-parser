package com.uspto.query.parser;

import static org.parboiled.support.ParseTreeUtils.printNodeTree;
import static org.parboiled.trees.GraphUtils.printTree;

import java.util.logging.Logger;

import org.parboiled.Parboiled;
import org.parboiled.errors.ErrorUtils;
import org.parboiled.parserunners.RecoveringParseRunner;
import org.parboiled.support.ParsingResult;
import org.parboiled.support.ToStringFormatter;
import org.parboiled.trees.GraphNode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.uspto.query.parser.SpanParsingRules.SpanNode;

public class SpanParseMain {
	

	  Object value = null;
	private static Logger logger = Logger.getLogger("parboilded");

	public String formatSearchText(String input, String operator) {

		SpanParsingRules parser = Parboiled.createParser(SpanParsingRules.class);
        String output = "";

		//CalcNode.setDefaultOp(operator.toUpperCase());

        ParsingResult<?> result = new RecoveringParseRunner(parser.InputLine()).run(input);
      

        if (!result.parseErrors.isEmpty()) {
        	logger.info(ErrorUtils.printParseError(result.parseErrors.get(0)));
            value = (Object)input;
        } else {
        	value = result.parseTreeRoot.getValue();
        	if (value instanceof GraphNode) {
                String printTree = printTree((GraphNode) value, new ToStringFormatter(null));
                
    			System.out.println("\nAbstract Syntax Tree:\n" +
                       printTree + '\n');
            } else {
               logger.info("\nParse Tree:\n" + printNodeTree(result) + '\n');
            }
        }
        if (value != null) {
            String str = value.toString();
            int ix = str.indexOf('|');
            if (ix >= 0) str = str.substring(ix + 2); // extract value part of AST node toString()
            output = str;
        }
        //logger.info("\nParsed Search Text is " + output + "\n");
        return output;

	}
	
	public Object parseQuery(String input, String operator) {
		SpanParsingRules parser = Parboiled.createParser(SpanParsingRules.class);
		
		//CalcNode.setDefaultOp(operator.toUpperCase());
		
		ParsingResult<?> result = new RecoveringParseRunner(parser.InputLine()).run(input);
       
        if(result.hasErrors()){
        	value = (Object)input;
        	return value;
        }else{
        	value = result.parseTreeRoot.getValue();
        	SpanNode spanNode = SpanNode.class.cast(value);
        	spanNode.setDefaultOp(operator);;
        	return spanNode.getValue();
        }
	}
	
	public static void main(String args[]) throws JsonProcessingException{
		SpanParseMain parseMain = new SpanParseMain();
		//System.out.println(parseMain.formatSearchText("CISCO OR VPN OR CLIENT OR cisco?","AND"));
		//System.out.println(parseMain.formatSearchText("cisco? OR VPN OR CLIENT OR ABC OR DEF","AND"));
		//System.out.println(parseMain.formatSearchText("VPN AND CLIENT AND CISCO AND cisco? OR ABC OR DEF AND XYZ MMM","AND"));
		//System.out.println(parseMain.formatSearchText("VPN ADJ CLIENT ADJ CISCO","AND"));
		//System.out.println(parseMain.formatSearchText("cisco  ADJ2 Client AdJ vpn","AND"));
		//System.out.println(parseMain.formatSearchText("cisco  NEAR10 VPN NEAR client","AND"));
		//System.out.println(parseMain.formatSearchText("cisco  AND VPN NEAR client","AND"));
		//System.out.println(parseMain.formatSearchText("(cisco NOT client) OR (VPN NOT client @AD>20101010) technical","AND"));
		//System.out.println(parseMain.formatSearchText("config~5 config^5","AND"));
		System.out.println(parseMain.parseQuery("cisco config^5","AND"));
		
	}



}
