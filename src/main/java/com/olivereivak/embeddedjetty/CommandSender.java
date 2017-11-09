package com.olivereivak.embeddedjetty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;

public class CommandSender {

	private static final Logger log = LoggerFactory.getLogger(CommandSender.class);

	private int port;

	public CommandSender(Integer port) {
		this.port = port;
	}

	public String getStatus() throws Exception {
		return sendCommand(JettyApp.COMMAND_STATUS);
	}

	public String sendStop() throws Exception {
		return sendCommand(JettyApp.COMMAND_STOP);
	}

	protected String sendCommand(String command) throws IOException {
		log.info("Sending command: {}", command);

		InetAddress address = InetAddress.getByName(null);

		try (Socket socket = new Socket(address, port);
			 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			) {
			out.println(command);
			return in.readLine();
		} catch (ConnectException e) {
			log.error("Failed to connect to application", e);
			return null;
		}
	}

}
