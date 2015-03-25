package com.kanishk.tweetstream.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import javax.net.ssl.HttpsURLConnection;

/**
 * The Class Response. A wrapper class to connection related properties(status, input stream).
 * Also useful for releasing the resources.
 */
public class Response {
	
	/** The response stream. */
	private InputStream responseStream;
	
	/** The connection. */
	private HttpURLConnection connection;
	
	/** The reader. */
	private BufferedReader reader;
	
	/** The response code. */
	private int responseCode;
	
	/** The is valid. */
	private boolean isValid;

	/**
	 * Instantiates a new response.
	 *
	 * @param urlConnection the url connection
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public Response(HttpsURLConnection urlConnection) throws IOException {
		this.connection = urlConnection;
		connection.connect();
		responseCode = connection.getResponseCode();
		if(isSuccess()) {
			responseStream = connection.getInputStream();
			isValid = true;
		} else {
			responseStream = connection.getErrorStream();
		}
		reader = new BufferedReader(new InputStreamReader(responseStream));
	}

	/**
	 * Checks if is success.
	 *
	 * @return true, if is success
	 */
	public boolean isSuccess() {
		return responseCode >= 200 && responseCode < 400;
	}

	/**
	 * Gets the response stream.
	 *
	 * @return the response stream
	 */
	public InputStream getResponseStream() {
		return responseStream;
	}

	/**
	 * Gets the connection.
	 *
	 * @return the connection
	 */
	public HttpURLConnection getConnection() {
		return connection;
	}
	
	/**
	 * Release resources.
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void releaseResources() throws IOException {
		if(this.connection != null) {
			connection.disconnect();
		}
		if(this.responseStream != null) {
			responseStream.close();
			reader.close();
		}
		isValid = false;
	}
	
	/**
	 * Stream reader.
	 *
	 * @return the buffered reader
	 */
	public BufferedReader streamReader() {
		return reader;
	}
	
	/**
	 * Checks if is valid.
	 *
	 * @return true, if is valid
	 */
	public boolean isValid() {
		return isValid;
	}
}
