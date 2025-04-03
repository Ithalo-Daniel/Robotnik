package myrobots;

import robocode.*;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;

public class Robotnik extends AdvancedRobot {
    private double moveDirection = 1;
    private double previousEnergy = 100;
    private ArrayList<EnemyWave> enemyWaves = new ArrayList<>();
    private static final double BULLET_POWER = 2.0;

    public void run() {
        setBodyColor(Color.RED);
        setGunColor(Color.BLACK);
        setRadarColor(Color.YELLOW);
        setScanColor(Color.GREEN);

        setAdjustGunForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        while (true) {
            setTurnRadarRight(360);
            setAhead(100 * moveDirection);
            setTurnRight(45);
            execute();
        }
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        double changeInEnergy = previousEnergy - e.getEnergy();
        if (changeInEnergy > 0 && changeInEnergy <= 3) {
            EnemyWave wave = new EnemyWave();
            wave.fireTime = getTime();
            wave.bulletVelocity = bulletVelocity(changeInEnergy);
            wave.distanceTraveled = 0;
            wave.direction = (int) moveDirection;
            wave.fireLocation = new Point2D.Double(getX(), getY());
            enemyWaves.add(wave);
        }
        previousEnergy = e.getEnergy();

        double absoluteBearing = getHeadingRadians() + e.getBearingRadians();
        double bearingFromGun = normalRelativeAngle(absoluteBearing - getGunHeadingRadians());

        // Se estiver muito perto, fixar a mira no inimigo
        if (e.getDistance() < 50) {
            setTurnGunRightRadians(bearingFromGun);
            setFire(3);
        } else {
            setTurnGunRightRadians(bearingFromGun);
            if (getGunHeat() == 0 && Math.abs(bearingFromGun) < Math.PI / 18) {
                setFire(Math.min(400 / e.getDistance(), 3));
            }
        }

        // Manter distância dos inimigos se houver muitos ao redor
        if (e.getDistance() < 100) {
            moveDirection = -moveDirection;
            setAhead(150 * moveDirection);
        }
    }

    public void onHitByBullet(HitByBulletEvent e) {
        if (getEnergy() < 50) {
            moveDirection = -moveDirection;
            setAhead(100 * moveDirection);
        }
    }

    public void onHitWall(HitWallEvent e) {
        moveDirection = -moveDirection;
        setTurnRight(90);
        setAhead(150 * moveDirection);
    }

    public void onHitRobot(HitRobotEvent e) {
        // Se colidir com um robô, se afastar na direção oposta
        double angle = normalRelativeAngleDegrees(e.getBearing() + 180);
        setTurnRight(angle);
        setAhead(100);
        // Focar a mira no inimigo colidido
        setTurnGunRight(getHeading() - getGunHeading() + e.getBearing());
        setFire(3);
    }

    private double bulletVelocity(double power) {
        return 20 - 3 * power;
    }

    private double normalRelativeAngle(double angle) {
        return Math.atan2(Math.sin(angle), Math.cos(angle));
    }

    private double normalRelativeAngleDegrees(double angle) {
        while (angle > 180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }

    private class EnemyWave {
        private Point2D.Double fireLocation;
        private long fireTime;
        private double bulletVelocity;
        private double distanceTraveled;
        private int direction;
    }
}
