/*
Copyright 2017-2018 Government of Canada - Public Services and Procurement Canada - buyandsell.gc.ca

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package ca.gc.pspc.referencevonactuator.vonconnector;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Map;

/**
 * <code>HttpMethod</code> convenience encapsulates boilerplate to obtain HTTP GET or POST connections.
 */
public enum HttpMethod {
    GET,
    POST;

    /**
     * Returns an HTTP connection on input URL for current method (GET, POST). Sets content type for json form input
     * on POST connections.
     * 
     * @param url
     *      URL to which to connect
     *
     * @return HTTP connection.
     */
    public HttpURLConnection getConn(URL url) throws IOException {
        return getConn(url, null);
    }

    /**
     * Returns an HTTP connection on input URL for current method (GET, POST) with specified headers.
     * Sets content type for json form input on POST connections.
     * 
     * @param url
     *      URL to which to connect
     * @param headers
     *      Additional headers to specify in request
     *
     * @return HTTP connection.
     */
    public HttpURLConnection getConn(URL url, Map<String, String> headers) throws IOException {
        HttpURLConnection rv = (HttpURLConnection)url.openConnection();
        if (headers != null) {
            for (String header : headers.keySet()) {
                rv.setRequestProperty(header, headers.get(header));
            }
        }

        try {
            rv.setRequestMethod(toString());
        }
        catch (ProtocolException x) {
            // pass: enum allows only legal values
        }
        if (this == HttpMethod.POST) {
            rv.setDoOutput(true);
            rv.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        }

        return rv;
    }
}
