

package parboiledtest;

import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.support.Var;
import org.parboiled.trees.ImmutableBinaryTreeNode;

import parboiledtest.ParsingRules.CalcNode;


@BuildParseTree
public class ParsingRules extends BaseParser<CalcNode> {
	public Rule InputLine() {
		return Sequence(Expression(), EOI);
	}

    public Rule Expression() {
        Var<String> op = new Var<String>();
        return Sequence(Expr(),
        		ZeroOrMore(
                        OR(), op.set(match()),
                        Expr(),
                        push(new CalcNode(op.get(), pop(1), pop()))
                ));
    }

    Rule Expr() {
        Var<String> op = new Var<String>();
        return Sequence(
                ExpTerm(),
                ZeroOrMore(
                        XOR(), op.set(match()),
                        ExpTerm(),
                        push(new CalcNode(op.get(), pop(1), pop()))
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
                        push(new CalcNode(op.get(), pop(1), pop()))
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
                         push(new CalcNode(op.get(), pop(1), pop()))
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
                         push(new CalcNode(op.get(), pop(1), pop()))
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
                        push(new CalcNode(op.get(), pop(1), pop()))
                )
        );
    }

    Rule Factor() {
        Var<String> op = new Var<String>();
        return Sequence(
                Atom(),
                ZeroOrMore(
                		FirstOf("^", "~","|"), op.set(match()),
                        Regex(),
                        push(new CalcNode(op.get(), pop(1), pop()))
                )
        );
    }
    
    Rule Atom() {
        return Sequence(SpacelessAtom(),
        		Optional(TestNot(FirstOf(AND(), OR(), XOR(), NOT(), ADJ(), NEAR(), ONEAR(), WITH(), SAME())), 
        				NonWhiteSpaceAtom(), push(new CalcNode("SPACE", pop(1), pop())))
    			);
    }
       
    
    Rule SpacelessAtom() {
   	 Var<String> op = new Var<String>();
       return Sequence(SubAtom(), ContextOperator(), op.set(match()), push(new CalcNode(op.get(), pop(), new CalcNode(""))));
    }
      
    
    Rule NonWhiteSpaceAtom() {
      	 Var<String> op = new Var<String>();
          return Sequence(NonWhitespaceSubAtom(), ContextOperator(), op.set(match()), push(new CalcNode(op.get(), pop(), new CalcNode(""))));
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
    	    			push(new CalcNode("SPACE", pop(1), pop())))
    			);
    }

    Rule LineNo() {
    	return Sequence(
    			Sequence(
    					'L', OneOrMore(Digit())
    					),
    			push(new CalcNode(matchOrDefault(" ")))
    			);
    }

    Rule Parens() {
        return Sequence("(", Expression(), ")", 
        		ZeroOrMore(NonOperatorWhiteSpace(), Expression(),
        				push(new CalcNode("SPACE", pop(1), pop()))));
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
    	    			push(new CalcNode("SPACE", pop(1), pop())))
    			);
    }

    Rule Phrase() {
    	return Sequence(
    			Sequence(
    					'"', OneOrMore(TestNot('"'),ANY),
    					ZeroOrMore(TestNot('"'),FirstOf(' ', OneOrMore(ANY))),
    					'"'
    					),
    			push(new CalcNode("ADJ", splitPhrase(match())))
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
    	    			push(new CalcNode("SPACE", pop(1), pop())))
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
    			push(new CalcNode(matchOrDefault(" ")))
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
    					push(new CalcNode(op.get(), pop(1), pop()))
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
    					push(new CalcNode(op.get(), pop(1), pop()))
    			));
    }

    
    

    Rule Space() {
    	return Sequence(
    			Regex(),
    			ZeroOrMore(
    					TestNot(FirstOf(AND(), OR(), XOR(), NOT(), ADJ(), NEAR(), ONEAR(), WITH(), SAME())), OneOrMore(' '),
    	    			Atom(),
    	    			push(new CalcNode("SPACE", pop(1), pop())))
    			);
    }
    
    

    Rule NonWhitespaceSpace() {
    	return Sequence(
    			NonWhitespaceRegex(),
    			ZeroOrMore(
    					TestNot(FirstOf(AND(), OR(), XOR(), NOT(), ADJ(), NEAR(), ONEAR(), WITH(), SAME())), OneOrMore(' '),
    	    			Atom(),
    	    			push(new CalcNode("SPACE", pop(1), pop())))
    			);
    }
    
    

    Rule Regex() {
    	return Sequence(
    			ZeroOrMore(TestNot(IgnoreCase("NOT ")), TestNot(AnyOf(":=~^<>")), FirstOf(OneOrMore(Alphabet()), AnyOf(",${}?+*"))),
    			push(new CalcNode(matchOrDefault(" ")))
    			);
    }
    
    
    
    Rule NonWhitespaceRegex() {
    	return Sequence(
    			OneOrMore(TestNot(IgnoreCase("NOT ")), TestNot(AnyOf(":=~^<>")), FirstOf(OneOrMore(Alphabet()), AnyOf(",${}?+*"))),
    			push(new CalcNode(matchOrDefault(" ")))
    			);
    }

    Rule Alphabet() {
    	return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), Digit(), '_', '-','/');
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

    
   
    public static class CalcNode extends ImmutableBinaryTreeNode<CalcNode> {
        private String value;
        private String[] operands;
        private String operator;
        private String distance = "0";
        private static String defaultOp = "AND";
        
        private boolean isRegex = false;
        private boolean isWildcard = false;


		public String getDefaultOp() {
			return defaultOp;
		}

		public static void setDefaultOp(String defaultOp) {
			CalcNode.defaultOp = defaultOp;
		}
		
		public CalcNode(String operator, String[] operands) {
            super(null, null);
            this.operator = operator.toUpperCase().trim();
            this.operands = operands;
        }

		public CalcNode(String value) {
            super(null, null);
            this.value = value;
        }

        public CalcNode(String operator, CalcNode left, CalcNode right) {
            super(left, right);
            operator = operator.toUpperCase().trim();

            if (operator.startsWith("ADJ") && operator.length() > 3) {
            	this.distance = operator.substring(3);
            	this.operator = "ADJ";

            } else if (operator.startsWith("NEAR") && operator.length() > 4) {
            	this.distance = operator.substring(4);
            	this.operator = "NEAR";

            } else if (operator.startsWith("ONEAR") && operator.length() > 5) {
            	this.distance = operator.substring(5);
            	this.operator = "ONEAR";
            	
            } else if (operator.startsWith("WITH") && operator.length() > 4) {
            	this.distance = operator.substring(4);
            	this.operator = "WITH";


            } else if (operator.startsWith("SAME") && operator.length() > 4) {
            	this.distance = operator.substring(4);
            	this.operator = "SAME";

            } else {
            	this.operator = operator;
            }
            if(distance.isEmpty()) {
            	distance = "0";
            }
        }

        public String getValue() {
        	
            if (operator == null) {
            	while (value.contains("$")) {
            		if (value.endsWith("$")) {
            			if (value.startsWith("$")) {
            				String str = value.substring(1, value.length() - 1);
            				if (!str.contains("$") && str.length() < 4) {
            					break;
            				}
            			}
            			value = value.substring(0, value.length() - 1) + "*";
            		}
            		if (!value.contains("$")) {
            			break;
            		}
            		String no = value.substring(value.indexOf("$")+1, value.indexOf("$")+2);
        			if (no.matches("[0-9]")) {
        				value = value.substring(0, value.indexOf("$")) + "[a-zA-Z0-9]{0," + no + "}" + value.substring(value.indexOf("$")+2);
        				//System.out.println("setting regex to true");
        				isRegex = true;
        			} else {
        				value = value.substring(0, value.indexOf("$")) + "*" + value.substring(value.indexOf("$")+1);
        			}
        			if (!value.contains("$") && value.contains("{0,")) {
                		isRegex = true;
                	}
        		}
            	
            	if (value.matches(".*[\\^\\|\\+].*")) {
            		isRegex = true;
            	} else if (value.endsWith("*") || value.endsWith("?")){
            		isWildcard = true;
            	}
            	else if (value.startsWith("@")) {
            	
            		String date = getDate(value);
        			value = date;
            	}
            	if (isWildcard || isRegex){
            		//System.out.println("isRegex: "+isRegex+" value "+value +" calcnode "+ super.toString());
            		return "SPAN_MULTI("+value+","+ (isRegex?"regex":"wildcard")+")";
            	}
            	return value;
            }
            if("".equals(operator)) {
            	return left().getValue();
            }
            if (operator.equalsIgnoreCase("SPACE")) {
            	this.operator = defaultOp.toUpperCase();
            } else if (operator.equalsIgnoreCase("|")) {
            	this.operator = "OR";
            }
            if (left()!= null && left().getValue().matches("L[0-9]+")) {
            	return right().getValue();
            }
            if (right()!= null && right().getValue().matches("L[0-9]+")) {
            	return left().getValue();
            }

            if (operator.equalsIgnoreCase("AND")) {
            	String strL, strR;
            	if (left().getValue().startsWith("SPAN_NEAR(") && left().getValue().endsWith(", slop=2147483647, in_order=0)")) {
            		strL = left().getValue().substring(10, left().getValue().length() - 30);
            	} else {
            		strL = left().getValue();
            	}
            	if (right().getValue().startsWith("SPAN_NEAR(")&& right().getValue().endsWith(", slop=2147483647, in_order=0)")) {
            		strR = right().getValue().substring(10, right().getValue().length() - 30);
            	} else {
            		strR = right().getValue();
            	}
            	
            	return "SPAN_NEAR(" + strL + ", " + strR + ", slop=2147483647, in_order=0)";

            } else if (operator.equalsIgnoreCase("OR")) {
            	String strL, strR;
            	if (left().getValue().startsWith("SPAN_OR(")) {
            		strL = left().getValue().substring(8, left().getValue().length() - 21);
            	} else {
            		strL = left().getValue();
            	}
            	if (right().getValue().startsWith("SPAN_OR(")) {
            		strR = right().getValue().substring(8, right().getValue().length() - 21);
            	} else {
            		strR = right().getValue();
            	}
            	if(strR.trim().equals("")) {
            		return left().getValue();
            	}
            
                return "SPAN_OR(" + strL + ", " + strR + ", slop=0, in_order=0)";

            } else if (operator.equalsIgnoreCase("XOR")) {
                return "OR(AND(" + left().getValue() + ", NOT(" + right().getValue() + ")), AND(NOT(" + left().getValue() + "), " + right().getValue() + "))";

            } else if (operator.equalsIgnoreCase("ADJ")) {
        		
            	if(operands != null) {
            		return "SPAN_NEAR(" + String.join(", ", operands) +", slop=" + distance + ", in_order=1)";
            	} else {
        			return "SPAN_NEAR(" + left().getValue() + ", " + right().getValue() + ", slop=" + distance + ", in_order=1)";
            	}
            } else if (operator.equalsIgnoreCase("NEAR")) {
                if (distance.equalsIgnoreCase("0")) {
            		
            		return "SPAN_NEAR(" + left().getValue() + ", " + right().getValue() + ", slop=0, in_order=0)";
            	} else {
            		
            		return "SPAN_NEAR(" + left().getValue() + ", " + right().getValue() + ", slop=" + distance + ", in_order=0)";
            	}

            } else if (operator.equalsIgnoreCase("ONEAR")) {
                if (distance.equalsIgnoreCase("0")) {        		
                	return "SPAN_NEAR(" + left().getValue() + ", " + right().getValue() + ", slop=0, in_order=1)";
            	} else {
            		return "SPAN_NEAR(" + left().getValue() + ", " + right().getValue() + ", slop=" + distance + ", in_order=1)";
            	}

            } else if (operator.equalsIgnoreCase("WITH")) {
            	
            	 if (distance.equalsIgnoreCase("0")) {
            		 return "SPAN_NEAR(" + left().getValue() + ", " + right().getValue() + ", slop=15, in_order=0)";
             	} else {
             		return "SPAN_NEAR(" + left().getValue() + ", " + right().getValue() + ", slop=" + distance + ", in_order=0)";
             	}
            	
            } else if (operator.equalsIgnoreCase("SAME")) {
            	if (distance.equalsIgnoreCase("0")) {
            		return "SPAN_NEAR(" + left().getValue() + ", " + right().getValue() + ", slop=200, in_order=0)";
            	} else {
            		return "SPAN_NEAR(" + left().getValue() + ", " + right().getValue() + ", slop=" + distance + ", in_order=0)";
            	}

            } else if (operator.equalsIgnoreCase("NOT") || operator.equalsIgnoreCase("-")) {
                return (left().getValue() != " " ? "AND(" + left().getValue() + ", NOT(" + right().getValue() + "))" : "NOT(" + right().getValue() + ")");

            } else if (operator.equalsIgnoreCase("~")) {
                return ("SPAN_MULTI(" + left().getValue() + ", FUZZY=" + right().getValue() + ")");

            } else if (operator.equalsIgnoreCase("^")) {
                return ("TERM(" + left().getValue() + ", BOOST=" + right().getValue() + ")");

            } else if (operator.contains(":") || operator.contains("=")) {
            	if (operator.equalsIgnoreCase(":<") || operator.equalsIgnoreCase("=<")) {
            		return ("CONTEXT(" + left().getValue() + "):<" + right().getValue());
            	} else if (operator.equalsIgnoreCase(":>") || operator.equalsIgnoreCase("=>")) {
            		return ("CONTEXT(" + left().getValue() + "):>" + right().getValue());
            	} else {
            		return ("CONTEXT(" + left().getValue() + "):" + right().getValue());
            	}
            	
            } else if(operator.equals(".")) {
            	if(right().getValue().equalsIgnoreCase("ti")) {
            		return ("CONTEXT(title):" + left().getValue());
            	} else if (right().getValue().equalsIgnoreCase("ab")) {
            		return ("CONTEXT(cdcdescription):" + left().getValue());
           
            		
            	} else {
            		return (left().getValue() + "." + right().getValue());
            	}

            } else if(operator.equalsIgnoreCase(".ab.")) {
            	return ("CONTEXT(cdcdescription):" + left().getValue());
            } else if(operator.equalsIgnoreCase(".ti.")) {
            	return ("CONTEXT(title):" + left().getValue());
            }  else if(operator.equalsIgnoreCase(".ab,ti.")) {
            	return "OR(CONTEXT(cdcdescription):"+left().getValue()+", CONTEXT(title):"+left().getValue()+")";
            } else if(operator.equalsIgnoreCase(".ti,ab.")) {
            	return "OR(CONTEXT(title):"+left().getValue()+", CONTEXT(cdcdescription):"+left().getValue()+")";
            } else if(operator.equalsIgnoreCase(".cpc.")) {
            	return ("CONTEXT(cpccode):" + left().getValue());
            } 
            else {
            	
            	System.out.println("LEFT: " + left().getValue());
            	System.out.println("OP: " + operator);
            	System.out.println("RIGHT: " + right().getValue());
                throw new IllegalStateException();
            }
        }

            

        public String getDate(String value) {
    		String dt = "";
    		String dtstr = "";
    		String date = "";
    		String start = "T00:00:00.000";
    		String end = "T23:59:59.999";
    		Boolean range = false;
    		Boolean not = false;
    		if (value.contains("<=") && value.contains(">=")) {
    			dt = value.substring(value.indexOf(">=") + 2, value.indexOf("<="));
    			dtstr = value.substring(value.indexOf("<=") + 2);
    			if (dt.startsWith("\"")) {
    				dt = dt.substring(1, dt.length() - 1);
    			}
    			if (dtstr.startsWith("\"")) {
    				dtstr = dtstr.substring(1, dtstr.length() - 1);
    			}
    			if (dt.length() > 4) {
    				dt = dt.substring(0, 4) + "-" + dt.substring(4, 6) + "-" + dt.substring(6) + start;
    			} else {
    				dt = dt + "-01-01" + start;
    			}
    			if (dtstr.length() > 4) {
    				dtstr = dtstr.substring(0, 4) + "-" + dtstr.substring(4, 6) + "-" + dtstr.substring(6) + end;
    			} else {
    				dtstr = dtstr + "-12-31" + end;
    			}
    			range = true;
    		} else if (value.contains("<=")) {
    			dt = value.substring(value.indexOf("<="));
    			if (dt.charAt(2) == '"') {
    				dt = dt.substring(3, dt.length() - 1);
    			} else {
    				dt = dt.substring(2);
    			}
    			if (dt.length() > 4) {
    				dt = dt.substring(0, 4) + "-" + dt.substring(4, 6) + "-" + dt.substring(6);
    				dtstr = dt + end;
    			} else {
    				dtstr = dt + "-12-31" + end;
    			}
				dt = "*";
				range = true;
    		} else if (value.contains(">=")) {
    			dt = value.substring(value.indexOf(">="));
    			if (dt.charAt(2) == '"') {
    				dt = dt.substring(3, dt.length() - 1);
    			} else {
    				dt = dt.substring(2);
    			}
    			if (dt.length() > 4) {
    				dt = dt.substring(0, 4) + "-" + dt.substring(4, 6) + "-" + dt.substring(6);
    				dt = dt + start;
    			} else {
    				dt = dt + "-01-01" + start;
    			}
    			dtstr = "*";
    			range = true;
    		} else if (value.contains("<>")) {
    			dt = value.substring(value.indexOf("<>"));
    			if (dt.charAt(2) == '"') {
    				dt = dt.substring(3, dt.length() - 1);
    			} else {
    				dt = dt.substring(2);
    			}
    			if (dt.length() > 4) {
    				dt = dt.substring(0, 4) + "-" + dt.substring(4, 6) + "-" + dt.substring(6);
    				dtstr = dt + end;
    				dt = dt + start;
    			} else {
    				dtstr = dt + "-12-31" + end;
    				dt = dt + "-01-01" + start;
    			}
    			range = true;
    			not = true;
    		} else if (value.contains("=")) {
    			dt = value.substring(value.indexOf("="));
    			if (dt.charAt(1) == '"') {
    				dt = dt.substring(2, dt.length() - 1);
    			} else {
    				dt = dt.substring(1);
    			}
    			if (dt.length() > 4) {
    				dt = dt.substring(0, 4) + "-" + dt.substring(4, 6) + "-" + dt.substring(6);
    				dtstr = dt + end;
    				dt = dt + start;
    			} else {
    				dtstr = dt + "-12-31" + end;
    				dt = dt + "-01-01" + start;
    			}
    			range = true;
    		} else if (value.contains("<")) {
    			dt = value.substring(value.indexOf("<"));
    			dtstr = "<";
    			if (dt.charAt(1) == '"') {
    				dt = dt.substring(2, dt.length() - 1);
    			} else {
    				dt = dt.substring(1);
    			}
        		if (dt.length() > 4) {
    				dt = dt.substring(0, 4) + "-" + dt.substring(4, 6) + "-" + dt.substring(6);
    				dt = dt + start;
    			} else {
    				dt = dt + "-01-01" + start;
    			}
    		} else if (value.contains(">")) {
    			dt = value.substring(value.indexOf(">"));
    			dtstr = ">";
    			if (dt.charAt(1) == '"') {
    				dt = dt.substring(2, dt.length() - 1);
    			} else {
    				dt = dt.substring(1);
    			}
        		if (dt.length() > 4) {
    				dt = dt.substring(0, 4) + "-" + dt.substring(4, 6) + "-" + dt.substring(6);
    				dt = dt + end;
    			} else {
    				dt = dt + "-12-31" + end;
    			}
    		}
    		if (range) {
    			date = "date:[" + dt + " TO " + dtstr + "]";
    			if (not) {
    				date = "NOT(" + date + ")";
    			}
    		} else {
    			date = "date:" + dtstr + dt;
    		}
			return date;
        }
        @Override
        public String toString() {
            return (operator == null ? "Value " + value : "Operator '" + operator + '\'') + " | " + getValue();
        }
    }
}
