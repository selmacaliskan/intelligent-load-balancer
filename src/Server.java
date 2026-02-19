import java.util.Random;

public class Server {
    private int id;
    private double baseLatency;
    private Random random = new Random();

    public Server(int id, double baseLatency) {
        this.id = id;
        this.baseLatency = baseLatency;
    }

    // BU METODDAN SADECE BİR TANE OLMALI:
    public double handleRequest() {
        double noise = random.nextGaussian() * 5;
        return Math.max(1, baseLatency + noise);
    }

    public void setBaseLatency(double baseLatency) {
        this.baseLatency = baseLatency;
    }

    public int getId() { return id; }
}