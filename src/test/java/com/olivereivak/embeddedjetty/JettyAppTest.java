package com.olivereivak.embeddedjetty;

import com.olivereivak.embeddedjetty.helper.HelloServlet;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.Test;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public class JettyAppTest {

	private static final int PORT = 8099;

	@Test
	public void run_shouldStartAndStopServer() throws Exception {
		String[] args = new String[] {"start"};

		List<EventListener> eventListeners = new ArrayList<>();

		EmbeddedJetty embeddedJetty = new EmbeddedJetty() {
			@Override
			protected ServletContextHandler getServerContextHandler() {
				ServletContextHandler sch = new ServletContextHandler(server, contextPath);
				sch.addServlet(DefaultServlet.class, "/");
				sch.addServlet(HelloServlet.class, "/hello");
				return sch;
			}
		};

		embeddedJetty.setServerPort(PORT)
					 .setEventListeners(eventListeners);

		new JettyApp() {
			@Override
			protected CommandHandler getCommandHandler() {
				return new CommandHandler(commandPort, embeddedJetty) {
					protected void exit() {
						// don't call System.exit() in test
					}
				};
			}
		}
				.setEmbeddedJetty(embeddedJetty)
				.setJoin(false)
				.run(args);

		Client client = Client.create(new DefaultClientConfig());
		WebResource resource = client.resource("http://localhost:" + PORT);
		String result = resource.path("hello").accept("text/html").get(String.class);
		assertThat(result, is("Hello!\n"));

		new JettyApp().run(new String[] {"stop"});

		embeddedJetty.join(); // block until all jetty threads finish

		assertFalse("Server should not be running", embeddedJetty.server.isRunning());
	}

}
