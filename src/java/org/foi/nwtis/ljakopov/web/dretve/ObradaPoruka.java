/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.ljakopov.web.dretve;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    String poruka = "";
    String uredjaj = "";
    Connection c;

    public String ProvjeriRegex(String poruka) {
        poruka = poruka.trim();
        String sintaksaAdd = "^ADD IoT ([1-6]{1}) (\"[^\\\\s]{1,30}\") GPS: ([0-9]{1,3})(([,.]{1})([0-9]{6})),([0-9]{1,3})(([,.]{1})([0-9]{6}));$";
        Pattern pattern = Pattern.compile(sintaksaAdd);
        Matcher m = pattern.matcher(poruka);
        boolean statusAdd = m.matches();
        boolean statusTemp = false;
        boolean statusEvent = false;
        System.out.println("Oo je kod ADD: " + statusAdd);
        if (statusAdd) {
            return "ADD";
        } else {
            System.out.println("OVO JE PORUKA KOJA JE DOŠLA DO konzole: " + poruka);
            String sintaksaTemp = "^TEMP IoT [1-6]{1} T: [0-9]{4}([.])(((0[13578]|(10|12))\\1(0[1-9]|[1-2][0-9]|3[0-1]))|(02\\1(0[1-9]|[1-2][0-9]))|((0[469]|11)\\1(0[1-9]|[1-2][0-9]|30))) (?<![:0-9])(0?[0-9]|1[0-9]|2[0-3]):(60|[0-5][0-9])(?::)?(60|[0-5][0-9])?(?![:0-9]+\\b) C: ([0-9]{1,2});$";
            Pattern patternTemp = Pattern.compile(sintaksaTemp);
            Matcher mTemp = patternTemp.matcher(poruka);
            statusTemp = mTemp.matches();
            System.out.println("Ovo je status TEMP: " + statusTemp);
            if (statusTemp) {
                System.out.println("OVO JE TEMP");
                return "TEMP";
            } else {
                System.out.println("OVO JE PORUKA KOJA JE DOŠLA DO konzole: " + poruka);
                String sintaksaEvent = "^EVENT IoT [1-6]{1} T: [0-9]{4}([.])(((0[13578]|(10|12))\\1(0[1-9]|[1-2][0-9]|3[0-1]))|(02\\1(0[1-9]|[1-2][0-9]))|((0[469]|11)\\1(0[1-9]|[1-2][0-9]|30))) (?<![:0-9])(0?[0-9]|1[0-9]|2[0-3]):(60|[0-5][0-9])(?::)?(60|[0-5][0-9])?(?![:0-9]+\\b) F: ([0-9]{1,2});$";
                Pattern patternEvent = Pattern.compile(sintaksaEvent);
                Matcher mEvent = patternEvent.matcher(poruka);
                statusEvent = mEvent.matches();
                System.out.println("Ovo je status EVENT: " + statusEvent);
                if (statusEvent) {
                    System.out.println("OVO JE EVENT");
                    return "EVENT";
                } else {
                    return "NISTA";
                }
            }
        }

    }

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
        String db_Username = konf.dajPostavku("user.username");
        String db_Password = konf.dajPostavku("user.password");
        String db_Host = konf.dajPostavku("server.database");
        String db_name = konf.dajPostavku("user.database");
        String db_driver = konf.dajPostavku("driver.database.mysql");

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

            String traziNazivUTablici = "SELECT naziv FROM uredaji WHERE naziv=?";
            // Connect to the store
            try {
                Store store = session.getStore("imap");
                store.connect(server, korisnik, lozinka);

                /**
                 * kreiraju se novi folderi za spremanje poruka koje
                 * zadovoljavaju regex i ostalih poruka
                 */
                Folder folderZaSpremanje = store.getDefaultFolder();
                if (folderZaSpremanje.getFolder(ostalePoruke).exists() != true) {
                    folderZaSpremanje.getFolder(ostalePoruke).create(Folder.HOLDS_MESSAGES);
                }

                if (folderZaSpremanje.getFolder(odredjenePoruke).exists() != true) {
                    folderZaSpremanje.getFolder(odredjenePoruke).create(Folder.HOLDS_MESSAGES);
                }

                // Open the INBOX folder
                Folder folder = store.getFolder("INBOX");
                folder.open(Folder.READ_WRITE);

                Folder folderZaSpremanjeIspravnihPoruka = store.getFolder(odredjenePoruke);
                folderZaSpremanjeIspravnihPoruka.open(Folder.READ_WRITE);
                Folder folderZaSpremanjeOstalihPoruka = store.getFolder(ostalePoruke);
                folderZaSpremanjeOstalihPoruka.open(Folder.READ_WRITE);

                Message[] messages = folder.getMessages();
                Message[] porukaZaPremjestanje = new Message[1];
                System.out.println("OVo je broj porula: " + messages.length);
                for (int i = 0; i < messages.length; ++i) {
                    System.out.println("OVo je SUBJECT: " + messages[i].getSubject());

                    if (messages[i].getSubject().equals(nwtis_porukaString)) {
                        porukaZaPremjestanje[0] = messages[i];
                        try {
                            Object o = messages[i].getContent();
                            System.out.println("Iamo poruku: " + (String) o);
                            uredjaj = (String) o;
                            poruka = ProvjeriRegex((String) o);

                        } catch (IOException ex) {
                            Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        System.out.println("Ovo je poruka u poruki za premsjetsanje: " + porukaZaPremjestanje[0].getSubject());

                        try {
                            Class.forName(db_driver);
                        } catch (ClassNotFoundException ex) {
                            Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        try {
                            if (c == null) {
                                System.out.println("PONOVNA KONEKCIJA");
                                c = DriverManager.getConnection(db_Host + db_name,
                                        db_Username,
                                        db_Password);
                            }
                        } catch (SQLException ex) {
                            Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        switch (poruka) {
                            case "ADD":
                                System.out.println("OVO JE PROBA ZA ADD: " + uredjaj.indexOf('"'));
                                System.out.println("OVO JE PROBA ZA ADD: " + uredjaj.lastIndexOf('"'));
                                System.out.println("OVO JE PRAVA RIJEC: " + uredjaj.substring(uredjaj.indexOf('"') + 1, uredjaj.lastIndexOf('"')));
                                 {
                                    try {
                                        PreparedStatement ps = c.prepareStatement(traziNazivUTablici);
                                        ps.setString(1, uredjaj.substring(uredjaj.indexOf('"') + 1, uredjaj.lastIndexOf('"')));
                                        ResultSet resultSet = ps.executeQuery();
                                        if(resultSet.next()){
                                            System.out.println("ZAPIS VEC POSTOJI U BAZI");
                                        }
                                        else{
                                            System.out.println("ZAPIS NE POSTOJI U BAZIIII");
                                        }
                                        
                                    } catch (SQLException ex) {
                                        Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                                break;
                        }
                    } else {

                    }

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
