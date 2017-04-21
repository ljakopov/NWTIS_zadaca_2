/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.ljakopov.web.zrna;

import java.util.ArrayList;
import java.util.Date;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
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

    private ArrayList<Izbornik> mape = new ArrayList<>();
    private String odabranaMapa;
    private ArrayList<Poruka> poruke = new ArrayList<>();
    private int ukupupnoPorukaMapa = 0;
    int brojPrikazanihPoruka = 0;
    int pozicijaOdPoruke = 0;
    int pozicijaDoPoruke = 0;
    private String traziPoruke;

    /**
     * Creates a new instance of PregledPoruka
     */
    public PregledPoruka() {
        preuzmiMape();
        preuzmiPoruke();
    }

    void preuzmiMape() {
        //TODO promjeni sa stvarnim preuzimanjem mapa
        mape.add(new Izbornik("INBOX", "INBOX"));
        mape.add(new Izbornik("NWTiS_poruke", "NWTiS_poruke"));
        mape.add(new Izbornik("NWTiS_ostale", "NWTiS_ostale"));
        mape.add(new Izbornik("Sent", "Sent"));
        mape.add(new Izbornik("Spam", "Spam"));
    }

    void preuzmiPoruke() {
        poruke.clear();
        //TODO promjeni sa stvarnim preuzimanjem poruka
        //TODO razmisli o optimiranju preuzimanja poruka
        int i = 0;
        poruke.add(new Poruka(Integer.toString(i++), new Date(), new Date(), "pero@localhost", "P " + Integer.toString(i), "Poruka " + Integer.toString(i), "0"));
        poruke.add(new Poruka(Integer.toString(i++), new Date(), new Date(), "pero@localhost", "P " + Integer.toString(i), "Poruka " + Integer.toString(i), "0"));
        poruke.add(new Poruka(Integer.toString(i++), new Date(), new Date(), "pero@localhost", "P " + Integer.toString(i), "Poruka " + Integer.toString(i), "0"));
        poruke.add(new Poruka(Integer.toString(i++), new Date(), new Date(), "pero@localhost", "P " + Integer.toString(i), "Poruka " + Integer.toString(i), "0"));
        poruke.add(new Poruka(Integer.toString(i++), new Date(), new Date(), "pero@localhost", "P " + Integer.toString(i), "Poruka " + Integer.toString(i), "0"));
        poruke.add(new Poruka(Integer.toString(i++), new Date(), new Date(), "pero@localhost", "P " + Integer.toString(i), "Poruka " + Integer.toString(i), "0"));
        poruke.add(new Poruka(Integer.toString(i++), new Date(), new Date(), "pero@localhost", "P " + Integer.toString(i), "Poruka " + Integer.toString(i), "0"));
        poruke.add(new Poruka(Integer.toString(i++), new Date(), new Date(), "pero@localhost", "P " + Integer.toString(i), "Poruka " + Integer.toString(i), "0"));
        poruke.add(new Poruka(Integer.toString(i++), new Date(), new Date(), "pero@localhost", "P " + Integer.toString(i), "Poruka " + Integer.toString(i), "0"));
        poruke.add(new Poruka(Integer.toString(i++), new Date(), new Date(), "pero@localhost", "P " + Integer.toString(i), "Poruka " + Integer.toString(i), "0"));
        poruke.add(new Poruka(Integer.toString(i++), new Date(), new Date(), "pero@localhost", "P " + Integer.toString(i), "Poruka " + Integer.toString(i), "0"));
        poruke.add(new Poruka(Integer.toString(i++), new Date(), new Date(), "pero@localhost", "P " + Integer.toString(i), "Poruka " + Integer.toString(i), "0"));

        ukupupnoPorukaMapa = poruke.size();
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

}
