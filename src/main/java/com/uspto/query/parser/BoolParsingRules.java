package com.uspto.query.parser;





import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.support.Var;
import org.parboiled.trees.ImmutableBinaryTreeNode;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.uspto.query.parser.BoolParsingRules.BoolNode;



@BuildParseTree
public class BoolParsingRules extends BaseParser<BoolNode> {
	public Rule InputLine() {
		return Sequence(Expression(), EOI);
	}

    public Rule Expression() {
        Var<String> op = new Var<String>();
        return Sequence(Expr(),
        		ZeroOrMore(
                        OR(), op.set(match()),
                        Expr(),
                        push(new BoolNode(op.get(), pop(1), pop()))
                ));
    }

    Rule Expr() {
        Var<String> op = new Var<String>();
        return Sequence(
                ExpTerm(),
                ZeroOrMore(
                        XOR(), op.set(match()),
                        ExpTerm(),
                        push(new BoolNode(op.get(), pop(1), pop()))
                        //push (new  CalcCNode.op.get().pop(1),pop()))
                        
                )
        );
    }

     Rule ExpTerm() {
        Var<String> op = new Var<String>();
        return Sequence(
        		SameExp(),
                ZeroOrMore(
                		FirstOf(AND(), NOT()), op.set(match()),
                		SameExp(),
                        push(new BoolNode(op.get(), pop(1), pop()))
                )
        );
    }

     Rule SameExp() {
         Var<String> op = new Var<String>();
         return Sequence(
        		 WithExp(),
                 ZeroOrMore(
                         SAME(), op.set(match()),
                         WithExp(),
                         push(new BoolNode(op.get(), pop(1), pop()))
                 )
         );
     }

     Rule WithExp() {
         Var<String> op = new Var<String>();
         return Sequence(
                 Term(),
                 ZeroOrMore(
                         WITH(), op.set(match()),
                         Term(),
                         push(new BoolNode(op.get(), pop(1), pop()))
                 )
         );
     }

    Rule Term() {
        Var<String> op = new Var<String>();
        return Sequence(
                Factor(),
                ZeroOrMore(
                		FirstOf(ADJ(), NEAR(), ONEAR()), op.set(match()),
                		Factor(),
                        push(new BoolNode(op.get(), pop(1), pop()))
                )
        );
    }

    Rule Factor() {
        Var<String> op = new Var<String>();
        return Sequence(
                Atom(),
                ZeroOrMore(
                		AnyOf("|"), op.set(match()),
                        Regex(),
                        push(new BoolNode(op.get(), pop(1), pop()))
                )
        );
    }
    
    Rule Atom() {
        return Sequence(SpacelessAtom(),
        		Optional(TestNot(FirstOf(AND(), OR(), XOR(), NOT(), ADJ(), NEAR(), ONEAR(), WITH(), SAME())), 
        				NonWhiteSpaceAtom(), push(new BoolNode("SPACE", pop(1), pop())))
    			);
    }
       
    
    Rule SpacelessAtom() {
   	 Var<String> op = new Var<String>();
       return Sequence(SubAtom(), ContextOperator(), op.set(match()), push(new BoolNode(op.get(), pop(), new BoolNode(""))));
    }
      
    
    Rule NonWhiteSpaceAtom() {
      	 Var<String> op = new Var<String>();
          return Sequence(NonWhitespaceSubAtom(), ContextOperator(), op.set(match()), push(new BoolNode(op.get(), pop(), new BoolNode(""))));
    }
    
       
    Rule SubAtom() {
        return FirstOf(Parens(), LineNos(), Phrases(), Dates(), Context());
    }
    
        
    Rule NonWhitespaceSubAtom() {
        return FirstOf(Parens(), LineNos(), Phrases(), Dates(), NonWhitespaceContext());
    }
    
     
    Rule LineNos() {
    	return Sequence(
    			LineNo(),
    			ZeroOrMore(
    					TestNot(FirstOf(AND(), OR(), XOR(), NOT(), ADJ(), NEAR(), ONEAR(), WITH(), SAME())), OneOrMore(' '),
    	    			Atom(),
    	    			push(new BoolNode("SPACE", pop(1), pop())))
    			);
    }

    Rule LineNo() {
    	return Sequence(
    			Sequence(
    					'L', OneOrMore(Digit())
    					),
    			push(new BoolNode(matchOrDefault(" ")))
    			);
    }

    Rule Parens() {
        return Sequence("(", Expression(), ")", 
        		ZeroOrMore(NonOperatorWhiteSpace(), Expression(),
        				push(new BoolNode("SPACE", pop(1), pop()))));
    }
    Rule NonOperatorWhiteSpace() {
    	 return OneOrMore(TestNot(FirstOf(AND(), OR(), XOR(), NOT(), ADJ(), NEAR(), ONEAR(), WITH(), SAME())), AnyOf(" \t\f"));
    }

    Rule Phrases() {
    	return Sequence(
    			Phrase(),
    			ZeroOrMore(
    					TestNot(FirstOf(AND(), OR(), XOR(), NOT(), ADJ(), NEAR(), ONEAR(), WITH(), SAME())), OneOrMore(' '),
    	    			Atom(),
    	    			push(new BoolNode("SPACE", pop(1), pop())))
    			);
    }

    Rule Phrase() {
    	return Sequence(
    			Sequence(
    					'"', OneOrMore(TestNot('"'),ANY),
    					ZeroOrMore(TestNot('"'),FirstOf(' ', OneOrMore(ANY))),
    					'"'
    					),
    			push(new BoolNode("ADJ", splitPhrase(match())))
    			);
    }
    
    String[] splitPhrase(String phrase) {
    	return phrase.substring(1, phrase.length()-1).split(" ");
    }

    Rule Dates() {
    	return Sequence(
    			Date(),
    			ZeroOrMore(
    					TestNot(FirstOf(AND(), OR(), XOR(), NOT(), ADJ(), NEAR(), ONEAR(), WITH(), SAME())), OneOrMore(' '),
    	    			Atom(),
    	    			push(new BoolNode("SPACE", pop(1), pop())))
    			);
    }

    Rule Date() {
    	return Sequence(
    			FirstOf(
    			Sequence(
    					FirstOf(IgnoreCase("@RLAD"), IgnoreCase("@AD"), IgnoreCase("@PD"), IgnoreCase("@FD"), IgnoreCase("@RLFD"), IgnoreCase("@ISD")),
    	    			WhiteSpace(), FirstOf("<=", ">=", '=', "<>", '<', '>'), WhiteSpace(), Optional('"'),
    	    			FirstOf("19", "20"), Digit(), Digit(), FirstOf(Sequence('0', Digit()), Sequence('1', FirstOf('0', '1', '2'))),
    	    			FirstOf(Sequence('0', Digit()), Sequence('1', Digit()), Sequence('2', Digit()), Sequence('3', FirstOf('0', '1'))),
    	    			Optional('"'),
    	    			Optional(FirstOf("<=", ">=", '<', '>', '=', "<>"), Optional('"'),
    	    			FirstOf("19", "20"), Digit(), Digit(), FirstOf(Sequence('0', Digit()), Sequence('1', FirstOf('0', '1', '2'))),
    	    			FirstOf(Sequence('0', Digit()), Sequence('1', Digit()), Sequence('2', Digit()), Sequence('3', FirstOf('0', '1'))),
    	    			Optional('"'))
    			),
    			Sequence(
    					FirstOf(IgnoreCase("@AY"), IgnoreCase("@PY"), IgnoreCase("@FY"), IgnoreCase("@ISY")),
    					WhiteSpace(), FirstOf("<=", ">=", '=', "<>", '<', '>'), WhiteSpace(), Optional('"'),
    	    			FirstOf("19", "20"), Digit(), Digit(), Optional('"'),
    	    			Optional(FirstOf("<=", ">=", '<', '>', '=', "<>"), Optional('"'),
    	    			FirstOf("19", "20"), Digit(), Digit(), Optional('"'))
    			)),
    			push(new BoolNode(matchOrDefault(" ")))
    			);
    }
    
    

    
    
    Rule Context() {
        Var<String> op = new Var<String>();
    	return Sequence(
    			Space(),
    			ZeroOrMore(
    					FirstOf(":<", ":>", ":", "=<", "=>", "="), op.set(match()),
    					WhiteSpace(),
    					Expression(),
    					push(new BoolNode(op.get(), pop(1), pop()))
    			));
    }
    
    
    
    Rule NonWhitespaceContext() {
        Var<String> op = new Var<String>();
    	return Sequence(
    			NonWhitespaceSpace(),
    			ZeroOrMore(
    					FirstOf(":<", ":>", ":", "=<", "=>", "="), op.set(match()),
    					WhiteSpace(),
    					Expression(),
    					push(new BoolNode(op.get(), pop(1), pop()))
    			));
    }

    
    

    Rule Space() {
    	return Sequence(
    			Regex(),
    			ZeroOrMore(
    					TestNot(FirstOf(AND(), OR(), XOR(), NOT(), ADJ(), NEAR(), ONEAR(), WITH(), SAME())), OneOrMore(' '),
    	    			Atom(),
    	    			push(new BoolNode("SPACE", pop(1), pop())))
    			);
    }
    
    

    Rule NonWhitespaceSpace() {
    	return Sequence(
    			NonWhitespaceRegex(),
    			ZeroOrMore(
    					TestNot(FirstOf(AND(), OR(), XOR(), NOT(), ADJ(), NEAR(), ONEAR(), WITH(), SAME())), OneOrMore(' '),
    	    			Atom(),
    	    			push(new BoolNode("SPACE", pop(1), pop())))
    			);
    }
    
    

    Rule Regex() {
    	return Sequence(
    			ZeroOrMore(TestNot(IgnoreCase("NOT ")), TestNot(AnyOf(":=~^<>")), FirstOf(OneOrMore(Alphabet()), AnyOf(",${}?+*"))),
    			push(new BoolNode(matchOrDefault(" ")))
    			);
    }
    
    
    
    Rule NonWhitespaceRegex() {
    	return Sequence(
    			OneOrMore(TestNot(IgnoreCase("NOT ")), TestNot(AnyOf(":=~^<>")), FirstOf(OneOrMore(Alphabet()), AnyOf(",${}?+*"))),
    			push(new BoolNode(matchOrDefault(" ")))
    			);
    }

    Rule Alphabet() {
    	return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), Digit(), '_', '-','/', '^','~');
    }

    Rule Digit() {
        return CharRange('0', '9');
    }
    
    Rule AtLeastOneWhiteSpace() {
        return OneOrMore(AnyOf(" \t\f"));
    }

    Rule WhiteSpace() {
        return ZeroOrMore(AnyOf(" \t\f"));
    }

    Rule AND() {
    	return Sequence(
    			WhiteSpace(), IgnoreCase("AND"), WhiteSpace()
    			);
    }
    
    Rule ContextOperator() {
    	return 	Sequence(
    			WhiteSpace(), FirstOf(IgnoreCase(".ab."), IgnoreCase(".ti."), IgnoreCase(".cpc."), IgnoreCase(".ti,ab."), IgnoreCase(".ab,ti."), IgnoreCase("."), ""), WhiteSpace()    			
    			);
    }

    Rule OR() {
    	return Sequence(
    			WhiteSpace(), IgnoreCase("OR"), WhiteSpace()
    			);
    }

    Rule XOR() {
    	return Sequence(
    			WhiteSpace(), IgnoreCase("XOR"), WhiteSpace()
    			);
    }

    Rule ADJ() {
    	return Sequence(
    			WhiteSpace(), IgnoreCase("ADJ"), Optional(Digit()), Optional(Digit()), WhiteSpace()
    			);
    }

    Rule NEAR() {
    	return Sequence(
    			WhiteSpace(), IgnoreCase("NEAR"), Optional(Digit()), Optional(Digit()), WhiteSpace()
    			);
    }

    Rule ONEAR() {
    	return Sequence(
    			WhiteSpace(), IgnoreCase("ONEAR"), Optional(Digit()), Optional(Digit()), WhiteSpace()
    			);
    }

    Rule WITH() {
    	return Sequence(
    			WhiteSpace(), IgnoreCase("WITH"), ZeroOrMore(Digit())
    			//Optional(Digit()), Optional(Digit())
    			,WhiteSpace()

    			//WhiteSpace(), IgnoreCase("WITH"), WhiteSpace()
    			);
    }
    
    Rule SAME() {
    	return Sequence(
    			WhiteSpace(), IgnoreCase("SAME"), ZeroOrMore(Digit())
    			//Optional(Digit()), Optional(Digit())
    			, WhiteSpace()
    			);
    }
    
/*
    Rule SAME() {
    	return Sequence(
    			WhiteSpace(), IgnoreCase("SAME"), Optional(Digit()), Optional(Digit()), WhiteSpace()
    			);
    }
*/
    
    
    Rule NOT() {
    	return Sequence(
    			//WhiteSpace(), FirstOf(IgnoreCase("NOT"), '-'), WhiteSpace()
    				WhiteSpace(), FirstOf(IgnoreCase("NOT"), '-'), AtLeastOneWhiteSpace()
    			);
    }

    
   
    public class BoolNode extends ImmutableBinaryTreeNode<BoolNode> {
        private Object value;
        private String[] operands;
        private String operator;
        private String distance = "0";
        private String defaultOp = "AND";
        private ObjectMapper mapper = new ObjectMapper();
        private String defaultField = "text";
        private boolean isRegex = false;
        private boolean isWildcard = false;
        JsonNode leftNode;
    	JsonNode rightNode;
    	ObjectNode json;
    	ObjectNode boolAND;
    	ObjectNode boolOR;
    	ObjectNode term;
    	ObjectNode fieldValue;
        ArrayNode mustClauses;
        ArrayNode shouldClauses;
        ArrayNode mustNotClauses;
    	Object left;
    	Object right;
    	JsonNode jsonNode;
    	JsonNode fieldValueNode;
		ObjectNode objectNode;
		String fuzziness = "0";
		String boostValue = "0";
    	


		public String getDefaultOp() {
			return defaultOp;
		}

		public void setDefaultOp(String defaultOp) {
			this.defaultOp = defaultOp;
		}
		
		public String getDefaultField() {
			return defaultField;
		}

		public void setDefaultField(String defaultField) {
			this.defaultField = defaultField;
		}

		public BoolNode(String operator, String[] operands) {
            super(null, null);
            this.operator = operator.toUpperCase().trim();
            this.operands = operands;
        }

		public BoolNode(Object value) {
            super(null, null);
            this.value = value;
        }

        public BoolNode(String operator, BoolNode left, BoolNode right) {
            super(left, right);
            operator = operator.toUpperCase().trim();

            if (operator.startsWith("ADJ") && operator.length() > 3) {
            	this.distance = operator.substring(3);
            	this.operator = "AND";

            } else if (operator.startsWith("NEAR") && operator.length() > 4) {
            	this.distance = operator.substring(4);
            	this.operator = "AND";

            } else if (operator.startsWith("ONEAR") && operator.length() > 5) {
            	this.distance = operator.substring(5);
            	this.operator = "AND";
            	
            } else if (operator.startsWith("WITH") && operator.length() > 4) {
            	this.distance = operator.substring(4);
            	this.operator = "AND";


            } else if (operator.startsWith("SAME") && operator.length() > 4) {
            	this.distance = operator.substring(4);
            	this.operator = "AND";

            } else {
            	this.operator = operator;
            }
        }

        
		public Object getValue() {
			String strValue = "";
            if (operator == null) {
	            	if(value instanceof String){
	            		strValue = (String)value;
	            	
	            	while (strValue.contains("$")) {
	            		if (strValue.endsWith("$")) {
	            			if (strValue.startsWith("$")) {
	            				String str = strValue.substring(1, strValue.length() - 1);
	            				if (!str.contains("$") && str.length() < 4) {
	            					break;
	            				}
	            			}
	            			strValue = strValue.substring(0, strValue.length() - 1) + "*";
	            		}
	            		if (!strValue.contains("$")) {
	            			break;
	            		}
	            		String no = strValue.substring(strValue.indexOf("$")+1, strValue.indexOf("$")+2);
	        			if (no.matches("[0-9]")) {
	        				strValue = strValue.substring(0, strValue.indexOf("$")) + "[a-zA-Z0-9]{0," + no + "}" + strValue.substring(strValue.indexOf("$")+2);
	        				isRegex = true;
	        			} else {
	        				strValue = strValue.substring(0, strValue.indexOf("$")) + "*" + strValue.substring(strValue.indexOf("$")+1);
	        			}
	        			if (!strValue.contains("$") && strValue.contains("{0,")) {
	                		isRegex = true;
	                	}
	        		}
	            	if (strValue.startsWith("^")) {
	            		isRegex = true;
	            	} else if (strValue.endsWith("*") || strValue.endsWith("?")){
	            		isWildcard = true;
	            	}
	            	else if (strValue.startsWith("@")) {
	            		return getDate(strValue);
	            	}else if(strValue.contains("^")){
	            		boostValue = strValue.substring(strValue.indexOf("^")+1);
	            		if(!boostValue.matches("\\d+")){
	            			boostValue = "0";
	            		}
	            		strValue = strValue.substring(0,strValue.indexOf("^"));
	            		objectNode = mapper.createObjectNode();
	            		objectNode.put("value", strValue);
	            		objectNode.put("boost", boostValue);
	            		term = mapper.createObjectNode();
	            		term.replace(defaultField, objectNode);
	            		json = mapper.createObjectNode();
	            		json.replace("term",term);
	            		return (JsonNode)json;
	            	}
	            	else if(strValue.contains("~")){
	            		fuzziness = strValue.substring(strValue.indexOf("~")+1);
	            		if(!fuzziness.matches("\\d+")){
	            			fuzziness = "0";
	            		}
	            		strValue = strValue.substring(0,strValue.indexOf("~"));
	            		objectNode = mapper.createObjectNode();
	            		objectNode.put("value", strValue);
	            		objectNode.put("fuzziness", fuzziness);
	            		term = mapper.createObjectNode();
	            		term.replace(defaultField, objectNode);
	            		json = mapper.createObjectNode();
	            		json.replace("fuzzy",term);
	            		return (JsonNode)json;
	            	}
	            	if (isWildcard || isRegex){
	            		 fieldValueNode = createFieldValueNode(getDefaultField(), strValue,false);
	            		 objectNode = mapper.createObjectNode();
	                     if(isWildcard){
	                    	 objectNode.replace("wildcard", fieldValueNode);
	                     }else{
	                    	 objectNode.replace("regexp",fieldValueNode);
	                     }
	                     return (JsonNode)objectNode;
	            	}
	            	return createFieldValueNode(getDefaultField(), strValue,true);
	            }
	            	return null;
            }
            if("".equals(operator)) {
            	return left().getValue();
            }
            if (operator.equalsIgnoreCase("SPACE")) {
            	this.operator = defaultOp.toUpperCase();
            } else if (operator.equalsIgnoreCase("|")) {
            	this.operator = "OR";
            }
            if (left()!= null && left().getValue() instanceof String && ((String)left().getValue()).matches("L[0-9]+")) {
            	return right().getValue();
            }
            if (right()!= null && right().getValue() instanceof String && ((String)right().getValue()).matches("L[0-9]+")) {
            	return left().getValue();
            }
            
            left = left().getValue();
            right = right().getValue();
            
            
            if (operator.equalsIgnoreCase("AND")) {
            
            	json = mapper.createObjectNode();
            	boolAND = mapper.createObjectNode();
                mustClauses = mapper.createArrayNode();
                mustNotClauses = mapper.createArrayNode();
            	
            	if (left instanceof String) {
            		
                   /*fieldValue = mapper.createObjectNode().put(defaultField,(String)left);
                   term  = mapper.createObjectNode();
                   term.replace("term",fieldValue);
                   mustClauses.add(term);*/
            		
            	} else {
            		if(left instanceof JsonNode  ){
            			 jsonNode =  ((JsonNode)left).get("BOOL_AND");
            			if( jsonNode != null){
		            			ArrayNode clauses =  (ArrayNode) jsonNode.get("must");
		            			if(clauses != null ){
		            				mustClauses.addAll(clauses);
		            			}
		            			clauses =  (ArrayNode) jsonNode.get("must_not");
		            			if(clauses != null ){
		            				mustNotClauses.addAll(clauses);
		            			}
            			}else{
            				mustClauses.add(((JsonNode)left));
            			}
            		}
            	}
            	if (right().getValue() instanceof String) {
            		/*fieldValue = mapper.createObjectNode().put(defaultField,(String)right);
            		term  = mapper.createObjectNode();
            		term.replace("term",fieldValue);
                    mustClauses.add(term);*/
            	} else {
            		if(right instanceof JsonNode ){
            			 jsonNode =  ((JsonNode)right).get("BOOL_AND");
            			if( jsonNode != null){
		            			ArrayNode clauses =  (ArrayNode) jsonNode.get("must");
		            			if(clauses != null ){
		            				mustClauses.addAll(clauses);
		            			}
		            			clauses =  (ArrayNode) jsonNode.get("must_not");
		            			if(clauses != null ){
		            				mustNotClauses.addAll(clauses);
		            			}
            			}else{
            				mustClauses.add(((JsonNode)right));
            			}
            		}
            	}
            	
            	if(mustClauses.size() > 0){
            		boolAND.replace("must", mustClauses);
            	}
            	if(mustNotClauses.size() > 0){
            		boolAND.replace("must_not", mustNotClauses);
            	}
	            json.replace("BOOL_AND",boolAND);
	            return json;
             
            } else if (operator.equalsIgnoreCase("OR")) {
            	json = mapper.createObjectNode();
            	boolOR = mapper.createObjectNode();
                shouldClauses = mapper.createArrayNode();
            	
            	if (left instanceof String) {
                   fieldValue = mapper.createObjectNode().put(defaultField,(String)left);
                   term  = mapper.createObjectNode();
                   term.replace("term",fieldValue);
                   shouldClauses.add(term);
            		
            	} else {
            		if(left instanceof JsonNode  ){
            			if(((JsonNode)left).get("BOOL_OR") != null){
		            			ArrayNode clauses =  (ArrayNode) ((JsonNode)left().getValue()).get("BOOL_OR").get("should");
		            			if(clauses != null ){
		            				shouldClauses.addAll(clauses);
		            			}
            			}else if(((JsonNode)left().getValue()).get("must_not") != null){
            			//TBD if this is required	
            			}else{
            				shouldClauses.add(((JsonNode)left));
            			}
            		}
            	}
            	if (right instanceof String) {
            		fieldValue = mapper.createObjectNode().put(defaultField,(String)right);
            		term  = mapper.createObjectNode();
                    term.replace("term",fieldValue);
                    shouldClauses.add(term);
            	} else {
            		if(right instanceof JsonNode ){
            			if( ((JsonNode)right).get("BOOL_OR") != null){
	            			ArrayNode clauses =  (ArrayNode) ((JsonNode)right).get("BOOL_OR").get("should");
	            			if(clauses != null ){
	            				shouldClauses.addAll(clauses);
	            			}else{
	            				//TBD if this is require
	            			}
            			}else{
            				shouldClauses.add(((JsonNode)right));
            			}
            		}
            	}
                boolOR.replace("should", shouldClauses);
                json.replace("BOOL_OR",boolOR);
                return json;

            } else if (operator.equalsIgnoreCase("NOT") || operator.equalsIgnoreCase("-")) {
            	json = mapper.createObjectNode();
            	boolAND = mapper.createObjectNode();
                mustNotClauses = mapper.createArrayNode();
                mustClauses = mapper.createArrayNode();
                
                if(left != null){
                	if(left instanceof String){
                		if((String)left != " "){
	                		term = mapper.createObjectNode();
		            		term.put(defaultField, (String)left);
		            		mustClauses.add(term);
                		}
	            	}else{
	            		if(((JsonNode)left).get("BOOL_AND") != null){
	            			mustClauses.addAll((ArrayNode)(((JsonNode)left).get("BOOL_AND").get("must")));
	            			mustNotClauses.addAll((ArrayNode)(((JsonNode)left).get("BOOL_AND").get("must_not")));
	            		}else{
	            			mustClauses.add((JsonNode) left);
	            		}
	            	}
                	if(right instanceof String){
                		term = mapper.createObjectNode();
	            		term.put(defaultField, (String)right);
	            		mustNotClauses.add(term);
	            	}else{
	            		if( right !=null && ((JsonNode)right).get("BOOL_AND") != null){
	            			if(((JsonNode)right).get("BOOL_AND").has("must")){
	            				mustClauses.addAll((ArrayNode)(((JsonNode)right).get("BOOL_AND").get("must")));
	            			}
	            			if(((JsonNode)right).get("BOOL_AND").has("must_not")){
	            				mustNotClauses.addAll((ArrayNode)(((JsonNode)right).get("BOOL_AND").get("must_not")));
	            			}
	            		}else{
	            			mustNotClauses.add((JsonNode) right);
	            		}
	            	}
                	if(mustNotClauses.size() > 0){
                		boolAND.replace("must_not", mustNotClauses);
                	}
                	if(mustClauses.size() > 0){
                		boolAND.replace("must", mustClauses);
                	}
                	json.replace("BOOL_AND",boolAND);
                }else{
	            	if(right instanceof String){
	            		term = mapper.createObjectNode();
	            		term.put(defaultField, (String)right);
	            		mustNotClauses.add(term);
	            	}else{
	            		mustNotClauses.add((JsonNode) right);
	            	}
	            	boolAND.replace("must_not", mustNotClauses);
                	json.replace("BOOL_AND",boolAND);
                }
                return json;
            } /*else if (operator.equalsIgnoreCase("~")) {
            	JsonNode termNode;
            	ObjectNode fieldValueNode;
            	ObjectNode fuzzy;
            	String field = null;
            	String value = null;
            	String fuzziness = null;
            	leftNode = ((JsonNode)left().getValue());
            	rightNode = ((JsonNode)right().getValue());
            	if(leftNode !=null && leftNode.has("term")){
            		termNode = leftNode.get("term");
            		if(termNode.has(defaultField)){
            			field = defaultField;
            			value = termNode.get(defaultField).asText();
            		}else if(termNode.has("title")){
            			field = "title";
            			value = termNode.get(field).asText();
            		}else if(termNode.has("description")){
            			field = "description";
            			value = termNode.get(field).asText();
            		}
            		if(rightNode !=null && rightNode.has("term")){
            			termNode = rightNode.get("term");
            			fuzziness = termNode.get(defaultField).asText();
            		}
            		if(value !=null){
            			if( fuzziness !=null){
            				if(field != null){
            					fieldValueNode = mapper.createObjectNode();
            					fieldValueNode.put("value",value);
            					fieldValueNode.put("fuzziness",fuzziness);
            					fuzzy = mapper.createObjectNode();
            					fuzzy.replace(field, fieldValueNode);
            					json = mapper.createObjectNode();
            					json.replace("fuzzy", fuzzy);
            					return json;
            				}else{
            					fieldValueNode = mapper.createObjectNode();
            					fieldValueNode.put("value",value);
            					fieldValueNode.put("fuzziness",fuzziness);
            					fuzzy = mapper.createObjectNode();
            					fuzzy.replace(defaultField, fieldValueNode);
            					json = mapper.createObjectNode();
            					json.replace("fuzzy", fuzzy);
            					return json;
            				}
            			}else{
            				if(field != null){
            					fieldValueNode = mapper.createObjectNode();
            					fieldValueNode.put(field,value);
            					json = mapper.createObjectNode();
            					json.replace("fuzzy", fieldValueNode);
            					return json;
            				}else{
            					fieldValueNode = mapper.createObjectNode();
            					fieldValueNode.put(defaultField,value);
            					json = mapper.createObjectNode();
            					json.replace("fuzzy", fieldValueNode);
            					return json;
            				}
            			}
            		}
            	}
            	return null;
            	
            } else if (operator.equalsIgnoreCase("^")) {
            	
            	JsonNode termNode;
            	ObjectNode fieldValueNode;
            	String field = null;
            	String value = null;
            	String boost = null;
            	leftNode = ((JsonNode)left().getValue());
            	rightNode = ((JsonNode)right().getValue());
            	if(leftNode !=null && leftNode.has("term")){
            		termNode = leftNode.get("term");
            		if(termNode.has(defaultField)){
            			field = defaultField;
            			value = termNode.get(defaultField).asText();
            		}else if(termNode.has("title")){
            			field = "title";
            			value = termNode.get(field).asText();
            		}else if(termNode.has("description")){
            			field = "description";
            			value = termNode.get(field).asText();
            		}
            		if(rightNode !=null && rightNode.has("term")){
            			termNode = rightNode.get("term");
            			boost = termNode.get(defaultField).asText();
            		}
            		if(value !=null){
            			if( boost !=null){
            				if(field != null){
            					fieldValueNode = mapper.createObjectNode();
            					fieldValueNode.put("value",value);
            					fieldValueNode.put("boost",boost);
            					term = mapper.createObjectNode();
            					term.replace(field, fieldValueNode);
            					json = mapper.createObjectNode();
            					json.replace("term", term);
            					return json;
            				}else{
            					fieldValueNode = mapper.createObjectNode();
            					fieldValueNode.put("value",value);
            					fieldValueNode.put("boost",boost);
            					term = mapper.createObjectNode();
            					term.replace(defaultField, fieldValueNode);
            					json = mapper.createObjectNode();
            					json.replace("term", term);
            					return json;
            				}
            			}else{
            				if(field != null){
            					fieldValueNode = mapper.createObjectNode();
            					fieldValueNode.put(field,value);
            					json = mapper.createObjectNode();
            					json.replace("term", fieldValueNode);
            					return json;
            				}else{
            					fieldValueNode = mapper.createObjectNode();
            					fieldValueNode.put(defaultField,value);
            					json = mapper.createObjectNode();
            					json.replace("term", fieldValueNode);
            					return json;
            				}
            			}
            		}
            	}
            	return null;
            }*/ else if (operator.contains(":") || operator.contains("=")) {
            	if (operator.equalsIgnoreCase(":<") || operator.equalsIgnoreCase("=<")) {
            		return ("CONTEXT(" + left().getValue() + "):<" + right().getValue());
            	} else if (operator.equalsIgnoreCase(":>") || operator.equalsIgnoreCase("=>")) {
            		return ("CONTEXT(" + left().getValue() + "):>" + right().getValue());
            	} else {
            		return ("CONTEXT(" + left().getValue() + "):" + right().getValue());
            	}
            	
            } else if(operator.equals(".")) {
            	
            	if(right instanceof String && ((String)right).equalsIgnoreCase("ti")) {
            		return ("CONTEXT(title):" + left().getValue());
            	} else if (right instanceof String && ((String)right).equalsIgnoreCase("ab")) {
            		return ("CONTEXT(cdcdescription):" + left().getValue());
           
            		
            	} else {
            		return left;
            	}

            } else if(operator.equalsIgnoreCase(".ab.") || operator.equalsIgnoreCase(".ti.") || operator.equalsIgnoreCase(".cpc.") || 
            		operator.equalsIgnoreCase(".ab,ti.") || operator.equalsIgnoreCase(".ti,ab.")) {
            	json = null;
            	JsonNode termNode;
            	ObjectNode fieldValueNode;
            	String value = null;
            	leftNode = ((JsonNode)left);
            	rightNode = ((JsonNode)right);
            	if(leftNode !=null && leftNode.has("term")){
            		termNode = leftNode.get("term");
            		if(termNode.has(defaultField)){
            			value = termNode.get(defaultField).asText();
            			if(operator.equalsIgnoreCase(".ab.")){
	            			fieldValueNode = mapper.createObjectNode();
	    					fieldValueNode.put("description",value);
	    					json = mapper.createObjectNode();
	    					json.replace("term", fieldValueNode);
	    					return json;
            			}else if(operator.equalsIgnoreCase(".ti.")){
            				fieldValueNode = mapper.createObjectNode();
	    					fieldValueNode.put("title",value);
	    					json = mapper.createObjectNode();
	    					json.replace("term", fieldValueNode);
            			}
            			else if(operator.equalsIgnoreCase(".cpc.")){
            				fieldValueNode = mapper.createObjectNode();
	    					fieldValueNode.put("cpc",value);
	    					json = mapper.createObjectNode();
	    					json.replace("term", fieldValueNode);
            			}
            			else if(operator.equalsIgnoreCase(".ab,ti.") || operator.equalsIgnoreCase(".ti,ab.")){
            				json = mapper.createObjectNode();
            				boolOR = mapper.createObjectNode();
            				shouldClauses = mapper.createArrayNode();
            				term = mapper.createObjectNode();
            				fieldValueNode = mapper.createObjectNode();
	    					fieldValueNode.put("title",value);
	    					term = mapper.createObjectNode();
	    					term.replace("term", fieldValueNode);
	    					shouldClauses.add(term);
	    					
	    					term = mapper.createObjectNode();
            				fieldValueNode = mapper.createObjectNode();
	    					fieldValueNode.put("description",value);
	    					term = mapper.createObjectNode();
	    					term.replace("term", fieldValueNode);
	    					shouldClauses.add(term);
	    					
	    					boolOR.replace("should", shouldClauses);
	    					json.replace("BOOL_OR", boolOR);
            			}
            		}
            	}
            	return json;
            } 
            else {

                throw new IllegalStateException();
            }
			//return distance;
        }

            

        public Object getDate(String value) {
    		String dt = "";
    		ObjectNode paramValue;
    		ObjectNode dateNode;
    		ObjectNode rangeNode = null;
    		if (value.contains("<=")) {
    			dt = value.substring(value.indexOf("<="));
    			if (dt.charAt(2) == '"') {
    				dt = dt.substring(3, dt.length() - 1);
    			} else {
    				dt = dt.substring(2);
    			}
    			
    			paramValue = mapper.createObjectNode();
    			paramValue.put("lte", dt);
    			paramValue.put("format", "yyyyMMdd||yyyy");
    			
    			dateNode =  mapper.createObjectNode();
    			dateNode.replace("date", (JsonNode)paramValue);
    			
    			rangeNode = mapper.createObjectNode();
    			rangeNode.replace("range", (JsonNode)dateNode);
    			
    		} else if (value.contains(">=")) {
    			dt = value.substring(value.indexOf(">="));
    			if (dt.charAt(2) == '"') {
    				dt = dt.substring(3, dt.length() - 1);
    			} else {
    				dt = dt.substring(2);
    			}
    			
    			paramValue = mapper.createObjectNode();
    			paramValue.put("gte", dt);
    			paramValue.put("format", "yyyyMMdd||yyyy");
    			
    			dateNode =  mapper.createObjectNode();
    			dateNode.replace("date", (JsonNode)paramValue);
    			
    			rangeNode = mapper.createObjectNode();
    			rangeNode.replace("range", (JsonNode)dateNode);
    			
    		} else if (value.contains("<>")) {
    			dt = value.substring(value.indexOf("<>"));
    			if (dt.charAt(2) == '"') {
    				dt = dt.substring(3, dt.length() - 1);
    			} else {
    				dt = dt.substring(2);
    			}
    			
    			paramValue = mapper.createObjectNode();
    			paramValue.put("lt", dt);
    			paramValue.put("gt", dt);
    			paramValue.put("format", "yyyyMMdd||yyyy");
    			
    			
    			dateNode =  mapper.createObjectNode();
    			dateNode.replace("date", (JsonNode)paramValue);
    			
    			rangeNode = mapper.createObjectNode();
    			rangeNode.replace("range", (JsonNode)dateNode);
   
    		} else if (value.contains("=")) {
    			dt = value.substring(value.indexOf("="));
    			if (dt.charAt(1) == '"') {
    				dt = dt.substring(2, dt.length() - 1);
    			} else {
    				dt = dt.substring(1);
    			}
    			
    			paramValue = mapper.createObjectNode();
    			paramValue.put("lte", dt);
    			paramValue.put("gte", dt);
    			paramValue.put("format", "yyyyMMdd||yyyy");
    			
    			
    			dateNode =  mapper.createObjectNode();
    			dateNode.replace("date", (JsonNode)paramValue);
    			
    			rangeNode = mapper.createObjectNode();
    			rangeNode.replace("range", (JsonNode)dateNode);
    			
    		} else if (value.contains("<")) {
    			dt = value.substring(value.indexOf("<"));
    			if (dt.charAt(1) == '"') {
    				dt = dt.substring(2, dt.length() - 1);
    			} else {
    				dt = dt.substring(1);
    			}
    			
    			paramValue = mapper.createObjectNode();
    			paramValue.put("lt", dt);
    			paramValue.put("format", "yyyyMMdd||yyyy");
    			
    			
    			dateNode =  mapper.createObjectNode();
    			dateNode.replace("date", (JsonNode)paramValue);
    			
    			rangeNode = mapper.createObjectNode();
    			rangeNode.replace("range", (JsonNode)dateNode);
    			
    		} else if (value.contains(">")) {
    			dt = value.substring(value.indexOf(">"));
    			if (dt.charAt(1) == '"') {
    				dt = dt.substring(2, dt.length() - 1);
    			} else {
    				dt = dt.substring(1);
    			}
        		
    			paramValue = mapper.createObjectNode();
    			paramValue.put("gt", dt);
    			paramValue.put("format", "yyyyMMdd||yyyy");
    			
    			
    			dateNode =  mapper.createObjectNode();
    			dateNode.replace("date", (JsonNode)paramValue);
    			
    			rangeNode = mapper.createObjectNode();
    			rangeNode.replace("range", (JsonNode)dateNode);
    		}
    		
    		return rangeNode;
        }

        @Override
        public String toString() {
            try {
            	return mapper.writeValueAsString(getValue());
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			return "";
        }
        
        public JsonNode createFieldValueNode(String field, String value,boolean nodeAsTerm){
        	if(value !=null && !value.trim().isEmpty()){
        		ObjectNode node = mapper.createObjectNode();
            	node.put(field, value);
            	
            	if(nodeAsTerm){
            		ObjectNode termNode = mapper.createObjectNode();
            		termNode.replace("term", node);
            		return (JsonNode)termNode;
            	}
            	return (JsonNode)node;
            	
        	}
        	return null;
        }
    }
}
