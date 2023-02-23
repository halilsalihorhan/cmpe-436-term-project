import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;

public class RRect {
    public int id;
    public int left;
    public int top;
    public int right;
    public int bottom;
    public float rotation;
    public String color;
    public Semaphore lock;

    public RRect(int id, int left, int top, int right, int bottom, float rotation, String color, Semaphore lock) {
        this.id = id;
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.rotation = rotation;
        this.color = color;
        this.lock = lock;
    }

    public void update(int left, int top, int right, int bottom, float rotation, String color) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.rotation = rotation;
        this.color = color;
    }

    public void lock() throws InterruptedException {
        lock.acquire();
    }

    public void unlock() {
        lock.release();
    }
}
