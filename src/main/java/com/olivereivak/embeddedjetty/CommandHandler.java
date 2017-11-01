package com.olivereivak.embeddedjetty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class CommandHandler implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(CommandHandler.class);

	public static final String RESPONSE_STATUS_OK = "ok";
	public static final String RESPONSE_STOPPING = "stopping";

	private int port;
	private EmbeddedJetty embeddedJetty;

	public CommandHandler(Integer port, EmbeddedJetty embeddedJetty) {
		this.port = port;
		this.embeddedJetty = embeddedJetty;

	}

	public void run() {
		try {
			start();
		} catch (Exception e) {
			log.error("CommandHandler failed while running", e);
		}
	}

	public void start() throws Exception {
		InetAddress address = InetAddress.getByName(null);

		try (ServerSocket serverSocket = new ServerSocket(port, 0, address);
			 Socket socket = serverSocket.accept();

			 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		) {
			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				boolean shouldStop = handleInput(out, inputLine);
				if (shouldStop) {
					break;
				}
			}
		}
	}

	/**
	 * @return  true if the command handler should exit
	 */
	protected boolean handleInput(PrintWriter out, String inputLine) throws Exception {
		if (inputLine.equals(JettyApp.COMMAND_STATUS)) {
			out.println(RESPONSE_STATUS_OK);
		} else if (inputLine.equals(JettyApp.COMMAND_STOP)) {
			out.println(RESPONSE_STOPPING);
			embeddedJetty.stop();
			// figure out why the vm won't stop without this
			System.exit(0);
			return true;
		}
		return false;
	}

}
