package harjoitustyo;

public class Item implements Cloneable {

    private int itemNumber;
    private static int lastNumber = 0;
    private String itemName;
    private double Weight;
    private double Width;
    private double Height;
    private double Lenght;
    private boolean fragile;
    private boolean broken = false;
    private String dimensions;
    private double volume;

    public Item() {
    }

    /**
     * @param itemNumber
     * @param itemName
     * @param weight
     * @param width
     * @param height
     * @param lenght
     * @param fragile
     */
    public Item(int itemNumber, String itemName, double weight, double width, double height, double lenght, boolean fragile) {
        this.itemNumber = itemNumber;
        Item.lastNumber = this.itemNumber;
        this.itemName = itemName;
        Weight = weight;
        Width = width;
        Height = height;
        Lenght = lenght;
        this.fragile = fragile;
    }

    /**
     * @param weight
     * @param width
     * @param height
     * @param lenght
     * @param fragile
     */
    public Item(String itemName, double weight, double width, double height, double lenght, boolean fragile) {
        this.itemName = itemName;
        Weight = weight;
        Width = width;
        Height = height;
        Lenght = lenght;
        this.fragile = fragile;
        // Increment itemNumber
        this.itemNumber = ++Item.lastNumber;
    }
    public Item clone() {
        return new Item(this);
    }

    public Item(Item copy) {
        this.itemNumber = copy.itemNumber;
        this.itemName = copy.itemName;
        this.Weight = copy.Weight;
        this.Width = copy.Width;
        this.Height = copy.Height;
        this.Lenght = copy.Lenght;
        this.fragile = copy.fragile;
        this.dimensions = copy.dimensions;
        this.volume = copy.volume;

    }
    @Override
    public String toString() {
        return String.format("%s", this.itemName);
    }

    /**
     * @return the itemNumber
     */
    public int getItemNumber() {
        return itemNumber;
    }

    /**
     * @param itemNumber the itemNumber to set
     */
    public void setItemNumber(int itemNumber) {
        this.itemNumber = itemNumber;
    }

    /**
     * @return the itemName
     */
    public String getItemName() {
        return itemName;
    }

    /**
     * @param itemName the itemName to set
     */
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    /**
     * @return the weight
     */
    public double getWeight() {
        return Weight;
    }

    /**
     * @param weight the weight to set
     */
    public void setWeight(double weight) {
        Weight = weight;
    }

    /**
     * @return the width
     */
    public double getWidth() {
        return Width;
    }

    /**
     * @param width the width to set
     */
    public void setWidth(double width) {
        Width = width;
    }

    /**
     * @return the height
     */
    public double getHeight() {
        return Height;
    }

    /**
     * @param height the height to set
     */
    public void setHeight(double height) {
        Height = height;
    }

    /**
     * @return the lenght
     */
    public double getLenght() {
        return Lenght;
    }

    /**
     * @param lenght the lenght to set
     */
    public void setLenght(double lenght) {
        Lenght = lenght;
    }

    /**
     * @return the fragile
     */
    public boolean isFragile() {
        return fragile;
    }

    /**
     * @param fragile the fragile to set
     */
    public void setFragile(boolean fragile) {
        this.fragile = fragile;
    }

    /**
     * @return the broken
     */
    public boolean isBroken() {
        return broken;
    }

    /**
     * @param broken the broken to set
     */
    public void setBroken(boolean broken) {
        this.broken = broken;
    }
    public String getDimensions() {
        this.dimensions = String.format("%sx%sx%s", this.Lenght, this.Width, this.Height);
        return dimensions;
    }
    public double getVolume() {
        this.volume = this.Height*this.Width*this.Lenght;
        return volume;
    }
}
