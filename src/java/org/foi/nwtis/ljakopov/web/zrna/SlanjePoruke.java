/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.ljakopov.web.zrna;

import static java.lang.ProcessBuilder.Redirect.to;
import java.util.Date;
import static java.util.Date.from;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import static javax.ws.rs.client.Entity.text;

/**
 *
 * @author ljakopov
 */
@Named(value = "slanjePoruke")
@RequestScoped
public class SlanjePoruke {

    private String posluzitelj = "127.0.0.1";
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
            Address fromAddress = new InternetAddress(this.salje);
            message.setFrom(fromAddress);

            // Parse and set the recipient addresses
            Address[] toAddresses = InternetAddress.parse(this.prima);
            message.setRecipients(Message.RecipientType.TO, toAddresses);

            // Set the subject and text
            message.setSentDate(new Date());
            message.setSubject(this.predmet);
            message.setText(this.sadrzaj);

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

        return "PoslanaPoruka";
    }

    public String pocetna() {
        return "Pocetna";
    }

    public String pregledPoruka() {
        return "PregledPoruka";
    }

}
