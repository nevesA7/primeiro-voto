package br.edu.unijorge.util;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

/** Sons da urna gerados por código (sem arquivos de áudio). Falha em silêncio se não houver áudio. */
public final class SomUrna {

    private static final float TAXA = 22050f;

    private SomUrna() {
    }

    public static void tecla() {
        tocar(880, 45);
    }

    public static void confirma() {
        tocar(1000, 160);
    }

    public static void erro() {
        tocar(300, 220);
    }

    public static void fim() {
        tocar(700, 1200);
    }

    private static void tocar(double frequencia, int duracaoMs) {
        Thread t = new Thread(() -> {
            try {
                AudioFormat formato = new AudioFormat(TAXA, 8, 1, true, false);
                byte[] buffer = new byte[(int) (TAXA * duracaoMs / 1000)];
                for (int i = 0; i < buffer.length; i++) {
                    double angulo = 2.0 * Math.PI * i * frequencia / TAXA;
                    buffer[i] = (byte) (Math.sin(angulo) * 60);
                }
                try (SourceDataLine linha = AudioSystem.getSourceDataLine(formato)) {
                    linha.open(formato);
                    linha.start();
                    linha.write(buffer, 0, buffer.length);
                    linha.drain();
                }
            } catch (Exception | Error e) {
                // sem dispositivo de áudio: segue sem som
            }
        }, "som-urna");
        t.setDaemon(true);
        t.start();
    }
}
