package main;


import config.Configuration;
import eu.nites.esb.iec.common.message.base.*;
import eu.nites.esb.iec.common.message.common.ReadingTypeRef;
import eu.nites.esb.iec.common.message.meterreadings.MeterAsset;
import eu.nites.esb.iec.common.message.meterreadings.MeterReading;
import eu.nites.esb.iec.common.message.meterreadings.MeterReadings;
import eu.nites.esb.iec.common.message.meterreadings.Readings;
import eu.nites.esb.iec.common.message.reply.ReplyMessage;
import external.ws.SendMeterReadings_Service;
import jdk.internal.org.xml.sax.SAXException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

public class Transformer {

    private static final Log LOG = LogFactory.getLog(Transformer.class);

    public static ReplyMessage sendIecToServer(IecMessage message, String filePath) {

        URL url = null;
        try {
            url = new URL(Configuration.eps_base_url + Configuration.eps_amm);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        if (url != null) {
            return new SendMeterReadings_Service(url).getSendMeterReadingsPort().createMeterReadings(message);
        } else {
            LOG.fatal("Message not sent. URL is invalid. File: " + filePath + " was not sent.");
            return null;
        }
    }

    public static void setIecHeader(IecMessage iecMessage, String filePath) {

        iecMessage.setHeader(new Header());
        iecMessage.getHeader().setCorrelationID(UUID.randomUUID().toString());
        iecMessage.getHeader().setSource(SourceEnum.COMET);
        iecMessage.getHeader().setVerb(VerbEnum.CREATE);
        iecMessage.getHeader().setNoun(NounEnum.METER_READINGS);

        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTime(new Date());

        try {
            iecMessage.getHeader()
                    .setTimestamp(DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar));
        } catch (DatatypeConfigurationException e) {
            LOG.fatal("ERROR IN THE CALENDAR CONVERSION! Header date parsing EXCEPTION happened for file : " + filePath);
        }

    }

    public static void convertIec(IecMessage iecMessage, String line, String filePath) {

        String manuf = line.substring(0, 6);
        String specialManuf = manuf.substring(0, 3);
        String serial = line.substring(6, 15);

        switch (specialManuf) {
            case "SIT":
                manuf = manuf.substring(0, 6);
                break;
            case "MAC":
                manuf = manuf.substring(0, 6);
                break;
            case "ENL":
                if (!manuf.contains("ENLDB2") && !manuf.contains("ENLDMG")) {
                    manuf = manuf.substring(0, 3);
                    serial = line.substring(7, 15);
                } else {
                    manuf = manuf.substring(0, 6);
                }
                break;
            case "SGM":
                manuf = manuf.substring(0, 3);
                serial = line.substring(7, 15);
                break;
        }

        String date = line.substring(15, 34);
        XMLGregorianCalendar xmlDate = null;

        try {
            xmlDate = stringToXmlGregorianCalendar(date.trim(), date.charAt(13) == '.');
        } catch (Exception e) {
            LOG.fatal("EXCEPTION WHEN PARSING DATE! DATE WAS: " + date + " ERROR: " + e + System.lineSeparator() + " Date parsing EXCEPTION happened for file: " + filePath);
        }

        String vtActiveEnergy = line.substring(34, 44);
        String mtActiveEnergy = line.substring(44, 54);
        String vtActivePower = line.substring(54, 61);
        String mtActivePower = line.substring(61, 68);
        String vtReactiveEnergy = line.substring(68, 78);
        String mtReactiveEnergy = line.substring(78, 88);

        MeterReading mr = new MeterReading();
        mr.setReadings(new ArrayList<>());
        MeterAsset ma = new MeterAsset();
        ma.setMrid(serial + "-" + manuf);

        Readings rActiveEnerg1 = new Readings();
        ReadingTypeRef rtrActiveEnerg1 = new ReadingTypeRef();
        rtrActiveEnerg1.setMrid("ActiveEnergyPlus_CUM_T1_Billing");
        rActiveEnerg1.setTimeStamp(xmlDate);
        rActiveEnerg1.setReadingType(rtrActiveEnerg1);
        rActiveEnerg1.setValue(vtActiveEnergy);
        mr.getReadings().add(rActiveEnerg1);

        Readings rActiveEnerg2 = new Readings();
        ReadingTypeRef rtrActiveEnerg2 = new ReadingTypeRef();
        rtrActiveEnerg2.setMrid("ActiveEnergyPlus_CUM_T2_Billing");
        rActiveEnerg2.setTimeStamp(xmlDate);
        rActiveEnerg2.setReadingType(rtrActiveEnerg2);
        rActiveEnerg2.setValue(mtActiveEnergy);
        mr.getReadings().add(rActiveEnerg2);

        Readings rActivePower1 = new Readings();
        ReadingTypeRef rtrActivePow1 = new ReadingTypeRef();
        rtrActivePow1.setMrid("ActivePowerPlus_MAX_15min_T1_Billing");
        rActivePower1.setTimeStamp(xmlDate);
        rActivePower1.setReadingType(rtrActivePow1);
        rActivePower1.setValue(vtActivePower);
        mr.getReadings().add(rActivePower1);

        Readings rActivePower2 = new Readings();
        ReadingTypeRef rtrActivePow2 = new ReadingTypeRef();
        rtrActivePow2.setMrid("ActivePowerPlus_MAX_15min_T2_Billing");
        rActivePower2.setTimeStamp(xmlDate);
        rActivePower2.setReadingType(rtrActivePow2);
        rActivePower2.setValue(mtActivePower);
        mr.getReadings().add(rActivePower2);

        Readings rReActiveEnerg1 = new Readings();
        ReadingTypeRef rtrReActiveEnerg1 = new ReadingTypeRef();
        rtrReActiveEnerg1.setMrid("ReactiveEnergyPlus_CUM_T1_Billing");
        rReActiveEnerg1.setTimeStamp(xmlDate);
        rReActiveEnerg1.setReadingType(rtrReActiveEnerg1);
        rReActiveEnerg1.setValue(vtReactiveEnergy);
        mr.getReadings().add(rReActiveEnerg1);

        Readings rReActiveEnerg2 = new Readings();
        ReadingTypeRef rtrReActiveEnerg2 = new ReadingTypeRef();
        rtrReActiveEnerg2.setMrid("ReactiveEnergyPlus_CUM_T2_Billing");
        rReActiveEnerg2.setTimeStamp(xmlDate);
        rReActiveEnerg2.setReadingType(rtrReActiveEnerg2);
        rReActiveEnerg2.setValue(mtReactiveEnergy);
        mr.getReadings().add(rReActiveEnerg2);

        mr.setMeterAsset(ma);
        ((MeterReadings) iecMessage.getPayload().getBasePayload()).getMeterReading().add(mr);
    }

    private static XMLGregorianCalendar stringToXmlGregorianCalendar(String value, boolean dots) throws SAXException {

        try {
            SimpleDateFormat simpleDateFormat;
            if (dots) {
                simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH.mm.ss");
            } else {
                simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            }

            GregorianCalendar gregorianCalendar = (GregorianCalendar) GregorianCalendar.getInstance();
            gregorianCalendar.setTime(simpleDateFormat.parse(value));

            return DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
        } catch (Exception e) {
            throw new SAXException("Parsing error!");
        }
    }

}
