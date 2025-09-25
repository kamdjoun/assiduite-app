package com.soutenance.assiduite1;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.soutenance.assiduite1.EtudiantRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class ArduinoService {


    @Autowired
    private EtudiantRepository etudiantRepo;

    private static final String PORT_NAME = "COM6"; // Remplacez par le port de votre Arduino
    private static final int BAUD_RATE = 9600;

    // Variables de gestion de l'état de la capture
    private static final AtomicBoolean isCapturing = new AtomicBoolean(false);
    private static Long currentEtudiantId = null;
    private static final SerialPort comPort = SerialPort.getCommPort(PORT_NAME);
    private StringBuilder receivedDataBuffer = new StringBuilder();

    @PostConstruct
    public void start() {
        comPort.setBaudRate(BAUD_RATE);
        if (!comPort.openPort()) {
            System.err.println("Erreur: Impossible d'ouvrir le port série " + PORT_NAME);
            return;
        }
        System.out.println("Port série " + PORT_NAME + " ouvert. En attente de données de l'Arduino...");

        comPort.addDataListener(new SerialPortDataListener() {
            @Override
            public int getListeningEvents() {
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
            }

            @Override
            public void serialEvent(SerialPortEvent event) {
                byte[] newData = new byte[comPort.bytesAvailable()];
                comPort.readBytes(newData, newData.length);
                String data = new String(newData);
                receivedDataBuffer.append(data);

                if (receivedDataBuffer.indexOf("\n") != -1) {
                    String fullMessage = receivedDataBuffer.toString().trim();
                    System.out.println("Message complet reçu : " + fullMessage);
                    processMessageFromArduino(fullMessage);
                    receivedDataBuffer.setLength(0);
                }
            }
        });
    }

    /**
     * Méthode appelée par le contrôleur pour lancer la capture d'empreinte.
     * @param etudiantId L'ID de l'étudiant concerné.
     * @return true si la capture est lancée, false si une capture est déjà en cours.
     */
    // Dans votre ArduinoService.java
    public boolean startFingerprintCapture(Long etudiantId) {
        if (isCapturing.get()) {
            System.err.println("Erreur : une capture est déjà en cours.");
            return false;
        }

        if (!comPort.isOpen()) {
            System.err.println("Erreur : le port série n'est pas ouvert.");
            return false;
        }

        currentEtudiantId = etudiantId;
        isCapturing.set(true);

        // Envoyer la commande et l'ID dans un seul message
        String command = "C:" + etudiantId + "\n";
        comPort.writeBytes(command.getBytes(), command.length());

        System.out.println("Commande envoyée à l'Arduino : " + command.trim());
        return true;
    }

    private void processMessageFromArduino(String message) {
        if (!isCapturing.get() || currentEtudiantId == null) {
            System.err.println("Message reçu sans capture en cours. Ignoré.");
            return;
        }

        if (message.startsWith("CAPTURE_SUCCESS")) {
            // TODO: Récupérer les données de l'empreinte de l'Arduino. C'est la partie la plus complexe.
            // Pour l'instant, nous allons simuler les données. L'Arduino doit envoyer l'ID et les données binaires.

            // Simule des données d'empreinte (un simple tableau de bytes)
            byte[] empreinte = new byte[1024]; // Par exemple, une taille de 1024 bytes

            // Logique de sauvegarde
            etudiantRepo.findById(currentEtudiantId).ifPresent(etudiant -> {
                etudiant.setEmpreinteDigitale(empreinte);
                etudiantRepo.save(etudiant);
                System.out.println("Empreinte de l'étudiant " + currentEtudiantId + " sauvegardée avec succès !");
            });

            // Réinitialise l'état
            isCapturing.set(false);
            currentEtudiantId = null;

        } else if (message.startsWith("CAPTURE_FAILED")) {
            System.err.println("Échec de la capture d'empreinte pour l'étudiant " + currentEtudiantId);
            isCapturing.set(false);
            currentEtudiantId = null;
        }
    }
}