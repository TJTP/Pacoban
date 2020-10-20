package tracks.multiPlayer.myController.heuristicSearch;

public enum Itype {
    PELLET(5), FRUIT(4), POWER(6), BLOCK(0),
    HUNGARY0(28), HUNGARY1(31),
    ENERGETIC0(29), ENERGETIC1(32),
    GHOST0OK(15), GHOST0SC(16),
    GHOST1OK(18), GHOST1SC(19),
    GHOST2OK(21), GHOST2SC(22),
    GHOST3OK(24), GHOST3SC(25);

    private int val = 0;

    Itype(int value) {
        this.val = value;
    }
    public int getValue() {
        return val;
    }
}
