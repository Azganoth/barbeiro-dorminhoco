package application;

public class Semaforo {

    private int valor;

    public Semaforo(int valor) {
        this.valor = valor;
    }

    /**
     * Signal - Se o semáforo estiver com o valor zero e existir algum processo adormecido, um processo será acordado. Caso contrário, o valor do semáforo é incrementado.
     */
    public synchronized void V() {
        valor++;
        notify();
    }

    /**
     * Wait - Decrementa o valor do semáforo. Se o semáforo está com valor zero, o processo é posto para dormir.
     */
    public synchronized void P() {
        while (valor <= 0) {
            try {
                wait();
            } catch (Exception e) {
                System.err.println("Erro ao bloquear processo.");
            }
        }

        valor--;
    }
}
