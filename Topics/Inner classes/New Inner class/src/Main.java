class Vehicle {

    private String name;

    // Constructor for Vehicle
    public Vehicle(String name) {
        this.name = name;
    }

    class Engine {
        void start() {
            System.out.println("RRRrrrrrrr....");
        }
    }

    // Inner class Body
    class Body {

        String color;

        // Constructor for Body
        public Body(String color) {
            this.color = color;
        }

        // Method to print the color of the body
        void printColor() {
            System.out.println("Vehicle " + Vehicle.this.name + " has " + color + " body.");
        }
    }
}

// This code should work
class EnjoyVehicle {

    public static void main(String[] args) {

        Vehicle vehicle = new Vehicle("Dixi");
        Vehicle.Body body = vehicle.new Body("red");
        body.printColor();
    }
}
