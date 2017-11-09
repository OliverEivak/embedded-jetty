package com.olivereivak.embeddedjetty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.olivereivak.embeddedjetty.CommandHandler.RESPONSE_STATUS_OK;
import static com.olivereivak.embeddedjetty.CommandHandler.RESPONSE_STOPPING;

public class JettyApp {

	private static final Logger log = LoggerFactory.getLogger(JettyApp.class);

	public static final String COMMAND_START = "start";
	public static final String COMMAND_STATUS = "status";
	public static final String COMMAND_STOP = "stop";

	protected EmbeddedJetty embeddedJetty;
	protected CommandHandler commandHandler;
	protected CommandSender commandSender;

	protected int commandPort = 16586; // chosen by random.org, guaranteed to be random;
	protected boolean join = true;

	public JettyApp run(String[] args) throws Exception {
		commandSender = new CommandSender(commandPort);

		String command = getCommand(args);

		switch (command) {
		case COMMAND_START:
			start();
			break;
		case COMMAND_STATUS:
			status();
			break;
		case COMMAND_STOP:
			stop();
			break;
		default:
			start();
		}

		return this;
	}

	private String getCommand(String[] args) {
		return args.length > 0 ? args[0] : "";
	}

	private void start() throws Exception {
		if (embeddedJetty == null) {
			embeddedJetty = new EmbeddedJetty();
		}

		commandHandler = getCommandHandler();

		Thread handlerThread = new Thread(commandHandler);
		handlerThread.setName("CommandHandler");
		handlerThread.setDaemon(true);
		handlerThread.start();

		embeddedJetty.start();

		if (join) {
			embeddedJetty.join();
		}
	}

	private void status() throws Exception {
		String status = commandSender.getStatus();

		if (RESPONSE_STATUS_OK.equals(status)) {
			log.info("Application is running.");
		} else {
			log.info("Application is NOT running.");
		}
	}

	private void stop() throws Exception {
		String result = commandSender.sendStop();

		if (RESPONSE_STOPPING.equals(result)) {
			log.info("Application is stopping...");
		} else {
			log.error("Failed to send stop command.");
		}
	}

	public JettyApp setEmbeddedJetty(EmbeddedJetty embeddedJetty) {
		this.embeddedJetty = embeddedJetty;
		return this;
	}

	public JettyApp setCommandPort(int commandPort) {
		this.commandPort = commandPort;
		return this;
	}

	public JettyApp setJoin(boolean join) {
		this.join = join;
		return this;
	}

	protected CommandHandler getCommandHandler() {
		return new CommandHandler(commandPort, embeddedJetty);
	}

}
