package parboiledtest;

import static org.parboiled.support.ParseTreeUtils.printNodeTree;
import static org.parboiled.trees.GraphUtils.printTree;

import java.util.logging.Logger;

import org.parboiled.Parboiled;
import org.parboiled.errors.ErrorUtils;
import org.parboiled.parserunners.RecoveringParseRunner;
import org.parboiled.support.ParsingResult;
import org.parboiled.support.ToStringFormatter;
import org.parboiled.trees.GraphNode;

import parboiledtest.ParsingRules.CalcNode;

public class ParseMain {

	private static Logger logger = Logger.getLogger("parboilded");

	public String formatSearchText(String input, String operator) {

		ParsingRules parser = Parboiled.createParser(ParsingRules.class);
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
    			//logger.info("\nAbstract Syntax Tree:\n" +
                       // printTree + '\n');
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

}
