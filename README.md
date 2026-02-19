# Softmax Load Balancer

A Java-based adaptive load balancing system that applies Softmax action selection and Q-Learning to dynamically optimize request distribution across a server cluster based on observed response times.

---

## Overview

Traditional load balancing strategies such as Round-Robin and Random distribution operate without awareness of server performance. This implementation introduces a reinforcement learning approach, enabling the system to continuously learn from runtime behavior and route traffic with increasing efficiency over time.

The system is designed around three core components: a server model with configurable latency profiles, a Q-Learning update mechanism, and a Softmax-based probabilistic selection policy.

---

## Algorithm

### Background

Standard load balancing algorithms treat all servers as equivalent routing targets. Round-Robin cycles through servers in fixed order regardless of their current performance. Random selection offers no improvement — both strategies produce the same long-term distribution and neither adapts to changing server conditions.

This project replaces static routing with a **multi-armed bandit** approach, a well-established class of reinforcement learning problems concerned with balancing exploration of unknown options against exploitation of known high-performing ones.

### Q-Learning — Performance Scoring

Each server maintains a Q-value representing its estimated performance quality. After every request, the Q-value is updated using the following rule:

```
Q[i] ← Q[i] + α × (r - Q[i])

where:
  Q[i]  = current score of server i
  α     = learning rate (0.1)
  r     = reward signal = 100 / response_time_ms
```

This formula is an **exponential moving average**. It weights recent observations more heavily than historical ones, allowing the system to adapt if server performance degrades or improves during operation. A reward inversely proportional to latency ensures that faster servers accumulate higher scores over time.

Initial Q-values are set to 1.0 for all servers, providing an unbiased starting point before any observations have been collected.

### Softmax Selection — Probabilistic Routing

Q-values are converted into selection probabilities using the Softmax function:

```
P(server_i) = exp(Q[i] / τ) / Σ exp(Q[j] / τ)

where:
  τ (tau) = temperature parameter controlling exploration (0.5)
```

The temperature parameter τ governs the trade-off between exploitation and exploration:

- **Low τ** — probability mass concentrates on the highest-scoring server. The system aggressively exploits its current best-known option.
- **High τ** — probabilities converge toward uniform distribution. The system explores all servers more evenly.

At τ = 0.5, the system reliably identifies and favors the fastest server while maintaining non-zero selection probability for all others. This guarantees that performance regressions in the preferred server will be detected and corrected without manual intervention.

### Selection Mechanism

Server selection uses cumulative probability sampling:

```
1. Compute Softmax probability distribution over all servers
2. Draw a uniform random value r ∈ [0, 1]
3. Traverse the distribution cumulatively; return the first server
   for which the cumulative probability meets or exceeds r
```

This is mathematically equivalent to sampling from a categorical distribution and is the standard implementation pattern for Softmax-based action selection.

### Convergence Behavior

The system begins with no prior knowledge. Over successive requests, Q-values diverge as performance differences between servers become statistically significant. In a stable environment with three servers at 40ms, 80ms, and 150ms respectively, the routing distribution after convergence approximates:

```
Server 0 (40ms)  →  ~85–90% of requests
Server 1 (80ms)  →  ~8–12% of requests
Server 2 (150ms) →  ~2–4% of requests
```

This yields an average response time approaching 45ms, compared to approximately 90ms under Round-Robin — a reduction of approximately 50% with no changes to infrastructure.

---

## Project Structure

```
├── Main.java                  # Simulation entry point and request loop
├── Server.java                # Server model with Gaussian latency noise
└── SoftmaxLoadBalancer.java   # Q-Learning update and Softmax selection
```

---

## Requirements

- Java 8 or higher
- No external dependencies

---

## Usage

```bash
javac *.java
java Main
```

---

## Configuration

| Parameter | Location | Default | Description |
|---|---|---|---|
| `baseLatency` | `Server` constructor | varies | Mean response time in milliseconds |
| `tau (τ)` | `SoftmaxLoadBalancer` constructor | 0.5 | Exploration temperature |
| `alpha (α)` | `update()` method | 0.1 | Q-value learning rate |
| Request count | `Main.java` loop | 200 | Total simulation requests |

---

## Time Complexity

| Method | Complexity | Note |
|---|---|---|
| `update()` | O(1) | Direct index access |
| `calculateSoftmax()` | O(n) | Two linear passes over server array |
| `selectServer()` | O(n) | Softmax + cumulative sampling |
| Full simulation | O(R × n) | R requests, n servers |

For the default configuration (n=3, R=200), total algorithmic overhead is negligible relative to actual network latency.

---

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
