package memmonitor;

import main.ExploreConcepts;

class Main {
    public static void main(String[] args) {
        String filename = args[0];
        int minsup = Integer.parseInt(args[1]);
        float minconf = Float.parseFloat(args[2]);
        long interval = Long.parseLong(args[3]);

        MemoryMonitor process = new MemoryMonitor(interval);
        process.start();
        new ExploreConcepts(filename, minsup, minconf, 0).run();
        process.disable();

    }
}

class MemoryMonitor extends Thread {
    private long interval; // millisec
    private boolean running;

    MemoryMonitor(long interval) {
        this.interval = interval;
        running = true;
    }

    public void disable() {
        running = false;
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(interval);
                long occupiedmem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                System.out.println(occupiedmem);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}