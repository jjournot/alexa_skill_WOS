package com.aws;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.SpeechletV2;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.Reprompt;
import com.amazon.speech.ui.SimpleCard;


/**
 * This app shows how to manage players on the wall of shame:
 * <p>
 * <ul>
 * <li><b>Custom slot type</b>: demonstrates using custom slot types to handle a finite set of known values</li>
 * </ul>
 * <p>
 * <h2>Examples</h2>
 * <p>
 * <b>One-shot model</b>
 * <p>
 * User: "Alexa, ask WallOfShame Helper to add Waseem."
 * <p>
 * Alexa:"Shame the player and will remember it."
 */
public class WallOfShameSpeechlet implements SpeechletV2
{
	private static final Logger log = LoggerFactory.getLogger(WallOfShameSpeechlet.class);

	private Map<String, Integer> shamedPlayers = new HashMap<String, Integer>();
	private static List<String> bashingQuote = new ArrayList<String>();

	static
	{
		bashingQuote.add("I'm not surprised. {player} is a terrible player");
		bashingQuote.add("I've seen {player} play once. It injured my eyes permanently. That's why I'm blind. Sad story");
		bashingQuote.add("{player} deserves to be there.");
	}

	/**
	 * The key to get the item from the intent.
	 */
	private static final String PLAYER_SLOT = "Player";

	public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> requestEnvelope)
	{
		log.info("onSessionStarted requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
				requestEnvelope.getSession().getSessionId());

		// any initialization logic goes here
	}

	public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> requestEnvelope)
	{
		log.info("onLaunch requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
				requestEnvelope.getSession().getSessionId());

		String speechOutput = "Welcome to the wall of shame Helper. You can ask a question like, "
				+ "who's the worst player? Or add a player on the wall ... Now, what can I help you with?";
		// If the user either does not reply to the welcome message or says
		// something that is not understood, they will be prompted again with this text.
		String repromptText = "For instructions on what you can say, please say help me.";

		// Here we are prompting the user for input
		return newAskResponse(speechOutput, repromptText);
	}

	public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope)
	{
		IntentRequest request = requestEnvelope.getRequest();
		log.info("onIntent requestId={}, sessionId={}", request.getRequestId(), requestEnvelope.getSession().getSessionId());

		Intent intent = request.getIntent();
		String intentName = (intent != null) ? intent.getName() : null;

		if ("WallOfShameAddition".equals(intentName))
		{
			return addPlayer(intent);
		}
		else if ("WallOfShameClean".equals(intentName))
		{
			return cleanWall(intent);
		}
		else if ("WallOfShameWorst".equals(intentName))
		{
			return getWorstPlayer(intent);
		}
		else if ("WallOfShameRemoval".equals(intentName))
		{
			return removePlayer(intent);
		}
		else if ("AMAZON.HelpIntent".equals(intentName))
		{
			return getHelp();
		}
		else if ("AMAZON.StopIntent".equals(intentName))
		{
			PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
			outputSpeech.setText("Goodbye");

			return SpeechletResponse.newTellResponse(outputSpeech);
		}
		else if ("AMAZON.CancelIntent".equals(intentName))
		{
			PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
			outputSpeech.setText("Goodbye");

			return SpeechletResponse.newTellResponse(outputSpeech);
		}
		else
		{
			String errorSpeech = "This is unsupported.  Please try something else.";
			return newAskResponse(errorSpeech, errorSpeech);
		}
	}

	public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> requestEnvelope)
	{
		log.info("onSessionEnded requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
				requestEnvelope.getSession().getSessionId());

		// any cleanup logic goes here
	}

	private SpeechletResponse addPlayer(Intent intent)
	{
		Slot playerSlot = intent.getSlot(PLAYER_SLOT);
		if (playerSlot != null && playerSlot.getValue() != null)
		{
			String playerName = playerSlot.getValue();

			Random rand = new Random();
			String randomQuote = bashingQuote.get(rand.nextInt(bashingQuote.size()));

			String message = randomQuote.replace("{player}", playerName);

			if (shamedPlayers.get(playerName) == null)
			{
				shamedPlayers.put(playerName, 1);
			}
			else
			{
				int newScore = shamedPlayers.get(playerName) + 1;
				shamedPlayers.put(playerName, newScore);
			}

			PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
			outputSpeech.setText(message);

			SimpleCard card = new SimpleCard();
			card.setTitle(playerSlot + " added to the wall of shame");
			card.setContent(message);

			return SpeechletResponse.newTellResponse(outputSpeech, card);
		}
		else
		{
			// There was no item in the intent so return the help prompt.
			return getHelp();
		}
	}

	private SpeechletResponse removePlayer(Intent intent)
	{
		Slot playerSlot = intent.getSlot(PLAYER_SLOT);
		if (playerSlot != null && playerSlot.getValue() != null)
		{
			String playerName = playerSlot.getValue();

			String message = "Everybody deserves to have a second chance. " + playerName + ", try to stay out of the wall or just stop playing";

			PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
			outputSpeech.setText(message);

			SimpleCard card = new SimpleCard();
			card.setTitle(playerSlot + " removed from the wall of shame");
			card.setContent(message);

			return SpeechletResponse.newTellResponse(outputSpeech, card);
		}
		else
		{
			// There was no item in the intent so return the help prompt.
			return getHelp();
		}
	}

	private SpeechletResponse getWorstPlayer(Intent intent)
	{
		String message = "I don't have any data to prove it but my guess goes to Venelin";

		Optional<Map.Entry<String, Integer>> entry = shamedPlayers.entrySet().stream()
				.sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
				.findFirst();

		if (entry.isPresent()){
			message = "In the race of mediocrity, " + entry.get().getKey() + " is ahead and it's well deserved";
		}

		PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
		outputSpeech.setText(message);

		SimpleCard card = new SimpleCard();
		card.setTitle("Who is the worst player?");
		card.setContent(message);

		return SpeechletResponse.newTellResponse(outputSpeech, card);
	}

	private SpeechletResponse cleanWall(Intent intent)
	{
		shamedPlayers.clear();
		String message = "The wall have been cleaned.";

		PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
		outputSpeech.setText(message);

		SimpleCard card = new SimpleCard();
		card.setTitle("Clean wall");
		card.setContent(message);

		return SpeechletResponse.newTellResponse(outputSpeech, card);
	}

	/**
	 * Creates a {@code SpeechletResponse} for the HelpIntent.
	 *
	 * @return SpeechletResponse spoken and visual response for the given intent
	 */
	private SpeechletResponse getHelp()
	{
		String speechOutput =
				"You can ask questions about the wall of shame such as, who's " + "the worst player, or, you can say exit... "
						+ "Now, how can I help you?";
		String repromptText = "You can say things like, what's the worst player, or you can say exit... Now, how can I help you?";
		return newAskResponse(speechOutput, repromptText);
	}

	/**
	 * Wrapper for creating the Ask response. The OutputSpeech and {@link Reprompt} objects are
	 * created from the input strings.
	 *
	 * @param stringOutput
	 * 		the output to be spoken
	 * @param repromptText
	 * 		the reprompt for if the user doesn't reply or is misunderstood.
	 * @return SpeechletResponse the speechlet response
	 */
	private SpeechletResponse newAskResponse(String stringOutput, String repromptText)
	{
		PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
		outputSpeech.setText(stringOutput);

		PlainTextOutputSpeech repromptOutputSpeech = new PlainTextOutputSpeech();
		repromptOutputSpeech.setText(repromptText);
		Reprompt reprompt = new Reprompt();
		reprompt.setOutputSpeech(repromptOutputSpeech);

		return SpeechletResponse.newAskResponse(outputSpeech, reprompt);
	}
}

