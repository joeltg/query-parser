package com.uspto.query.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class QueryParser {
	
	SpanParseMain spanParseMain = new SpanParseMain();
	BoolParseMain boolParseMain = new BoolParseMain();
	ObjectMapper mapper = new ObjectMapper();
	ArrayNode mustClauses;
	ObjectNode bool;
	ObjectNode jsonQuery;
	ObjectNode json;
	Object obj;
	
	public String pasrseToElastic(String query, String operator, String filters) {
		mustClauses = mapper.createArrayNode();
		obj = boolParseMain.parseQuery(query, operator);
		if(obj instanceof String){
			return obj.toString();
		}
		mustClauses.add((JsonNode) obj);
		obj = spanParseMain.parseQuery(query, operator);
		mustClauses.add((JsonNode) obj);
		bool = mapper.createObjectNode();
		bool.replace("must", mustClauses);
		jsonQuery = mapper.createObjectNode();
		jsonQuery.replace("bool", bool);
		json = mapper.createObjectNode();
		json.replace("query", jsonQuery);
		
		return json.toString().replace("BOOL_AND", "bool").replace("BOOL_OR", "bool");
	}

	public static void main(String args[]) throws JsonProcessingException{
		QueryParser queryParser = new QueryParser();
		System.out.println("Final Json : "+queryParser.pasrseToElastic("cisco AND vpn", "AND", null));
	}
}
