package com.company;

import java.util.Comparator;

public class Tuple {

    private String link = "";   //Stores the name of the link
    private float weight = 0;   //Stores the heuristic weight calculated of the link
    private int countNum = 0;   //Assume all nodes are numbered 1..i..N in the order in which they are added to the PQ. This stores i.

    //Use this constructor when creating a weighted queue
    public Tuple(String link, float weight, int countNum) {
        this.link = link;
        this.weight = weight;
        this.countNum = countNum;
    }

    //Use this constructor when creating a normal FIFO queue
    public Tuple(String link, int countNum) {
        this.link = link;
        this.weight = 0.0f;
        this.countNum = countNum;
    }

    public String getLink() {
        return link;
    }

    public float getWeight() {
        return weight;
    }

    public int getCountNum() {
        return countNum;
    }

    public void printTuple(){
        System.out.println("Link: " + getLink());
        System.out.println("Weight: " + getWeight());
        System.out.println("CountNum: " + getCountNum());
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null) {
            return false;
        }

        /* Check if obj is an instance of Tuple or not
         "null instanceof [type]" also returns false */
        if (!(obj instanceof Tuple)) {
            return false;
        }

        // typecast obj to Tuple so that we can compare data members
        Tuple t = (Tuple) obj;
        if(!t.getLink().equals(this.link)){
            return false;
        }
//
//        if(! (t.getWeight() == this.getWeight())){
//            return false;
//        }

        return true;

    }
}

class TupleComparator implements Comparator<Tuple>{

    /* Code from PriorityQueue.java

    private void siftUpUsingComparator(int k, E x) {
        while (k > 0) {
            int parent = (k - 1) >>> 1;
            Object e = queue[parent];
            if (comparator.compare(x, (E) e) >= 0) //compare(child, parent) >= 0
                break;//So if child > parent, it wont heapify. So we will have to reverse the order
            queue[k] = e;
            k = parent;
        }
        queue[k] = x;
    }

    */

    @Override
    public int compare(Tuple o1, Tuple o2) {

        boolean debug = false;

        if(debug) System.out.println("*** START COMPARISON ***");
        if(debug) System.out.println("Comparing:");
        if(debug) System.out.println("o1.getWeight(): " + o1.getWeight());
        if(debug) System.out.println("o1.getCountNum(): " + o1.getCountNum());
        if(debug) System.out.println("o2.getWeight(): " + o2.getWeight());
        if(debug) System.out.println("o2.getCountNum(): " + o2.getCountNum());

        //Returning opposite of what is true to sort in descending order and not ascending as is done
        //by the PQ by default.
        //See code snippet above

        if(o1.getWeight() > o2.getWeight()){
            if(debug) System.out.println("o1.getWeight() > o2.getWeight()");
            if(debug) System.out.println(o1.getWeight() + " > " + o2.getWeight());
            return -1;
        }
        if(o1.getWeight() < o2.getWeight()){
            if(debug) System.out.println("o1.getWeight() < o2.getWeight()");
            if(debug) System.out.println(o1.getWeight() + " < " + o2.getWeight());
            return 1;
        }

        if(o1.getWeight() == o2.getWeight()){
            if(debug) System.out.println("Equal weights");
            if(o1.getCountNum() < o2.getCountNum()){
                if(debug) System.out.println("o1.getCountNum() < o2.getCountNum()");
                if(debug) System.out.println(o1.getCountNum() + " < " + o2.getCountNum());
                return -1;
            }
            if(o1.getCountNum() > o2.getCountNum()){
                if(debug) System.out.println("o1.getCountNum() > o2.getCountNum()");
                if(debug) System.out.println(o1.getCountNum() + " > " + o2.getCountNum());
                return 1;
            }
            if(o1.getCountNum() == o2.getCountNum()){
                if(debug) System.out.println("And Equal countNums too apparently");
            }
        }
        if(debug)
            if(o1.getWeight() == o2.getWeight() && o1.getCountNum() == o2.getCountNum())
                System.out.println("Something went wrong in Tuple.compare()");

        return 0;
    }

        /*
        https://stackoverflow.com/questions/2811319/difference-between-and

        >>> This is the logical shift right operator => equivalent to dividing by 2
        >> is arithmetic shift right, >>> is logical shift right.
        In an arithmetic shift, the sign bit is extended to preserve the signedness of the number.
        For example: -2 represented in 8 bits would be 11111110 (because the most significant bit has negative weight).
        Shifting it right one bit using arithmetic shift would give you 11111111, or -1.
        Logical right shift, however, does not care that the value could possibly represent a signed number;
        it simply moves everything to the right and fills in from the left with 0s.
        Shifting our -2 right one bit using logical shift would give 01111111

        so in effect what we are doing is that we are dividing size by 2 and then subtracting 1 from it.
        The reason is that: elements from index "size" to "size/2" are going to be the leaf nodes in the PQ.
        So we only need to check the elements that are lesser than the index "size/2"
        */

        /*
        What is the difference between Comparable and Comparator?

        https://stackoverflow.com/questions/4108604/java-comparable-vs-comparator
        When your class implements Comparable, the compareTo method of the class is defining the "natural" ordering of that object.
        That method is contractually obligated (though not demanded) to be in line with other methods on that object,
        such as a 0 should always be returned for objects when the .equals() comparisons return true.

        A Comparator is its own definition of how to compare two objects, and can be used to compare objects in a way
        that might not align with the natural ordering.
        For example, Strings are generally compared alphabetically. Thus the "a".compareTo("b") would use alphabetical comparisons.
        If you wanted to compare Strings on length, you would need to write a custom comparator.
        In short, there isn't much difference. They are both ends to similar means.
        In general implement comparable for natural order, (natural order definition is obviously open to interpretation),
        and write a comparator for other sorting or comparison needs.
        */


}
