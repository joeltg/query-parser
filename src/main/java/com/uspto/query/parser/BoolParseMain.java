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

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.uspto.query.parser.BoolParsingRules.CalcNode;



public class BoolParseMain {
	


	private static Logger logger = Logger.getLogger("parboilded");

	public String formatSearchText(String input, String operator) {

		BoolParsingRules parser = Parboiled.createParser(BoolParsingRules.class);
        String output = "";

		CalcNode.setDefaultOp(operator.toUpperCase());

        ParsingResult<?> result = new RecoveringParseRunner(parser.InputLine()).run(input);
        Object value = null;

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
        //System.out.println("value.getClass().getName() ::::::::::::::: " +value.getClass().getName());
        /*if(value != null){
        	
        	BoolParsingRules1.CalcNode value1 = BoolParsingRules1.CalcNode.class.cast(value);
        	//System.out.println("getParsedQuery : "+value1.getParsedQuery());
        	//System.out.println(value1.getParsedQuery().getClass().getName());
        	System.out.println("value : "+value);
        	ObjectNode node = (ObjectNode) value1.getParsedQuery();
        	System.out.println(node.get(0));
        }*/
        if (value != null) {
            String str = value.toString();
            int ix = str.indexOf('|');
            if (ix >= 0) str = str.substring(ix + 2); // extract value part of AST node toString()
            output = str;
        }
        //logger.info("\nParsed Search Text is " + output + "\n");
        return output;
	}
	
	public static void main(String args[]){
		BoolParseMain parseMain = new BoolParseMain();
		//System.out.println(parseMain.formatSearchText("CISCO OR VPN OR CLIENT OR cisco?","AND"));
		//System.out.println(parseMain.formatSearchText("cisco? OR VPN OR CLIENT OR ABC OR DEF","AND"));
		//System.out.println(parseMain.formatSearchText("VPN AND CLIENT AND CISCO AND cisco? OR ABC OR DEF AND XYZ MMM","AND"));
		//System.out.println(parseMain.formatSearchText("VPN ADJ CLIENT ADJ CISCO","AND"));
		//System.out.println(parseMain.formatSearchText("cisco  ADJ2 Client AdJ vpn","AND"));
		System.out.println(parseMain.formatSearchText("(cisco OR XYZ) AND VPN  (client OR router) cisco? OR @AD>19961011 technical","AND"));
		//System.out.println(parseMain.formatSearchText("cisco  AND VPN NEAR client","AND"));
		//System.out.println(parseMain.formatSearchText("cisco  OR NOT router","AND"));
		//System.out.println(parseMain.formatSearchText("cisco OR NOT VPN OR NOT client OR NOT technical","AND"));
		//System.out.println(parseMain.formatSearchText("cisco NOT (1 or 2 or 3 or 4)","AND"));
		//System.out.println(parseMain.formatSearchText("cisco  NOT VPN  NOT client  NOT technical","AND"));
		//System.out.println(parseMain.formatSearchText("cisco OR NOT router OR NOT client","AND"));
		//System.out.println(parseMain.formatSearchText("router.ti","AND"));
	}



}
