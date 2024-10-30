package OBJECTS;

public class Site {
    private int row;
    private int seat;
    private char state; // F: free - R: reserved - O: occupied S:Selected
    private String image;

    public Site(int row, int seat, char state) {
        this.row = row;
        this.seat = seat;
        this.state = state;
        this.setImage(state);
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return seat;
    }

    public void setColumn(int column) {
        this.seat = column;
    }

    public char getState() {
        return state;
    }

    private void setImage(char state){
        switch (state) {
            case 'F':
                this.image = "/resources/seat-green.png";
                break;
            case 'O':
                this.image = "/resources/seat-red.png";
                break;
            case 'R':
                this.image = "/resources/seat-blue.png";
                break;
            case 'S':
                this.image = "/resources/seat-purple.png";
                break;
            default:
                break;
        }
    }

    public void setState(char state) {
        this.state = state;
        this.setImage(state);
    }

    public String getImage() {
        return image;
    }

}
