public class Seat {
    private int rowIndex;
    private int siteIndex;
    private String block;

    // Constructor
    public Seat(int rowIndex, int siteIndex, String block) {
        this.rowIndex = rowIndex;
        this.siteIndex = siteIndex;
        this.block = block;
    }

    // Getter para rowIndex
    public int getRowIndex() {
        return rowIndex;
    }

    // Setter para rowIndex (opcional, si necesitas cambiar el valor)
    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    // Getter para siteIndex
    public int getSiteIndex() {
        return siteIndex;
    }

    // Setter para siteIndex (opcional)
    public void setSiteIndex(int siteIndex) {
        this.siteIndex = siteIndex;
    }

    // Getter para block
    public String getBlock() {
        return block;
    }

    // Setter para block (opcional)
    public void setBlock(String block) {
        this.block = block;
    }

    @Override
    public String toString() {
        return "Seat{" +
                "rowIndex=" + rowIndex +
                ", siteIndex=" + siteIndex +
                ", block='" + block + '\'' +
                '}';
    }
}
