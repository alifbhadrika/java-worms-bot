package za.co.entelect.challenge;

import za.co.entelect.challenge.command.*;
import za.co.entelect.challenge.entities.*;
import za.co.entelect.challenge.enums.CellType;
import za.co.entelect.challenge.enums.Direction;

import java.util.*;
import java.util.stream.Collectors;

public class Bot {

    private Random random;
    private GameState gameState;
    private Opponent opponent;
    private MyWorm currentWorm;

    public Bot(Random random, GameState gameState) {
        this.random = random;
        this.gameState = gameState;
        this.opponent = gameState.opponents[0];
        this.currentWorm = getCurrentWorm(gameState);
    }

    private MyWorm getCurrentWorm(GameState gameState) {
        return Arrays.stream(gameState.myPlayer.worms)
                .filter(myWorm -> myWorm.id == gameState.currentWormId)
                .findFirst()
                .get();
    }

    private Position getCupu(GameState gamestate){
        for(Worm cacingnya : gamestate.opponents[0].worms){
            if(cacingnya.id == 1 && (cacingnya.health>0&&cacingnya.health<=150)){
                return cacingnya.position;
            }
        }
        return null;
    
    
    }
    /*private Worm getCupu2(GameState gamestate){
        for(Worm cacingnya : gamestate.opponents[0].worms){
            if(cacingnya.id == 1 && (cacingnya.health>0&&cacingnya.health<=150)){
                return cacingnya;
            }
        }
        return null;
    }*/

    private Position toTarget(Position target){
        Direction arahkecupu = resolveDirection(currentWorm.position, target);
        Position baru = new Position();
        baru.x = currentWorm.position.x + arahkecupu.x;
        baru.y = currentWorm.position.y + arahkecupu.y;
        return baru;
    }

    private CellType celltype (int x, int y, GameState state) {
        Cell[][] map = state.map;
        return map[y][x].type;
    }

    private Position CacingLainSelainCupu(Opponent lawan){
        int cacing2jarak = euclideanDistance(currentWorm.position.x, currentWorm.position.y, lawan.worms[1].position.x, lawan.worms[1].position.y);
        //int cacing2health = lawan.worms[1].health;
        int cacing3jarak = euclideanDistance(currentWorm.position.x, currentWorm.position.y, lawan.worms[2].position.x, lawan.worms[2].position.y);
        //int cacing3health =  lawan.worms[2].health;

        Position cacing2 = lawan.worms[1].position;
        Position cacing3 = lawan.worms[2].position;

        if(cacing2jarak<cacing3jarak){
            return cacing2;
        } else {
            return cacing3;
        }
    }

    private Command gasKeCupu(){
        Position sicupu = getCupu(gameState);
        //Worm sicupu2 = getCupu2(gameState);
        //System.out.println("ada cupu= "+(sicupu2==null?"gk":sicupu2.health));
        //System.out.println("ada cupu= "+(getCupu2(gameState)==null?"gk":getCupu2(gameState).health));
        if (sicupu != null){
            Position target = toTarget(sicupu);
            CellType nextdir = celltype(target.x, target.y, gameState);

            Worm enemyWorm = getFirstWormInRange();
            int health = currentWorm.id == 1?150:100;
            
            if (enemyWorm != null) {
                if(enemyWorm.id!=1){
                    if(enemyWorm.health>0){
                        if(currentWorm.health>=(3*health/4)){
                            Direction direction = resolveDirection(currentWorm.position, enemyWorm.position);

                            /*if (idcacing != currentWorm.id){
                                return new Select(idcacing, (new ShootCommand(direction)));
                            }*/
                            return new ShootCommand(direction);
                            // atau attack pake strat lain
                        }
                    }
                } else {
                    // gebukin si commando
                    // atau pake strat lain
                    if(enemyWorm.health>0){
                        Direction direction = resolveDirection(currentWorm.position, enemyWorm.position);
                        /*if (idcacing != currentWorm.id){
                            return new Select(idcacing, (new ShootCommand(direction)));
                        }*/
                        return new ShootCommand(direction);
                    }
                }
            }

            if(nextdir == CellType.AIR){
                /*if (idcacing != currentWorm.id){
                    return new Select(idcacing, (new MoveCommand(target.x, target.y)));
                }*/
                return new MoveCommand(target.x, target.y);
            } else if(nextdir == CellType.DIRT){
                /*if (idcacing != currentWorm.id){
                    return new Select(idcacing, (new DigCommand(target.x, target.y)));
                }*/
                return new DigCommand(target.x, target.y);
            }

            return null;
        } else {
            // kalo cupu udah gaada
            Position cacinglain = CacingLainSelainCupu(opponent);
            Position target = toTarget(cacinglain);
            CellType nextdir = celltype(target.x, target.y, gameState);

            Worm enemyWorm = getFirstWormInRange();
            
            if (enemyWorm != null) {
                if(enemyWorm.health>0){
                        Direction direction = resolveDirection(currentWorm.position, enemyWorm.position);

                        /*if (idcacing != currentWorm.id){
                            return new Select(idcacing, (new ShootCommand(direction)));
                        }*/
                        return new ShootCommand(direction);
                }
            }

            if(nextdir == CellType.AIR){
                /*if (idcacing != currentWorm.id){
                    return new Select(idcacing, (new MoveCommand(target.x, target.y)));
                }*/
                return new MoveCommand(target.x, target.y);
            } else if(nextdir == CellType.DIRT){
                /*if (idcacing != currentWorm.id){
                    return new Select(idcacing, (new DigCommand(target.x, target.y)));
                }*/
                return new DigCommand(target.x, target.y);
            }
            return null;
        }
    }

    public Command run() {
        //Position A = getCupu(gameState);
        //System.out.println(A.x +" - "+ A.y);
        //Direction z = resolveDirection(currentWorm.position, A);
        //System.out.println(z.x+"-"+z.y);
        Command gas = gasKeCupu();

        if(gas!=null){
            return gas;
        }
        
        /*Worm enemyWorm = getFirstWormInRange();
        if (enemyWorm != null) {
            Direction direction = resolveDirection(currentWorm.position, enemyWorm.position);
            return new ShootCommand(direction);
        }

        List<Cell> surroundingBlocks = getSurroundingCells(currentWorm.position.x, currentWorm.position.y);
        int cellIdx = random.nextInt(surroundingBlocks.size());

        Cell block = surroundingBlocks.get(cellIdx);
        if (block.type == CellType.AIR) {
            return new MoveCommand(block.x, block.y);
        } else if (block.type == CellType.DIRT) {
            return new DigCommand(block.x, block.y);
        }*/

        return new DoNothingCommand();
    }

    private Worm getFirstWormInRange() {

        Set<String> cells = constructFireDirectionLines(currentWorm.weapon.range)
                .stream()
                .flatMap(Collection::stream)
                .map(cell -> String.format("%d_%d", cell.x, cell.y))
                .collect(Collectors.toSet());

        for (Worm enemyWorm : opponent.worms) {
            String enemyPosition = String.format("%d_%d", enemyWorm.position.x, enemyWorm.position.y);
            if (cells.contains(enemyPosition)) {
                return enemyWorm;
            }
        }

        return null;
    }

    private List<List<Cell>> constructFireDirectionLines(int range) {
        List<List<Cell>> directionLines = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            List<Cell> directionLine = new ArrayList<>();
            for (int directionMultiplier = 1; directionMultiplier <= range; directionMultiplier++) {

                int coordinateX = currentWorm.position.x + (directionMultiplier * direction.x);
                int coordinateY = currentWorm.position.y + (directionMultiplier * direction.y);

                if (!isValidCoordinate(coordinateX, coordinateY)) {
                    break;
                }

                if (euclideanDistance(currentWorm.position.x, currentWorm.position.y, coordinateX, coordinateY) > range) {
                    break;
                }

                Cell cell = gameState.map[coordinateY][coordinateX];
                if (cell.type != CellType.AIR) {
                    break;
                }

                directionLine.add(cell);
            }
            directionLines.add(directionLine);
        }

        return directionLines;
    }

    private List<Cell> getSurroundingCells(int x, int y) {
        ArrayList<Cell> cells = new ArrayList<>();
        for (int i = x - 1; i <= x + 1; i++) {
            for (int j = y - 1; j <= y + 1; j++) {
                // Don't include the current position
                if (i != x && j != y && isValidCoordinate(i, j)) {
                    cells.add(gameState.map[j][i]);
                }
            }
        }

        return cells;
    }

    private int euclideanDistance(int aX, int aY, int bX, int bY) {
        return (int) (Math.sqrt(Math.pow(aX - bX, 2) + Math.pow(aY - bY, 2)));
    }

    private boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < gameState.mapSize
                && y >= 0 && y < gameState.mapSize;
    }

    private Direction resolveDirection(Position a, Position b) {
        StringBuilder builder = new StringBuilder();

        int verticalComponent = b.y - a.y;
        int horizontalComponent = b.x - a.x;

        if (verticalComponent < 0) {
            builder.append('N');
        } else if (verticalComponent > 0) {
            builder.append('S');
        }

        if (horizontalComponent < 0) {
            builder.append('W');
        } else if (horizontalComponent > 0) {
            builder.append('E');
        }

        return Direction.valueOf(builder.toString());
    }
}
