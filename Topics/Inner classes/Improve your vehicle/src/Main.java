class Vehicle {

    private String name;

    // Create constructor for Vehicle
    public Vehicle(String name) {
        this.name = name;
    }

    class Engine {

        // Add field horsePower
        int horsePower;

        // Create constructor for Engine
        public Engine(int horsePower) {
            this.horsePower = horsePower;
        }

        void start() {
            System.out.println("RRRrrrrrrr....");
        }

        // Create method printHorsePower()
        void printHorsePower() {
            // Correctly refer to the Vehicle's name field
            System.out.println("Vehicle " + Vehicle.this.name + " has " + horsePower + " horsepower.");
        }
    }
}

// This code should work
class EnjoyVehicle {

    public static void main(String[] args) {

        Vehicle vehicle = new Vehicle("Dixi");
        Vehicle.Engine engine = vehicle.new Engine(20);
        engine.printHorsePower();
    }
}
