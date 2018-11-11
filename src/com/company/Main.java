package com.company;

import java.util.Comparator;
import java.util.PriorityQueue;

public class Main {

    public static void main(String[] args) {
	// write your code here

        Comparator<Tuple> myComp = new TupleComparator();
        PriorityQueue<Tuple> P = new PriorityQueue<Tuple>(myComp);
//        MyPriorityQueue<Integer> P = new MyPriorityQueue<Integer>(Comparator.<Integer>reverseOrder());
        for (int i = 0; i < 10; i++){
            System.out.println("******* Adding element: " + i);
            Tuple myTuple = new Tuple("myStr", (float)2*i, i);
            P.add(myTuple);
            System.out.println("P.peek().getCountNum(): " + P.peek().getCountNum());
            System.out.println("************************************");
        }
        P.add(new Tuple("myStr", 18.0f, 10));

        System.out.println("P.size(): " + P.size());
        System.out.println("*************** Extracting Stuff ***************");
        System.out.println("Weight: " + P.peek().getWeight() );
        System.out.println("Count: " + P.peek().getCountNum() );
        System.out.println("*****");
        P.poll();
        System.out.println("*****");
        System.out.println("***** Extracting Stuff *******");
        System.out.println("Weight: " + P.peek().getWeight() );
        System.out.println("Count: " + P.peek().getCountNum() );
        System.out.println("*****");
        P.poll();
        P.add(new Tuple("myStr", 14.0f, 9));
        System.out.println("*****");
        System.out.println("***** Extracting Stuff *******");
        System.out.println("Weight: " + P.peek().getWeight() );
        System.out.println("Count: " + P.peek().getCountNum() );
        System.out.println("*****");
        P.poll();
        System.out.println("*****");
        System.out.println("***** Extracting Stuff *******");
        System.out.println("Weight: " + P.peek().getWeight() );
        System.out.println("Count: " + P.peek().getCountNum() );
        System.out.println("*****");
        P.poll();
        System.out.println("*****");
        System.out.println("***** Extracting Stuff *******");
        System.out.println("Weight: " + P.peek().getWeight() );
        System.out.println("Count: " + P.peek().getCountNum() );
        System.out.println("*****");
        P.poll();
        System.out.println("*****");
        System.out.println("***** Extracting Stuff *******");
        System.out.println("Weight: " + P.peek().getWeight() );
        System.out.println("Count: " + P.peek().getCountNum() );
        System.out.println("*****");
        P.poll();
        System.out.println("*****");
        System.out.println("***** Extracting Stuff *******");
        System.out.println("Weight: " + P.peek().getWeight() );
        System.out.println("Count: " + P.peek().getCountNum() );
        System.out.println("*****");
        P.poll();
        System.out.println("*****");
        System.out.println("***** Extracting Stuff *******");
        System.out.println("Weight: " + P.peek().getWeight() );
        System.out.println("Count: " + P.peek().getCountNum() );
        System.out.println("*****");
        P.poll();
        System.out.println("*****");
        System.out.println("***** Extracting Stuff *******");
        System.out.println("Weight: " + P.peek().getWeight() );
        System.out.println("Count: " + P.peek().getCountNum() );
        System.out.println("*****");
        P.poll();
        System.out.println("*****");
        System.out.println("***** Extracting Stuff *******");
        System.out.println("Weight: " + P.peek().getWeight() );
        System.out.println("Count: " + P.peek().getCountNum() );
        System.out.println("*****");





    }
}
