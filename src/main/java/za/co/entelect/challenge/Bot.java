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

    public Command run() {
        if (myPlayer.worms.length() > 0){
            MyWorm[] myOtherWorms = myPlayer.worms;
            myOtherWorms = Arrays.stream(myOtherWorms).filter(myWorm -> myWorm.id != currentWorm.id).toArray();
            for (Worm myWorm : myOtherWorms) {
                Worm enemyWorm = getShootableOpponent(myWorm);
                if (enemyWorm != null) {
                    Direction direction = resolveDirection(myWorm.position, enemyWorm.position);
                    return new SelectCommand(myWorm.id, new attack(myWorm, enemyWorm));
                }
            }
        }
        Worm enemyWorm = getShootableOpponent(currentWorm);
        if (enemyWorm != null) {
            Direction direction = resolveDirection(currentWorm.position, enemyWorm.position);
            return new attack(currentWorm, enemyWorm);
        }

        List<Cell> surroundingBlocks = getSurroundingCells(currentWorm.position.x, currentWorm.position.y);
        int cellIdx = random.nextInt(surroundingBlocks.size());

        Cell block = surroundingBlocks.get(cellIdx);
        if (block.type == CellType.AIR) {
            return new MoveCommand(block.x, block.y);
        } else if (block.type == CellType.DIRT) {
            return new DigCommand(block.x, block.y);
        }

        return new DoNothingCommand();
    }

    private Worm getShootableOpponent(Worm myworm){
        Set<String> cells = constructFireDirectionLines(myworm.weapon.range)
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

        if (verticalComponent > 0) {
            builder.append('N');
        } else if (verticalComponent < 0) {
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

private Command attack(Worm myCurrentWorm, Worm nearTarget){
    // Prekondisi: nearTarget != null
    int x = nearTarget.position.x;
    int y = nearTarget.position.y;
    if (canSnowballThem(nearTarget, gameState)){
        return new SnowballCommand(x,y);
    }
    else if (canBananaBombThem(nearTarget, gameState)){
        return new BananaCommand(x,y);
    }
    else{
        Direction direction = resolveDirection(myCurrentWorm.position, nearTarget.position);
        return new ShootCommand(direction);
    }
}

private boolean canBananaBombThem(Worm target, GameState gameState) {
    return isWormStunned(target, gameState) && currentWorm.id == 2
        && gameState.myPlayer.worms[1].bananaBombs.count > 0
        && euclideanDistance(currentWorm.position.x, currentWorm.position.y, target.position.x, target.position.y) <= gameState.myPlayer.worms[1].bananaBombs.range
        && euclideanDistance(currentWorm.position.x, currentWorm.position.y, target.position.x > gameState.myPlayer.worms[1].bananaBombs.damageRadius * 0.75;
}

private boolean canSnowballThem(Worm target, GameState gameState) {
    return !isWormStunned(target, gameState) && currentWorm.id == 3
        && gameState.myPlayer.worms[2].snowballs.count > 0
        && euclideanDistance(currentWorm.position.x, currentWorm.position.y, target.position.x <= gameState.myPlayer.worms[2].snowballs.range
        && euclideanDistance(currentWorm.position.x, currentWorm.position.y, target.position.x > gameState.myPlayer.worms[2].freezeRadius * Math.sqrt(2);
}

private boolean isWormStunned (Worm target, GameState gameState){
    for(Worm cacingnya : gamestate.opponents[0].worms){
        if(cacingnya.id == target.id) {
            return cacingnya.roundsUntilUnfrozen > 0;
        }
    }
}
