package com.aws;

import java.util.HashSet;
import java.util.Set;

import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;


/**
 * This class is created by the Lambda environment when a request comes in. All calls will be
 * dispatched to the Speechlet passed into the super constructor.
 */
public final class WallOfShameSpeechletRequestStreamHandler extends SpeechletRequestStreamHandler
{
	private static final Set<String> supportedApplicationIds;

	static
	{
		/*
		 * This Id can be found on https://developer.amazon.com/edw/home.html#/ "Edit" the relevant
		 * Alexa Skill and put the relevant Application Ids in this Set.
		 */
		supportedApplicationIds = new HashSet<String>();
		supportedApplicationIds.add("amzn1.ask.skill.b5d35f54-1ef3-4d0a-8f2a-63c21ac5d7a2");
	}

	public WallOfShameSpeechletRequestStreamHandler()
	{
		super(new com.aws.WallOfShameSpeechlet(), supportedApplicationIds);
	}
}
