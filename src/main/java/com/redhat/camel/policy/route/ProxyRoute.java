package com.redhat.camel.policy.route;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.logging.Logger;
import com.redhat.camel.policy.configuration.SSLProxyConfiguration;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProxyRoute extends RouteBuilder {
	private static final Logger LOGGER = Logger.getLogger(ProxyRoute.class.getName());

	@Autowired
	SSLProxyConfiguration proxyConfig;

    @Override
    public void configure() throws Exception {
		
		//from("netty4-http:proxy://0.0.0.0:8080")
		// see: https://camel.apache.org/components/latest/netty-http-component.html

		/*
		from("netty-http:proxy://0.0.0.0:"
				+ proxyConfig.getPort()
				+ "?ssl=true&keyStoreFile="
				+ proxyConfig.getKeystoreDest()
				+ "&passphrase="
				+ proxyConfig.getKeystorePass()
				+ "&trustStoreFile="
				+ proxyConfig.getKeystoreDest())
		 */

		// from("netty4-http:proxy://0.0.0.0:8443"
		from("netty-http:proxy://0.0.0.0:8443"
				+ "?ssl=true&keyStoreFile="
				+ "/tls/keystore.jks"
				+ "&passphrase="
				+ "yVQbjz6KEKQlkvo3o06AfQt7p"
				+ "&trustStoreFile="
				+ "/tls/keystore.jks")
			.process((e) -> {
				System.out.println("\n:: proxy received\n");
			})
			// &httpClient.redirectsEnabled=true
			/*
			.toD("netty-http:"
				+ "${headers." + Exchange.HTTP_SCHEME + "}://"
				+ "${headers." + Exchange.HTTP_HOST + "}:"
				+ "${headers." + Exchange.HTTP_PORT + "}"
				+ "${headers." + Exchange.HTTP_PATH + "}")
			*/
			//.toD("http4://0.0.0.0:8081/?throwExceptionOnFailure=false&connectionClose=false&bridgeEndpoint=true&copyHeaders=true")
			.process((e) -> {
				System.out.println("\n:: route processing ended\n");
			});
		
	}
	
	private static void beforeRedirect(final Exchange exchange) {
		LOGGER.info("BEFORE REDIRECT");
		final Message message = exchange.getIn();
		Iterator<String> iName = message.getHeaders().keySet().iterator();

		LOGGER.info("header values:");
		while(iName.hasNext()) {
			String key = (String) iName.next();
			LOGGER.info("\t[" +key+ "] - {"+message.getHeader(key)+"}");
		}

		// HttpServletRequest req = exchange.getIn().getBody(HttpServletRequest.class);
		InetSocketAddress remoteAddress = (InetSocketAddress)message.getHeader("CamelNettyRemoteAddress");

		LOGGER.info("");
		LOGGER.info("REQUEST REMOTE ADDRESS: " + remoteAddress.toString());
		LOGGER.info("REQUEST CANONICAL HOST NAME: " + remoteAddress.getAddress().getCanonicalHostName());
		LOGGER.info("REQUEST HOST ADDRESS: " + remoteAddress.getAddress().getHostAddress());
		LOGGER.info("REQUEST HOST NAME: " + remoteAddress.getAddress().getHostName());
		LOGGER.info("REQUEST ADDRESS: " + new String(remoteAddress.getAddress().getAddress(), StandardCharsets.UTF_8));
		LOGGER.info("REQUEST HOST NAME: " + remoteAddress.getHostName());

		LOGGER.info("");

		String host = (String) message.getHeader("CamelHttpHost");
		String path = (String) message.getHeader("CamelHttpPath");
		Integer port = (Integer) message.getHeader("CamelHttpPort");
		String scheme = (String) message.getHeader("CamelHttpScheme");

		LOGGER.info("REDIRECTING TO HTTP_HOST: " + host);
		LOGGER.info("REDIRECTING TO HTTP_PORT: " + port);
		LOGGER.info("REDIRECTING TO HTTP_PATH: " + path);
		LOGGER.info("REDIRECTING TO HTTP_SCHEME: " + scheme);

		LOGGER.info("--------------------------------------------------------------------------------");
		LOGGER.info("PROXY FORWARDING TO "
				+ message.getHeader(Exchange.HTTP_SCHEME)
				+ message.getHeader(Exchange.HTTP_HOST)
				+ ":" + message.getHeader(Exchange.HTTP_PORT)
				+ message.getHeader(Exchange.HTTP_PATH));
		LOGGER.info("--------------------------------------------------------------------------------");
	}

}
