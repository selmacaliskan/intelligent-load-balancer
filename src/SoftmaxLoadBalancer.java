public class SoftmaxLoadBalancer {
    private Server[] servers;
    private double[] qValues; // Her sunucunun "puanı"
    private double tau;       // Keşif (exploration) katsayısı

    public SoftmaxLoadBalancer(Server[] servers, double tau) {
        this.servers = servers;
        this.tau = tau;
        this.qValues = new double[servers.length];
        // Başlangıçta tüm sunuculara eşit puan veriyoruz
        for (int i = 0; i < qValues.length; i++) qValues[i] = 1.0;
    }

    public Server selectServer() {
        double[] probs = calculateSoftmax();
        double r = Math.random();
        double cumulative = 0;

        for (int i = 0; i < probs.length; i++) {
            cumulative += probs[i];
            if (r <= cumulative) return servers[i];
        }
        return servers[servers.length - 1];
    }

    private double[] calculateSoftmax() {
        double[] probs = new double[qValues.length];
        double sum = 0;
        for (double q : qValues) sum += Math.exp(q / tau);
        for (int i = 0; i < qValues.length; i++) {
            probs[i] = Math.exp(qValues[i] / tau) / sum;
        }
        return probs;
    }

    public void update(int id, double latency) {
        double reward = 100.0 / latency; // Latency azaldıkça ödül artar
        double alpha = 0.1; // Öğrenme hızı
        qValues[id] += alpha * (reward - qValues[id]);
    }
}