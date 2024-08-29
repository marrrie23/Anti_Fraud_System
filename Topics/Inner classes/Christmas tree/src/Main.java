class ChristmasTree {

    private String color;

    public ChristmasTree(String color) {
        this.color = color;
    }

    // Method putTreeTopper with one string parameter color
    void putTreeTopper(String topperColor) {
        // Create an instance of the inner class TreeTopper
        TreeTopper treeTopper = new TreeTopper(topperColor);
        // Call the sparkle method of TreeTopper
        treeTopper.sparkle();
    }

    class TreeTopper {

        private String color;

        public TreeTopper(String color) {
            this.color = color;
        }

        // Method sparkle to print the required message
        void sparkle() {
            System.out.println("Sparkling " + this.color + " tree topper looks stunning with " + ChristmasTree.this.color + " Christmas tree!");
        }
    }
}

// This code should work
class CreateHoliday {

    public static void main(String[] args) {

        ChristmasTree christmasTree = new ChristmasTree("green");
        christmasTree.putTreeTopper("silver");
    }
}
