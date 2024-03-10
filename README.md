# MailSender

MailSender è un'applicazione Java che consente l'invio di e-mail personalizzate a un insieme di destinatari. L'applicazione utilizza un server SMTP di Gmail per l'invio di e-mail.

## Funzionalità

- **Lettura delle credenziali**: L'applicazione legge le credenziali dell'account di posta elettronica da un file `credentials.txt`. Queste credenziali sono utilizzate per autenticarsi al server SMTP.

- **Validazione dell'indirizzo email**: Prima dell'invio, l'applicazione verifica che l'indirizzo email del destinatario sia valido. Questo aiuta a prevenire errori di invio.

- **Configurazione del server SMTP**: L'applicazione configura le proprietà necessarie per l'invio di e-mail tramite un server SMTP di Gmail. Questo include l'indirizzo del server, la porta e le opzioni di sicurezza.

- **Personalizzazione delle e-mail**: L'applicazione legge un file `generalMail.txt` e aggiunge le parole contenute nel file a una lista di parole da modificare. Queste parole vengono poi sostituite con parole specifiche del destinatario nel corpo dell'e-mail.

- **Lettura dei destinatari**: L'applicazione legge i destinatari da un file CSV `recipientData.csv`. Ogni riga del file CSV rappresenta un destinatario e contiene l'indirizzo e-mail del destinatario e le parole specifiche del destinatario.

- **Invio parallelo delle e-mail**: L'applicazione avvia un thread per ogni destinatario per l'invio delle e-mail. Questo permette di inviare e-mail a più destinatari in parallelo, migliorando l'efficienza dell'applicazione.

- **Gestione delle eccezioni**: L'applicazione gestisce le eccezioni per i file non trovati e gli errori durante l'attesa dei thread. Questo aiuta a prevenire che l'applicazione si interrompa in caso di errori.
