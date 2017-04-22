/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.ljakopov.web.dretve;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.servlet.ServletContext;
import org.foi.nwtis.ljakopov.konfiguracije.Konfiguracija;

/**
 *
 * @author grupa_2
 */
public class ObradaPoruka extends Thread {

    private boolean prekid_obrade = false;
    private ServletContext sc = null;

    @Override
    public void interrupt() {
        prekid_obrade = true;
        super.interrupt(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void run() {
        Konfiguracija konf = (Konfiguracija) sc.getAttribute("Mail_Konfig");
        String server = konf.dajPostavku("mail.server");
        String port = konf.dajPostavku("mail.port");
        String korisnik = konf.dajPostavku("mail.usernameThread");
        String lozinka = konf.dajPostavku("mail.passwordThread");
        String odredjenePoruke = konf.dajPostavku("mail.folderNWTiS");
        String ostalePoruke = konf.dajPostavku("mail.folderOther");
        String nwtis_porukaString = konf.dajPostavku("mail.subject");

        int trajanjeCiklusa = Integer.parseInt(konf.dajPostavku("mail.timeSecThread"));
        //TODO i za ostale parametre
        int trajanjeObrade = 0;
        //TODO odredi trajanje obrade
        int redniBrojCiklusa = 0;

        while (!prekid_obrade) {
            // Start the session
            java.util.Properties properties = System.getProperties();
            properties.put("mail.smtp.host", server);
            Session session = Session.getInstance(properties, null);

            // Connect to the store
            try {
                Store store = session.getStore("imap");
                store.connect(server, korisnik, lozinka);

                /**
                 * kreiraju se novi folderi za spremanje poruka koje zadovoljavaju regex i 
                 * ostalih poruka
                 */
                Folder folderZaSpremanje = store.getDefaultFolder();
                if (folderZaSpremanje.getFolder(ostalePoruke).exists() != false) {
                    folderZaSpremanje.getFolder(ostalePoruke).create(Folder.HOLDS_MESSAGES);
                }

                if (folderZaSpremanje.getFolder(odredjenePoruke).exists() != false) {
                    folderZaSpremanje.getFolder(odredjenePoruke).create(Folder.HOLDS_MESSAGES);
                }

                // Open the INBOX folder
                Folder folder = store.getFolder("INBOX");
                folder.open(Folder.READ_ONLY);
                Folder folderZaSpremanjeIspravnihPoruka = store.getFolder(odredjenePoruke);
                folderZaSpremanjeIspravnihPoruka.open(Folder.READ_ONLY);
                Folder folderZaSpremanjeOstalihPoruka = store.getFolder(ostalePoruke);
                folderZaSpremanjeOstalihPoruka.open(Folder.READ_ONLY);

                String subjekt="";
                String porukaString="";
                Message[] messages = folder.getMessages();
                System.out.println("OVo je broj porula: " + messages.length);
                for (int i = 0; i < messages.length; ++i) {
                    System.out.println("OVo je SUBJECT: " + messages[i].getSubject());
                    if(messages[i].getSubject().equals(nwtis_porukaString)){
                        //folder.copyMessages((Message[])messages[i].to, folder);
                        
                    }
                    //subjekt=messages[i].getSubject();
                    //TODO dovrišiti čitanje, obradu i prebacivanje u mape
                }
                redniBrojCiklusa++;
                System.out.println("Obrada prouka u cilkusu: " + redniBrojCiklusa);
                sleep(trajanjeCiklusa * 1000 - trajanjeObrade);

            } catch (NoSuchProviderException ex) {
                Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
            } catch (MessagingException | InterruptedException ex) {
                Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    @Override
    public synchronized void start() {
        super.start(); //To change body of generated methods, choose Tools | Templates.
    }

    public void setSc(ServletContext sc) {
        this.sc = sc;
    }
}
