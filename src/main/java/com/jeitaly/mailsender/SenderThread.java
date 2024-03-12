package com.jeitaly.mailsender;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * <h1>SenderThread</h1>
 * La classe SenderThread implementa l'interfaccia Runnable per l'invio di e-mail in parallelo.
 * <p>
 * Questa classe contiene le seguenti informazioni:
 * <ul>
 * <li>Una sessione di posta elettronica.</li>
 * <li>L'indirizzo e-mail del mittente.</li>
 * <li>L'indirizzo e-mail del destinatario.</li>
 * <li>L'oggetto dell'e-mail.</li>
 * <li>Un file che contiene il corpo generale dell'e-mail.</li>
 * <li>Una lista di parole da sostituire nel corpo dell'e-mail.</li>
 * <li>Una lista di parole specifiche del destinatario da inserire nel corpo dell'e-mail.</li>
 * </ul>
 * <p>
 * Quando un oggetto SenderThread viene eseguito, invia un'e-mail al destinatario sostituendo le parole nel corpo dell'e-mail con le parole specifiche del destinatario.
* <p>
*/
public class SenderThread implements Runnable {

    private Session session;
    private String senderMail;
    private String recipientMail;
    private String mailobject;
    private File generalMail;
    private List<String> wordsToEdit;
    private List<String> recipientWords;

    public SenderThread(Session session,String senderMail, String recipientMail, String mailobject, File generalMail, List<String> wordsToEdit, List<String> recipientWords) {
        this.session = session;
        this.senderMail = senderMail;
        this.recipientMail = recipientMail;
        this.mailobject = mailobject;
        this.generalMail = generalMail;
        this.wordsToEdit = wordsToEdit;
        this.recipientWords = recipientWords;
    }


    @Override
    public void run() {
        
        try {
            // Crea un file temporaneo per la mail modificata
            File tempEditedMail = File.createTempFile("tempEditedMail", ".txt");
            tempEditedMail.deleteOnExit();

            //Preparo la mail da inviare
            editMail(generalMail, wordsToEdit, recipientWords, tempEditedMail);

            // Invia la mail
            sendMail(session, senderMail, recipientMail, mailobject, tempEditedMail);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Modifica il contenuto di un'email generale sostituendo le parole specificate con le parole del destinatario.
     * <p>
     * Questo metodo legge il contenuto del file di posta generale, sostituisce le parole indicate con le parole corrispondenti
     * dal file recipientData.csv, e scrive l'email modificata in un file temporaneo.
     * <p>
     * Se si verifica un'eccezione durante la lettura, la scrittura o la modifica del file, viene stampato lo stack trace dell'eccezione.
     *
     * @param generalMail Il file che contiene il corpo generale dell'email.
     * @param wordsToEdit La lista di parole da sostituire nel corpo dell'email.
     * @param recipientWords La lista di parole specifiche del destinatario da inserire nel corpo dell'email.
     * @param tempEditedMail Il file temporaneo in cui scrivere l'email modificata.
     */
    private void editMail(File generalMail, List<String> wordsToEdit, List<String> recipientWords, File tempEditedMail) {
        try{
            // Leggi il contenuto del file generalMail.txt
            String content = new String(Files.readAllBytes(generalMail.toPath()), StandardCharsets.UTF_8);
    
            // Sostituisci le parole nel contenuto
            for (int i = 0; i < wordsToEdit.size(); i++) {
                String word = wordsToEdit.get(i);
                String recipientWord = recipientWords.get(i);
                content = content.replaceAll(Pattern.quote(word), Matcher.quoteReplacement(recipientWord));
            }
    
            // Scrivi il contenuto modificato nel file temporaneo
            Files.write(tempEditedMail.toPath(), content.getBytes(StandardCharsets.UTF_8));
    
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Invia un'e-mail utilizzando una sessione di posta elettronica, un mittente, un destinatario, un oggetto e un file che contiene il corpo dell'e-mail.
     * <p>
     * Questo metodo crea un nuovo messaggio, imposta il mittente, il destinatario, l'oggetto e il contenuto del messaggio, e poi invia il messaggio.
     * Il contenuto del messaggio viene letto dal file fornito.
     * <p>
     * Se si verifica un'eccezione durante l'impostazione del messaggio o l'invio del messaggio, viene stampato lo stack trace dell'eccezione.
     * 
     * @param session La sessione di posta elettronica da utilizzare per inviare l'e-mail.
     * @param senderMail L'indirizzo e-mail del mittente.
     * @param recipientMail L'indirizzo e-mail del destinatario.
     * @param mailobject L'oggetto dell'e-mail.
     * @param tempEditedMail Il file che contiene il corpo dell'e-mail.
     */
    private void sendMail(Session session, String senderMail, String recipientMail, String mailobject, File tempEditedMail) {
        try {
            // Crea nuovo messaggio
            Message message = new MimeMessage(session);

            // Imposta il mittente
            message.setFrom(new InternetAddress(senderMail));

            // Imposta il destinatario
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientMail));

            // Imposta l'oggetto
            message.setSubject(mailobject);

            // Imposta il contenuto del messaggio
            message.setText(new String(Files.readAllBytes(tempEditedMail.toPath()), StandardCharsets.UTF_8));

            // Invia il messaggio
            Transport.send(message);
        
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
