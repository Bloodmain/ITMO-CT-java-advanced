package info.kgeorgiy.ja.dunaev.walk;

import info.kgeorgiy.ja.dunaev.walk.exceptions.WalkException;

public class RecursiveWalk {
    public static void main(String[] args) {
        try {
            Walker.walk(args, Integer.MAX_VALUE);
        } catch (final WalkException e) {
            System.err.println("Recursive walk exception: " + e.getMessage());
        }
    }
}
