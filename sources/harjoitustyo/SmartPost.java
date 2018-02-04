package harjoitustyo;

public final class SmartPost {
    private int OfficeId;
    private String officeName;
    private String availibility;
    private GeoLocation location;

    public SmartPost(){};

    /**
     * @param officeId
     * @param officeName
     * @param location
     */
    public SmartPost(int officeId, String officeName, String availibility, GeoLocation location) {
        OfficeId = officeId;
        this.officeName = officeName;
        this.availibility = availibility;
        this.location = location;
    }

    @Override
    public String toString() {
        return this.officeName;
    }


    /**
     * @return the officeId
     */
    public int getOfficeId() {
        return OfficeId;
    }

    /**
     * @return the officeName
     */
    public String getOfficeName() {
        return officeName;
    }


    /**
     * @return the availibility
     */
    public String getAvailibility() {
        return availibility;
    }

    /**
     * @return the location
     */
    public GeoLocation getLocation() {
        return location;
    }


}

