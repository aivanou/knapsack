package com.ooyala.challenge;

import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/**
 */
public class Test {

    static BlockingQueue<Integer> queue = new LinkedBlockingQueue<>();

    public static void main(String[] args) {
        Thread th = new Thread(new Task());
        th.start();
        th.interrupt();
        while (true) {
        }
    }

    private static class Task implements Runnable {
        @Override public void run() {
            while (true) {
                long start = System.nanoTime();
                while((System.nanoTime()-start)/1000000000.0 <5){}
                System.out.println("nice  "+Thread.currentThread().isInterrupted());
                try {
                    queue.poll(1, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    System.out.println("LALALA: " + e.getMessage());
                }
            }
        }
    }

}
