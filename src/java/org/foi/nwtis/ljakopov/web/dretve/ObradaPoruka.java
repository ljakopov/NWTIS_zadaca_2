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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
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
    String uredjajZaNaziv;
    String[] uredjaj;
    Connection c;
    int brojPoruka = 0;
    int brojDodanihIoT = 0;
    int brojMjerenihTemp = 0;
    int brojIzvrsenihEvent = 0;
    int brojPogresaka = 0;
    long pocetakObrade = 0;
    long zavrsetakObrade = 0;

    public String ProvjeriRegex(String poruka) {
        poruka = poruka.trim();
        String sintaksaAdd = "^ADD IoT ([0-9]{1,6}) (\"[^\\\\s]{1,30}\") GPS: ([0-9]{1,3})(([,.]{1})([0-9]{6})),([0-9]{1,3})(([,.]{1})([0-9]{6}));$";
        Pattern pattern = Pattern.compile(sintaksaAdd);
        Matcher m = pattern.matcher(poruka);
        boolean statusAdd = m.matches();
        boolean statusTemp = false;
        boolean statusEvent = false;
        if (statusAdd) {
            System.out.println("Oo je kod ADD: " + statusAdd);
            return "ADD";
        } else {
            System.out.println("OVO JE PORUKA KOJA JE DOŠLA DO konzole: " + poruka);
            String sintaksaTemp = "^TEMP IoT [0-9]{1,6} T: [0-9]{4}([.])(((0[13578]|(10|12))\\1(0[1-9]|[1-2][0-9]|3[0-1]))|(02\\1(0[1-9]|[1-2][0-9]))|((0[469]|11)\\1(0[1-9]|[1-2][0-9]|30))) (?<![:0-9])(0?[0-9]|1[0-9]|2[0-3]):(60|[0-5][0-9])(?::)?(60|[0-5][0-9])?(?![:0-9]+\\b) C: ([0-9]{1,2});$";
            Pattern patternTemp = Pattern.compile(sintaksaTemp);
            Matcher mTemp = patternTemp.matcher(poruka);
            statusTemp = mTemp.matches();
            if (statusTemp) {
                System.out.println("OVO JE TEMP: " + poruka);
                return "TEMP";
            } else {
                String sintaksaEvent = "^EVENT IoT [0-9]{1,6} T: [0-9]{4}([.])(((0[13578]|(10|12))\\1(0[1-9]|[1-2][0-9]|3[0-1]))|(02\\1(0[1-9]|[1-2][0-9]))|((0[469]|11)\\1(0[1-9]|[1-2][0-9]|30))) (?<![:0-9])(0?[0-9]|1[0-9]|2[0-3]):(60|[0-5][0-9])(?::)?(60|[0-5][0-9])?(?![:0-9]+\\b) F: ([0-9]{1,2});$";
                Pattern patternEvent = Pattern.compile(sintaksaEvent);
                Matcher mEvent = patternEvent.matcher(poruka);
                statusEvent = mEvent.matches();
                if (statusEvent) {
                    System.out.println("OVO JE EVENT: " + poruka);
                    return "EVENT";
                } else {
                    return "NISTA";
                }
            }
        }
    }

    private void PosaljiStatistiku(String posluzitelj, String salje, String prima, String predmet, String sadrzaj) {
        String status;

        try {
            // Create the JavaMail session
            java.util.Properties properties = System.getProperties();
            properties.put("mail.smtp.host", posluzitelj);

            Session session
                    = Session.getInstance(properties, null);

            // Construct the message
            Message message = new MimeMessage(session);

            // Set the from address
            Address fromAddress = new InternetAddress(salje);
            message.setFrom(fromAddress);

            // Parse and set the recipient addresses
            Address[] toAddresses = InternetAddress.parse(prima);
            message.setRecipients(Message.RecipientType.TO, toAddresses);

            // Set the subject and text
            message.setSentDate(new Date());
            message.setSubject(predmet);
            message.setText(sadrzaj);

            Transport.send(message);

            status = "Your message was sent.";
        } catch (AddressException e) {
            e.printStackTrace();
            status = "There was an error parsing the addresses.";
        } catch (SendFailedException e) {
            e.printStackTrace();
            status = "There was an error sending the message.";
        } catch (MessagingException e) {
            e.printStackTrace();
            status = "There was an unexpected error.";
        }

    }

    @Override
    public void interrupt() {
        prekid_obrade = true;
        super.interrupt(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void run() {
        SimpleDateFormat dt = new SimpleDateFormat("dd.MM.yyyy hh.mm.ss.SSS");
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
        String saljiStatistiku = konf.dajPostavku("mail.usernameStatistics");
        String saljiStatistikuPredmet = konf.dajPostavku("mail.subjectStatistics");

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

            String traziNazivUTablici = "SELECT naziv FROM uredaji WHERE id=?";
            // Connect to the store
            try {
                String pocetakObradeZapis = dt.format(new Date());
                pocetakObrade = new Date().getTime();
                System.out.println("OVO JE VRIJEME: " + pocetakObradeZapis);
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
                brojPoruka = 0;
                brojDodanihIoT = 0;
                brojMjerenihTemp = 0;
                brojIzvrsenihEvent = 0;
                brojPogresaka = 0;

                Message[] messages = folder.getMessages();
                Message[] porukaZaPremjestanje = new Message[MIN_PRIORITY];
                System.out.println("OVo je broj porula: " + messages.length);
                for (int i = 0; i < messages.length; ++i) {
                    porukaZaPremjestanje[0] = messages[i];
                    System.out.println("OVO SU PORUKE: " + messages[i].getSubject());
                    brojPoruka++;
                    System.out.println("BROJ PORUKA: " + brojPoruka);

                    if (messages[i].getSubject().equals(nwtis_porukaString)) {
                        try {
                            Object o = messages[i].getContent();
                            System.out.println("Iamo poruku: " + (String) o);
                            uredjaj = ((String) o).split(" ");
                            uredjajZaNaziv = ((String) o);
                            poruka = ProvjeriRegex((String) o);

                        } catch (IOException ex) {
                            Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        //folder.copyMessages(porukaZaPremjestanje, folderZaSpremanjeIspravnihPoruka);
                        //messages[i].setFlag(Flag.DELETED, true);

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
                            case "ADD": {
                                try {
                                    PreparedStatement ps = c.prepareStatement(traziNazivUTablici);
                                    ps.setString(1, uredjaj[2]);
                                    ResultSet resultSet = ps.executeQuery();
                                    if (resultSet.next()) {
                                        System.out.println("ZAPIS VEC POSTOJI U BAZI");
                                        brojPogresaka++;
                                    } else {
                                        System.out.println("ZAPIS NE POSTOJI U BAZIIII");
                                        String sqlUnesiUredjaj = "INSERT INTO uredaji (id, naziv, latitude, longitude, status, vrijeme_promjene, vrijeme_kreiranja) VALUES(?, ?, ?, ?, 0, default, default)";
                                        PreparedStatement unesiUredjaj = c.prepareStatement(sqlUnesiUredjaj);
                                        unesiUredjaj.setString(1, uredjaj[2]);
                                        unesiUredjaj.setString(2, uredjajZaNaziv.substring(uredjajZaNaziv.indexOf('"') + 1, uredjajZaNaziv.lastIndexOf('"')));
                                        unesiUredjaj.setString(3, uredjajZaNaziv.substring(uredjajZaNaziv.indexOf(':') + 1, uredjajZaNaziv.lastIndexOf(',')).trim());
                                        unesiUredjaj.setString(4, uredjajZaNaziv.substring(uredjajZaNaziv.indexOf(',') + 1, uredjajZaNaziv.lastIndexOf(';')));
                                        unesiUredjaj.executeUpdate();
                                        brojDodanihIoT++;
                                    }

                                } catch (SQLException ex) {
                                    Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            break;
                            case "TEMP": {
                                try {
                                    PreparedStatement psTemp = c.prepareStatement(traziNazivUTablici);
                                    psTemp.setString(1, uredjaj[2]);
                                    ResultSet resultSetTemp = psTemp.executeQuery();
                                    if (resultSetTemp.next()) {
                                        System.out.println("ZAPIS VEC POSTOJI U BAZI" + uredjaj[4] + " " + uredjaj[7]);
                                        String sqlUnesiTemperaturu = "INSERT INTO temperature (id, temp, vrijeme_mjerenja, vrijeme_kreiranja) VALUES(?, ?, ?, ?)";
                                        PreparedStatement unesiTemperaturu = c.prepareStatement(sqlUnesiTemperaturu);
                                        unesiTemperaturu.setString(1, uredjaj[2]);
                                        unesiTemperaturu.setString(2, uredjaj[7].replace(";", ""));
                                        unesiTemperaturu.setString(3, uredjaj[4] + " " + uredjaj[5]);
                                        unesiTemperaturu.setString(4, uredjaj[4] + " " + uredjaj[5]);
                                        unesiTemperaturu.executeUpdate();
                                        brojMjerenihTemp++;
                                    } else {
                                        System.out.println("ZAPIS NE POSTOJI U BAZIIII");
                                        brojPogresaka++;
                                    }

                                } catch (SQLException ex) {
                                    Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            break;
                            case "EVENT": {
                                try {
                                    PreparedStatement psEvent = c.prepareStatement(traziNazivUTablici);
                                    psEvent.setString(1, uredjaj[2]);
                                    ResultSet resultSetEvent = psEvent.executeQuery();
                                    if (resultSetEvent.next()) {
                                        System.out.println("ZAPIS VEC POSTOJI U BAZI" + uredjaj[4] + " " + uredjaj[7]);
                                        String sqlUnesiEvent = "INSERT INTO dogadaji (id, vrsta, vrijeme_izvrsavanja, vrijeme_kreiranja) VALUES(?, ?, ?, ?)";
                                        PreparedStatement unesiEvent = c.prepareStatement(sqlUnesiEvent);
                                        unesiEvent.setString(1, uredjaj[2]);
                                        unesiEvent.setString(2, uredjaj[7].replace(";", ""));
                                        unesiEvent.setString(3, uredjaj[4] + " " + uredjaj[5]);
                                        unesiEvent.setString(4, uredjaj[4] + " " + uredjaj[5]);
                                        unesiEvent.executeUpdate();
                                        brojIzvrsenihEvent++;
                                    } else {
                                        System.out.println("ZAPIS NE POSTOJI U BAZIIII");
                                        brojPogresaka++;
                                    }

                                } catch (SQLException ex) {
                                    Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            break;
                        }
                    } else {
                        //System.out.println("OVO JE PREMJEŠTANJE PORUKE");
                        //folder.copyMessages(porukaZaPremjestanje, folderZaSpremanjeOstalihPoruka);
                        //messages[i].setFlag(Flag.DELETED, true);
                    }

                }
                folderZaSpremanjeIspravnihPoruka.close(true);
                folderZaSpremanjeOstalihPoruka.close(true);
                folder.close(true);
                store.close();
                System.out.println("OVO JE BROJ PORUKA: " + brojPoruka + " OVO JE BROJ POGREŠAKA: " + brojPogresaka + " IoT: " + brojDodanihIoT + " TEMP: " + brojMjerenihTemp + " EVENT: " + brojIzvrsenihEvent);

                String zavrsetakObradeZapis = dt.format(new Date());
                zavrsetakObrade = new Date().getTime();
                System.out.println("OVO JE TRAJANJE OBRADE U MILISEKUNDAMA: " + (zavrsetakObrade - pocetakObrade));
                System.out.println("OVO JE VRIJEME kraja: " + zavrsetakObradeZapis);
                redniBrojCiklusa++;
                System.out.println("Obrada prouka u cilkusu: " + redniBrojCiklusa);
                String statistikaPoruka = " Obrada započela u: " + pocetakObradeZapis
                        + " Obrada završila u: " + zavrsetakObradeZapis
                        + " Trajanje obrade u ms: " + (zavrsetakObrade - pocetakObrade)
                        + " Broj poruka: " + brojPoruka
                        + " Broj dodanih IOT: " + brojDodanihIoT
                        + " Broj mjerenih TEMP: " + brojMjerenihTemp
                        + " Broj izvršenih EVENT: " + brojIzvrsenihEvent
                        + " Broj pogrešaka: " + brojPogresaka;
                //PosaljiStatistiku(server, korisnik, saljiStatistiku, saljiStatistikuPredmet, statistikaPoruka);
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
