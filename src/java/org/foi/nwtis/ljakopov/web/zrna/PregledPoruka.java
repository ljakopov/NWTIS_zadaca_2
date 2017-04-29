/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.ljakopov.web.zrna;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.servlet.ServletContext;
import org.foi.nwtis.ljakopov.konfiguracije.Konfiguracija;
import org.foi.nwtis.ljakopov.web.kontrole.Izbornik;
import org.foi.nwtis.ljakopov.web.kontrole.Poruka;

/**
 *
 * @author Lovro
 */
@Named(value = "pregledPoruka")
@RequestScoped
public class PregledPoruka {

    private String posluzitelj;
    private String korisnik;
    private String lozinka;
    private ServletContext sc = null;

    private ArrayList<Izbornik> mape = new ArrayList<>();
    private String odabranaMapa = "INBOX";
    private ArrayList<Poruka> poruke = new ArrayList<>();
    private int ukupupnoPorukaMapa = 0;
    int brojPrikazanihPoruka = 0;
    int pozicijaOdPoruke = 0;
    int pozicijaDoPoruke = 0;
    private String traziPoruke;
    Folder folder = null;

    /**
     * Creates a new instance of PregledPoruka
     */
    public PregledPoruka() {
        preuzmiMape();
        preuzmiPoruke();
    }

    void preuzmiMape() {
        java.util.Properties properties = System.getProperties();
        properties.put("mail.smtp.host", "127.0.0.1");
        Session session = Session.getInstance(properties, null);
        Store store;

        try {
            store = session.getStore("imap");
            store.connect("127.0.0.1", "ljakopov@nwtis.nastava.foi.hr", "password");
            Folder[] f = store.getDefaultFolder().list();
            for (Folder fd : f) {
                mape.add(new Izbornik(fd.getName(), Integer.toString(fd.getMessageCount())));
            }

        } catch (NoSuchProviderException ex) {
            Logger.getLogger(PregledPoruka.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MessagingException ex) {
            Logger.getLogger(PregledPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    void preuzmiPoruke() {
        poruke.clear();
        folder = null;
        /*
        Konfiguracija konf = (Konfiguracija) sc.getAttribute("Mail_Konfig");
        String server = konf.dajPostavku("mail.server");
        System.out.println("OVO JE SERVER: " + server);
        String port = konf.dajPostavku("mail.port");
        String korisnik = konf.dajPostavku("mail.usernameThread");
        String lozinka = konf.dajPostavku("mail.passwordThread");
         */

        java.util.Properties properties = System.getProperties();
        properties.put("mail.smtp.host", "127.0.0.1");
        Session session = Session.getInstance(properties, null);
        System.out.println("OVO JE ISPIS ZA PROMJENU MAPE: " + this.odabranaMapa);

        Store store;
        try {
            store = session.getStore("imap");
            store.connect("127.0.0.1", "ljakopov@nwtis.nastava.foi.hr", "password");
            folder = store.getFolder(this.odabranaMapa);
            folder.open(Folder.READ_WRITE);
            Message[] messages = folder.getMessages();
            System.out.println("OVO JE TXT TRAZI: " + this.traziPoruke);
            for (int i = messages.length; i > 0; i--) {
                try {
                    if (this.traziPoruke != null && !"".equals(this.traziPoruke)) {
                        System.out.println("OVO JE PRETRAGA PORUKA");
                        if (messages[i - 1].getContent().toString().contains(this.traziPoruke) == true) {
                            poruke.add(new Poruka("0", messages[i - 1].getSentDate(), messages[i - 1].getReceivedDate(), Arrays.toString(messages[i - 1].getFrom()), messages[i - 1].getSubject(), messages[i - 1].getContent().toString(), "0"));
                        }
                    }
                    else{
                        System.out.println("OVO JE SAMO ISPIS");
                        poruke.add(new Poruka("0", messages[i - 1].getSentDate(), messages[i - 1].getReceivedDate(), Arrays.toString(messages[i - 1].getFrom()), messages[i - 1].getSubject(), messages[i - 1].getContent().toString(), "0"));
                    }
                } catch (IOException ex) {
                    Logger.getLogger(PregledPoruka.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (NoSuchProviderException ex) {
            Logger.getLogger(PregledPoruka.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MessagingException ex) {
            Logger.getLogger(PregledPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }
        ukupupnoPorukaMapa = poruke.size();

        //TODO promjeni sa stvarnim preuzimanjem poruka
        //TODO razmisli o optimiranju preuzimanja poruka
    }

    public String promjenaMape() {
        this.preuzmiPoruke();
        return "PromjenaMape";
    }

    public String traziPoruke() {
        this.preuzmiPoruke();
        return "FiltrirajPoruke";
    }

    public String prethodnePoruke() {
        this.preuzmiPoruke();
        return "PrethodnePoruke";
    }

    public String sljedecePoruke() {
        this.preuzmiPoruke();
        return "SljedecePoruke";
    }

    public String promjenaJezika() {
        return "PromjenaJezika";
    }

    public String saljiPoruku() {
        return "SaljiPoruku";
    }

    public String getOdabranaMapa() {
        return odabranaMapa;
    }

    public void setOdabranaMapa(String odabranaMapa) {
        this.odabranaMapa = odabranaMapa;
    }

    public int getUkupupnoPorukaMapa() {
        return ukupupnoPorukaMapa;
    }

    public void setUkupupnoPorukaMapa(int ukupupnoPorukaMapa) {
        this.ukupupnoPorukaMapa = ukupupnoPorukaMapa;
    }

    public String getTraziPoruke() {
        return traziPoruke;
    }

    public void setTraziPoruke(String traziPoruke) {
        this.traziPoruke = traziPoruke;
    }

    public ArrayList<Izbornik> getMape() {
        return mape;
    }

    public ArrayList<Poruka> getPoruke() {
        return poruke;
    }

    public void setSc(ServletContext sc) {
        this.sc = sc;
    }

}
