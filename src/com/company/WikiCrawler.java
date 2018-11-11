package com.company;

import java.io.*;
import java.net.URL;
import java.util.*;

public class WikiCrawler {

    private static final String BASE_URL = "https://en.wikipedia.org";
    private String SEED_URL;// https://en.wikipedia.org + /wiki/Physics :/wiki/Physics is SEED_URL
    private int MAX;//Stores the max number of nodes over which the graph has to be constructed
    private int count = 0;//Counts the number of nodes that have been added to the Queue so far
    private boolean topicSensitive;
    private String name;//Stores the name of the file to which the graph will be written
    private String[] TOPICS;
    private int politeness = 0;//wait for 2 seconds after every 10 requests
    private HashSet<String> VISITED = new HashSet<>();
    private HashSet<String> DoNotVisit = new HashSet<>();//Stores the links present in the robots.txt file
    private Comparator<Tuple> myComp = new TupleComparator();
    private PriorityQueue<Tuple> BFSQueue = new PriorityQueue<Tuple>(myComp);

    public WikiCrawler(String seedUrl, String[] keywords, int max, String fileName, boolean isTopicSensitive) {

        SEED_URL = seedUrl;
        MAX = max;
        TOPICS = keywords;
        name = fileName;
        topicSensitive = isTopicSensitive;
    }

    public static void main(String[] args) {

        String[] topics = {"tennis", "grand slam"};
        WikiCrawler w = new WikiCrawler("/wiki/Tennis", topics, 500, "WikiTennisGraph.txt", false);
        w.crawl();

    }

    public void crawl() {

        boolean debug = true;
        //Download the robots.txt file and store the links appearing in it in a hashTable
        activateRobots();

        try {

            PrintStream out = new PrintStream(new File(name));
            PrintStream console = System.out;
            //Change this to print to file
//            System.setOut(out);

            System.out.println(MAX);//The first line should indicate the number of vertices
            VISITED.add(SEED_URL);

            if (!topicSensitive) {
                BFSQueue.add(new Tuple(SEED_URL, count));
                count++;
            } else {
                BFSQueue.add(new Tuple(SEED_URL, 0, count));//Check if this is correct. Adding the first link with a weight of 0.
                count++;
            }

            String seed = "";
            while (!BFSQueue.isEmpty()) {
                seed = BFSQueue.poll().getLink();
                if (debug) System.out.println("Extracted Link from Q: " + seed);
                String actualText = actualTextComponent(seed);
                extractLinks(actualText, seed);
            }
            System.out.println("Value of Visited.size(): " + VISITED.size());
            System.out.println("Value of count: " + count);
//        out.close();
//        out.flush();

            System.setOut(console);
//        System.out.println("Where does this print?");
        } catch (Exception e) {
            System.out.println(e);
        }


    }

    //This method populates the links form the robots.txt page and stores them in
    //the DoNotVisit HashSet where they will be compared
    //with every future link that we see
    private void activateRobots() {

        boolean debug = true;
        String myString = "";

        try (InputStream is = new URL(BASE_URL + "/robots.txt").openStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(is));
        ) {
//            Scanner sc = new Scanner(url.openStream());

            StringBuilder content = new StringBuilder(1024);
            String s = "";
            while ((s = br.readLine()) != null) {
                s = s + "\n";
                content.append(s);
            }
            myString = content.toString();
            //System.out.println(content);
        } catch (IOException ex) {
            System.out.println(ex);
        }
        String[] temp;
        temp = myString.split("User-agent: \\*");// Split the robot.txt file at the required position
        myString = temp[1];
//        System.out.println(myString);
        System.out.println("********** Start Robots.txt *************");

        //Populating the DoNotVisit HashSet
        int startIndex = 0;
        int stopIndex = 0;
        while (true) {
            startIndex = myString.indexOf("Disallow:", startIndex);
            stopIndex = myString.indexOf("\n", startIndex + 9);
            String tempString = myString.substring(startIndex + 10, stopIndex);//tempString stores the disallowed URL
            DoNotVisit.add(tempString);
            if (debug) System.out.println(tempString);
            if (myString.indexOf("Disallow:", stopIndex + 1) == -1) {
                break;
            } else {
                startIndex = stopIndex;
            }
        }
        System.out.println("********** End Robots.txt *************");

    }

    //This method returns the html code of the page after the <p> tag as a String
    //Corresponds to the actualtextContent of the web page
    private String actualTextComponent(String seed_url) {
        String myString = "";
        try {
            URL url = new URL(BASE_URL + seed_url);
            InputStream is = url.openStream();
            politeness++;
            if (politeness > 10) {
                try {
                    Thread.sleep(2000);
                    System.out.println("Sleeping......");
                } catch (Exception e) {
                    System.out.println(e);
                }
                politeness = 0;
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder content = new StringBuilder(1024);
            String s = "";
            while ((s = br.readLine()) != null) {
                s = s + "\n";
                content.append(s);
            }
            myString = content.toString();
            //System.out.println(content);
        } catch (IOException ex) {

            System.out.println(ex);
        }

        String[] temp = myString.split("<p>", 2);
        String actualtextComp = "<p>" + temp[1];//returns the html code after the first <p> tag
//        System.out.println(actualtextComp);
        return actualtextComp;
    }

    //Takes the actualTextComponent as input
    //Extracts the wiki links from the html page
    private void extractLinks(String actualTextComp, String seed) {

        boolean debug = true;
        boolean deepDebug = false;
        int startIndex = 0;
        int stopIndex = 0;
        HashSet<String> alreadyAdded = new HashSet<>();
//        System.out.println("**** SEED URL IS: " + seed_url + " ******" );
        if (actualTextComp.contains("\"/wiki/")) {
            while (true) {

                startIndex = actualTextComp.indexOf("\"/wiki/", startIndex);
//            System.out.println("Start Index is: " + startIndex);
                stopIndex = actualTextComp.indexOf("\"", startIndex + 1);
//            System.out.println("Stop Index is: " + stopIndex);
                String tempString = actualTextComp.substring(startIndex + 1, stopIndex);
                if (deepDebug) System.out.println("Link is:" + tempString);

                if (VISITED.size() < MAX && VISITED.contains(tempString) &&
                        !(alreadyAdded.contains(tempString)) && !(seed.equals(tempString))) {
                    if (debug) System.out.println("Adding edge 1");
                    System.out.println(seed + " " + tempString);
                    alreadyAdded.add(tempString);
                }

                if (VISITED.size() < MAX && checkCriteria(tempString, seed)) {
                    if (debug) System.out.println("Adding edge 2");

                    System.out.println(seed + " " + tempString);
                    VISITED.add(tempString);
                    if (!topicSensitive) {
                        BFSQueue.add(new Tuple(tempString, count));
                    } else {
                        BFSQueue.add(new Tuple(tempString, 0, count));//!!!!!!!!!!!!!!!!! CHANGE THIS !!!!!!!!!!!!!!!!!
                    }
                    alreadyAdded.add(tempString);
                    count++;
                }

                if (VISITED.size() == MAX) {
                    if (VISITED.contains(tempString) && !(alreadyAdded.contains(tempString)) &&
                            !(seed.equals(tempString))) {
//                    System.out.println("Edge already formed with " + tempString + " is " +
//                            alreadyAdded.contains(tempString));
                        if (debug) System.out.println("Adding edge 3");
                        System.out.println(seed + " " + tempString);
                        alreadyAdded.add(tempString);
                    }

                }

                if (actualTextComp.indexOf("\"/wiki/", stopIndex + 1) == -1) {
                    break;
                } else {
                    startIndex = stopIndex;
                }

            }
        }
    }

    private boolean checkCriteria(String url, String seed_url) {

        boolean debug = true;
        if (url.contains("#") || url.contains(":")) {
            if (debug) System.out.println(url + " :Failed (url.contains(\"#\") || url.contains(\":\")");
            return false;
        }
        if (url.equals(seed_url)) {
            if (debug) System.out.println(url + " :Failed (url.equals(seed_url)");
            return false;
        }

        if (VISITED.contains(url)) {
            if (debug) System.out.println(url + " :Failed (VISITED.contains(url))");
            return false;
        }

        if (DoNotVisit.contains(url)) {
            if (debug) System.out.println(url + " Failed URL in Robots.txt");
            return false;
        }

        return true;

    }

}
