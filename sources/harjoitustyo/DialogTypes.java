package harjoitustyo;

import java.util.ArrayList;

public enum DialogTypes {
    //Titles
    NEWITEMERROR("Tavaran syöttövirhe"),
    MODITEMERROR("Tavaran muokkausvirhe"),
    NEWPACKAGERROR("Paketin syöttövirhe"),
    MODPACKAGERROR("Paketin muokkausvirhe"),
    DELPACKAGE_ERROR("Paketin poistovirhe"),
    ITEM_WARNING("Varoitus tavaran lisäyksessä pakettiin"),
    SHIPSENT_ERROR("Lähetyksen lähetysvirhe"),
    SHIPMOD_ERROR("Lähetyksen muokkausvirhe"),
    // DESCRIPTIONS
    EMPTYFIELD("Seuraava kenttä oli tyhjä: ", new ArrayList<String> ()),
    EMPTYFIELDS("Seuraavat kentät olivat tyhjiä: ", new ArrayList<String> ()),
    DPARSE_ERRORS("Seuraavat kentät eivät olleet desimaalilukuja:", new ArrayList<String>()),
    DPARSE_ERROR("Seuraava kenttä ei ollut desimaaliluku: ", new ArrayList<String> ()),
    // ERRORS FOR PACKAGES
    PATH_ERROR("Et ole valinnut paketin lähtöpaikkaa etkä määränpäätä!\n"),
    STARTPOINT_ERROR("Et ole valinnut paketin paketin lähtöpistettä!\n"),
    ENDPOINT_ERROR("Et ole valinnut paketin päämäärää!\n"),
    SAMEPOINT_ERROR("Paketilla ei voi olla samaa alku- ja päätepistettä!\n"),
    DISTANCE_ERROR("Etäisyys päätepisteiden välillä on liian suuri nykyisellä pakettiluokalla\n"),
    ITEMS_ERROR("Paketissa ei ole lisättyjä esineitä!\n"),
    MODSENTPACK_ERROR("Et voi muokata jo lähetettyä pakettia!\n"),
    NOMODPACK_ERROR("Et ole valinnut muokattavaa pakettia!\n"),

    //ERRORS FOR SHIPMENTS
    NOSHIPMOD_ERROR("Et ole valinnut muokattavaa lähetystä!\n"),
    NOSHIPSENT_ERROR("Et ole valinnut lähettettävää lähetystä!\n"),
    SENTSHIPMOD_ERROR("Et voi muokata jo lähettettyä lähetystä!\n"),
    SENTSHIPSEND_ERROR("Et voi lähettää jo lähettettyä lähetystä!\n"),

    // ITEM CLASS LIMIT ERRORS
    TOOBIG_ERROR("Valitun tavaran koko on liian suuri nykyiseen pakettiin.\n"),
    TOOHEAVY_ERROR("Valitun tavaran paino on liian suuri nykyiseen pakettiin.\n"),
    BIGHEAVY_ERROR("Valitun tavaran koko and ja paino ovat liian suuria nykyisen paketin luokkarajoitteisiin.\n"),
    FRAG_CONFIRM("Oletko varma, että haluat lisätä särkyvää tavaraa särkevään pakettiin\n");

    private final String desc;
    private ArrayList<String> fields;

    private DialogTypes(String desc,ArrayList<String> fields) {
        this.desc = desc;
        this.fields = fields;
    }
    private DialogTypes(String desc) {
        this.desc = desc;
    }


    /**
     * @return the desc
     */
    public String getDesc() {
        return desc;
    }


    /**
     * @return the fields
     */
    public ArrayList<String> getFields() {
        return fields;
    }
    /**
     * @param fields the fields to set
     */
    public void setFields(ArrayList<String> fields) {
        this.fields = fields;
    }

    @Override
    public String toString() {
        return desc;
    }

    public String getEmptyFields() {
        return desc + String.join(", ",fields)+"\n";
    }

}
