package net.mantagames.jgm.engine.gl;

public class Location {
	private float x;
	private float y;
	private float z;
	private float yaw;
	private float pitch;
	private float roll;

	public Location(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public float getX() {
		return this.x;
	}

	public float getY() {
		return this.y;
	}

	public float getZ() {
		return this.z;
	}

	public Location setY(float y) {
		this.y = y;
		return this;
	}

	public Location setX(float x) {
		this.x = x;
		return this;
	}

	public Location setZ(float z) {
		this.z = z;
		return this;
	}

	public Location add(Location loc) {
		return add(loc.getX(), loc.getY(), loc.getZ());
	}

	public Location subtract(Location loc) {
		return subtract(loc.getX(), loc.getY(), loc.getZ());
	}

	public Location add(float x, float y, float z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	public Location subtract(float x, float y, float z) {
		this.x -= x;
		this.y -= y;
		this.z -= z;
		return this;
	}

	public float distTo(Location loc) {
		return (float)Math.sqrt(distSquared(loc));
	}

	public float distSquared(Location loc) {
		return (this.x - loc.getX()) * (this.x - loc.getX()) + (this.y - loc.getY()) * (this.y - loc.getY()) + (this.z - loc.getZ()) * (this.z - loc.getZ());
	}

	public Location clone() {
		return new Location(this.x, this.y, this.z).setYaw(this.yaw).setPitch(this.pitch).setRoll(this.roll);
	}

	public int getBlockX() {
		return (int)this.x;
	}

	public int getBlockY() {
		return (int)this.y;
	}

	public int getBlockZ() {
		return (int)this.z;
	}

	public float getYaw() {
		return this.yaw;
	}

	public float getPitch() {
		return this.pitch;
	}

	public float getRoll() {
		return this.roll;
	}

	public Location setYaw(float f) {
		this.yaw = f;
		return this;
	}

	public Location setRoll(float f) {
		this.roll = f;
		return this;
	}

	public Location setPitch(float f) {
		this.pitch = f;
		return this;
	}

	public Location add(double i, double j, double k) {
		return add((float)i, (float)j, (float)k);
	}

	public Vector getDirection() {
		float xd=(float) (Math.cos(Math.toRadians(yaw - 90))*Math.cos(Math.toRadians(-pitch)));
		float yd=(float)-Math.sin(Math.toRadians(yaw - 90))*(float)Math.cos(Math.toRadians(-pitch));
		float zd=(float)Math.sin(Math.toRadians(-pitch));
		
		return Vector.getDirection(this, this.clone().add(xd, yd, zd));
	}
}