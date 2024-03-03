package info.kgeorgiy.ja.dunaev.arrayset;

import java.util.*;

public class Test {
    public static void main(String[] args) {
        SortedSet<Integer> set = new ArraySet<>(List.of(0,1,2,3,4), Integer::compare);
        System.out.println(Arrays.toString(set.toArray()));
        set.remove(3);
        System.out.println(Arrays.toString(set.toArray()));
    }
}
