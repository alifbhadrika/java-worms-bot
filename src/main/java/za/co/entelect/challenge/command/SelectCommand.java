package za.co.entelect.challenge.command;

import za.co.entelect.challenge.entities.Worm;
public class SelectCommand implements Command {

    private final Worm myWorm;
    private final String command;

    public SelectCommand(Worm myWorm, Command command) {
        this.myWorm = myWorm;
        this.command = command.render();
    }

    @Override
    public String render() {
        return String.format("select %d;",myWorm.id) + command;
    }
}