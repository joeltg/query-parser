package edu.mit.kfg.priorart.parser;

import com.uspto.query.parser.QueryParser;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class ParserHandler implements RequestHandler<RequestClass, String> {
	public String handleRequest(RequestClass request, Context context) {
		QueryParser queryParser = new QueryParser();
		return queryParser.pasrseToElastic(request.query, request.operator, request.filters);
	}
}