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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * <code>Config</code> singleton finds configuration file and handles queries for configuration data
 * particular to VON connector operations.
 */
public class Config {

    private static Config theInstance = null;
    private Properties props;

    /**
     * Return the instance, populating it first if necessary.
     * 
     * @return the instance
     */
    synchronized static public Config getInstance() {
        if (theInstance == null) {
            theInstance = new Config();
        }

        return theInstance;
    }

    private Config() {
        props = new Properties();
        URL cfgPropsLoc = Thread
                .currentThread()
                .getContextClassLoader()
                .getResource("von-connector/config.properties");
        InputStream input = null;
        try {
            Path p = Paths.get(cfgPropsLoc.toURI());
            input = new FileInputStream(p.toFile());
            props.load(input);
        }
        catch (IOException | URISyntaxException x) {
            // still in static context: abandon startup sequence
            x.printStackTrace();
            System.exit(1);
        }
        finally {
            if (input != null) {
                try {
                    input.close();
                }
                catch (IOException x) {
                    // pass through; file closes on JVM exit
                }
            }
        }
    }

    /**
     * Return (<code>String</code>) value of input property, null if not present.
     * 
     * @param prop
     *     name of property to return
     *
     * @return value of property named
     */
    public String get(String prop) {
        return props.getProperty(prop);
    }

    /**
     * Return <code>Properties</code> subset of configuration with names starting with input prefix;
     * omit prefix in returned property names.
     * 
     * @param prefix
     *      prefix for properties of interest
     *
     * @return
     *      matching properties subset, with input prefix removed from property name
     */
    public Properties getPrefixed(String pfx) {
        Properties rv = new Properties();
        for (String k : props.stringPropertyNames()) {
            if (k.startsWith(pfx)) {
                rv.setProperty(k.substring(pfx.length()), props.getProperty(k));
            }
        }

        return rv;
    }

    /**
     * Return <code>Properties</code> subset of configuration with names matching input regex pattern.
     * 
     * @param pattern
     *      regex pattern for properties of interest
     *
     * @return
     *      matching properties subset
     */
    public Properties getMatching(String pattern) {
        Properties rv = new Properties();
        for (String k : props.stringPropertyNames()) {
            if (k.matches(pattern)) {
                rv.setProperty(k, props.getProperty(k));
            }
        }

        return rv;
    }
}
