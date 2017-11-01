package com.olivereivak.embeddedjetty;

import com.google.inject.servlet.GuiceFilter;
import org.eclipse.jetty.http.HttpCompliance;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.LowResourceMonitor;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

public class EmbeddedJetty {

	private static final Logger log = LoggerFactory.getLogger(EmbeddedJetty.class);

	private Server server;

	// These are used by the default implementation of getServer() so that they could be easily configured.
	// If more options are needed, then improve or override getServer().

	private int serverPort = 8080;
	private String contextPath = "/";
	private List<EventListener> eventListeners = new ArrayList<>();

	private int minThreads = 8;
	private int maxThreads = 200;

	private int requestHeaderSize = 8192;
	private int responseHeaderSize = 8192;
	private boolean sendServerVersion = false;

	private HttpCompliance httpCompliance = HttpCompliance.RFC7230;

	private String requestLogFileName = null;
	private String requestLogFilenameDateFormat = null;
	private int requestLogRetainDays = 31;
	private boolean requestLogAppend = true;
	private boolean requestLogExtended = true;
	private boolean requestLogLogCookies = false;
	private String requestLogTimeZone = "GMT";
	private boolean requestLogLogLatency = false;

	private int lowResourcesPeriod = 1000;
	private int lowResourcesIdleTimeout = 1000;
	private boolean lowResourcesMonitorThreads = true;
	private int lowResourcesMaxConnections = 0;
	private long lowResourcesMaxMemory = 0;
	private int lowResourcesMaxLowResourcesTime = 0;

	public void start() throws Exception {
		log.info("Starting EmbeddedJetty on port {}", serverPort);

		server = getServer();

		ServletContextHandler serverContextHandler = getServerContextHandler();

		for (EventListener eventListener : eventListeners) {
			serverContextHandler.addEventListener(eventListener);
		}

		serverContextHandler.addFilter(GuiceFilter.class, "/*", null);
		serverContextHandler.addServlet(DefaultServlet.class, "/");

		server.start();
		server.join();
	}

	public void stop() throws Exception {
		log.info("Stopping embedded jetty");
		server.stop();
	}

	/**
	 * Override this if you need to finely configure your Server.
	 *
	 * @see <a href="http://git.eclipse.org/c/jetty/org.eclipse.jetty.project.git/tree/examples/embedded/src/main/java/org/eclipse/jetty/embedded/LikeJettyXml.java">"http://git.eclipse.org/c/jetty/org.eclipse.jetty.project.git/tree/examples/embedded/src/main/java/org/eclipse/jetty/embedded/LikeJettyXml.java</a>
	 */
	protected Server getServer() {
		QueuedThreadPool threadPool = new QueuedThreadPool();
		threadPool.setMinThreads(minThreads);
		threadPool.setMaxThreads(maxThreads);

		Server server = new Server(threadPool);

		HttpConfiguration httpConfig = new HttpConfiguration();
		httpConfig.setRequestHeaderSize(requestHeaderSize);
		httpConfig.setResponseHeaderSize(responseHeaderSize);
		httpConfig.setSendServerVersion(sendServerVersion);

		HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory(httpConfig);
		httpConnectionFactory.setHttpCompliance(httpCompliance);

		ServerConnector http = new ServerConnector(server, httpConnectionFactory);
		http.setPort(serverPort);
		server.addConnector(http);

		HandlerCollection handlers = new HandlerCollection();
		ContextHandlerCollection contexts = new ContextHandlerCollection();
		handlers.setHandlers(new Handler[] { contexts, new DefaultHandler() });
		server.setHandler(handlers);

		NCSARequestLog requestLog = new NCSARequestLog();
		requestLog.setFilename(requestLogFileName);
		requestLog.setFilenameDateFormat(requestLogFilenameDateFormat);
		requestLog.setRetainDays(requestLogRetainDays);
		requestLog.setAppend(requestLogAppend);
		requestLog.setExtended(requestLogExtended);
		requestLog.setLogCookies(requestLogLogCookies);
		requestLog.setLogTimeZone(requestLogTimeZone);
		requestLog.setLogLatency(requestLogLogLatency);

		RequestLogHandler requestLogHandler = new RequestLogHandler();
		requestLogHandler.setRequestLog(requestLog);
		handlers.addHandler(requestLogHandler);

		// TODO: logging

		LowResourceMonitor lowResourcesMonitor = new LowResourceMonitor(server);
		lowResourcesMonitor.setPeriod(lowResourcesPeriod);
		lowResourcesMonitor.setLowResourcesIdleTimeout(lowResourcesIdleTimeout);
		lowResourcesMonitor.setMonitorThreads(lowResourcesMonitorThreads);
		lowResourcesMonitor.setMaxConnections(lowResourcesMaxConnections);
		lowResourcesMonitor.setMaxMemory(lowResourcesMaxMemory);
		lowResourcesMonitor.setMaxLowResourcesTime(lowResourcesMaxLowResourcesTime);
		server.addBean(lowResourcesMonitor);

		return server;
	}

	protected ServletContextHandler getServerContextHandler() {
		return new ServletContextHandler(server, contextPath);
	}

	// Convenient builder style setters for fast configuration

	public EmbeddedJetty setServerPort(int serverPort) {
		this.serverPort = serverPort;
		return this;
	}

	public EmbeddedJetty setContextPath(String contextPath) {
		this.contextPath = contextPath;
		return this;
	}

	public EmbeddedJetty setEventListeners(List<EventListener> eventListeners) {
		this.eventListeners = eventListeners;
		return this;
	}

	public EmbeddedJetty setMinThreads(int minThreads) {
		this.minThreads = minThreads;
		return this;
	}

	public EmbeddedJetty setMaxThreads(int maxThreads) {
		this.maxThreads = maxThreads;
		return this;
	}

	public EmbeddedJetty setRequestHeaderSize(int requestHeaderSize) {
		this.requestHeaderSize = requestHeaderSize;
		return this;
	}

	public EmbeddedJetty setResponseHeaderSize(int responseHeaderSize) {
		this.responseHeaderSize = responseHeaderSize;
		return this;
	}

	public EmbeddedJetty setSendServerVersion(boolean sendServerVersion) {
		this.sendServerVersion = sendServerVersion;
		return this;
	}

	public EmbeddedJetty setHttpCompliance(HttpCompliance httpCompliance) {
		this.httpCompliance = httpCompliance;
		return this;
	}

	public EmbeddedJetty setRequestLogFileName(String requestLogFileName) {
		this.requestLogFileName = requestLogFileName;
		return this;
	}

	public EmbeddedJetty setRequestLogFilenameDateFormat(String requestLogFilenameDateFormat) {
		this.requestLogFilenameDateFormat = requestLogFilenameDateFormat;
		return this;
	}

	public EmbeddedJetty setRequestLogRetainDays(int requestLogRetainDays) {
		this.requestLogRetainDays = requestLogRetainDays;
		return this;
	}

	public EmbeddedJetty setRequestLogAppend(boolean requestLogAppend) {
		this.requestLogAppend = requestLogAppend;
		return this;
	}

	public EmbeddedJetty setRequestLogExtended(boolean requestLogExtended) {
		this.requestLogExtended = requestLogExtended;
		return this;
	}

	public EmbeddedJetty setRequestLogLogCookies(boolean requestLogLogCookies) {
		this.requestLogLogCookies = requestLogLogCookies;
		return this;
	}

	public EmbeddedJetty setRequestLogTimeZone(String requestLogTimeZone) {
		this.requestLogTimeZone = requestLogTimeZone;
		return this;
	}

	public EmbeddedJetty setRequestLogLogLatency(boolean requestLogLogLatency) {
		this.requestLogLogLatency = requestLogLogLatency;
		return this;
	}

	public EmbeddedJetty setLowResourcesPeriod(int lowResourcesPeriod) {
		this.lowResourcesPeriod = lowResourcesPeriod;
		return this;
	}

	public EmbeddedJetty setLowResourcesIdleTimeout(int lowResourcesIdleTimeout) {
		this.lowResourcesIdleTimeout = lowResourcesIdleTimeout;
		return this;
	}

	public EmbeddedJetty setLowResourcesMonitorThreads(boolean lowResourcesMonitorThreads) {
		this.lowResourcesMonitorThreads = lowResourcesMonitorThreads;
		return this;
	}

	public EmbeddedJetty setLowResourcesMaxConnections(int lowResourcesMaxConnections) {
		this.lowResourcesMaxConnections = lowResourcesMaxConnections;
		return this;
	}

	public EmbeddedJetty setLowResourcesMaxMemory(int lowResourcesMaxMemory) {
		this.lowResourcesMaxMemory = lowResourcesMaxMemory;
		return this;
	}

	public EmbeddedJetty setLowResourcesMaxLowResourcesTime(int lowResourcesMaxLowResourcesTime) {
		this.lowResourcesMaxLowResourcesTime = lowResourcesMaxLowResourcesTime;
		return this;
	}
}
