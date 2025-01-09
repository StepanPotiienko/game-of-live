class Cell {
    private boolean state;
    private final int x;
    private final int y;

    public Cell(boolean state, int x, int y) {
        this.state = state;
        this.x = x;
        this.y = y;
    }

    public void changeState(boolean newState) {
        this.state = newState;
    }

    public boolean getState() {
        return this.state;
    }

    int[] getPosition() {
        return new int[]{this.x, this.y};
    }
}
