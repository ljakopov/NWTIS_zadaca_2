/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.ljakopov.web.zrna;

import javax.inject.Named;
import javax.enterprise.context.RequestScoped;

/**
 *
 * @author Lovro
 */
@Named(value = "slanjePoruke")
@RequestScoped
public class SlanjePoruke {

    private String posluzitelj;
    private String salje;
    private String prima;
    private String predmet;
    private String sadrzaj;

    /**
     * Creates a new instance of SlanjePoruke
     */
    public SlanjePoruke() {
    }

    public String getSalje() {
        return salje;
    }

    public void setSalje(String salje) {
        this.salje = salje;
    }

    public String getPrima() {
        return prima;
    }

    public void setPrima(String prima) {
        this.prima = prima;
    }

    public String getPredmet() {
        return predmet;
    }

    public void setPredmet(String predmet) {
        this.predmet = predmet;
    }

    public String getSadrzaj() {
        return sadrzaj;
    }

    public void setSadrzaj(String sadrzaj) {
        this.sadrzaj = sadrzaj;
    }

    public String saljiPoruku() {
        //TODO dodaj za slanje poruke prema primjeru s predavanja koji je priložen uz zadaću
        this.sadrzaj = "";
        this.predmet = "";
        this.prima = "";
        return "PoslanaPoruka";
    }

    public String pocetna() {
        return "Pocetna";
    }

    public String pregledPoruka() {
        return "PregledPoruka";
    }

}
