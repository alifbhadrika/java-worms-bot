package za.co.entelect.challenge.command;

import package za.co.entelect.challenge.entities.Worm;
public class SelectCommand implements Command {

    private final int myWormId;
    private final String command;

    public SelectCommand(Worm myWormId, Command command) {
        this.myWorm = myWormId;
        this.command = command.render();
    }

    @Override
    public String render() {
        return String.format("select %d;",myWormId) + command;
    }
}