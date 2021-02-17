package za.co.entelect.challenge.command;

import za.co.entelect.challenge.entities.Worm;
public class SnowballCommand implements Command {

    private final int x;
    private final int y;

    public SnowballCommand(Worm opponentWorm) {
        this.x = opponentWorm.position.x;
        this.y = opponentWorm.position.y;
    }

    @Override
    public String render() {
        return String.format("snowball %d %d",x,y);
    }
}
