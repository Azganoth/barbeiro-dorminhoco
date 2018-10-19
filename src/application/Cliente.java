package application;

public class Cliente {
    private long tempo;

    public Cliente() {
        this.tempo = (long)(Math.random() * 10000);
    }

    // Gets and Sets

    public long getTempo() {
        return tempo;
    }
}
