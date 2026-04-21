package players;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import Tools.Coordinates;
import Tools.Hitbox;
import monsters.Monsters;

public class Player {

	private final Coordinates position;
	private final Hitbox hitbox;
	private final double maxSpeed;

	private volatile int lives = 3;  			// Player starts with 3 lives
	private volatile boolean hasHoney = false; 	// Whether the player has harvested honey
	private volatile boolean isHarvesting = false; // Whether the player is currently harvesting honey
	private volatile long harvestStartTime = 0; // Timestamp when harvesting started (in milliseconds since epoch)

	private final double spawnX, spawnY; // Initial spawn X and Y coordinate (used for respawning after being hit)
	private final Hitbox hiveZone, spawnZone; // Hitbox for the hive and the spawn zone
	private final ArrayList<Monsters> monsters; // List of monsters to check for collisions against

	private final Set<Integer> keysPressed = Collections.synchronizedSet(new HashSet<>()); // syncronized set to avoid cross tread issues when adding/removing keys

	// Set bounderies for the player not to move outside the window or play area.
	private double minX = 0.0;
	private double minY = 0.0;
	private double maxX = Double.POSITIVE_INFINITY;
	private double maxY = Double.POSITIVE_INFINITY;

	private volatile boolean running = false; // Whether the player movement thread is running
	private Thread movementThread;

	private volatile long invincibleUntil = 0; // Timestamp until which the player is invincible after being hit (in milliseconds since epoch)
	private volatile boolean won = false; // Has the player won the game ?
	private volatile boolean gameOver = false; // Has the player lost all lives ?

	public Player(
		double spawnX,
		double spawnY,
		double maxSpeed,
		Hitbox hitbox,
		Hitbox hiveZone,
		Hitbox spawnZone,
		ArrayList<Monsters> monsters
	) {
		this.spawnX = spawnX;
		this.spawnY = spawnY;
		this.position = new Coordinates(spawnX, spawnY);
		this.maxSpeed = maxSpeed;
		this.hitbox = hitbox;
		this.hiveZone = hiveZone;
		this.spawnZone = spawnZone;
		this.monsters = monsters != null ? monsters : new ArrayList<>();

		if (this.hitbox != null) {
			this.hitbox.update(this.position);
		}
	}

	public void startMovement() {
		if (running) {
			return;
		}

		running = true;

		// Start a new tread to parallelize the update position of monsters and player

		movementThread = new Thread(() -> {
			long lastTime = System.currentTimeMillis(); // Start time for the movement loop

			while (running) {
				long now = System.currentTimeMillis(); // Current time for this iteration
				double dt = (now - lastTime) / 1000.0; // calculate delta time in seconds
				lastTime = now; // update the last time for the next iteration

				// Update position based on the key pressed, and sync the hitbox with the new position
				// Check if the player is harvesting honey, handle collisions with monsters, and check win condition
				updatePosition(dt);
				syncHitbox();
				updateHarvesting(now);
				handleMonsterCollisions(now);
				checkWinCondition();

				try {
					 Thread.sleep(16); 
					}
				catch (InterruptedException e) {
					Thread.currentThread().interrupt(); 
					break; 
				} // sleep for 16ms to match 60 fps
			}
		}, "player-movement");

		movementThread.start();
	}

	// Method to stop the movement thread when the game is over or the player has won
	public void stopMovement() {
		running = false;
		if (movementThread != null) {
			movementThread.interrupt();
		}
	}

	// Methods to handle key presses and releases, updating the set of currently pressed keys
	public void onKeyPressed(int keyCode) {
		if (isMovementKey(keyCode)) {
			keysPressed.add(keyCode);
		}
	}

	// Method to handle key releases, removing the key from the set of currently pressed keys
	public void onKeyReleased(int keyCode) {
		if (isMovementKey(keyCode)) {
			keysPressed.remove(keyCode);
		}
	}

	// Helper method to determine if a key code corresponds to an arrow key
	private boolean isMovementKey(int keyCode) {
		return keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_DOWN;
	}

	// Method to update the position of the player based on the key currently pressed inside the Set.
	// We used synchronized blocks to avoid cross thread issues when reading the position and the keys pressed, 
	// as they are shared between the movement thread and the main thread (rendering and input handling).
	private void updatePosition(double dt) {
		double dirX = 0.0;
		double dirY = 0.0;

		synchronized (keysPressed) {
			if (keysPressed.contains(KeyEvent.VK_LEFT)) {
				dirX -= 1.0;
			}
			if (keysPressed.contains(KeyEvent.VK_RIGHT)) {
				dirX += 1.0;
			}
			if (keysPressed.contains(KeyEvent.VK_UP)) {
				dirY -= 1.0;
			}
			if (keysPressed.contains(KeyEvent.VK_DOWN)) {
				dirY += 1.0;
			}
		}

		if (dirX != 0.0 || dirY != 0.0) {
			double magnitude = Math.hypot(dirX, dirY);
			dirX /= magnitude;
			dirY /= magnitude;
		}

		synchronized (position) {
			// update position and direction
			double newX = position.getX() + (dirX * maxSpeed * dt);
			double newY = position.getY() + (dirY * maxSpeed * dt);

			position.setX(clamp(newX, minX, maxX)); // clamp the new position to not the borders of the map
			position.setY(clamp(newY, minY, maxY));
		}
	}

	private void syncHitbox() {
		if (hitbox == null) {
			return;
		}

		synchronized (position) {
			hitbox.update(position);
		}
	}

	private void updateHarvesting(long now) {
		if (hasHoney || hitbox == null || hiveZone == null) {
			isHarvesting = false;
			harvestStartTime = 0; // 0 
			return;
		}

		if (overlaps(hitbox, hiveZone)) { // Check if the hitbox of the player and hitbox of the hive overlap
			if (!isHarvesting) {	// Harversting just started
				isHarvesting = true;
				harvestStartTime = now;
			} else if (now - harvestStartTime >= 3000) { // Hervesting has started. Checking if 3 seconds have passed
				hasHoney = true;
				isHarvesting = false;
				harvestStartTime = 0;
			}
		} else {
			isHarvesting = false;
			harvestStartTime = 0;
		}
	}

	private void handleMonsterCollisions(long now) {
		if (hitbox == null || now <= invincibleUntil) {	 // If the player has no hitbox or is currently invincible, skip collision checks
			return;
		}

		for (Monsters monster : monsters) { // iterate over the monsters to check for collisions
			if (monster == null || monster.getHitbox() == null) {
				continue;
			}

			if (overlaps(hitbox, monster.getHitbox())) { // player has been touched by a monster.
				lives = Math.max(0, lives - 1); // remove one life when the player is hit, but don't let it go below 0
				hasHoney = false;
				isHarvesting = false;
				harvestStartTime = 0;

				synchronized (position) { // update the position of the player to it's spawn point
					position.setX(spawnX);
					position.setY(spawnY);
				}
				syncHitbox();

				invincibleUntil = now + 1000;

				if (lives == 0) {
					gameOver = true;
					running = false;
				}
				break;
			}
		}
	}

	private void checkWinCondition() {
		if (gameOver || !hasHoney || hitbox == null || spawnZone == null) {
			return;
		}

		if (overlaps(hitbox, spawnZone)) {
			hasHoney = false;
			won = true;
			running = false;
		}
	}

	// Method overlap to check if a rectangle overlaps with annther one.
	private boolean overlaps(Hitbox a, Hitbox b) {
		return a.getX() < b.getX() + b.getWidth()
			&& a.getX() + a.getWidth() > b.getX()
			&& a.getY() < b.getY() + b.getHeight()
			&& a.getY() + a.getHeight() > b.getY();
	}

	// Clamp method to limit a value 
	private double clamp(double value, double min, double max) {
		return Math.max(min, Math.min(max, value));
	}

	public double getX() {
		synchronized (position) {
			return position.getX();
		}
	}

	public double getY() {
		synchronized (position) {
			return position.getY();
		}
	}

	public Coordinates getPosition() {
		return position;
	}

	public Hitbox getHitbox() {
		return hitbox;
	}

	public double getMaxSpeed() {
		return maxSpeed;
	}

	public int getLives() {
		return lives;
	}

	public boolean hasHoney() {
		return hasHoney;
	}

	public boolean isHarvesting() {
		return isHarvesting;
	}

	public long getHarvestStartTime() {
		return harvestStartTime;
	}

	public long getInvincibleUntil() {
		return invincibleUntil;
	}

	public boolean isRunning() {
		return running;
	}

	public boolean isWon() {
		return won;
	}

	public boolean isGameOver() {
		return gameOver;
	}
	
	
	public void setMovementBounds(double minX, double minY, double maxX, double maxY) {
		this.minX = minX;
		this.minY = minY;
		this.maxX = Math.max(minX, maxX);
		this.maxY = Math.max(minY, maxY);

		synchronized (position) {
			position.setX(clamp(position.getX(), this.minX, this.maxX));
			position.setY(clamp(position.getY(), this.minY, this.maxY));
		}
		syncHitbox();
	}
}
