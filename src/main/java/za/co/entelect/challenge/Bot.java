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
        if(lawan.worms[1].health<=0){
            return lawan.worms[2].position;
        } else if(lawan.worms[2].health<=0){
            return lawan.worms[1].position;
        }
        
        int cacing2jarak = euclideanDistance(currentWorm.position.x, currentWorm.position.y, lawan.worms[1].position.x, lawan.worms[1].position.y);
        int cacing2health = lawan.worms[1].health;
        int cacing3jarak = euclideanDistance(currentWorm.position.x, currentWorm.position.y, lawan.worms[2].position.x, lawan.worms[2].position.y);
        int cacing3health =  lawan.worms[2].health;

        Position cacing2 = lawan.worms[1].position;
        Position cacing3 = lawan.worms[2].position;

        if(cacing2jarak<cacing3jarak || cacing2health<cacing3health){
            return cacing2;
        } else {
            return cacing3;
        }
    }

    private Command gasKeCupu(){
        Position sicupu = getCupu(gameState);
        
        if (sicupu != null){
            Position target = toTarget(sicupu);
            CellType nextdir = celltype(target.x, target.y, gameState);

            Worm enemyWorm = getShootableOpponent(currentWorm);
            int health = currentWorm.id == 1?150:100;

            if (enemyWorm != null) {
                if(enemyWorm.id!=1){
                    if(enemyWorm.health>0){
                        if(currentWorm.health>=(3*health/4)){
                            return attack(currentWorm, enemyWorm);
                        }
                    }
                } else {
                    // gebukin si commando
                    // atau pake strat lain
                    if(enemyWorm.health>0){
                        return attack(currentWorm, enemyWorm);
                    }
                }
            }

            if(nextdir == CellType.AIR){
                return new MoveCommand(target.x, target.y);
            } else if(nextdir == CellType.DIRT){
                return new DigCommand(target.x, target.y);
            }

            return null;
        } else {
            // kalo cupu udah gaada
            Position cacinglain = CacingLainSelainCupu(opponent);
            Position target = toTarget(cacinglain);
            CellType nextdir = celltype(target.x, target.y, gameState);
            
            Worm enemyWorm = getShootableOpponent(currentWorm);

            if (enemyWorm != null) {
                if(enemyWorm.health>0){
                    return attack(currentWorm, enemyWorm);
                }
            }

            if(nextdir == CellType.AIR){
                return new MoveCommand(target.x, target.y);
            } else if(nextdir == CellType.DIRT){
                return new DigCommand(target.x, target.y);
            }
            return null;
        }
    }
    public Command run() {
        Command gas = gasKeCupu();

        if(gas!=null){
            return gas;
        }
        return new DoNothingCommand();
    }
    private Worm getShootableOpponent(MyWorm myworm){
        Set<String> cells = constructCellDirectionLines(myworm.weapon.range)
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

    private List<List<Cell>> constructCellDirectionLines(int range) {
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
    
    private Command attack(Worm myCurrentWorm, Worm nearTarget){
        if (canBananaBombThem(nearTarget, gameState)){
            return new BananaCommand(nearTarget);
        }
        else if (canSnowballThem(nearTarget, gameState)){
            return new SnowballCommand(nearTarget);
        }
        else{
            Direction direction = resolveDirection(myCurrentWorm.position, nearTarget.position);
            return new ShootCommand(direction);
        }
    }

    private boolean canBananaBombThem(Worm target, GameState gameState) {
        return  isWormStunned(target, gameState) && currentWorm.id == 2
                && gameState.myPlayer.worms[1].bananaBombs.count > 0
                && euclideanDistance(currentWorm.position.x, currentWorm.position.y, target.position.x, target.position.y) <= gameState.myPlayer.worms[1].bananaBombs.range
                && !isBananaBombtoAlly(target, gameState)
                ;
    }

    private boolean canSnowballThem(Worm target, GameState gameState) {
        return !isWormStunned(target, gameState) && currentWorm.id == 3
                && gameState.myPlayer.worms[2].snowballs.count > 0
                && euclideanDistance(currentWorm.position.x, currentWorm.position.y, target.position.x, target.position.y)<= gameState.myPlayer.worms[2].snowballs.range
                && !isSnowballtoAlly(target,gameState)
                ;
    }

    private boolean isWormStunned (Worm target, GameState gameState){
        for(Worm cacingnya : gameState.opponents[0].worms){
            if(cacingnya.id == target.id) {
                return cacingnya.roundsUntilUnfrozen > 0;
            }
        }
        return false;
    }

    private boolean isBananaBombtoAlly (Worm enemyWorm,GameState gameState){
        int x, y;
        int r = gameState.myPlayer.worms[1].bananaBombs.damageRadius;
        for (x=-r; x<=r;x++){
            for(y=-r; y<=r; y++){
                for (Worm worm : gameState.myPlayer.worms){
                    if(enemyWorm.position.x + x == worm.position.x && enemyWorm.position.y + y == worm.position.y){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isSnowballtoAlly (Worm enemyWorm,GameState gameState){
        int x, y;
        int r = gameState.myPlayer.worms[2].snowballs.freezeRadius;
        for (x=-r; x<=r;x++){
            for(y=-r; y<=r; y++){
                for (Worm worm : gameState.myPlayer.worms){
                    if(enemyWorm.position.x + x == worm.position.x && enemyWorm.position.y + y == worm.position.y){
                        return true;
                    }
                }
            }
        }
        return false;
    }
}


