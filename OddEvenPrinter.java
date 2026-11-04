public final class OddEvenPrinter {
    private static final int LIMIT = 20;
    private int number = 1;

    private synchronized void printNumbers(boolean odd) {
        while (number <= LIMIT) {
            while (number <= LIMIT && (number % 2 == 1) != odd) {
                try {
                    wait();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    notifyAll();
                    return;
                }
            }

            if (number <= LIMIT) {
                System.out.println(number++);
                notifyAll();
            }
        }
    }

    public void start() throws InterruptedException {
        Thread oddThread = new Thread(() -> printNumbers(true), "odd-thread");
        Thread evenThread = new Thread(() -> printNumbers(false), "even-thread");

        oddThread.start();
        evenThread.start();
        oddThread.join();
        evenThread.join();
    }

    public static void main(String[] args) throws InterruptedException {
        new OddEvenPrinter().start();
    }
}