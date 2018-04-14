# twitter-fetch-geo-tweets
[![No Maintenance Intended](http://unmaintained.tech/badge.svg)](http://unmaintained.tech/)

Small tool to filter and show tweets by location using Twitter Streaming API.

## Usage

Execute via CLI. Tool drops a `tweets.csv` file with the most important attributes of a tweet in the folder where the tool is executed.

### Example: Fetch tweets from New York City

`java -jar twitter-fetch-geo-tweets.jar -swlo -74.0 -swla 40.0 -nelo -73.0 -nela 41.0`

### Command-line reference

```
usage: java -jar twitter-fetch-geo-tweets.jar
 -h,--help                           Print this help statement
 -l,--limit <arg>                    Number of data sets until program
                                     ends. Default: 20
 -nela,--NorthEastLatitude <arg>
 -nelo,--NorthEastLongiitude <arg>
 -swla,--southWestLatitude <arg>
 -swlo,--southWestLongitude <arg>
```
