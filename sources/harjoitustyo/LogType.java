/**
 * tkt
 * Jul 28, 2017
 * LogType.java
 * 
 */
package harjoitustyo;

/**
 * @author tkt
 *
 */
public enum LogType {


    // REMOVE ALL SENT
    REMOVE_SENT(16,"Lähetykset","Käyttäjä poisti kaikki lähetetyt lähetykset %s ja paketit %s"),
    // ITEMS
    ITEM_ADD(1,"Esine","Käyttäjä loi uuden tavaran %s"),
    ITEM_REMOVE(2,"Esine","Käyttäjä poisti tavaran %s"),
    ITEM_MOD(3,"Esine","Käyttäjä muokkasi tavaraa %s"),


    //Packages
    PACKAGE_ADD(4,"Paketti","Käyttäjä loi uuden paketin %s"),
    PACKAGE_REMOVE(5,"Paketti","Käyttäjä poisti paketin %s"),
    PACKAGE_MOD(6,"Paketti","Käyttäjä muokkasi pakettia %s"),
    
    //Shipments
    SHIP_ADD(7,"Lähetys","Käyttäjä loi uuden lähetyksen (%d), johon kuuluivat paketit %s"),
    SHIP_REMOVE(8,"Lähetys","Käyttäjä poisti lähetyksen (%d), joka sisälsi paketit %s"),
    SHIP_MOD(9,"Lähetys","Käyttäjä muokkasi lähetystä (%d): %s"),
    SHIP_SEND(10,"Lähetys","Käyttäjä lähetti lähetyksen (%d), joka sisälsi paketit %s %s"),

    //Statistics
    STAT_DATE(11,"Statistiikka","Käyttäjä katsoi päivän %s lähetyksiä"),
    STAT_SHIP(12,"Statistiikka","Käyttäjä katsoi lähetyksen %d paketteja"),
    STAT_PACK(13,"Statistiikka","Käyttäjä katsoi paketin %d esineitä"),

    //Program
    PROG_START(14,"Ohjelma","Käyttäjä käynnisti ohjelman"),
    PROG_END(15,"Ohjelma","Käyttäjä sulki ohjelman");

    private final int logCode;
    private final String type;
    private String desc;

    /**
     * @param logCode
     * @param type
     * @param desc
     */
    LogType(int logCode, String type, String desc) {
        this.logCode = logCode;
        this.type = type;
        this.desc = desc;
    }


    /**
     * @return the logCode
     */
    public int getLogCode() {
        return logCode;
    }


    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @return the desc
     */
    public String getDesc() {
        return desc;
    }

    /**
     * @param desc the desc to set
     */
    public void setDesc(String desc) {
        this.desc = desc;
    }

}
