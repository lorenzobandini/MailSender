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
            File credentiaFile = new File("./src/main/java/com/jeitaly/mailsender/credentials.txt");
            Scanner credentialReader = new Scanner(credentiaFile);
            String senderEmail = credentialReader.nextLine();
            if(!isValidEmail(senderEmail)) {
                System.out.println("L'indirizzo email inserito non è valido.");
                credentialReader.close();
                return;
            }
            String senderPassword = credentialReader.nextLine();
            credentialReader.close();

            // Imposta le proprietà
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.from", senderEmail);
            
            // Crea una sessione di autenticazione
            Session session = Session.getInstance(props, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(senderEmail, senderPassword);
                }
            });
            if(session == null) {
                System.out.println("Errore durante la creazione della sessione.");
                return;
            }

            // Legge le occorrenze nel testo
            File generalMail = new File("./src/main/java/com/jeitaly/mailsender/generalMail.txt");
            Scanner generalMailReader = new Scanner(generalMail);
            List<String> words = new ArrayList<>();
            Pattern pattern = Pattern.compile("\\[(.*?)\\]");
            Matcher matcher = pattern.matcher(generalMailReader.useDelimiter("\\Z").next());
            while (matcher.find()) {
                words.add(matcher.group(1));
            }
            generalMailReader.close();
            //ho un array con tutte le occorrenze da sostituire nell'ordine di comparsa nel testo

            // Legge il file CSV
            File csvFile = new File("./src/main/java/com/jeitaly/mailsender/emails.csv");
            CSVReader csvReader = new CSVReader(new FileReader(csvFile));
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
                senderThread.setWords(words);
                senderThread.setRecipientWords(recipientWords);
                new Thread(senderThread).start();
            }
            csvReader.close();
            
        } catch (FileNotFoundException e) {
            System.out.println("Errore durante l'apertura del file.");
            e.printStackTrace();
        }
    }

    public static boolean isValidEmail(String email) {
    String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
    Pattern pattern = Pattern.compile(emailRegex);
    Matcher matcher = pattern.matcher(email);
    return matcher.matches();
    }
}