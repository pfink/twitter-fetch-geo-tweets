/**
The MIT License (MIT)
Copyright (c) 2016 Patrick Fink

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package de.pfink.twitterapi;

import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.endpoint.Location;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;


public class TwitterFetchGeoTweets extends Thread {
    private static final String accessToken = "xxx";
    private static final String accessSecret = "xxx";
    private static final String consumerKey = "xxx";
    private static final String consumerSecret = "xxx";
    

    private static Location.Coordinate southwest;
    private static Location.Coordinate northeast;
    private static int limit = 20;
    

    public static void main(String[] args) throws ParseException {
        //Parse cmd line params
        Options options = buildCmdOptions();
        boolean printHelp;        
        
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        
        try {
            cmd = parser.parse(options, args);
        }
        catch(MissingOptionException ex) {
            printHelp = true;
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "java -jar twitter-fetch-geo-tweets.jar", options );
            System.exit(1);
        }        
      
        southwest = new Location.Coordinate((double) cmd.getParsedOptionValue("swlo"), (double) cmd.getParsedOptionValue("swla"));
        northeast = new Location.Coordinate((double) cmd.getParsedOptionValue("nelo"), (double) cmd.getParsedOptionValue("nela"));

        new TwitterFetchGeoTweets().start();
    }

    public void run() {
        BlockingQueue<String> queue = new LinkedBlockingQueue<String>(10000);
        StatusesFilterEndpoint endpoint = new StatusesFilterEndpoint();

        // filter by location
        endpoint.locations(Lists.newArrayList(new Location(southwest, northeast)));
        
        //authenticate
        Authentication auth = new OAuth1(consumerKey, consumerSecret, accessToken, accessSecret);

        // Create a new BasicClient. By default gzip is enabled.
        Client client = new ClientBuilder()
                .hosts(Constants.STREAM_HOST)
                .endpoint(endpoint)
                .authentication(auth)
                .processor(new StringDelimitedProcessor(queue))
                .build();

        // Establish a connection
        client.connect();
        
        try {
            //Open CSV file
            Writer filewriter = new FileWriter("tweets.csv", true);
        
            filewriter = new BufferedWriter(filewriter);
            filewriter.write("User;User screen name;Created At;Location;Text\n");
        
        
            //Parse messages and write them to csv file
            for (int msgRead = 0; msgRead < limit; msgRead++) {
                String jsonMsg = queue.take();
                JSONObject msg = new JSONObject(jsonMsg);
                
                List<String> csvLineList = new ArrayList();
                JSONObject user = msg.getJSONObject("user");
                
                csvLineList.add(user.getString("name"));
                csvLineList.add(user.getString("screen_name"));
                csvLineList.add(msg.getString("created_at"));                
                csvLineList.add(msg.getJSONObject("place").getString("full_name"));
                csvLineList.add(msg.getString("text"));
                
                String csvLine = StringUtils.join(csvLineList, ";")+"\n";
                System.out.println(csvLine);                
                filewriter.write(csvLine);                
                filewriter.flush();
            }        
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        client.stop();
    }
    
    
    public static Options buildCmdOptions() {
        //CMD Options
        Options options = new Options();
        options.addOption("l", "limit", true, "Number of fetched tweets. Default: 20");
        
        options.addOption(Option.builder("l")
                    .longOpt("limit")
                    .desc("Number of data sets until program ends. Default: 20")
                    .hasArg()
                    .type(Number.class)
                    .build());
        
        options.addOption(Option.builder("swla")
                    .longOpt("southWestLatitude")
                    .hasArg()
                    .type(Number.class)                    
                    .required()
                    .build());
        options.addOption(Option.builder("swlo")
                    .longOpt("southWestLongitude")
                    .hasArg()
                    .type(Number.class)
                    .required()
                    .build());
        
        options.addOption(Option.builder("nela")
                    .longOpt("NorthEastLatitude")
                    .hasArg()
                    .type(Number.class)                    
                    .required()
                    .build());
        
        options.addOption(Option.builder("nelo")
                    .longOpt("NorthEastLongiitude")
                    .hasArg()
                    .type(Number.class)
                    .required()
                    .build());        
        

        options.addOption("h", "help", false, "Print this help statement");
        
        return options;        
    }
}
