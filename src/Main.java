public class Main {
    public static void main(String[] args) {
        // 3 Sunuculu bir küme oluşturuyoruz
        Server[] cluster = {
                new Server(0, 40),  // Hızlı
                new Server(1, 80),  // Orta
                new Server(2, 150)  // Yavaş
        };

        SoftmaxLoadBalancer lb = new SoftmaxLoadBalancer(cluster, 0.5);

        System.out.println("Simülasyon başlıyor...");
        for (int i = 1; i <= 200; i++) {
            Server picked = lb.selectServer();
            double responseTime = picked.handleRequest();
            lb.update(picked.getId(), responseTime);

            if (i % 20 == 0) {
                System.out.println("İstek " + i + ": Sunucu " + picked.getId() + " seçildi. Süre: " + (int)responseTime + "ms");
            }
        }
    }
}