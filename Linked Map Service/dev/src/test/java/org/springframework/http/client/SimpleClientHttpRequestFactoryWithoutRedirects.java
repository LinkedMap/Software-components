/**
 * This file is part of Linked Map Service (LMS).
 *
 * Linked Map Service (LMS) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Linked Map Service (LMS) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Linked Map Service (LMS).  If not, see <http://www.gnu.org/licenses/>.
 */
package org.springframework.http.client;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.http.HttpMethod;
import org.springframework.util.Assert;

/**
 * Fork of {@link SimpleClientHttpRequestFactory} just for allowing GET without
 * redirection.
 * 
 * @author Arjen Poutsma
 * @author Juergen Hoeller
 * @since 3.0
 * @see java.net.HttpURLConnection
 * @see HttpComponentsClientHttpRequestFactory
 */
public class SimpleClientHttpRequestFactoryWithoutRedirects implements
        ClientHttpRequestFactory, AsyncClientHttpRequestFactory {

    private static final int DEFAULT_CHUNK_SIZE = 4096;

    private Proxy proxy;

    private boolean bufferRequestBody = true;

    private int chunkSize = DEFAULT_CHUNK_SIZE;

    private int connectTimeout = -1;

    private int readTimeout = -1;

    private boolean outputStreaming = true;

    private AsyncListenableTaskExecutor taskExecutor;

    /**
     * Set the {@link Proxy} to use for this request factory.
     */
    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }

    /**
     * Indicates whether this request factory should buffer the
     * {@linkplain ClientHttpRequest#getBody() request body} internally.
     * <p>
     * Default is {@code true}. When sending large amounts of data via POST or
     * PUT, it is recommended to change this property to {@code false}, so as
     * not to run out of memory. This will result in a {@link ClientHttpRequest}
     * that either streams directly to the underlying {@link HttpURLConnection}
     * (if the {@link org.springframework.http.HttpHeaders#getContentLength()
     * Content-Length} is known in advance), or that will use
     * "Chunked transfer encoding" (if the {@code Content-Length} is not known
     * in advance).
     * 
     * @see #setChunkSize(int)
     * @see HttpURLConnection#setFixedLengthStreamingMode(int)
     */
    public void setBufferRequestBody(boolean bufferRequestBody) {
        this.bufferRequestBody = bufferRequestBody;
    }

    /**
     * Sets the number of bytes to write in each chunk when not buffering
     * request bodies locally.
     * <p>
     * Note that this parameter is only used when
     * {@link #setBufferRequestBody(boolean) bufferRequestBody} is set to
     * {@code false}, and the
     * {@link org.springframework.http.HttpHeaders#getContentLength()
     * Content-Length} is not known in advance.
     * 
     * @see #setBufferRequestBody(boolean)
     */
    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    /**
     * Set the underlying URLConnection's connect timeout (in milliseconds). A
     * timeout value of 0 specifies an infinite timeout.
     * <p>
     * Default is the system's default timeout.
     * 
     * @see URLConnection#setConnectTimeout(int)
     */
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /**
     * Set the underlying URLConnection's read timeout (in milliseconds). A
     * timeout value of 0 specifies an infinite timeout.
     * <p>
     * Default is the system's default timeout.
     * 
     * @see URLConnection#setReadTimeout(int)
     */
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    /**
     * Set if the underlying URLConnection can be set to 'output streaming'
     * mode. When output streaming is enabled, authentication and redirection
     * cannot be handled automatically. If output streaming is disabled the
     * {@link HttpURLConnection#setFixedLengthStreamingMode(int)
     * setFixedLengthStreamingMode} and
     * {@link HttpURLConnection#setChunkedStreamingMode(int)
     * setChunkedStreamingMode} methods of the underlying connection will never
     * be called.
     * <p>
     * Default is {@code true}.
     * 
     * @param outputStreaming
     *            if output streaming is enabled
     */
    public void setOutputStreaming(boolean outputStreaming) {
        this.outputStreaming = outputStreaming;
    }

    /**
     * Sets the task executor for this request factory. Setting this property is
     * required for {@linkplain #createAsyncRequest(URI, HttpMethod) creating
     * asynchronous request}.
     * 
     * @param taskExecutor
     *            the task executor
     */
    public void setTaskExecutor(AsyncListenableTaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    @Override
    public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod)
            throws IOException {
        HttpURLConnection connection = openConnection(uri.toURL(), this.proxy);
        prepareConnection(connection, httpMethod.name());
        if (this.bufferRequestBody) {
            return new SimpleBufferingClientHttpRequest(connection,
                this.outputStreaming);
        } else {
            return new SimpleStreamingClientHttpRequest(connection,
                this.chunkSize, this.outputStreaming);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Setting the
     * {@link #setTaskExecutor(org.springframework.core.task.AsyncListenableTaskExecutor)
     * taskExecutor} property is required before calling this method.
     */
    @Override
    public AsyncClientHttpRequest createAsyncRequest(URI uri,
            HttpMethod httpMethod) throws IOException {
        Assert.state(this.taskExecutor != null,
            "Asynchronous execution requires an "
                    + "AsyncTaskExecutor to be set");
        HttpURLConnection connection = openConnection(uri.toURL(), this.proxy);
        prepareConnection(connection, httpMethod.name());
        if (this.bufferRequestBody) {
            return new SimpleBufferingAsyncClientHttpRequest(connection,
                this.outputStreaming, this.taskExecutor);
        } else {
            return new SimpleStreamingAsyncClientHttpRequest(connection,
                this.chunkSize, this.outputStreaming, this.taskExecutor);
        }
    }

    /**
     * Opens and returns a connection to the given URL.
     * <p>
     * The default implementation uses the given
     * {@linkplain #setProxy(java.net.Proxy) proxy} - if any - to open a
     * connection.
     * 
     * @param url
     *            the URL to open a connection to
     * @param proxy
     *            the proxy to use, may be {@code null}
     * @return the opened connection
     * @throws IOException
     *             in case of I/O errors
     */
    protected HttpURLConnection openConnection(URL url, Proxy proxy)
            throws IOException {
        URLConnection urlConnection = (proxy != null ? url
            .openConnection(proxy) : url.openConnection());
        Assert.isInstanceOf(HttpURLConnection.class, urlConnection);
        return (HttpURLConnection) urlConnection;
    }

    /**
     * Template method for preparing the given {@link HttpURLConnection}.
     * <p>
     * The default implementation prepares the connection for input and output,
     * and sets the HTTP method.
     * 
     * @param connection
     *            the connection to prepare
     * @param httpMethod
     *            the HTTP request method ({@code GET}, {@code POST}, etc.)
     * @throws IOException
     *             in case of I/O errors
     */
    protected void prepareConnection(HttpURLConnection connection,
            String httpMethod) throws IOException {
        if (this.connectTimeout >= 0) {
            connection.setConnectTimeout(this.connectTimeout);
        }
        if (this.readTimeout >= 0) {
            connection.setReadTimeout(this.readTimeout);
        }
        connection.setDoInput(true);

        // BEGIN PATH
        // if ("GET".equals(httpMethod)) {
        // connection.setInstanceFollowRedirects(true);
        // } else {
        connection.setInstanceFollowRedirects(false);
        // } END PATCH

        if ("PUT".equals(httpMethod) || "POST".equals(httpMethod)
                || "PATCH".equals(httpMethod)) {
            connection.setDoOutput(true);
        } else {
            connection.setDoOutput(false);
        }
        connection.setRequestMethod(httpMethod);
    }

}
