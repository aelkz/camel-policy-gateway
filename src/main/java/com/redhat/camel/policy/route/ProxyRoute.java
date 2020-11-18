package com.redhat.camel.policy.route;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.logging.Logger;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.stereotype.Component;

@Component
public class ProxyRoute extends RouteBuilder {
	private static final Logger LOGGER = Logger.getLogger(ProxyRoute.class.getName());

    @Override
    public void configure() throws Exception {
		
		from("netty4-http:proxy://0.0.0.0:8080")
			.process((e) -> {
				System.out.println("\n:: proxy received\n");
			})
			// &httpClient.redirectsEnabled=true
			.toD("http4://0.0.0.0:8081/?throwExceptionOnFailure=false&connectionClose=false&bridgeEndpoint=true&copyHeaders=true")
			.process((e) -> {
				System.out.println("\n:: route processing ended\n");
			});
		
		restConfiguration()
			//.contextPath("/")
			.enableCORS(true)
			.corsHeaderProperty("Access-Control-Allow-Headers", "Authorization, Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers, Content-Length")
			.bindingMode(RestBindingMode.json)
			.dataFormatProperty("prettyPrint", "true")
			.host("0.0.0.0")
			.port(8081);
		 
		rest()
			//.skipBindingOnErrorCode(false) // enable json marshalling for body in case of errors
			//.post() // TODO
			//.put() // TODO
			//.delete() // TODO
			//.patch() // TODO
			.enableCORS(true)
			.get()
				// curl -k -vvv http://localhost:8080 -H 'Accept: application/json'
				// curl -k -vvv http://localhost:8081 -H 'Accept: application/json'
				.route().to("direct:internal-rest")
			.endRest()
			.get("/get") // esse /get corresponde ao path da URI que veio na chamada do proxy. Exemplo: https://xyz.com/foo -> .get(/"foo")
				// EXAMPLE: curl -k -vvv http://www.postman-echo.com/get -H 'Accept: application/json' -x "http://0.0.0.0:8080"
				.route().to("direct:internal-rest")
			.endRest();

		from("direct:internal-rest")
			.process((e) -> {
				System.out.println("\n:: internal-rest received\n");
			})
			.process(ProxyRoute::beforeRedirect)
			.to("https4://www.postman-echo.com/get?test=123&bridgeEndpoint=true&throwExceptionOnFailure=false")
			.unmarshal().json(JsonLibrary.Jackson)
			.process((e) -> {
				System.out.println(":: request to backend forwarded");
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
