package za.co.entelect.challenge.command;

public class Select implements Command {

    private final int id;
    private final String cmd;

    public Select(int id, Command cmd) {
        this.id = id;
        this.cmd = cmd.render();
    }

    @Override
    public String render() {
        return String.format("select %d;", id)+cmd;
    }
}
