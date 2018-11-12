package com.company;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;

public class Main {

    public static void main(String[] args) {
	// write your code here

        Comparator<Tuple> myComp = new TupleComparator();
        PriorityQueue<Tuple> P = new PriorityQueue<Tuple>(myComp);
//        MyPriorityQueue<Integer> P = new MyPriorityQueue<Integer>(Comparator.<Integer>reverseOrder());
        for (int i = 0; i < 10; i++){
            System.out.println("******* Adding element: " + i);
            Tuple myTuple = new Tuple("myStr"+i, (float)2*i, i);
            P.add(myTuple);
            System.out.println("P.peek().getCountNum(): " + P.peek().getCountNum());
            System.out.println("************************************");
        }
//        P.add(new Tuple("myStr", 18.0f, 10));

        System.out.println("P.size(): " + P.size());
        printPQ(P);
        System.out.println("*************** Removing Stuff ***************");
        P.remove(new Tuple("myStr4", 10, 5));
        P.remove(new Tuple("myStr9", 10, 5));

        System.out.println("***********************************************");
        printPQ(P);
    }

    public static void printPQ(PriorityQueue P){

        Iterator i = P.iterator();
        System.out.println("************ Printing Priority Queue");
        while(i.hasNext()){
            Tuple t = (Tuple) i.next();
            t.printTuple();
            System.out.println("*****");
        }

    }
}
