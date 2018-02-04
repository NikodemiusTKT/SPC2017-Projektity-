package harjoitustyo;

public class GeoLocation {
    private int geoId;
    private double latitude;
    private double longitude;
    private String streetAddress;
    private String postNumber;
    private String city;


    /**
     * @param geoId
     * @param startLong
     * @param startLat
     * @param endLong
     * @param endLat
     * @param streetAddress
     * @param postNumber
     * @param city
     */
    public GeoLocation(int geoId,double latitude, double longitude, String streetAddress, String postNumber, String city) {
        this.geoId = geoId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.streetAddress = streetAddress;
        this.postNumber = postNumber;
        this.city = city;
    }

    public GeoLocation(GeoLocation clone) {
         this.geoId = clone.geoId;
         this.latitude = clone.latitude;
         this.longitude = clone.longitude;
         this.streetAddress = clone.streetAddress;
         this.postNumber = clone.postNumber;
         this.city  = clone.city;
    }
    public GeoLocation clone() {
        return new GeoLocation(this);
    }

    /**
     * @return the geoId
     */
    public int getGeoId() {
        return geoId;
    }

    /**
     * @param geoId the geoId to set
     */
    public void setGeoId(int geoId) {
        this.geoId = geoId;
    }

    /**
     * @return the latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * @param latitude the latitude to set
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * @return the longitude
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * @param longitude the longitude to set
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     * @return the streetAddress
     */
    public String getStreetAddress() {
        return streetAddress;
    }

    /**
     * @param streetAddress the streetAddress to set
     */
    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    /**
     * @return the postNumber
     */
    public String getPostNumber() {
        return postNumber;
    }

    /**
     * @param postNumber the postNumber to set
     */
    public void setPostNumber(String postNumber) {
        this.postNumber = postNumber;
    }

    /**
     * @return the city
     */
    public String getCity() {
        return city;
    }

    /**
     * @param city the city to set
     */
    public void setCity(String city) {
        this.city = city;
    }

}
