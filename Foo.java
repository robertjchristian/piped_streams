import java.io.*;

import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;


public class Foo {
    public static void main(String[] args) {
        TestRunner tester = new TestRunner();
        tester.run();
    }
}

class TestRunner implements Runnable {
    public void run() {
        PipedOutputStream os = new PipedOutputStream();
        PipedInputStream is = new PipedInputStream();

        try {
            is.connect(os);
        } catch (IOException e) {
            // This is not possible, as piped streams
            // are being used correctly.
            assert false;
        }

        Producer producer = new Producer(5, 10, os);
        Consumer consumer = new Consumer(5, is);
        producer.start();
        consumer.start();
    }


}

class Consumer extends Thread {
    int interval;
    InputStream is;
    static final int max = 10;
    byte data[] = new byte[max];

    public Consumer(int interval, InputStream is) {
        super();
        this.interval = interval;
        this.is = is;
    }

    @Override
    public void run() {
        try {
            System.out.println("Consumer started");
            int amount;
            while ((amount = is.read(data)) >= 0) {
                String s = new String(data, 0, amount);
                System.out.println(MessageFormat.format("Consumed {0} bytes: {1}", amount, s));
                synchronized (this) {
                    wait(interval);
                }
            }
            is.close();
            System.out.println("Consumer finished");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class Producer extends Thread {
    int interval;
    int iterations;
    OutputStream os;
    byte data[] = { 'a', 'b', 'b' };

    public Producer(int interval, int iterations, OutputStream os) {
        this.interval = interval;
        this.iterations = iterations;
        this.os = os;
    }

    @Override
    public void run() {
        try {
            System.out.println("Producer started");
            for (int i = 0; i < iterations; i++) {
                System.out.println("Produce...");
                System.out.flush();
                os.write(data);
                synchronized (this) {
                    this.wait(interval);
                }
            }
            os.close();
            System.out.println("Producer finished");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
