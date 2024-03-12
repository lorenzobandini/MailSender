package com.jeitaly.mailsender;
import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.mail.*;

/**
 * <h1>MailSenderMain</h1>
 * La classe MailSenderMain fornisce un'applicazione per l'invio di e-mail a un insieme di destinatari.
 * <p>
 * <b>Note:</b> Questa classe utilizza le seguenti risorse:
 * <ul>
 * <li>Un file "credentials.txt" per leggere le credenziali dell'account di posta elettronica.</li>
 * <li>Un server SMTP di Gmail per l'invio di e-mail.</li>
 * <li>Un file di testo "generalMail.txt" per raccogliere tutte le parole da sostituire nel corpo dell'e-mail.</li>
 * <li>Un file CSV "recipientData.csv" che contiene le informazioni sui destinatari delle e-mail.</li>
 * </ul>
 * <p>
 * Ogni riga del file CSV deve contenere alla prima posizione l'indirizzo e-mail del destinatario e in seconda posizione l'oggetto della mail, a seguire le parole da sostituire nel corpo dell'e-mail.
 * <p>
 * Questa classe gestisce anche le eccezioni per i file non trovati e gli errori durante l'attesa dei thread.
 * <p>
 */
public class MailSenderMain {

    public static void main(String[] args) throws IOException {
        try {
            // Legge le credenziali dal file credentials.txt
            File credentiaFile = new File("./src/main/java/com/jeitaly/mailsender/credentialsFile.txt");
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
            System.out.println("Le parole da modificare sono: " + wordsToEdit);

            // Legge i destinatari dal file emails.csv
            File csvFile = new File("./src/main/java/com/jeitaly/mailsender/recipientData.csv");
            CSVReader csvReader = new CSVReader(new FileReader(csvFile));

            // Avvia i thread per l'invio delle email
            ExecutorService executor = Executors.newFixedThreadPool(10);
            threadStarter(csvFile, session, senderEmail, generalMail, wordsToEdit, csvReader, executor);
            executor.shutdown();

            // Attende che tutti i thread abbiano terminato
            executor.awaitTermination(30, TimeUnit.SECONDS);

        } catch (FileNotFoundException e) {
            System.out.println("Errore durante l'apertura del file.");
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("Errore durante l'attesa dei thread.");
            e.printStackTrace();
        }
    }

    /**
     * Verifica se un indirizzo e-mail è valido.
     * <p>
     * Questo metodo utilizza un'espressione regolare per verificare se l'indirizzo e-mail fornito è valido.
     * L'espressione regolare accetta qualsiasi combinazione di caratteri prima del simbolo &#64,
     * seguiti da qualsiasi carattere dopo il simbolo &#64.
     * 
     * @param email L'indirizzo e-mail da verificare.
     * @return true se l'indirizzo e-mail è valido, false altrimenti.
     */
    private static boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    /**
     * Configura le proprietà per l'invio di e-mail tramite un server SMTP di Gmail.
     * <p>
     * Questo metodo imposta le seguenti proprietà:
     * <ul>
     * <li>mail.smtp.auth: abilita l'autenticazione SMTP.</li>
     * <li>mail.smtp.starttls.enable: abilita la comunicazione sicura tramite TLS.</li>
     * <li>mail.smtp.host: imposta l'host del server SMTP su "smtp.gmail.com".</li>
     * <li>mail.smtp.port: imposta la porta del server SMTP su "587".</li>
     * <li>mail.smtp.from: imposta l'indirizzo e-mail del mittente.</li>
     * </ul>
     * 
     * @param props Le proprietà da configurare.
     * @param senderEmail L'indirizzo e-mail del mittente.
     */
    private static void insertPropertiesMail(Properties props, String senderEmail) {
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.from", senderEmail);
    }

    /**
     * Scansiona il file di posta generale e aggiunge tutte le parole racchiuse tra parentesi quadrate alla lista delle parole da modificare.
     * <p>
     * Questo metodo utilizza un'espressione regolare per trovare tutte le parole racchiuse tra parentesi quadrate nel file di posta generale.
     * Se una parola non è già presente nella lista delle parole da modificare, viene aggiunta.
     * <p>
     * Se il file di posta generale non può essere aperto, stampa un messaggio di errore e termina l'esecuzione.
     * 
     * @param generalMail Il file di testo che contiene il corpo generale dell'e-mail.
     * @param wordsToEdit La lista di parole da sostituire nel corpo dell'e-mail.
     * @return La lista aggiornata di parole da sostituire nel corpo dell'e-mail.
     */
    private static List<String> scannerWords(File generalMail, List<String> wordsToEdit) {
        try {
            Scanner generalMailReader = new Scanner(generalMail);
            Pattern pattern = Pattern.compile("\\[(.*?)\\]");
            Matcher matcher = pattern.matcher(generalMailReader.useDelimiter("\\Z").next());
            while (matcher.find()) {
                if(!wordsToEdit.contains(matcher.group(0))){
                    wordsToEdit.add(matcher.group(0));
                }
            }
            generalMailReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("Errore durante l'apertura del file.");
            e.printStackTrace();
        }
        return wordsToEdit;
    }

    /**
     * Avvia un nuovo thread per ogni destinatario di e-mail.
     * <p>
     * Questo metodo legge ogni riga del file CSV fornito, verifica che il numero di parole da sostituire corrisponda al numero di parole fornite,
     * crea una lista di parole per ogni destinatario e avvia un nuovo thread per inviare l'e-mail.
     * <p>
     * In caso di errore durante l'avvio dei thread, stampa un messaggio di errore e termina l'esecuzione.
     * 
     * @param csvFile Il file CSV che contiene le informazioni sui destinatari delle e-mail.
     * @param session La sessione di posta elettronica.
     * @param senderEmail L'indirizzo e-mail del mittente.
     * @param generalMail Il file di testo che contiene il corpo generale dell'e-mail.
     * @param wordsToEdit La lista di parole da sostituire nel corpo dell'e-mail.
     * @param csvReader Il lettore CSV per leggere il file CSV.
     * @param executor Il servizio executor per gestire i thread.
     * @throws IOException Se si verifica un errore durante la lettura del file CSV.
     */
    private static void threadStarter(File csvFile, Session session, String senderEmail, File generalMail, List<String> wordsToEdit, CSVReader csvReader, ExecutorService executor) throws IOException {
        try{
            for(String[] row : csvReader) {

                // Controllo che il file csv sia scritto bene ad ogni riga
                if((row.length - 2) != wordsToEdit.size()) {
                    System.out.println("Errore: il numero di parole da sostituire non corrisponde al numero di parole fornite.");
                    return;
                }

                // Creo una lista di parole per ogni destinatario
                String recipientEmail = row[0];
                if(!isValidEmail(recipientEmail)) {
                    System.out.println("Errore: l'indirizzo email del destinatario non è valido.");
                    return;
                }
                String mailObject = row[1];
                List<String> recipientWords = new ArrayList<>();
                for(int i = 2; i < row.length; i++) {
                    recipientWords.add(row[i].trim());
                }

                // Mando in escuzione un thread per ogni destinatario
                executor.execute(new SenderThread(session, senderEmail, recipientEmail, mailObject, generalMail, wordsToEdit, recipientWords));
            }
        }catch(Exception e) {   
            System.out.println("Errore durante la partenza dei thread.");
            e.printStackTrace();
        } finally {
        csvReader.close();
        }
    }
}
