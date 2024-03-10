package com.jeitaly.mailsender;

import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.mail.*;

public class MailSenderMain {

    public static void main(String[] args) throws IOException {
        try {
            // Legge le credenziali dal file credentials.txt
            File credentiaFile = new File("./src/main/java/com/jeitaly/mailsender/credentials.txt");
            Scanner credentialReader = new Scanner(credentiaFile);
            String senderEmail = credentialReader.nextLine();

            // Verifica che l'indirizzo email sia valido
            if(!isValidEmail(senderEmail)) {
                System.out.println("L'indirizzo email inserito non è valido.");
                credentialReader.close();
                return;
            }
            String senderPassword = credentialReader.nextLine();
            credentialReader.close();

            // Imposta le proprietà per l'invio di email
            Properties props = new Properties();
            insertPropertiesMail(props, senderEmail);
        
            // Crea la sessione per l'invio di email
            Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(senderEmail, senderPassword);
                }
            });
            if(session == null) {
                System.out.println("Errore durante la creazione della sessione.");
                return;
            }

            // Legge le parole dal file generalMail.txt
            File generalMail = new File("./src/main/java/com/jeitaly/mailsender/generalMail.txt");
            List<String> wordsToEdit = new ArrayList<>();
            
            // Aggiunge le parole tra parentesi quadre del file generalMail.txt alla lista wordsToEdit
            wordsToEdit = scannerWords(generalMail, wordsToEdit);

            // Legge i destinatari dal file emails.csv
            File csvFile = new File("./src/main/java/com/jeitaly/mailsender/recipientData.csv");
            CSVReader csvReader = new CSVReader(new FileReader(csvFile));

            // Avvia i thread per l'invio delle email
            threadStarter(csvFile, session, generalMail, wordsToEdit, csvReader);

            
        } catch (FileNotFoundException e) {
            System.out.println("Errore durante l'apertura del file.");
            e.printStackTrace();
        }
    }

    /**
     * Verifica se la stringa fornita è un indirizzo email valido.
     * Un indirizzo email valido per questo metodo corrisponde al pattern "^[A-Za-z0-9+_.-]+@(.+)$".
     *
     * @param email la stringa da verificare.
     * @return true se la stringa è un indirizzo email valido, false altrimenti.
     */
    private static boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    /**
     * Imposta le proprietà necessarie per l'invio di email tramite SMTP utilizzando un server Gmail.
     *
     * @param props l'oggetto Properties in cui inserire le proprietà.
     * @param senderEmail l'indirizzo email del mittente.
     */
    private static void insertPropertiesMail(Properties props, String senderEmail) {
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.from", senderEmail);
    }


    /**
     * Legge un file e aggiunge alla lista fornita tutte le parole racchiuse tra parentesi quadrate.
     *
     * @param generalMail il file da cui leggere le parole.
     * @param wordsToEdit la lista a cui aggiungere le parole trovate.
     * @return la lista di parole con le nuove parole aggiunte.
     */
    private static List<String> scannerWords(File generalMail, List<String> wordsToEdit) {
        try {
            Scanner generalMailReader = new Scanner(generalMail);
            Pattern pattern = Pattern.compile("\\[(.*?)\\]");
            Matcher matcher = pattern.matcher(generalMailReader.useDelimiter("\\Z").next());
            while (matcher.find()) {
                wordsToEdit.add(matcher.group(1));
            }
            generalMailReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("Errore durante l'apertura del file.");
            e.printStackTrace();
        }
        return wordsToEdit;
    }


    private static void threadStarter(File csvFile, Session session, File generalMail, List<String> wordsToEdit, CSVReader csvReader) throws IOException {
        try{
            for(String[] row : csvReader) {
                // Invia la mail
                String recipientEmail = row[0];
                List<String> recipientWords = new ArrayList<>();
                for(int i = 1; i < row.length; i++) {
                    recipientWords.add(row[i]);
                }
                SenderThread senderThread = new SenderThread();
                senderThread.setSession(session);
                senderThread.setRecipientEmail(recipientEmail);
                senderThread.setGeneralMail(generalMail);
                senderThread.setWords(wordsToEdit);
                senderThread.setRecipientWords(recipientWords);
                new Thread(senderThread).start();
            }
            csvReader.close();
        } catch (IOException e) {
            System.out.println("Errore durante la lettura del file.");
            e.printStackTrace();
        }
    }

}