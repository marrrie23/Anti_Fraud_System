/*class Vehicle {
    // Vehicle of your dream

    class Engine {
        void start() {
            System.out.println("RRRrrrrrrr....");
        }
    }
}
*/
class EnjoyVehicle {
    public static void startVehicle() {
        // Create an instance of the inner class Engine directly
        Vehicle.Engine myEngine = new Vehicle().new Engine();

        // Start the engine
        myEngine.start();
    }
}

public class Main {
    public static void main(String[] args) {
        EnjoyVehicle.startVehicle();
    }
}
