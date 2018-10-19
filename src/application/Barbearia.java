package application;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;

/**
 * A barbearia tem:
 *      Um barbeiro.
 *      Uma cadeira de barbeiro.
 *      Seis cadeiras de espera.
 *
 * Se o movimento estiver fraco (nenhum cliente), o barbeiro dorme.
 * Quando um cliente chega no salão vazio, ele tem que acordar o barbeiro.
 * Se outros clientes chegam enquanto o barbeiro está ocupado, eles:
 *      Sentam na cadeira de espera.
 *      Vão embora, se não houver cadeira vazia.
 *
 * Neste trabalho, utilizei Threads para resolver o problema do barbeiro dorminhoco, fazendo uso dos métodos wait(), e notify() para gerenciar o atendimento de clientes {@see Semaforo}
 */
public class Barbearia {

    @FXML SVGPath cadeiraBarbeiro;
    @FXML SVGPath cadeiraEspera1;
    @FXML SVGPath cadeiraEspera2;
    @FXML SVGPath cadeiraEspera3;
    @FXML SVGPath cadeiraEspera4;
    @FXML SVGPath cadeiraEspera5;
    @FXML SVGPath cadeiraEspera6;
    @FXML TextArea txtLog;
    @FXML TextField txtAtendidos;
    @FXML TextField txtDesistentes;

    Semaforo clientes = new Semaforo(0);
    Semaforo barbeiros = new Semaforo(0);
    Semaforo mutuo = new Semaforo(1);

    // Barbearia
    final byte CADEIRAS = 6;
    boolean aberta = false;
    byte clientesEsperando = 0;
    short atendidos = 0;
    short desistentes = 0;

    // Barbeiro
    boolean cortando = false;
    boolean dormindo = false;

    // Cores
    Color disponivelCor = new Color(34.0 / 255, 231.0 / 255, 70.0 / 255, 1);
    Color indisponivelCor = new Color(120.0 / 255, 120.0 / 255, 120.0 / 255, 1);
    Color ocupadoCor = new Color(219.0 / 255, 59.0 / 255, 59.0 / 255, 1);
    Color dormindoCor = new Color(48.0 / 255, 117.0 / 255, 215.0 / 255, 1);

    @FXML
    public void initialize() {
        status.setDaemon(true);
        status.start();
        barbeiro.setDaemon(true);
        barbeiro.start();

        // Desativar caixas de texto
        txtLog.setEditable(false);
        txtAtendidos.setEditable(false);
        txtDesistentes.setEditable(false);
    }

    @FXML
    public void abrirBarbearia() {
        if (cortando || clientesEsperando != 0) {
            txtLog.appendText("Aguarde todos os clientes serem atendidos.\n");
        } else if (!aberta) {
            aberta = true;
            atendidos = 0;
            desistentes = 0;
            txtLog.appendText("Barbearia aberta!\n");
            txtAtendidos.setText("" + atendidos);
            txtDesistentes.setText("" + desistentes);
        }
    }

    @FXML
    public synchronized void fecharBarbearia() {
        if (aberta) {
            aberta = false;
            dormindo = false;
            txtLog.appendText("Barbearia fechada!\n");
        }
    }

    @FXML
    public void novoCliente() {
        if (aberta) {
            mutuo.P();

            if (clientesEsperando < CADEIRAS) {
                clientesEsperando++;
                txtLog.appendText("Novo cliente chegou!\n");
                clientes.V();
                mutuo.V();
            } else {
                mutuo.V();
                desistentes++;
                txtDesistentes.setText("" + desistentes);
                txtLog.appendText("Cliente foi embora!\n");
            }
        } else {
            txtLog.appendText("Abra a barbearia para poder atender clientes.\n");
        }
    }

    // THREADS

    // Barbeiro
    Thread barbeiro = new Thread() {
        public void cortarCabelo(Cliente cliente) {
            cortando = true;

            try {
                txtLog.appendText("Cortando cabelo de cliente: ETA " + (cliente.getTempo() / 1000) + "s\n");
                Thread.sleep(cliente.getTempo());
                atendidos++;
                txtAtendidos.setText("" + atendidos);
                txtLog.appendText("Cabelo do cliente cortado.\n");
                cortando = false;
                Thread.sleep(500);
            } catch (InterruptedException e) {
                System.err.println("A (Thread)barbeiro foi interrompida.");
            }
        }

        @Override
        public void run() {
            while (true) {
                if (clientesEsperando == 0) {
                    if (aberta)
                        dormindo = true;
                    clientes.P();
                } else {
                    mutuo.P();
                    clientesEsperando--;
                    barbeiros.V();
                    mutuo.V();
                    dormindo = false;
                    cortarCabelo(new Cliente());
                }

                // Slow down this shit too
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    System.err.print("A (Thread)barbeiro foi interrompida.");
                }
            }
        }
    };

    // Status da barbearia
    Thread status = new Thread() {
        @Override
        public void run() {
            Color estadoCor;
            while (true) {
                estadoCor = aberta ? disponivelCor : indisponivelCor;

                if (cortando)
                    cadeiraBarbeiro.setFill(ocupadoCor);
                else if (dormindo && clientesEsperando == 0)
                    cadeiraBarbeiro.setFill(dormindoCor);
                else
                    cadeiraBarbeiro.setFill(estadoCor);

                switch (clientesEsperando) {
                    case 0:
                        cadeiraEspera1.setFill(estadoCor);
                        cadeiraEspera2.setFill(estadoCor);
                        cadeiraEspera3.setFill(estadoCor);
                        cadeiraEspera4.setFill(estadoCor);
                        cadeiraEspera5.setFill(estadoCor);
                        cadeiraEspera6.setFill(estadoCor);
                        break;
                    case 1:
                        cadeiraEspera1.setFill(ocupadoCor);
                        cadeiraEspera2.setFill(estadoCor);
                        cadeiraEspera3.setFill(estadoCor);
                        cadeiraEspera4.setFill(estadoCor);
                        cadeiraEspera5.setFill(estadoCor);
                        cadeiraEspera6.setFill(estadoCor);
                        break;
                    case 2:
                        cadeiraEspera1.setFill(ocupadoCor);
                        cadeiraEspera2.setFill(ocupadoCor);
                        cadeiraEspera3.setFill(estadoCor);
                        cadeiraEspera4.setFill(estadoCor);
                        cadeiraEspera5.setFill(estadoCor);
                        cadeiraEspera6.setFill(estadoCor);
                        break;
                    case 3:
                        cadeiraEspera1.setFill(ocupadoCor);
                        cadeiraEspera2.setFill(ocupadoCor);
                        cadeiraEspera3.setFill(ocupadoCor);
                        cadeiraEspera4.setFill(estadoCor);
                        cadeiraEspera5.setFill(estadoCor);
                        cadeiraEspera6.setFill(estadoCor);
                        break;
                    case 4:
                        cadeiraEspera1.setFill(ocupadoCor);
                        cadeiraEspera2.setFill(ocupadoCor);
                        cadeiraEspera3.setFill(ocupadoCor);
                        cadeiraEspera4.setFill(ocupadoCor);
                        cadeiraEspera5.setFill(estadoCor);
                        cadeiraEspera6.setFill(estadoCor);
                        break;
                    case 5:
                        cadeiraEspera1.setFill(ocupadoCor);
                        cadeiraEspera2.setFill(ocupadoCor);
                        cadeiraEspera3.setFill(ocupadoCor);
                        cadeiraEspera4.setFill(ocupadoCor);
                        cadeiraEspera5.setFill(ocupadoCor);
                        cadeiraEspera6.setFill(estadoCor);
                        break;
                    case 6:
                        cadeiraEspera1.setFill(ocupadoCor);
                        cadeiraEspera2.setFill(ocupadoCor);
                        cadeiraEspera3.setFill(ocupadoCor);
                        cadeiraEspera4.setFill(ocupadoCor);
                        cadeiraEspera5.setFill(ocupadoCor);
                        cadeiraEspera6.setFill(ocupadoCor);
                        break;
                    default:
                        break;
                }

                // Slow down this shit
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    System.err.print("A (Thread)colorirCadeiras foi interrompida.");
                }
            }
        }
    };
}
