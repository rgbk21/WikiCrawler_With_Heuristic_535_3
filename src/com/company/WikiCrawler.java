package com.company;

import java.io.*;
import java.net.URL;
import java.util.*;

public class WikiCrawler {

    private static final String BASE_URL = "https://en.wikipedia.org";
//    private static final String BASE_URL = "http://web.cs.iastate.edu/~pavan";
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
    private HashSet<String> TOPICSasHASH = new HashSet<>();

//    private Hashtable<String,Integer>

    public static void main(String[] args) {

        String[] topics = {"tennis", "wimbledon"};
//        String[] topics = {};
        WikiCrawler w = new WikiCrawler("/wiki/Tennis", topics, 100, "ComplexityTheoryGraph.txt", true);
//        WikiCrawler w = new WikiCrawler("/wiki/L.html", topics, 10, "PAVAN_TEST_SITE.txt", false);
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

        boolean debug = true;
        //Download the robots.txt file and store the links appearing in it in a hashTable
        activateRobots();

        try {

            PrintStream file = new PrintStream(new File(name));
            PrintStream console = System.out;
            //Change this to print to file
            System.setOut(file);

            System.out.println(MAX);//The first line should indicate the number of vertices
            VISITED.add(SEED_URL);

            if (!topicSensitive) {
                BFSQueue.add(new Tuple(SEED_URL, count));
                if(debug) System.out.println("Not Topic Sensitive - Adding SEED_URL to the Queue");
                count++;
            } else {
                //Copy the contents of the TOPIC Array List ot the HashSet
                for(int i = 0; i < TOPICS.length; i++){
                    TOPICSasHASH.add(TOPICS[i].toLowerCase());
                }
                BFSQueue.add(new Tuple(SEED_URL, 0, count));//Check if this is correct. Adding the first link with a weight of 0.
                BFSQAsHash.put(SEED_URL, 0f);
                count++;
            }

            String seed = "";
            while (!BFSQueue.isEmpty()) {
                if(debug) System.out.println("*********************************************************");
                Tuple nextTuple = BFSQueue.poll();
                seed = nextTuple.getLink();

                if(debug) System.out.println("Queue returned:");
                if(debug) System.out.println("Link to Page: " + nextTuple.getLink());
                if(debug) System.out.println("With Weight: " + nextTuple.getWeight());
                if(debug) System.out.println("And Count: " + nextTuple.getCountNum());

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

        boolean debug = false;
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

        if(temp.length > 1) {
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
        }else{
            System.out.println("ERROR: robots.txt not found at this link:" + BASE_URL + "/robots.txt");
        }

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
        boolean debugWG = true;//Debug the weighted graph part only
        boolean debug_edge = true;//Debug the edge formations

        int startIndex = 0;//Start index of /wiki/XXXXXX
        int stopIndex = 0;//Stop index of /wiki/XXXXXX
        int startAnchorTag = 0;//Start index of <a href="/wiki/Racket_sport" class="mw-redirect" title="Racket sport">racket sport</a>
        int stopAnchorTag = 0;//Index of </a> in: <a href="/wiki/Racket_sport" class="mw-redirect" title="Racket sport">racket sport</a>
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
                stopAnchorTag = actualTextComp.indexOf("</a>", stopAnchorTag);
                //"</a>" anotherIndex stores the end index of this
                int anotherIndex = actualTextComp.indexOf(">", stopAnchorTag) ;
                entireHref = actualTextComp.substring(startAnchorTag, anotherIndex + 1);
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

                /*THIS SHOULD PROBABLY BE ANOTHER METHOD BUT THERE IS JUST TOO MUCH STUFF TO PASS TO THE METHOD*/

                /* STARTING CALCULATION FOR LINK WEIGHT*/

                //CASE 1:
                //If the anchorTagText or the httpText contains the strings,
                // set the weight to 1
                float linkWeight = 0;//stores the weight of the httpText i.e. seed --- httpText
                boolean hit = false;//is TRUE if any of the terms in the TOPICS Array is found in the anchor or the http text.
                int minDist;//Stores the minimum distance returned by the getHeuristicDistance

                if( (!httpText.contains("#")) && (! httpText.contains(":"))){
                    for (int i = 0; i < TOPICS.length; i++) {
                        String t = TOPICS[i].toLowerCase();
                        if (anchorTagText.toLowerCase().contains(t) ||
                                httpText.toLowerCase().contains(t)) {
                            if (debugWG) System.out.println("Hit!");
                            if (debugWG) System.out.println("anchorTagText: " + anchorTagText);
                            if (debugWG) System.out.println("httpText: " + httpText);
                            if (debugWG) System.out.println("Topics: " + t);

                            linkWeight = 1;
                            hit = true;
                        }
                    }
                }
                if(debugWG) if(!hit) System.out.println("NO HIT!!!!!");
                //String[] topics = {"tennis", "grand slam"};

                //CASE 2:
                //If the anchorTagText or the httpText DOIES NOT contain the strings,
                // Search in the surrounding text of the httpText (the link)
                if(!hit && (!httpText.contains("#") && !httpText.contains(":"))) {
                    System.out.println("*******EXTRACTING TEXT FOR*********");
                    System.out.println(httpText);
                    minDist = getHeuristicDistance(startAnchorTag, stopAnchorTag, actualTextComp);
                    System.out.println("For link: "+ seed + "------" + httpText);
                    if (debugWG) System.out.println("minDist: " + minDist);
                    if(minDist < 18){
                        linkWeight = calculateWeight(minDist);
                        if (debugWG) System.out.println("linkWeight: " + linkWeight);
                    }else{
                        linkWeight = 0;
                        if (debugWG) System.out.println("linkWeight: " + linkWeight);
                    }
                }

                /*
                //What about the case if you see the same link again?
                A - B - C
                B - D - E
                C - F - G
                D - H - F - J - H
                E - J - K

                Suppose page D has 2 links to the same page H, and the weight of the second H is more than the first H.
                You will have to replace the H in the BFSQ and the BFSQasHash with the new weight.
                But you will not add a new edge
                */
                if(alreadyAdded.contains(httpText)){
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
                            if(debugWG) System.out.println("******** IF STATEMENT 0 *********");
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
                            if(debugWG) System.out.println("****** END OF IF STATEMENT 0 ********");
                        }
                    }
                }

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
                    if (debug_edge) System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>> Adding edge 1");
                    System.out.println(seed + " " + httpText);
                    alreadyAdded.add(httpText);

                    //Now we will have to check if the current link from E - J has a higher weight than
                    // the previous link from F - J
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
                    if (debug_edge) System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>> Adding edge 2");

                    System.out.println(seed + " " + httpText);
                    VISITED.add(httpText);
                    //Since this is the first time that we are seeing this node
                    //We can simply go ahead and add this node to the BFSQueue
                    //Without any checks
                    if(debug_edge) if(BFSQAsHash.contains(httpText) || BFSQueue.contains(new Tuple(httpText, linkWeight, count))){
                        System.out.println("SOMETHING WENT HORRIBLY WRONG!!!");
                    }
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
                        if (debug_edge) System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>> Adding edge 3");
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

    private int getHeuristicDistance(int startAnchorTag, int stopAnchorTag, String actualTextComp){

        boolean debug = false;
        boolean debug1 = false;
        boolean debug_s = false;
        boolean debug_op = true;//prints out only the strings that are being added + other imp stuff
        boolean debug2 = false;//prints out the initial character before going into the while loop, fwd direction

        int textLimit = 18;//Controls how many words to the left and to the right you are going to focus on
        //minBackwardsDistStores the minimum index that gave a match with a word in the TOPICS
        // list in the backward direction
        int minBackwardsDist = Integer.MAX_VALUE;
        //Stores the minimum index that gave a match with a word in the TOPICS list
        // in the forward direction
        int minForwadsDist = Integer.MAX_VALUE;
        int minDist = 0;//Stores the minimum among the minBackwardsDist and minForwadsDist values

//        int lastIndex = actualTextComp.lastIndexOf("<a href");
        if(debug) System.out.println("startAnchorTag: " + startAnchorTag);
        int lastIndex = startAnchorTag;
        if(debug_s) System.out.println(actualTextComp.length());
        if(debug_s) System.out.println(lastIndex);
        if(debug_s) System.out.println(actualTextComp.charAt(lastIndex));
        int spaceCount = 1;
        int currIndex = lastIndex - 1;
        char c1 = actualTextComp.charAt(currIndex);
        while( ! ((c1 >= 65 && c1 <= 90) || (c1 >= 97 && c1 <=122) || (c1 == 60) || (c1 == 62) || (c1 >= 48 && c1 <=57) )){
            currIndex--;
            c1 = actualTextComp.charAt(currIndex);
            if(debug_s) System.out.println("New value of c: " + c1);
        }

        String seq = "";
        //and's estate of <a href="/wiki/Nantclwyd_Hall" title="Nantclwyd Hall">Nantclwyd Hall</a>,
        // in <a

        while(spaceCount < textLimit && currIndex >= 0){
            if(debug) System.out.println("At the top: " + currIndex);

            if(actualTextComp.charAt(currIndex) == ' '){

                if(debug1) System.out.println("1. Calling formatSeq with String: " + seq);
                seq = formatSeq(seq);
                if(seq.length() > 0) {
                    if(debug_op) System.out.println("1. Current String:" + seq);

                    //Checking if the term seq exists in the TOPICSasHASH table
                    if(checkSeq(seq)){
                        minBackwardsDist = spaceCount;
                        if(debug_op)System.out.println("1. Back Direction Found Match: " + seq);
                        break;
                    }
                    seq = "";
                    spaceCount++;
                    if (debug) System.out.println("1. Word Num is:" + spaceCount);
                }
            }
            else if(actualTextComp.charAt(currIndex) == '>'){

                String ignore = "";
                while(actualTextComp.charAt(currIndex) != '<'){
                    ignore = (actualTextComp.charAt(currIndex)) + ignore;
                    currIndex--;
                    if(debug) System.out.println("2. Current Index: " + currIndex);
                }
                currIndex--;
                if(currIndex < 0){
                    if(debug) System.out.println("Broke here...");
                    if(debug1) System.out.println("3. Calling formatSeq with String: " + seq);
                    seq = formatSeq(seq);
                    if(seq.length() > 0) {
                        if(debug_op) System.out.println("3. Current String:" + seq);

                        //Checking if the term seq exists in the TOPICSasHASH table
                        if(checkSeq(seq)){
                            minBackwardsDist = spaceCount;
                            if(debug_op)System.out.println("3. Back Direction Found Match: " + seq);
                            break;
                        }
                        seq = "";
                        spaceCount++;
                        if (debug) System.out.println("3. Word Num is:" + spaceCount);
                    }
                    break;
                }
                if(debug) System.out.println("Ignored: " + ignore);

                if(actualTextComp.charAt(currIndex) == ' ') {
                    if(seq.length() > 0) {
                        if (debug) System.out.println("4. In Dirty if block");
                        if(debug1) System.out.println("4. Calling formatSeq with String: " + seq);
                        seq = formatSeq(seq);
                        if (debug_op) System.out.println("4. Current String:" + seq);
                        //Checking if the term seq exists in the TOPICSasHASH table
                        if(checkSeq(seq)){
                            minBackwardsDist = spaceCount;
                            if(debug_op)System.out.println("4. Back Direction Found Match: " + seq);
                            break;
                        }
                        seq = "";
                        spaceCount++;
                        if (debug) System.out.println("4. Word Num is:" + spaceCount);
                    }
                }
            }

            c1 = actualTextComp.charAt(currIndex);
            while( ! ((c1 >= 65 && c1 <= 90) || (c1 >= 97 && c1 <=122) || (c1 == 60) || (c1 == 62) || (c1 >= 48 && c1 <=57) || c1 == 40 || c1 == 41)){
                currIndex--;
                c1 = actualTextComp.charAt(currIndex);
                if(debug) System.out.println("New value of c: " + c1);
            }


            if(actualTextComp.charAt(currIndex) != '>') {
                if(debug) System.out.println("Adding char: " + actualTextComp.charAt(currIndex));
                seq = actualTextComp.charAt(currIndex) + seq;
                currIndex--;
                if(debug) System.out.println("5. Current Index: " + currIndex);
                if(currIndex < 0){
                    if(debug) System.out.println("Reached end of String");
                    seq = formatSeq(seq);
                    if(seq.length() > 0) {
                        if(debug_op) System.out.println("5. Current String:" + seq);
                        //Checking if the term seq exists in the TOPICSasHASH table

                        if(checkSeq(seq)){
                            minBackwardsDist = spaceCount;
                            if(debug_op)System.out.println("5. Back Direction Found Match: " + seq);
                            break;
                        }
                        seq = "";
                        spaceCount++;
                        if (debug) System.out.println("5. Word Num is:" + spaceCount);
                    }
                }
            }else{
                if(debug) System.out.println("6. In Another Dirty if block");
                if(seq.length() > 0) {
                    seq = formatSeq(seq);
                    if (debug_op) System.out.println("6. Current String:" + seq );

                    //Checking if the term seq exists in the TOPICSasHASH table
                    if(checkSeq(seq)){
                        minBackwardsDist = spaceCount;
                        if(debug_op)System.out.println("6. Back Direction Found Match: " + seq);
                        break;
                    }
                    seq = "";
                    spaceCount++;
                    if(debug) System.out.println("6. Word Num is:" + spaceCount);
                }

            }

        }

        //Now in the forward direction:
        //<a href="/wiki/Tiebreak_(tennis)" class="mw-redirect" title="Tiebreak (tennis)">tiebreak</a>
        if(debug_op) System.out.println("**** FORWARD DIRECTION ****");
        //        String actualTextComp = "Another of the early enthusiasts of the game was King
        // <a href=\"/wiki/Charles_V_of_France\" title=\"Charles V of France\">Charles V of France</a>,
        // who had a court set up at the <a href=\"/wiki/Louvre_Palace\" title=\"Louvre Palace\">Louvre Palace</a>.<sup id=\"cite_ref-6\" class=\"reference\"><a href=\"#cite_note-6\">&#91;6&#93;</a></sup>";
//        int firstIndex = actualTextComp.indexOf("</a>");

        int startIndex;
        int stopIndex;
        //individualStrings Stores each individual string that will be compared with the TOPICSasHASH table
        ArrayList<String> individualStrings = new ArrayList<>();
        //tempIndividualStrings: The tempString betn >tempString tempString< is split into
        // individual strings and stored in this
        String[] tempIndividualStrings;
        //fwdCount: Keeps track of how many strings have been added to individualStrings
        //fwdCount is returned as the minForwadsDist finally
        int fwdCount = 1;
        boolean foundMatchFwd = false;

        startIndex = stopAnchorTag;
        String subs = actualTextComp.substring(startIndex - 5, startIndex + 5);
        if(debug2) System.out.println("Substring at startIndex: " + actualTextComp.charAt(startIndex));
        if(debug2) System.out.println(subs);

        while (individualStrings.size() < textLimit && !foundMatchFwd) {
            startIndex = actualTextComp.indexOf('>', startIndex);
            stopIndex = actualTextComp.indexOf('<', startIndex - 1);
            String tempString = actualTextComp.substring(startIndex + 1, stopIndex);
            tempString = tempString.trim();
            if(debug) System.out.println("****" + tempString +"****");
            if(tempString.length() > 0) {
                tempIndividualStrings = tempString.split("\\s+");
                for (int i = 0; i < tempIndividualStrings.length; i++) {
                    String t = tempIndividualStrings[i];
                    t = formatSeq(t);
                    if(t.length() > 0) {
                        individualStrings.add(t);
                        //Checking if the term seq exists in the TOPICSasHASH table
                        if(checkSeq(t)){
                            minForwadsDist = fwdCount;
                            if(debug_op)System.out.println("Fwd Direction - Found Match: " + t);
                            foundMatchFwd = true;
                            break;
                        }
                        fwdCount++;
                    }
                }
            }

            if (actualTextComp.indexOf('<', stopIndex + 1) == -1) {
                break;
            } else {
                startIndex = stopIndex;
            }

        }

        if (debug_op) for(int i = 0; i < Math.min(textLimit-1, individualStrings.size()); i++){
            System.out.println("Current String:" + individualStrings.get(i));
        }

        minDist = Math.min(minBackwardsDist, minForwadsDist);
        if(debug_op) System.out.println("minBackwardsDist: " + minBackwardsDist);
        if(debug_op) System.out.println("minForwadsDist: " + minForwadsDist);
        if(debug_op) System.out.println("Calculated minDist: " + minDist);

        return minDist;
    }

    //Processes the String, i.e.
    //Checks if the string appears in the hash table
    private boolean checkSeq(String s){
        boolean debug = false;

        if(debug) System.out.println("Checking:" + s);
        if(TOPICSasHASH.contains(s)) return true;
        return false;
    }

    //Takes the distance of the term d as inout and returns the weight as per the defined heuristic
    private float calculateWeight(int d){
        return (float) 1 /(d + 2);
    }

    //Takes a string as an input and converts the string to lower case, removes punctuations
    //and all special characters(should it though?)
    private String formatSeq(String seq){
        boolean debug = false;
        if(debug) System.out.println("Input String:" + seq);
        seq = seq.replaceAll("[^A-Za-z1-9]+", "").toLowerCase();
        if(debug) System.out.println("Output String:" + seq);
        return seq;
    }

}
