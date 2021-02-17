package za.co.entelect.challenge.command;

import  za.co.entelect.challenge.entities.Worm;
public class BananaCommand implements Command {

    private final int x;
    private final int y;

    public BananaCommand(Worm opponentWorm) {
        this.x = opponentWorm.position.x;
        this.y = opponentWorm.position.y;
    }

    @Override
    public String render() {
        return String.format("banana %d %d",x,y);
    }
}