# MailSender

MailSender è un'applicazione Java che consente l'invio di e-mail personalizzate a un insieme di destinatari. L'applicazione utilizza un server SMTP di Gmail per l'invio di e-mail e per essere utilizzata non bisogna avere l'autenticazione a due fattori attiva e deve essere attivata l'impostazione per l'accesso alle app meno sicure al link [App meno sicure](https://myaccount.google.com/u/0/lesssecureapps?pli=1&rapt=AEjHL4NxEA3BgTnjOoLiJZGRqx3c8Di_zV2OsETFEjOsSsITJqovt7AzFN2ac64Ofme7bfjxRvovEwJKAIqCbr_BaWo8em7-XtmzKiyw9T3NTOqWZhtBfaw&pageId=none) del profilo usato per l'invio delle mail.

Il progetto contiene un insieme di file a titolo di esempio con la dicitura `Example` alla fine del nome del file.
Per l'utilizzo dell'applicazione è necessario modificare i file `credentialsExample.txt`, `generalMailExample.txt` e `recipientDataExample.csv` con le proprie credenziali, il testo della mail e i destinatari rinominandoli rispettivamente in `credentials.txt`, `generalMail.txt` e `recipientData.csv`.

Viene utilizzato Maven per la gestione delle dipendenze e per la compilazione del progetto.

Il metodo più facile per eseguire il progetto è utilizzare il file `mailsender-1.0-SNAPSHOT.jar` ed eseguirlo nella stessa directory dei file `credentials.txt`, `generalMail.txt` e `recipientData.csv`.

In alternativa possiamo eseguire il programma eseguire il comando `mvn clean compile exec:java` nella directory del progetto.

## Funzionalità

- **Lettura delle credenziali**: L'applicazione legge le credenziali dell'account di posta elettronica da un file `credentials.txt`. Queste credenziali sono utilizzate per autenticarsi al server SMTP.

- **Validazione dell'indirizzo email**: Prima dell'invio, l'applicazione verifica che l'indirizzo email del destinatario sia valido. Questo aiuta a prevenire errori di invio.

- **Configurazione del server SMTP**: L'applicazione configura le proprietà necessarie per l'invio di e-mail tramite un server SMTP di Gmail. Questo include l'indirizzo del server, la porta e le opzioni di sicurezza.

- **Personalizzazione delle e-mail**: L'applicazione legge un file `generalMail.txt` e aggiunge le parole contenute nel file a una lista di parole da modificare. Queste parole vengono poi sostituite con parole specifiche del destinatario nel corpo dell'e-mail.

- **Lettura dei destinatari**: L'applicazione legge i destinatari da un file CSV `recipientData.csv`. Ogni riga del file CSV rappresenta un destinatario e contiene l'indirizzo e-mail del destinatario e le parole specifiche del destinatario.

- **Invio parallelo delle e-mail**: L'applicazione avvia un thread per ogni destinatario per l'invio delle e-mail. Questo permette di inviare e-mail a più destinatari in parallelo, migliorando l'efficienza dell'applicazione.

- **Gestione delle eccezioni**: L'applicazione gestisce le eccezioni per i file non trovati e gli errori durante l'attesa dei thread. Questo aiuta a prevenire che l'applicazione si interrompa in caso di errori.
