# WikiCrawler_With_Heuristic_535_3
This code enables you to crawl wikipedia pages using BFS and build a web graph over MAX number of vertices

How to run the WikiCrawler?
- Pick a SEED_URL of your choice. For eg. /wiki/Tennis
- Note the format of the URL. There must be a backwardSlash before the wiki. 
- Select a value for MAX for eg. 1000. These are the number of web pages that will be crawled.
- You have 2 options:

Option 1) A simple BFS by setting isTopicSensitive to false.
- Then starting from the SEED_URL, this will create a Web Graph by doing BFS traversal over MAX many number of nodes.
- Therefore your graph will contain MAX number of nodes.

Option 2) A BFS using Heuristic by setting isTopicSensitive to true. Provide a set of keywords in the TOPICS as an Array of Strings.
These words should be relevant to the topic and should closely describe the topic. Please note that only single words are supported currently.
So, if I was performing a BFS from /wiki/Tennis, I would select my topics Array as:
String[] topics = {"tennis", "wimbledon", "masters", "federer", "nadal", "djokovic", "williams", "osaka"}

- How the heuristic works:
- Informally: 
- We start at a root node say wiki page about tennis. Lets call this page p.
Page p has quite a few links, say q1; q2; · · ·q_i . How do we know whether the page pointed by q1 is
about tennis or not? The best way is to send a request to page q1 and check if that page has words
from our topic set. However, this approach is expensive. We will be sending requests to many pages
that are not about tennis. Instead, what we do is, we will use a heuristic to determine whether the page pointed
by link q_i is about tennis or not (without sending request to page q_i). We will assign a weight to
the link q_i. The weight will be higher, if our heuristic thinks that page q_i is about our topic. The
heuristic is simple: Look at the link q_i. If the anchor text of q_i or the http link of q_i contains any of
our topic strings, then it must be the case that q_i is about our topic. If neither the anchor text not
the http link has our topic words, then look at the text surrounding the link. If any of the topic
words appear in the surrounding text, then we should be reasonably confident that page q is about
our topic. Our confidence would be higher, if the topic words are closer to the link; our confidence
will be lower if the topic words are away from the link.

- Formally:
Let q_i be a link in a page p and T be a set of words (in the Topics array). We define the distance between q and T
(with respect to page p) as
dist(q_i; T) = min{dist(q_i, w)| w \in T}
We will assign a weight to each q_i as follows: Look at the all occurrence of qi in the page p. If
the anchor text of the link or the http address within the link contains a word from the set T,
then weight(q_i) = 1. Otherwise, let compute d = dist(qi; T). If d > 17, then weight(qi) = 0, else
weight(qi) = 1/(d+2) . If the topic set T is empty, then weight of every link is 0

- You can fine tune the Heuristic by controlling how many words to the left and to the right the code should look at by changing the 
textLimit parameter.
