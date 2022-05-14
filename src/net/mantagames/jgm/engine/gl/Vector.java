package net.mantagames.jgm.engine.gl;

public class Vector
{
	private float x;
	private float y;
	private float z;
	
	public Vector clone() {
		return new Vector(x, y, z);
	}

	public Vector(float x, float y, float z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector(double x, double y, double z) {
		this((float)x, (float)y, (float)z);
	}

	public Vector(Vector v) {
		this(v.getX(), v.getY(), v.getZ());
	}

	public Vector multiply(Vector v) {
		return multiply(v.getX(), v.getY(), v.getZ());
	}

	public Vector multiply(float f) {
		return multiply(f, f, f);
	}

	public Vector multiply(double d) {
		return multiply((float)d);
	}

	public Vector multiply(float x, float y, float z) {
		this.x *= x;
		this.y *= y;
		this.z *= z;
		return this;
	}

	public Vector setY(double f) {
		this.y = ((float)f);
		return this;
	}

	public Vector setX(double f) {
		this.x = ((float)f);
		return this;
	}

	public Vector setZ(double f) {
		this.z = ((float)f);
		return this;
	}

	public Vector setY(float f) {
		this.y = f;
		return this;
	}

	public Vector setX(float f) {
		this.x = f;
		return this;
	}

	public Vector setZ(float f) {
		this.z = f;
		return this;
	}

	public Vector add(Vector v) {
		return add(v.getX(), v.getY(), v.getZ());
	}

	public Vector add(float f) {
		return add(f, f, f);
	}

	public Vector add(float x, float y, float z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}
	
	public Vector subtract(float x, float y, float z) {
		this.x -= x;
		this.y -= y;
		this.z -= z;
		return this;
	}

	public Vector interpolateTo(Vector t, float p) {
		float xt = this.x + (t.x - this.x) * p;
		float yt = this.y + (t.y - this.y) * p;
		float zt = this.z + (t.z - this.z) * p;

		return new Vector(xt, yt, zt);
	}

	public void set(float x, float y, float z) {
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
	
	public Vector normalize() {
		Vector v = this.getNormal();
		x = v.getX();
		y = v.getY();
		z = v.getZ();
		return this;
	}
	
	public Vector getNormal() {
		double a = getMagnitude();
		if (a > 0)
			return new Vector(x / a, y / a, z / a);
		return new Vector(0, 0, 0);
	}

	public static Vector getDirection(Location b1, Location b) {
		double xdn = b.getX() - b1.getX();
		double ydn = b.getY() - b1.getY();
		double zdn = b.getZ() - b1.getZ();
		
		return new Vector(xdn, ydn, zdn);
	}
	
	public String toString() {
		return "(" + x + "," + y + "," + z + ")";
	}
	
	public boolean equals(Vector v) {
		if (v.getX() == x && v.getY() == y && v.getZ() == z)
			return true;
		return false;
	}

	public Vector multiply(double d, double i, double e) {
		return multiply((float)d, (float)i, (float)e);
	}

	public float getMagnitude() {
		return (float) Math.sqrt((x * x) + (y * y) + (z * z));
	}

	public float dot(Vector normal) {
		return (x*normal.x+y*normal.y+z*normal.z);
	}
}