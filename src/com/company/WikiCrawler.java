package com.company;

import java.io.*;
import java.net.URL;
import java.util.*;

public class WikiCrawler {

//    private static final String BASE_URL = "https://en.wikipedia.org";
    private static final String BASE_URL = "http://web.cs.iastate.edu/~pavan";
    private String SEED_URL;// https://en.wikipedia.org + /wiki/Physics :/wiki/Physics is SEED_URL
    private int MAX;//Stores the max number of nodes over which the graph has to be constructed
    private int count = 0;//Counts the number of nodes that have been added to the Queue so far
    private boolean topicSensitive;
    private String name;//Stores the name of the file to which the graph will be written
    private String[] TOPICS;
    private int politeness = 0;//wait for 2 seconds after every 10 requests
    private HashSet<String> VISITED = new HashSet<>();
    private HashSet<String> DoNotVisit = new HashSet<>();//Stores the links present in the robots.txt file
    private Hashtable<String, Float> BFSQAsHash = new Hashtable<String, Float>();//Stores the links in BFSQueue to make searching quicker when isTopicSensitive is true.
    private Comparator<Tuple> myComp = new TupleComparator();//Comparator that is passed to the Priority Queue
    private PriorityQueue<Tuple> BFSQueue = new PriorityQueue<Tuple>(myComp);//The Priority Queue for the BFS


    public static void main(String[] args) {

        String[] topics = {"tennis", "grand slam"};
//        WikiCrawler w = new WikiCrawler("/wiki/Tennis", topics, 100, "ComplexityTheoryGraph.txt", false);
        WikiCrawler w = new WikiCrawler("/wiki/L.html", topics, 10, "PAVAN_TEST_SITE.txt", false);
        w.crawl();
    }

    public WikiCrawler(String seedUrl, String[] keywords, int max, String fileName, boolean isTopicSensitive) {

        SEED_URL = seedUrl;
        MAX = max;
        TOPICS = keywords;
        name = fileName;
        topicSensitive = isTopicSensitive;
    }

    public void crawl() {

        boolean debug = false;
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
                if(debug) System.out.println("Not Topic Sensitive - Adding SEED_URL to the Queue");
                count++;
            } else {
                BFSQueue.add(new Tuple(SEED_URL, 0, count));//Check if this is correct. Adding the first link with a weight of 0.
                BFSQAsHash.put(SEED_URL, 0f);
                count++;
            }

            String seed = "";
            while (!BFSQueue.isEmpty()) {
                if(debug) System.out.println("*********************************************************");
                seed = BFSQueue.poll().getLink();
                if (topicSensitive) BFSQAsHash.remove(seed);
                if (debug) System.out.println("########## Extracted Link from Q: " + seed);
                String actualText = actualTextComponent(seed);
                //If the url is broken, ignore that and move onto the next one
                if (actualText == null) continue;
                //If isTopicSensitive is FALSE then perform the normal BFS
                if(!topicSensitive) extractLinks(actualText, seed);
                //If isTopicSensitive is TRUE, then perform the weighted BFS
                if (topicSensitive) extractLinks(actualText, seed, true);
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

    //This method populates the links from the robots.txt page and stores them in
    //the DoNotVisit HashSet where they will be compared
    //with every future link that we see
    private void activateRobots() {

        boolean debug = true;
        String myString = "";

        try (InputStream is = new URL(BASE_URL + "/wiki/robots.txt").openStream();
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
        System.out.println(myString);
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
    //Corresponds to the actualTextContent of the web page
    private String actualTextComponent(String seed_url) {

        boolean debug = false;
        String myString = "";

        String newURL = BASE_URL + seed_url;
        if(debug) System.out.println("Trying to access the URL: " + newURL );
        //http://web.cs.iastate.edu/~pavan/wiki/A.html
        try(InputStream is = new URL(newURL).openStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is))
        ) {
//            URL url = new URL(BASE_URL + seed_url);
//            InputStream is = url.openStream();
            politeness++;
            if (politeness > 10) {

                try {
                    Thread.sleep(2000);
                    if(debug) System.out.println("Sleeping......");
                } catch (Exception e) {
                    System.out.println(e);
                }
                politeness = 0;
            }
            StringBuilder content = new StringBuilder(1024);
            String s = "";
            while ((s = br.readLine()) != null) {
                s = s + "\n";
                content.append(s);
//                if(debug)System.out.println("In while loop: " + s);
            }
            myString = content.toString();
            if(debug) System.out.println(myString);
        } catch (IOException ex) {
            System.out.println(ex);
        }

        String[] temp = myString.split("<p>", 2);
        String actualtextComp = "";
        if(temp.length > 1) {
            //Split the String only if it contains <p>
            //Sometimes links are bad. They are old or badly formatted due to which
            //there is no page present at that link
            //Trying to access temp[1] goive indexOutOfBounds in those cases
            //THis avoids that
            actualtextComp = "<p>" + temp[1];//returns the html code after the first <p> tag
//        System.out.println(actualtextComp);
        }
        return actualtextComp;
    }

    //Takes the actualTextComponent as input
    //Extracts the wiki links from the html page
    //This is for the Simple BFS when the isTopicSensitive is set to FALSE
    private void extractLinks(String actualTextComp, String seed) {

        boolean debug = false;
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

        boolean debug = false;

        if (VISITED.contains(url)) {
            if (debug) System.out.println(url + " :Failed (VISITED.contains(url))");
            return false;
        }

        if (url.contains("#") || url.contains(":")) {
            if (debug) System.out.println(url + " :Failed (url.contains(\"#\") || url.contains(\":\")");
            return false;
        }
        if (url.equals(seed_url)) {
            if (debug) System.out.println(url + " :Failed (url.equals(seed_url)");
            return false;
        }

        if (DoNotVisit.contains(url)) {
            if (debug) System.out.println(url + " Failed URL in Robots.txt");
            return false;
        }

        return true;

    }


    //Takes the actualTextComponent as input
    //Extracts the wiki links from the html page
    //This is for the Weighted BFS when the isTopicSensitive is set to TRUE
    private void extractLinks(String actualTextComp, String seed, boolean topicSensitive) {

        boolean debug = false;
        boolean debugText = false;//Debug the breakup of text in the complete Anchor Tag
        boolean deepDebug = false;//Creates a shit ton of data
        boolean debugWG = false;//Debug the weighted graph part only

        int startIndex = 0;//Start index of /wiki/XXXXXX
        int stopIndex = 0;//Stop index of /wiki/XXXXXX
        int startAnchorTag = 0;//Start index of <a href="/wiki/Racket_sport" class="mw-redirect" title="Racket sport">racket sport</a>
        int stopAnchorTag = 0;//Stop index of <a href="/wiki/Racket_sport" class="mw-redirect" title="Racket sport">racket sport</a>
        String anchorTagText = "";//Stores the anchor tag text "racket sport" without quotations
        String httpText = "";//Stores the "/wiki/Racket_sport" without quotations
        String entireHref = "";//Stores the entire href <a href="/wiki/Racket_sport" class="mw-redirect" title="Racket sport">racket sport</a>

        HashSet<String> alreadyAdded = new HashSet<>();//Forgot what this was supposed to store. But I guess this is important

//        System.out.println("**** SEED URL IS: " + seed_url + " ******" );
        if (actualTextComp.contains("\"/wiki/")) {
            while (true) {
                if (debug) System.out.println("*********************");
                startIndex = actualTextComp.indexOf("\"/wiki/", startIndex);
                startAnchorTag = Math.max(startIndex - 8, 0) ;//Math.max to take care of indexOutOfBounds
//            System.out.println("Start Index is: " + startIndex);
                stopIndex = actualTextComp.indexOf("\"", startIndex + 1);
                stopAnchorTag = actualTextComp.indexOf(">", stopIndex) + 1;//Update this
                anchorTagText = actualTextComp.substring(stopAnchorTag, actualTextComp.indexOf("<", stopAnchorTag));
                if (debugText) System.out.println("anchorTagText:**" + anchorTagText+"**");
                stopAnchorTag = actualTextComp.indexOf("</a>", stopAnchorTag) + 4;
                entireHref = actualTextComp.substring(startAnchorTag, stopAnchorTag);
                if (debugText) System.out.println("entireHref:**" + entireHref + "**");
                httpText = actualTextComp.substring(startIndex + 1, stopIndex);
                if (debugText) System.out.println("httpText:**" + httpText+"**");
                if (deepDebug) System.out.println("Link is:" + httpText);

                //Now here we start finding the distances
                //Heuristic begins here
                /*
                Rough Algorithm
                For this link l
                    Search for all instances of this link in the current page actualTextComp
                    If the anchortext of the link or the http address contains a word from T
                        weight of the link l = 1
                    else{
                        look at the text from (index = startIndex - 17) to (index = stopIndex + 17)
                        for every term t in T{
                            if(dist(t from the link l) < minDistanceSeenSoFar){
                                minDistanceSeenSoFar = dist(t from the link l);
                                boolean termFound = true
                            }
                        }
                        weight of the link l = 1/(minDistanceSeenSoFar + 2)
                    }

                */

                //If the anchorTagText or the httpText contains the strings,
                // set the weight to 1
                float linkWeight = 0;//stores the weight of the httpText i.e. seed --- httpText
                boolean hit = false;//is TRUE if any of the terms in the TOPICS Array is found in the anchor or the http text.
                for (int i = 0; i < TOPICS.length; i++){
                    String t = TOPICS[i].toLowerCase();
                    if(anchorTagText.toLowerCase().contains(t) ||
                            httpText.toLowerCase().contains(t)){
                        if(debugWG) System.out.println("Hit!");
                        if(debugWG) System.out.println("anchorTagText: " + anchorTagText);
                        if(debugWG) System.out.println("httpText: " + httpText);
                        if(debugWG) System.out.println("Topics: " + t);

                        linkWeight = 1;
                        hit = true;
                    }
                }
                if(debugWG) if(!hit) System.out.println("NO HIT!!!!!");
                //String[] topics = {"tennis", "grand slam"};

                /*
                The below if statement captures the case where:
                A - B - C
                B - D - E
                C - F - G
                D - H - F - J
                E - J - K
                Say you are currently at page E and you saw the link to page J
                - Less than MAX number of nodes have been explored, so VISITED.size() < MAX
                - J was already added to the BFSQueue and VISITED when you first visited D, so VISITED.contains(httpText)
                - Since in page E, this si the first time you are seeing J, you have not added J to alreadyAdded yet.
                Hence this edge has to be added to the graph. So, alreadyAdded.contains(httpText) is FALSE
                - Finally, this link is not a self loop, so seed.equals(httpText) is also FALSE.
                */

                if (VISITED.size() < MAX && VISITED.contains(httpText) &&
                        !(alreadyAdded.contains(httpText)) && !(seed.equals(httpText))) {
                    if (debug) System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>> Adding edge 1");
                    System.out.println(seed + " " + httpText);
                    alreadyAdded.add(httpText);

                    //Now we will have to check if the current link from E - J has a higher weight than
                    // the previous link from D - J
                    Float prevWeight = BFSQAsHash.get(httpText);
                    if(prevWeight != null){
                        //This means that the current link httpText exists in the BFSQueue...
                        if (prevWeight < linkWeight){
                            //..and its weight in the queue is lesser than the current weight: linkWeight
                            //So we remove the Tuple from the BFSQueue
                            //The equals method that we have implemented in the Tuple class
                            //checks for equality only by comparing the link names.
                            //So we do not need to worry about the remaining fields as long
                            //as the names of the links are the same
                            //Hopefully...
                            if(debugWG) System.out.println("******** IF STATEMENT 1 *********");
                            if(debugWG) System.out.println("Removing:");
                            if(debugWG) System.out.println("Link: " + httpText );
                            if(debugWG) System.out.println("Earlier Weight: " + prevWeight );
                            if(debugWG) System.out.println("New Weight: " + linkWeight );
                            if(debugWG) System.out.println("Earlier Size of Q: " + BFSQueue.size());
                            BFSQueue.remove(new Tuple(httpText, prevWeight, 0));
                            if(debugWG) System.out.println("After removal Size of Q: " + BFSQueue.size());
                            if(debugWG) System.out.println("Was the correct element removed? - " + !BFSQueue.contains(new Tuple(httpText, prevWeight, 0)));

                            //And add a new Tuple to the BFSQueue
                            BFSQueue.add(new Tuple(httpText, linkWeight, count));
                            if(debugWG) System.out.println("After Adding new element Size of Q: " + BFSQueue.size());
                            if(debugWG) System.out.println("Was the correct element Added? - " + BFSQueue.contains(new Tuple(httpText, prevWeight, 0)));

                            count++;
                            //And update the BFSQAsHash as well
                            BFSQAsHash.remove(httpText);
                            BFSQAsHash.put(httpText, linkWeight);
                            if(debugWG) System.out.println("****** END OF IF STATEMENT 1 ********");
                        }
                    }
                }

                if (VISITED.size() < MAX && checkCriteria(httpText, seed)) {
                    if (debug) System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>> Adding edge 2");

                    System.out.println(seed + " " + httpText);
                    VISITED.add(httpText);
                    //Since this is the first time that we are seeing this node
                    //We can simply goi= ahead and add this node to the BFSQueue
                    //Without any checks
                    BFSQueue.add(new Tuple(httpText, linkWeight, count));//!!!!!!!!!!!!!!!!! CHANGE THIS !!!!!!!!!!!!!!!!!
                    alreadyAdded.add(httpText);
                    BFSQAsHash.put(httpText, linkWeight);
                    count++;
                }

                /*
                This captures the case where MAX number of nodes have already been visited
                NOw all we need to do is pop out the elements from the BFSQueue one by one
                And check if they have edges to any of the MAX number of nodes seen so far
                I will have to update the BFSQueue in this case too, wont I?
                It seems like it shouldnt matter
                But the resulting graph may turn out a bit different if we use the Heuristic
                Or so it seems atleast...
                I believe we can just copy-paste the code from the previous if statement?

                */
                if (VISITED.size() == MAX) {
                    if (VISITED.contains(httpText) && !(alreadyAdded.contains(httpText)) &&
                            !(seed.equals(httpText))) {
//                    System.out.println("Edge already formed with " + tempString + " is " +
//                            alreadyAdded.contains(tempString));
                        if (debug) System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>> Adding edge 3");
                        System.out.println(seed + " " + httpText);
                        alreadyAdded.add(httpText);


                        //Copied and Pasted from earlier case ------
                        //Now we will have to check if the current link from E - J has a higher weight than
                        // the previous link from D - J
                        Float prevWeight = BFSQAsHash.get(httpText);
                        if(prevWeight != null){
                            //This means that the current link httpText exists in the BFSQueue...
                            if (prevWeight < linkWeight){
                                //..and its weight in the queue is lesser than the current weight: linkWeight
                                //So we remove the Tuple from the BFSQueue
                                //The equals method that we have implemented in the Tuple class
                                //checks for equality only by comparing the link names.
                                //So we do not need to worry about the remaining fields as long
                                //as the names of the links are the same
                                //Hopefully...
                                if(debugWG) System.out.println("******** IF STATEMENT 3 *********");
                                if(debugWG) System.out.println("Removing:");
                                if(debugWG) System.out.println("Link: " + httpText );
                                if(debugWG) System.out.println("Earlier Weight: " + prevWeight );
                                if(debugWG) System.out.println("New Weight: " + linkWeight );
                                if(debugWG) System.out.println("Earlier Size of Q: " + BFSQueue.size());
                                BFSQueue.remove(new Tuple(httpText, prevWeight, 0));
                                if(debugWG) System.out.println("After removal Size of Q: " + BFSQueue.size());
                                if(debugWG) System.out.println("Was the correct element removed? - " + !BFSQueue.contains(new Tuple(httpText, prevWeight, 0)));

                                //And add a new Tuple to the BFSQueue
                                BFSQueue.add(new Tuple(httpText, linkWeight, count));
                                if(debugWG) System.out.println("After Adding new element Size of Q: " + BFSQueue.size());
                                if(debugWG) System.out.println("Was the correct element Added? - " + BFSQueue.contains(new Tuple(httpText, prevWeight, 0)));

                                count++;
                                //And update the BFSQAsHash as well
                                BFSQAsHash.remove(httpText);
                                BFSQAsHash.put(httpText, linkWeight);
                                if(debugWG) System.out.println("****** END OF IF STATEMENT 3 ********");

                            }
                        }

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

}
