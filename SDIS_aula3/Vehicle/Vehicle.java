package Vehicle;

public class Vehicle {
	public String getPlate() {
		return plate;
	}

	public void setPlate(String plate) {
		this.plate = plate;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	String plate;
	String owner;

	public Vehicle(String plate, String owner) {
		this.plate = plate;
		this.owner = owner;
	}
}