# Embedded-Jetty

A micro-library to add embedded Jetty support to a java application.


## How to use
Build and install the maven project, then add the dependency to your project:

```
<dependency>
      <groupId>com.olivereivak</groupId>
      <artifactId>embedded-jetty</artifactId>
      <version>1.0-SNAPSHOT</version>
</dependency>
```

Create a class that contains the main method
```
package com.example.test;

import com.olivereivak.embeddedjetty.JettyApp;

public class Main {

	public static void main(String[] args) throws Exception {
		new JettyApp().run(args);
	}

}
```
This will start an embedded jetty server on port 8080 and with context path that is "/"

Start your application with
```
java -jar myapp.jar start
```
or just
```
java -jar myapp.jar
```

Stop it with 
```
java -jar myapp.jar stop
```

And check status with
```
java -jar myapp.jar status
```

A more realistic Main class might look like this:
```
package com.example.test;

import com.olivereivak.embeddedjetty.EmbeddedJetty;
import com.olivereivak.embeddedjetty.JettyApp;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

public class Main {

	public static void main(String[] args) throws Exception {
		List<EventListener> listeners = new ArrayList<>();
		listeners.add(new MyGuiceServletContextListener());

		EmbeddedJetty embeddedJetty = new EmbeddedJetty()
				.setServerPort(4000)
				.setContextPath("/test")
				.setEventListeners(listeners);

		new JettyApp()
				.setEmbeddedJetty(embeddedJetty)
				.setCommandPort(5000)
				.run(args);
	}

}
```

And the accompanying listener that creates the Guice Injector:
```
package com.example.test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;

public class MyGuiceServletContextListener extends GuiceServletContextListener {

	@Override
	protected Injector getInjector() {
		return Guice.createInjector(new WhateverModule());
	}
	
}

``` 
