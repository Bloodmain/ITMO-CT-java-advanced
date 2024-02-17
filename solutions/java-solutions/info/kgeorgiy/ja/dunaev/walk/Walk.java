package info.kgeorgiy.ja.dunaev.walk;

import info.kgeorgiy.ja.dunaev.walk.exceptions.WalkException;

public class Walk {
    public static void main(String[] args) {
        try {
            Walker.walk(args, 0);
        } catch (final WalkException e) {
            System.err.println("Walk exception: " + e.getMessage());
        }
    }
}
