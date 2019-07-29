package edu.mit.kfg.priorart.parser;

import com.uspto.query.parser.QueryParser;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class ParserHandler implements RequestHandler<RequestClass, String> {
	public String handleRequest(RequestClass request, Context context) {
		QueryParser queryParser = new QueryParser();
		System.out.println("request.query was: " + request.query);
		return queryParser.pasrseToElastic(request.query, request.operator, request.filters);
	}
}